package br.com.erp.api.attribute.domain.repository;

import br.com.erp.api.attribute.domain.entity.Category;

import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);
    Optional<Category> findByNormalizedName(String name);
    Optional<Category> findById(Long id);
    void update(Category category);
    boolean existsByName(String name);
    void deleteById(Long id);
    boolean hasProductsAssociated(Long categoryId);
    boolean hasPublishedProducts(Long categoryId);
    boolean hasSubcategories(Long parentId);
    boolean hasActiveSubcategories(Long parentId);
}
