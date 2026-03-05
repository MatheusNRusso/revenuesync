package com.mtnrs.revenuesync.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mtnrs.revenuesync.domain.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponseDto(

        /**
         * DTO for payment responses sent to frontend
         * Uses Java Record for immutability and conciseness
         */

        @JsonProperty("id")
        Long id,

        @JsonProperty("external_payment_id")
        String externalPaymentId,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("status")
        PaymentStatus status,

        @JsonProperty("customer_email")
        String customerEmail,

        @JsonProperty("customer_name")
        String customerName,

        @JsonProperty("created_at")
        String createdAt
) {}
