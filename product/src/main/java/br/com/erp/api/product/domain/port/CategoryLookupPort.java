package br.com.erp.api.product.domain.port;

public interface CategoryLookupPort {
    boolean existsActiveById(Long id);
}
