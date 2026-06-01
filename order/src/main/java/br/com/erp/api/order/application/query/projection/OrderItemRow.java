package br.com.erp.api.order.application.query.projection;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Linha bruta de item do pedido enriquecida com dados de catálogo (cor + imagem da SKU).
 * O assembler resolve a image_key em URL pública antes de expor ao front-end.
 */
public record OrderItemRow(
        Long itemId,
        Long skuId,
        String skuCode,
        String productName,
        String colorName,
        String colorHex,
        String sizeLabel,
        String imageKey,
        UUID reservationId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
