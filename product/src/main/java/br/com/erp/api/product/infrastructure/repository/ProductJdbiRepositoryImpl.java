package br.com.erp.api.product.infrastructure.repository;

import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.port.ProductRepository;
import br.com.erp.api.product.infrastructure.mapper.ProductRowMapper;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductJdbiRepositoryImpl implements ProductRepository {

    private final Jdbi jdbi;

    public ProductJdbiRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Product save(Product product) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
            INSERT INTO products (name, description, slug, category_id, active)
            VALUES (:name, :description, :slug, :categoryId, :active)
        """)
                        .bind("name", product.getName())
                        .bind("description", product.getDescription())
                        .bind("slug", product.getSlugValue())
                        .bind("categoryId", product.getCategoryId().value())
                        .bind("active", product.isActive())
                        .executeAndReturnGeneratedKeys()
                        .map(new ProductRowMapper())
                        .one()
        );
    }

    @Override
    public Product update(Product product) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
            UPDATE products
            SET
                name = :name,
                description = :description,
                slug = :slug,
                category_id = :categoryId,
                active = :active
            WHERE id = :id
        """)
                        .bind("id", product.getId())
                        .bind("name", product.getName())
                        .bind("description", product.getDescription())
                        .bind("slug", product.getSlugValue())
                        .bind("categoryId", product.getCategoryId().value())
                        .bind("active", product.isActive())
                        .execute()
        );

        return product;
    }



    @Override
    public boolean existsByslug(String slug) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT COUNT(1)
                FROM products
WHERE slug = :slug
            """)
                        .bind("slug", slug)
                        .mapTo(Integer.class)
                        .one() > 0
        );
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT
                id,
                name,
                description,
                slug,
                category_id,
                active
            FROM products
            WHERE id = :id
       """)
                        .bind("id", id)
                        .map(new ProductRowMapper())
                        .findOne()
        );
    }



}
