package com.boostbank.wallet.wallet_service.service;

import com.boostbank.wallet.wallet_service.entity.IdempotencyKey;
import com.boostbank.wallet.wallet_service.entity.Transaction;
import com.boostbank.wallet.wallet_service.entity.User;
import com.boostbank.wallet.wallet_service.enums.TransactionType;
import com.boostbank.wallet.wallet_service.exception.DuplicateRequestException;
import com.boostbank.wallet.wallet_service.exception.InsufficientBalanceException;
import com.boostbank.wallet.wallet_service.exception.InvalidAmountException;
import com.boostbank.wallet.wallet_service.exception.UserNotFoundException;
import com.boostbank.wallet.wallet_service.repository.IdempotencyKeyRepository;
import com.boostbank.wallet.wallet_service.repository.TransactionRepository;
import com.boostbank.wallet.wallet_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


/**
 * Core service responsible for managing wallet operations.
 *
 * <p>This service implements the main business logic for the digital wallet system,
 * including balance management, fund transfers, and transaction history retrieval.</p>
 *
 * <p>Financial operations are executed within transactional boundaries to ensure atomicity
 * and consistency between wallet balances and transaction records.</p>
 *
 * <p>The service incorporates several safeguards to ensure financial correctness:</p>
 *
 * <ul>
 *     <li><b>Idempotency</b> – Prevents duplicate financial operations by storing and validating
 *     idempotency keys for each request.</li>
 *
 *     <li><b>Pessimistic Locking</b> – Uses database row-level locks to prevent concurrent updates
 *     to wallet balances during financial operations.</li>
 *
 *     <li><b>Deadlock Prevention</b> – Ensures deterministic locking order when multiple users are
 *     involved in a transaction (e.g., transfers).</li>
 *
 *     <li><b>Validation</b> – Enforces business rules such as valid transaction amounts and
 *     sufficient wallet balances.</li>
 * </ul>
 *
 * <p>This design ensures that wallet operations remain safe and consistent even under
 * concurrent request scenarios.</p>
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    private final TransactionTemplate transactionTemplate;

    /**
     * Retrieves the current balance of a user.
     *
     * @param userId the unique identifier of the user
     * @return the user's current wallet balance
     */
    public BigDecimal getBalance(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user.getBalance();
    }

    /**
     * Credits funds to a user's wallet.
     *
     * <p>This operation is executed within a database transaction to ensure
     * atomic updates between the user's wallet balance and the transaction record.</p>
     *
     * <p>An idempotency key is inserted before processing to prevent duplicate
     * financial operations caused by client retries.</p>
     *
     * @param idempotencyKey unique key used to ensure request idempotency
     * @param userId the user receiving the credited funds
     * @param amount the amount to credit
     *
     * @throws DuplicateRequestException if the idempotency key has already been processed
     * @throws InvalidAmountException if the amount is less than or equal to zero
     * @throws UserNotFoundException if the specified user does not exist
     */
    public void credit(String idempotencyKey, UUID userId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {

            // 1. Checks for duplicate transactions
            IdempotencyKey key;
            try {
                key = insertIdempotencyKey(idempotencyKey);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateRequestException("Duplicate request detected");
            }

            // 2. Validate transaction amount
            validateAmount(amount);

            // 3. Update balance
            User user = userRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setBalance(user.getBalance().add(amount));

            // 4. Create new credit transaction info
            Transaction tx = transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionType.CREDIT)
                            .amount(amount)
                            .destinationUserId(userId)
                            .timestamp(Instant.now())
                            .build()
            );
            key.setTransactionId(tx.getTransactionId());
            idempotencyKeyRepository.save(key);
        });
    }

    /**
     * Debits funds from a user's wallet.
     *
     * <p>The operation executes within a transactional boundary to guarantee
     * atomic balance updates and transaction persistence.</p>
     *
     * <p>Pessimistic locking is used to prevent concurrent balance modifications.
     * The user's balance is validated before deduction to ensure sufficient funds.</p>
     *
     * @param idempotencyKey unique key used to ensure request idempotency
     * @param userId the user whose wallet will be debited
     * @param amount the amount to deduct
     *
     * @throws DuplicateRequestException if the idempotency key has already been processed
     * @throws InvalidAmountException if the amount is less than or equal to zero
     * @throws InsufficientBalanceException if the wallet balance is insufficient
     * @throws UserNotFoundException if the specified user does not exist
     */
    public void debit(String idempotencyKey, UUID userId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {

            // 1. Checks for duplicate transactions
            IdempotencyKey key;
            try {
                key = insertIdempotencyKey(idempotencyKey);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateRequestException("Duplicate request detected");
            }

            // 2. Validate transaction amount
            validateAmount(amount);

            // 3. Check balance then update
            User user = userRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            validateBalance(amount, user);
            user.setBalance(user.getBalance().subtract(amount));

            // 4. Create new debit transaction info
            Transaction tx = transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionType.DEBIT)
                            .amount(amount)
                            .sourceUserId(userId)
                            .timestamp(Instant.now())
                            .build()
            );
            key.setTransactionId(tx.getTransactionId());
            idempotencyKeyRepository.save(key);
        });
    }

    /**
     * Transfers funds between two wallet users.
     *
     * <p>This operation is executed atomically within a database transaction to
     * guarantee that both balance updates occur together.</p>
     *
     * <p>Pessimistic locking is used to prevent concurrent balance updates.
     * Both users are locked in a deterministic order to avoid database deadlocks
     * during concurrent transfer operations.</p>
     *
     * @param idempotencyKey unique key used to ensure request idempotency
     * @param sourceUserId the user sending funds
     * @param destinationUserId the user receiving funds
     * @param amount the amount to transfer
     *
     * @throws DuplicateRequestException if the idempotency key has already been processed
     * @throws InvalidAmountException if the amount is less than or equal to zero
     * @throws InsufficientBalanceException if the source wallet has insufficient balance
     * @throws UserNotFoundException if either user cannot be found
     */
    public void transfer(String idempotencyKey, UUID sourceUserId, UUID destinationUserId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {

            // 1. Checks for duplicate transactions
            IdempotencyKey key;
            try {
                key = insertIdempotencyKey(idempotencyKey);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateRequestException("Duplicate request detected");
            }

            // 2. Validate transaction amount
            validateAmount(amount);

            // 3. Get database lock for both users
            User[] users = lockUsers(sourceUserId, destinationUserId);
            User sourceUser = users[0];
            User destinationUser = users[1];

            // 4. Validate and update balance for both
            validateBalance(amount, sourceUser);
            sourceUser.setBalance(sourceUser.getBalance().subtract(amount));
            destinationUser.setBalance(destinationUser.getBalance().add(amount));

            // 5. Create transfer transaction info
            Transaction tx = transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionType.TRANSFER)
                            .amount(amount)
                            .sourceUserId(sourceUserId)
                            .destinationUserId(destinationUserId)
                            .timestamp(Instant.now())
                            .build()
            );
            key.setTransactionId(tx.getTransactionId());
            idempotencyKeyRepository.save(key);
        });
    }

    /**
     * Retrieves the transaction history associated with a user.
     *
     * This includes:
     * - Transactions initiated by the user
     * - Transactions received by the user
     *
     * @param userId the user's ID
     * @return list of transactions involving the user
     */
    public List<Transaction> getTransactionHistory(UUID userId) {
        return transactionRepository
                .findBySourceUserIdOrDestinationUserId(userId, userId);
    }

    /**
     * Inserts a new idempotency key record into the database.
     *
     * <p>This method implements the "insert-first" idempotency pattern,
     * where the key is inserted before executing the operation. If a
     * duplicate key exists, the database will throw a constraint
     * violation which should be translated into a
     * {@link DuplicateRequestException}.</p>
     *
     * <p>This approach prevents race conditions where multiple requests
     * check for key existence simultaneously.</p>
     *
     * @param idempotencyKey the unique idempotency key provided by the client
     * @return IdempotencyKey the newly created unique idempotency key for a transaction
     */
    private IdempotencyKey insertIdempotencyKey(String idempotencyKey) {
        return idempotencyKeyRepository.save(
                IdempotencyKey.builder()
                        .idempotencyKey(idempotencyKey)
                        .createdAt(Instant.now())
                        .build()
        );
    }

    /**
     * Validates that a transaction amount is greater than zero.
     *
     * <p>This method is used to ensure that wallet operations such as
     * credit, debit, or transfer cannot be performed with invalid
     * monetary values.</p>
     *
     * @param amount the transaction amount
     * @throws InvalidAmountException if the amount is less than or equal to zero
     */
    private static void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    /**
     * Validates that the user has sufficient balance to perform
     * a debit or transfer operation.
     *
     * @param amount the amount to deduct from the wallet
     * @param user   the user whose balance is being validated
     * @throws InsufficientBalanceException if the wallet balance is insufficient
     */
    private static void validateBalance(BigDecimal amount, User user) {
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
    }

    /**
     * Retrieves and locks two user records in a deterministic order to prevent database deadlocks.
     *
     * <p>This method acquires pessimistic write locks on both the source and destination users
     * using {@code SELECT ... FOR UPDATE}. To avoid deadlocks when multiple transfers occur
     * concurrently, the users are always locked in a consistent order based on their UUID values.</p>
     *
     * <p>The locking order is determined by comparing the UUIDs of the source and destination users.
     * The user with the smaller UUID is locked first, followed by the other user. This guarantees
     * that concurrent transactions will attempt to acquire locks in the same order.</p>
     *
     * <p>After both users are locked, the method maps them back to their original roles
     * (source user and destination user) before returning them.</p>
     *
     * @param sourceUserId the ID of the user initiating the transfer
     * @param destinationUserId the ID of the user receiving the transfer
     * @return an array where:
     *         <ul>
     *             <li>index 0 = source user</li>
     *             <li>index 1 = destination user</li>
     *         </ul>
     * @throws UserNotFoundException if either user cannot be found in the database
     */
    private User[] lockUsers(UUID sourceUserId, UUID destinationUserId) {
        UUID first = sourceUserId.compareTo(destinationUserId) < 0 ? sourceUserId : destinationUserId;
        UUID second = sourceUserId.compareTo(destinationUserId) < 0 ? destinationUserId : sourceUserId;

        User firstUser = userRepository.findByIdForUpdate(first)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User secondUser = userRepository.findByIdForUpdate(second)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User sourceUser = firstUser.getId().equals(sourceUserId) ? firstUser : secondUser;
        User destinationUser = firstUser.getId().equals(destinationUserId) ? firstUser : secondUser;

        return new User[]{sourceUser, destinationUser};
    }
}
