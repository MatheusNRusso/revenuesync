package com.mtnrs.revenuesync.domain;

import com.mtnrs.revenuesync.domain.enums.ConversionPlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(
        name = "conversions",
        indexes = {
                @Index(name = "idx_conversions_payment_id", columnList = "paymentId"),
                @Index(name = "idx_conversions_platform", columnList = "platform"),
                @Index(name = "idx_conversions_created_at", columnList = "createdAt")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Conversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversionPlatform platform;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", columnDefinition = "jsonb")
    private String requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private String responsePayload;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public static Conversion of(
            Long paymentId,
            ConversionPlatform platform,
            BigDecimal value,
            String requestPayload,
            String responsePayload
    ) {
        if (paymentId == null) throw new IllegalArgumentException("paymentId must not be null");
        if (platform == null) throw new IllegalArgumentException("platform must not be null");
        if (value == null || value.signum() < 0) throw new IllegalArgumentException("value must be zero or positive");

        return Conversion.builder()
                .paymentId(paymentId)
                .platform(platform)
                .value(value)
                .requestPayload(requestPayload)
                .responsePayload(responsePayload)
                .build();
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
