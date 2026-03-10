package com.mtnrs.revenuesync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtnrs.revenuesync.client.GoogleAdsClient;
import com.mtnrs.revenuesync.client.MetaCapiClient;
import com.mtnrs.revenuesync.domain.Conversion;
import com.mtnrs.revenuesync.domain.enums.ConversionPlatform;
import com.mtnrs.revenuesync.repository.ConversionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ConversionService {

    private final MetaCapiClient metaCapiClient;
    private final GoogleAdsClient googleAdsClient;
    private final ConversionRepository conversionRepository;
    private final ObjectMapper objectMapper;

    public ConversionService(
            MetaCapiClient metaCapiClient,
            GoogleAdsClient googleAdsClient,
            ConversionRepository conversionRepository,
            ObjectMapper objectMapper
    ) {
        this.metaCapiClient = metaCapiClient;
        this.googleAdsClient = googleAdsClient;
        this.conversionRepository = conversionRepository;
        this.objectMapper = objectMapper;
    }

    // =========================================================================
    // WRITE OPERATIONS (existing)
    // =========================================================================

    @Transactional
    public Conversion sendToMeta(Long paymentId, BigDecimal value, String requestJson) {
        String response = metaCapiClient.sendPurchaseEvent(requestJson);

        Conversion conv = Conversion.of(
                paymentId,
                ConversionPlatform.META,
                value,
                ensureJson(requestJson),
                ensureJson(response)
        );

        return conversionRepository.save(conv);
    }

    @Transactional
    public Conversion sendToGoogle(Long paymentId, BigDecimal value, String requestJson) {
        String response = googleAdsClient.sendPurchaseConversion(requestJson);
        Conversion conv = Conversion.of(
                paymentId,
                ConversionPlatform.GOOGLE,
                value,
                requestJson,
                response);
        return conversionRepository.save(conv);
    }

    // =========================================================================
    // READ OPERATIONS (new - for Angular frontend)
    // =========================================================================

    /**
     * Returns all conversions ordered by creation date (newest first)
     */
    @Transactional(readOnly = true)
    public List<Conversion> findAll() {
        return conversionRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Returns conversions filtered by payment ID
     */
    @Transactional(readOnly = true)
    public List<Conversion> findByPaymentId(Long paymentId) {
        return conversionRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId);
    }


    /**
     * Returns conversions filtered by platform.
     *
     * @param platform the ConversionPlatform enum value (META or GOOGLE)
     * @return list of conversions for the specified platform
     */
    @Transactional(readOnly = true)
    public List<Conversion> findByPlatform(ConversionPlatform platform) {
        return conversionRepository.findByPlatformOrderByCreatedAtDesc(platform);
    }


    // =========================================================================
    // HELPERS
    // =========================================================================

    private String ensureJson(String payload) {
        if (payload == null || payload.isBlank()) return null;

        String p = payload.trim();
        if (p.startsWith("{") || p.startsWith("[")) return p;

        return "\"" + p.replace("\"", "\\\"") + "\"";
    }
}