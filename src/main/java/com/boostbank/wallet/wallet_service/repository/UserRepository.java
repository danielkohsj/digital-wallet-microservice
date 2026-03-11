package com.boostbank.wallet.wallet_service.repository;

import com.boostbank.wallet.wallet_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * Retrieves a user by their unique email address.
     *
     * @param email the email address associated with the user
     * @return an {@link Optional} containing the user if found,
     *         otherwise an empty Optional
     */
    Optional<User> findByEmail(String email);
}
