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

    private String ensureJson(String payload) {
        if (payload == null || payload.isBlank()) return null;

        // se já começa como objeto/array JSON, assume que é JSON
        String p = payload.trim();
        if (p.startsWith("{") || p.startsWith("[")) return p;

        // caso contrário, transforma em JSON string "..."
        return "\"" + p.replace("\"", "\\\"") + "\"";
    }


    @Transactional
    public Conversion sendToGoogle(Long paymentId, BigDecimal value, String requestJson) {
        String response = googleAdsClient.sendPurchaseConversion(requestJson);
        Conversion conv = Conversion.of(paymentId, ConversionPlatform.GOOGLE, value, requestJson, response);
        return conversionRepository.save(conv);
    }
}
