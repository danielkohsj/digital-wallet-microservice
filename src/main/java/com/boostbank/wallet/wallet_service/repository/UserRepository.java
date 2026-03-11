package com.boostbank.wallet.wallet_service.repository;

import com.boostbank.wallet.wallet_service.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;


/**
 * Repository interface for performing CRUD operations on {@link User} entities.
 *
 * <p>Provides standard CRUD operations via {@link JpaRepository} and
 * custom query methods for retrieving transaction history.</p>
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Retrieves a user with a pessimistic write lock.
     *
     * <p>This ensures that the selected row is locked for the duration
     * of the transaction, preventing concurrent updates to the user's
     * wallet balance.</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(UUID id);

    /**
     * Retrieves a user by their unique email address.
     *
     * @param email the email address associated with the user
     * @return an {@link Optional} containing the user if found,
     *         otherwise an empty Optional
     */
    Optional<User> findByEmail(String email);
}
