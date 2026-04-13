package br.com.erp.api.product.domain.port;

import br.com.erp.api.product.domain.entity.Product;

import java.util.Optional;

public interface ProductRepositoryPort {
    Long save(Product product);
    void update(Product product);
    boolean existsByslug(String slug);
    Optional<Product> findById(Long id);
    boolean existsById(Long id);
    void updateStatus(Product product);
    void updatePrimaryImage(Product product);
    String findCategoryNameByProductId(Long productId);
    String findCategoryNormalizedNameByProductId(Long productId);
    Optional<Product> findBySlug(String slug);
    void deleteById(Long id);
}
