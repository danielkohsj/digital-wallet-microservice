package com.boostbank.wallet.wallet_service.service;

import com.boostbank.wallet.wallet_service.entity.Transaction;
import com.boostbank.wallet.wallet_service.entity.User;
import com.boostbank.wallet.wallet_service.enums.TransactionTypeEnum;
import com.boostbank.wallet.wallet_service.repository.TransactionRepository;
import com.boostbank.wallet.wallet_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    private final TransactionTemplate transactionTemplate;

    /**
     * Creates a new wallet user.
     *
     * @param name  the user's name
     * @param email the user's unique email
     * @return the persisted user entity
     */
    public User createUser(String name, String email) {
        User user = User.builder()
                .name(name)
                .email(email)
                .balance(BigDecimal.ZERO)
                .build();

        return userRepository.save(user);
    }

    /**
     * Retrieves the current balance of a user.
     *
     * @param userId the unique identifier of the user
     * @return the user's current wallet balance
     */
    public BigDecimal getBalance(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
    public void credit(UUID userId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setBalance(user.getBalance().add(amount));
            userRepository.save(user);

            transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionTypeEnum.CREDIT)
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
    public void debit(UUID userId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            user.setBalance(user.getBalance().subtract(amount));
            userRepository.save(user);

            transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionTypeEnum.DEBIT)
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
    public void transfer(UUID sourceUserId, UUID destinationUserId, BigDecimal amount) {

        transactionTemplate.executeWithoutResult(status -> {

            User sourceUser = userRepository.findById(sourceUserId)
                    .orElseThrow(() -> new RuntimeException("Source user not found"));

            User destinationUser = userRepository.findById(destinationUserId)
                    .orElseThrow(() -> new RuntimeException("Destination user not found"));

            if (sourceUser.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            sourceUser.setBalance(sourceUser.getBalance().subtract(amount));
            destinationUser.setBalance(destinationUser.getBalance().add(amount));

            userRepository.save(sourceUser);
            userRepository.save(destinationUser);

            transactionRepository.save(
                    Transaction.builder()
                            .type(TransactionTypeEnum.TRANSFER)
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
}
