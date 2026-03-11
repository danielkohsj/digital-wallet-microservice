package com.boostbank.wallet.wallet_service.repository;

import com.boostbank.wallet.wallet_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


/**
 * Repository interface for accessing and managing {@link Transaction} entities.
 *
 * <p>Provides standard CRUD operations via {@link JpaRepository} and
 * custom query methods for retrieving transaction history.</p>
 */
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Retrieves all transactions where the given user is either the
     * source or destination of the transaction.
     *
     * <p>This method is useful for retrieving a user's full transaction history,
     * including transfers sent and received.</p>
     *
     * @param sourceUserId the ID of the user initiating the transaction
     * @param destinationUserId the ID of the user receiving the transaction
     * @return list of transactions associated with the given user
     */
    List<Transaction> findBySourceUserIdOrDestinationUserId(UUID sourceUserId, UUID destinationUserId);
}
