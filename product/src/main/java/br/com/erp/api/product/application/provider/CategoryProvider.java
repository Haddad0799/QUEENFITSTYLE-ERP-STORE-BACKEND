package br.com.erp.api.product.application.provider;

import br.com.erp.api.product.application.dto.CategorySnapshot;
import br.com.erp.api.shared.application.projection.IdNameProjection;

import java.util.Optional;

public interface CategoryProvider {
    boolean existsActiveById(Long id);
    boolean isSubcategory(Long id);
    Optional<IdNameProjection> findByName(String name);
    CategorySnapshot findById(Long id);
}
