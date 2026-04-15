package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.infrastructure.mapper.ProductRowMapper;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class ProductJdbiRepositoryImpl implements ProductRepositoryPort {

    private final Jdbi jdbi;

    public ProductJdbiRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Long save(Product product) {
        return jdbi.withHandle(handle ->
                handle.createUpdate("""
            INSERT INTO products (name, description, slug, category_id, status, is_launch)
            VALUES (:name, :description, :slug, :categoryId, :status, :isLaunch)
        """)
                        .bind("name", product.getName())
                        .bind("description", product.getDescription())
                        .bind("slug", product.getSlugValue())
                        .bind("categoryId", product.getCategoryId())
                        .bind("status", product.getStatus().name())
                        .bind("isLaunch", product.isLaunch())
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .one()
        );
    }

    @Override
    public void update(Product product) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
            UPDATE products
            SET
                name = :name,
                description = :description,
                slug = :slug,
                category_id = :categoryId,
                status = :status,
                is_launch = :isLaunch
            WHERE id = :id
        """)
                        .bind("id", product.getId())
                        .bind("name", product.getName())
                        .bind("description", product.getDescription())
                        .bind("slug", product.getSlugValue())
                        .bind("categoryId", product.getCategoryId())
                        .bind("status", product.getStatus().name())
                        .bind("isLaunch", product.isLaunch())
                        .execute()
        );
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
            status,
            is_launch,
            primary_image_id
        FROM products
        WHERE id = :id
   """)
                        .bind("id", id)
                        .map(new ProductRowMapper())
                        .findOne()
        );
    }

    @Override
    public boolean existsById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT 1
                FROM products
                WHERE id = :id
                LIMIT 1
            """)
                        .bind("id", id)
                        .mapTo(Integer.class)
                        .findOne()
                        .isPresent()
        );
    }

    @Override
    public void updateStatus(Product product) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                UPDATE products
                SET status = :status
                WHERE id = :id
            """)
                        .bind("status", product.getStatus().name())
                        .bind("id", product.getId())
                        .execute()
        );
    }

    @Override
    public void updatePrimaryImage(Product product) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                UPDATE products
                SET primary_image_id = :primaryImageId
                WHERE id = :id
            """)
                        .bind("primaryImageId", product.getPrimaryImageId())
                        .bind("id", product.getId())
                        .execute()
        );
    }

    @Override
    public String findCategoryNameByProductId(Long productId) {
        return jdbi.withHandle(handle ->
                Objects.requireNonNull(handle.createQuery("""
                                    SELECT c.display_name
                                    FROM products p
                                    JOIN categories c ON c.id = p.category_id
                                    WHERE p.id = :productId
                                """)
                        .bind("productId", productId)
                        .mapTo(String.class)
                        .findOne()
                        .orElse(null))
        );
    }

    @Override
    public String findCategoryNormalizedNameByProductId(Long productId) {
        return jdbi.withHandle(handle ->
                Objects.requireNonNull(handle.createQuery("""
                                    SELECT c.normalized_name
                                    FROM products p
                                    JOIN categories c ON c.id = p.category_id
                                    WHERE p.id = :productId
                                """)
                        .bind("productId", productId)
                        .mapTo(String.class)
                        .findOne()
                        .orElse(null))
        );
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT
                    id,
                    name,
                    description,
                    slug,
                    category_id,
                    status,
                    is_launch,
                    primary_image_id
                FROM products
                WHERE slug = :slug
            """)
                        .bind("slug", slug)
                        .map(new ProductRowMapper())
                        .findOne()
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbi.useHandle(handle ->
                handle.createUpdate("DELETE FROM products WHERE id = :id")
                        .bind("id", id)
                        .execute()
        );
    }

}
