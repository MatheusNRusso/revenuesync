package com.mtnrs.revenuesync.service;

import org.springframework.stereotype.Service;

@Service
public class StripeSignatureService {

    /**
     * Valida o header Stripe-Signature.
     * Começar retornando true e depois evoluir para validação real.
     */
    public void verifyOrThrow(String payload, String stripeSignatureHeader, String webhookSecret) {
        if (stripeSignatureHeader == null || stripeSignatureHeader.isBlank()) {
            throw new IllegalArgumentException("Missing Stripe-Signature header");
        }

        // TODO: validar assinatura real (HMAC + timestamp tolerance).

    }
}
