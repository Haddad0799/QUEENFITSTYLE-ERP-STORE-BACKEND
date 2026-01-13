package br.com.erp.api.catalog.domain.repository;

import br.com.erp.api.catalog.domain.entity.Category;

import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);
    Optional<Category> findByNormalizedName(String name);
    Optional<Category> findById(Long id);
    void update(Category category);
    boolean existsByName(String name);
}
