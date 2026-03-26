package br.com.erp.api.product.application.port;

public interface ProductCatalogPort {
    void publish(Long productId);
    void publishIfPublished(Long productId);
    void unpublish(Long productId);
}
