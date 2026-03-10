package com.mtnrs.revenuesync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtnrs.revenuesync.config.StripeConfig;
import com.mtnrs.revenuesync.domain.Payment;
import com.mtnrs.revenuesync.domain.enums.LeadSource;
import com.mtnrs.revenuesync.domain.enums.PaymentStatus;
import com.mtnrs.revenuesync.dto.stripe.StripeEventDto;
import com.mtnrs.revenuesync.dto.stripe.StripeObjectDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StripeWebhookService {

    private final StripeSignatureService signatureService;
    private final StripeConfig stripeConfig;
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final ConversionService conversionService;
    private final LeadService leadService;

    public StripeWebhookService(
            StripeSignatureService signatureService,
            StripeConfig stripeConfig,
            ObjectMapper objectMapper,
            PaymentService paymentService,
            ConversionService conversionService,
            LeadService leadService
    ) {
        this.signatureService = signatureService;
        this.stripeConfig = stripeConfig;
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
        this.conversionService = conversionService;
        this.leadService = leadService;
    }

    @Transactional
    public void handle(String rawPayload, String stripeSignatureHeader) throws Exception {
        signatureService.verifyOrThrow(rawPayload, stripeSignatureHeader, stripeConfig.webhookSecret());

        StripeEventDto event = objectMapper.readValue(rawPayload, StripeEventDto.class);

        if (!"checkout.session.completed".equals(event.type())) {
            return;
        }

        StripeObjectDto obj = event.data().object();

        String externalId = obj.id();
        String currency = obj.currency();
        String email = obj.customerDetails() != null ? obj.customerDetails().email() : null;
        String name = obj.customerDetails() != null ? obj.customerDetails().name() : null;

        BigDecimal amount = obj.amountTotal() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(obj.amountTotal()).movePointLeft(2); // cents -> dollars

        PaymentStatus status = "paid".equalsIgnoreCase(obj.paymentStatus())
                ? PaymentStatus.SUCCEEDED
                : PaymentStatus.PROCESSING;


        Payment payment = paymentService.upsertFromStripe(
                externalId,
                amount,
                currency,
                status,
                name,
                email,
                rawPayload,
                event.id()
        );

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            Long paymentId = payment.getId();

            String metaRequestJson = "{\"event\":\"Purchase\",\"value\":" + amount + "}";
            String googleRequestJson = "{\"conversion\":\"Purchase\",\"value\":" + amount + "}";

            conversionService.sendToMeta(paymentId, amount, metaRequestJson);
            conversionService.sendToGoogle(paymentId, amount, googleRequestJson);

            if (email != null && !email.isBlank()) {
                String leadRequestJson = "{\"email\":\"" + email + "\",\"source\":\"STRIPE\"}";
                leadService.createLead(email, null, LeadSource.STRIPE_CHECKOUT, leadRequestJson);
            }
        }
    }
}
