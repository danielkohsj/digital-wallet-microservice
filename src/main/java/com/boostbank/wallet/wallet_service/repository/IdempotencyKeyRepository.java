package com.boostbank.wallet.wallet_service.repository;

import com.boostbank.wallet.wallet_service.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
}
