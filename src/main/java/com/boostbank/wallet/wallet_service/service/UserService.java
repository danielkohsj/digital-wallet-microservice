package com.boostbank.wallet.wallet_service.service;

import com.boostbank.wallet.wallet_service.entity.User;
import com.boostbank.wallet.wallet_service.exception.DuplicateRequestException;
import com.boostbank.wallet.wallet_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


/**
 * Service responsible for managing user-related operations within the wallet system.
 *
 * <p>This service handles the lifecycle of wallet users, including user creation
 * and validation of unique user attributes such as email addresses.</p>
 *
 * <p>User records created through this service are initialized with a wallet
 * balance of zero and are subsequently used by {@link WalletService} for
 * financial operations such as credit, debit, and transfers.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}
