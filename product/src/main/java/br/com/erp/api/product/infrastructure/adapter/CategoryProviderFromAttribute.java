package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.attribute.application.query.CategoryQueryService;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoryDetailsDTO;
import br.com.erp.api.product.application.dto.CategorySnapshot;
import br.com.erp.api.product.application.provider.CategoryProvider;
import br.com.erp.api.shared.application.projection.IdNameProjection;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CategoryProviderFromAttribute implements CategoryProvider {

    private final CategoryQueryService categoryQueryService;

    public CategoryProviderFromAttribute(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @Override
    public boolean existsActiveById(Long id) {
        return categoryQueryService.existsActiveById(id);
    }

    @Override
    public boolean isSubcategory(Long id) {
        return categoryQueryService.isSubcategory(id);
    }

    @Override
    public Optional<IdNameProjection> findByName(String name) {
        return categoryQueryService.findByName(name)
                .map(c -> new IdNameProjection(c.id(), c.name()));
    }

    @Override
    public CategorySnapshot findById(Long id) {
        CategoryDetailsDTO dto = categoryQueryService.findById(id);
        return new CategorySnapshot(
                dto.id(),
                dto.name(),
                dto.normalizedName(),
                dto.parentId()
        );
    }
}
