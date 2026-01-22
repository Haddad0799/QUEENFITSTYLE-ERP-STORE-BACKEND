package br.com.erp.api.product.domain.port;

import br.com.erp.api.product.domain.entity.Product;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Product update(Product product);
    boolean existsByslug(String slug);
    Optional<Product> findById(Long id);


}
