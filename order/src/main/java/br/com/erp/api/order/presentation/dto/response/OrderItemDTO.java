package br.com.erp.api.order.presentation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDTO(
        Long itemId,
        Long skuId,
        String skuCode,
        String productName,
        String colorName,
        String colorHex,
        String sizeLabel,
        String imageUrl,
        UUID reservationId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
