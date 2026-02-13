
package com.mtnrs.revenuesync.service;

import com.mtnrs.revenuesync.domain.Payment;
import com.mtnrs.revenuesync.domain.enums.PaymentStatus;
import com.mtnrs.revenuesync.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment upsertFromStripe(
            String externalId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String customerEmail,
            String rawPayload,
            String eventId
    ) {
        // Idempotência por externalId (Stripe object id)
        return paymentRepository.findByExternalId(externalId)
                .map(existing -> {
                    // regra simples: atualiza status/payload/eventId
                    existing.transitionTo(status);
                    return existing; // JPA dirty-check
                })
                .orElseGet(() -> paymentRepository.save(
                        Payment.of(externalId, amount, currency, status, customerEmail, rawPayload, eventId)
                ));
    }
}
