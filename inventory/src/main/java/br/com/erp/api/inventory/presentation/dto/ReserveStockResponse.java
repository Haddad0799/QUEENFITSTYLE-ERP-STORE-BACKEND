package br.com.erp.api.inventory.presentation.dto;

import java.util.UUID;

public record ReserveStockResponse(UUID reservationId, String skuCode, int quantity) {}

