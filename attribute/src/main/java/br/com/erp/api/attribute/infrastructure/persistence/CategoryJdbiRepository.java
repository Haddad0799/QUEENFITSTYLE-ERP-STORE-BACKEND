package br.com.erp.api.attribute.infrastructure.persistence;

import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.repository.CategoryRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CategoryJdbiRepository implements CategoryRepository {

    private final Jdbi jdbi;

    public CategoryJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Category save(Category category) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                INSERT INTO categories (display_name, normalized_name, active, parent_id)
                VALUES (:displayName, :normalizedName, :active, :parentId)
                RETURNING id, display_name, normalized_name, active, parent_id
            """)
                        .bind("displayName", category.getDisplayName())
                        .bind("normalizedName", category.getNormalizedName())
                        .bind("active", category.isActive())
                        .bind("parentId", category.getParentId())
                        .map(new CategoryRowMapper())
                        .one()
        );
    }

    @Override
    public Optional<Category> findByNormalizedName(String name) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, display_name, normalized_name, active, parent_id
                FROM categories
                WHERE normalized_name = :name
            """)
                        .bind("name", name)
                        .map(new CategoryRowMapper())
                        .findOne()
        );
    }

    @Override
    public Optional<Category> findById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, display_name, normalized_name, active, parent_id
                FROM categories
                WHERE id = :id
            """)
                        .bind("id", id)
                        .map(new CategoryRowMapper())
                        .findOne()
        );
    }

    @Override
    public void update(Category category) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
            UPDATE categories
            SET display_name = :displayName,
                normalized_name = :normalizedName,
                active = :active
            WHERE id = :id
        """)
                        .bind("id", category.getId())
                        .bind("displayName", category.getDisplayName())
                        .bind("normalizedName", category.getNormalizedName())
                        .bind("active", category.isActive())
                        .execute()
        );
    }

    @Override
    public boolean existsByName(String normalizedName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT COUNT(1)
                FROM categories
                WHERE normalized_name = :name
            """)
                        .bind("name", normalizedName)
                        .mapTo(Integer.class)
                        .one() > 0
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbi.useHandle(handle ->
                handle.createUpdate("DELETE FROM categories WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

    @Override
    public boolean hasProductsAssociated(Long categoryId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT COUNT(1) > 0
                    FROM products
                    WHERE category_id = :categoryId
                """)
                        .bind("categoryId", categoryId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public boolean hasPublishedProducts(Long categoryId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT COUNT(1) > 0
                    FROM products
                    WHERE category_id = :categoryId
                      AND status = 'PUBLISHED'
                """)
                        .bind("categoryId", categoryId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public boolean hasSubcategories(Long parentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT COUNT(1) > 0
                    FROM categories
                    WHERE parent_id = :parentId
                """)
                        .bind("parentId", parentId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public boolean hasActiveSubcategories(Long parentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT COUNT(1) > 0
                    FROM categories
                    WHERE parent_id = :parentId
                      AND active = true
                """)
                        .bind("parentId", parentId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }
}

