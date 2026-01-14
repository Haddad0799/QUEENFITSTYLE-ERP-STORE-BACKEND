package br.com.erp.api.catalog.infrastructure.persistence;

import br.com.erp.api.catalog.domain.entity.Category;
import br.com.erp.api.catalog.domain.repository.CategoryRepository;
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
                INSERT INTO categories (display_name, normalized_name, active)
                VALUES (:displayName, :normalizedName, :active)
                RETURNING id, display_name, normalized_name, active
            """)
                        .bind("displayName", category.getDisplayName())
                        .bind("normalizedName", category.getNormalizedName())
                        .bind("active", category.isActive())
                        .map(new CategoryRowMapper())
                        .one()
        );
    }

    @Override
    public Optional<Category> findByNormalizedName(String name) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, display_name, normalized_name, active
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
                SELECT id, display_name, normalized_name, active
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

}

