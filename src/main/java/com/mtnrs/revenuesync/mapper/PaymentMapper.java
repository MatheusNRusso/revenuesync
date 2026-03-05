package com.mtnrs.revenuesync.mapper;

import com.mtnrs.revenuesync.domain.Payment;
import com.mtnrs.revenuesync.dto.payment.PaymentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Converts a Payment entity to a PaymentResponseDto
     *
     * @param payment the Payment entity to convert
     * @return the converted PaymentResponseDto
     */
    @Mapping(target = "externalPaymentId", source = "externalId")
    @Mapping(target = "customerName", source = "payment", qualifiedByName = "extractCustomerName")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatDateTime")
    PaymentResponseDto toDto(Payment payment);

    /**
     * Extracts the customer name from the raw payload JSON
     * The name is stored in the "name" field of the customer_details object
     *
     * @param payment the Payment entity containing the raw payload
     * @return the extracted customer name, or null if not found
     */
    @Named("extractCustomerName")
    default String extractCustomerName(Payment payment) {

        if (payment == null || payment.getRawPayload() == null) {
            return null;
        }

        String rawPayload = payment.getRawPayload();

        // Look for "name":"value" pattern in the JSON
        int nameIndex = rawPayload.indexOf("\"name\":\"");
        if (nameIndex > 0) {
            int start = nameIndex + 8; // length of "name":"
            int end = rawPayload.indexOf("\"", start);
            if (end > start) {
                return rawPayload.substring(start, end);
            }
        }

        return null;
    }

    /**
     * Formats OffsetDateTime to ISO 8601 string
     *
     * @param dateTime the OffsetDateTime to format
     * @return formatted string or null if input is null
     */
    @Named("formatDateTime")
    default String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}