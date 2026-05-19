package br.com.erp.api.inventory.presentation.dto;

public record InventoryDTO(Long skuId, int quantity, int reserved, int minQuantity, int available) {}

