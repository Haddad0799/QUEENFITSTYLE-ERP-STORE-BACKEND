package br.com.erp.api.product.application.event;

import br.com.erp.api.product.application.dto.ProductSnapshot;

public record ProductPublishedEvent(Long productId, ProductSnapshot snapshot) {}