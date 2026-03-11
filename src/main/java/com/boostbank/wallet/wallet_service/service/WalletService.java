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


@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    private final TransactionTemplate transactionTemplate;

    /**
     * Creates a new wallet user.
     *
     * @param name  the user's name
     * @param email the user's unique email
     * @return the persisted user entity
     */
    public User createUser(String name, String email) {
        try {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .balance(BigDecimal.ZERO)
                    .build();

            return userRepository.save(user);

        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateRequestException("User with this email already exists");
        }
    }

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
     * Credits (adds funds to) a user's wallet.
     *
     * This operation runs inside a transactional boundary to ensure
     * atomicity between the balance update and transaction recording.
     *
     * Steps:
     * 1. Begin database transaction.
     * 2. Retrieve the user from the database.
     * 3. Validate that the amount is greater than zero.
     * 4. Add the specified amount to the user's balance.
     * 5. Persist the updated balance.
     * 6. Create a {@link Transaction} record of type CREDIT.
     * 7. Store the transaction in the database.
     *
     * If any step fails, the entire transaction is rolled back.
     *
     * @param userId the user receiving the credited funds
     * @param amount the amount to credit
     */
    public void credit(String idempotencyKey, UUID userId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {

            // 1. Checks for duplicate transactions
            try {
                insertIdempotencyKey(idempotencyKey);
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
            transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionType.CREDIT)
                            .amount(amount)
                            .destinationUserId(userId)
                            .timestamp(Instant.now())
                            .build()
            );
        });
    }

    /**
     * Debits (deducts funds from) a user's wallet.
     *
     * Steps:
     * 1. Begin a transactional operation.
     * 2. Retrieve the user from the database.
     * 3. Validate that the amount is greater than zero.
     * 4. Check if the user has sufficient balance.
     * 5. Deduct the amount from the user's balance.
     * 6. Persist the updated balance.
     * 7. Create a transaction record of type DEBIT.
     * 8. Save the transaction record.
     *
     * If any step fails (e.g. insufficient funds),
     * the transaction will be rolled back automatically.
     *
     * @param userId the user whose wallet will be debited
     * @param amount the amount to deduct
     */
    public void debit(String idempotencyKey, UUID userId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {

            // 1. Checks for duplicate transactions
            try {
                insertIdempotencyKey(idempotencyKey);
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
            transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionType.DEBIT)
                            .amount(amount)
                            .sourceUserId(userId)
                            .timestamp(Instant.now())
                            .build()
            );
        });
    }

    /**
     * Transfers funds between two users' wallets.
     *
     * This operation must be atomic to ensure that both balance updates
     * occur together or not at all.
     *
     * Steps:
     * 1. Begin a transactional operation.
     * 2. Retrieve the source and destination users.
     * 3. Validate that the transfer amount is greater than zero.
     * 4. Ensure the source user has sufficient balance.
     * 5. Deduct the amount from the source user's wallet.
     * 6. Add the amount to the destination user's wallet.
     * 7. Persist both updated users.
     * 8. Create a transaction record of type TRANSFER.
     * 9. Store the transaction in the database.
     *
     * If any step fails, the entire transfer is rolled back.
     *
     * @param sourceUserId      the user sending funds
     * @param destinationUserId the user receiving funds
     * @param amount            the amount to transfer
     */
    public void transfer(String idempotencyKey, UUID sourceUserId, UUID destinationUserId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {

            // 1. Checks for duplicate transactions
            try {
                insertIdempotencyKey(idempotencyKey);
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
            transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionType.TRANSFER)
                            .amount(amount)
                            .sourceUserId(sourceUserId)
                            .destinationUserId(destinationUserId)
                            .timestamp(Instant.now())
                            .build()
            );
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
     */
    private void insertIdempotencyKey(String idempotencyKey) {
        idempotencyKeyRepository.save(
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
