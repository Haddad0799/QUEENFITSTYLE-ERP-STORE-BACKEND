package br.com.erp.api.product.application.event;

public record ProductPublishedEvent(Long productId, br.com.erp.api.product.application.dto.ProductSnapshot snapshot) {}