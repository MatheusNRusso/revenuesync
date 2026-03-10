package com.mtnrs.revenuesync.service;

import com.mtnrs.revenuesync.domain.Payment;
import com.mtnrs.revenuesync.domain.enums.PaymentStatus;
import com.mtnrs.revenuesync.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // ========== QUERIES (FOR FRONTEND) ==========

    /**
     * Retrieves all payments from the database
     */
    @Transactional(readOnly = true)
    public List<Payment> getAll() {
        log.debug("Fetching all payments");
        return paymentRepository.findAll();
    }

    /**
     * Retrieves a payment by its ID
     */
    @Transactional(readOnly = true)
    public Optional<Payment> getById(Long id) {
        log.debug("Fetching payment by id: {}", id);
        return paymentRepository.findById(id);
    }

    /**
     * Retrieves a payment by its Stripe external ID
     */
    @Transactional(readOnly = true)
    public Optional<Payment> getByExternalId(String externalId) {
        log.debug("Fetching payment by externalId: {}", externalId);
        return paymentRepository.findByExternalId(externalId);
    }

    /**
     * Checks if a payment exists by external ID (for idempotency)
     */
    @Transactional(readOnly = true)
    public boolean existsByExternalId(String externalId) {
        log.debug("Checking existence by externalId: {}", externalId);
        return paymentRepository.existsByExternalId(externalId);
    }

    // ========== BUSINESS OPERATIONS (WEBHOOK) ==========

    /**
     * Idempotent upsert from Stripe webhook
     * Uses externalId as idempotency key
     */
    @Transactional
    public Payment upsertFromStripe(
            String externalId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String customerName,
            String customerEmail,
            String rawPayload,
            String eventId
    ) {
        log.info("Upserting payment from Stripe: externalId={}", externalId);

        return paymentRepository.findByExternalId(externalId)
                .map(existing -> {
                    log.debug("Updating existing payment: id={}", existing.getId());
                    existing.transitionTo(status);
                    return existing;
                })
                .orElseGet(() -> {
                    log.debug("Creating new payment for externalId: {}", externalId);
                    return paymentRepository.save(
                            Payment.of(externalId, amount, currency, status, customerName, customerEmail, rawPayload, eventId)
                    );
                });
    }
}