package com.boostbank.wallet.wallet_service.controller;

import com.boostbank.wallet.wallet_service.dto.request.*;
import com.boostbank.wallet.wallet_service.dto.response.*;
import com.boostbank.wallet.wallet_service.entity.Transaction;
import com.boostbank.wallet.wallet_service.entity.User;
import com.boostbank.wallet.wallet_service.enums.ResultInfo;
import com.boostbank.wallet.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * REST controller responsible for handling wallet-related API requests.
 *
 * <p>This controller exposes endpoints for managing users and performing
 * wallet operations such as crediting funds, debiting funds, transferring
 * funds between users, retrieving wallet balances, and viewing transaction
 * history.</p>
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Creates a new wallet user.
     *
     * Steps:
     * 1. Receive a {@link CreateUserRequest} containing user details.
     * 2. Validate the request payload using {@link Valid}.
     * 3. Call {@link WalletService#createUser(String, String)} to create the user.
     * 4. Map the returned {@link User} entity into {@link UserResponse}.
     * 5. Wrap the response inside an {@link BaseResponse} object.
     *
     * @param request request containing user name and email
     * @return API response containing the created user
     */
    @PostMapping("/users")
    public BaseResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {

        User user = walletService.createUser(request.getName(), request.getEmail());

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .balance(user.getBalance())
                .build();

        return BaseResponse.<UserResponse>builder()
                .result(ResultInfo.SUCCESS)
                .message("User created successfully")
                .data(response)
                .build();
    }

    /**
     * Credits funds into a user's wallet.
     *
     * Steps:
     * 1. Receive a {@link CreditRequest} containing the user ID and credit amount.
     * 2. Validate the request payload.
     * 3. Call {@link WalletService#credit}.
     * 4. The service performs the credit operation and records the transaction.
     * 5. Return a success response wrapped in {@link BaseResponse}.
     *
     * @param request request containing credit details
     * @return API response indicating operation status
     */
    @PostMapping("/credit")
    public BaseResponse<Void> credit(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                     @Valid @RequestBody CreditRequest request) {

        walletService.credit(idempotencyKey, request.getUserId(), request.getAmount());

        return BaseResponse.<Void>builder()
                .result(ResultInfo.SUCCESS)
                .message("Wallet credited successfully")
                .build();
    }

    /**
     * Debits funds from a user's wallet.
     *
     * Steps:
     * 1. Receive a {@link DebitRequest}.
     * 2. Validate the request payload.
     * 3. Call {@link WalletService#debit}.
     * 4. The service validates the balance and deducts funds.
     * 5. Return a success response wrapped in {@link BaseResponse}.
     *
     * @param request request containing debit information
     * @return API response indicating operation result
     */
    @PostMapping("/debit")
    public BaseResponse<Void> debit(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                    @Valid @RequestBody DebitRequest request) {

        walletService.debit(idempotencyKey, request.getUserId(), request.getAmount());

        return BaseResponse.<Void>builder()
                .result(ResultInfo.SUCCESS)
                .message("Wallet debited successfully")
                .build();
    }

    /**
     * Transfers funds between two wallet users.
     *
     * Steps:
     * 1. Receive a {@link TransferRequest}.
     * 2. Validate the request payload.
     * 3. Call {@link WalletService#transfer}.
     * 4. The service deducts funds from the source wallet.
     * 5. The service credits funds into the destination wallet.
     * 6. A transaction record is stored for audit purposes.
     * 7. Return a success response.
     *
     * @param request request containing transfer details
     * @return API response indicating operation result
     */
    @PostMapping("/transfer")
    public BaseResponse<Void> transfer(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                       @Valid @RequestBody TransferRequest request) {

        walletService.transfer(
                idempotencyKey,
                request.getSourceUserId(),
                request.getDestinationUserId(),
                request.getAmount()
        );

        return BaseResponse.<Void>builder()
                .result(ResultInfo.SUCCESS)
                .message("Transfer completed successfully")
                .build();
    }

    /**
     * Retrieves the wallet balance for a specific user.
     *
     * Steps:
     * 1. Receive the user ID from the request path.
     * 2. Call {@link WalletService#getBalance(UUID)}.
     * 3. Wrap the returned balance in a {@link BalanceResponse}.
     * 4. Return the result wrapped in {@link BaseResponse}.
     *
     * @param userId the user identifier
     * @return API response containing wallet balance
     */
    @GetMapping("/balance/{userId}")
    public BaseResponse<BalanceResponse> getBalance(@PathVariable UUID userId) {

        BalanceResponse response = BalanceResponse.builder()
                .balance(walletService.getBalance(userId))
                .build();

        return BaseResponse.<BalanceResponse>builder()
                .result(ResultInfo.SUCCESS)
                .message("Balance retrieved successfully")
                .data(response)
                .build();
    }

    /**
     * Retrieves transaction history for a given user.
     *
     * Steps:
     * 1. Receive the user ID from the request path.
     * 2. Call {@link WalletService#getTransactionHistory(UUID)}.
     * 3. Convert {@link Transaction} entities into {@link TransactionResponse}.
     * 4. Return the list wrapped inside {@link BaseResponse}.
     *
     * @param userId the user identifier
     * @return API response containing transaction history
     */
    @GetMapping("/transactions/{userId}")
    public BaseResponse<List<TransactionResponse>> getTransactions(@PathVariable UUID userId) {

        List<TransactionResponse> responses = walletService
                .getTransactionHistory(userId)
                .stream()
                .map(tx -> TransactionResponse.builder()
                        .id(tx.getTransactionId())
                        .type(tx.getType())
                        .amount(tx.getAmount())
                        .sourceUserId(tx.getSourceUserId())
                        .destinationUserId(tx.getDestinationUserId())
                        .timestamp(tx.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return BaseResponse.<List<TransactionResponse>>builder()
                .result(ResultInfo.SUCCESS)
                .message("Transaction history retrieved successfully")
                .data(responses)
                .build();
    }
}
