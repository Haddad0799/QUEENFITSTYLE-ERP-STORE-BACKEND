package br.com.erp.api.product.domain.enumerated;

public enum ProductStatus {
    DRAFT,           // Produto sendo cadastrado
    READY_FOR_SALE,  // Tem SKU ativo e está consistente
    PUBLISHED,       // Disponível no ecommerce
    INACTIVE,        // Desativado temporariamente
    ARCHIVED         // Produto morto / histórico
}
