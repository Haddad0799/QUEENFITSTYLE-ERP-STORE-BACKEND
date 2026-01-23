package br.com.erp.api.product.infrastructure.repository;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.query.ProductQueryRepository;
import br.com.erp.api.product.application.query.filter.ProductFilter;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.infrastructure.mapper.ProductRowMapper;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductJdbiQueryRepositoryImpl implements ProductQueryRepository {

    private final Jdbi jdbi;

    public ProductJdbiQueryRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Page<ProductDetailsDTO> findAll(ProductFilter filter, Pageable pageable) {

        StringBuilder sql = new StringBuilder("""
            SELECT id, name, description, slug, category_id, active
            FROM products
            WHERE 1=1
        """);

        if (filter.active() != null) {
            sql.append(" AND active = :active ");
        }

        if (filter.categoryId() != null) {
            sql.append(" AND category_id = :categoryId ");
        }

        // Paginação
        sql.append(" LIMIT :limit OFFSET :offset ");

        // 1️⃣ Executa query e mapeia para Product
        List<Product> products = jdbi.withHandle(handle -> {
            var query = handle.createQuery(sql.toString())
                    .bind("limit", pageable.getPageSize())
                    .bind("offset", pageable.getOffset());

            if (filter.active() != null)
                query.bind("active", filter.active());

            if (filter.categoryId() != null)
                query.bind("categoryId", filter.categoryId());

            return query.map(new ProductRowMapper()).list();
        });

        // 2️⃣ Converte para DTO
        List<ProductDetailsDTO> content = products.stream()
                .map(p -> new ProductDetailsDTO(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getSlugValue(),
                        p.getCategoryId(),
                        p.isActive()
                ))
                .toList();

        // Count query
        Long total = jdbi.withHandle(handle -> {
            String countSql = """
                SELECT COUNT(*)
                FROM products
                WHERE (:active IS NULL OR active = :active)
                  AND (:categoryId IS NULL OR category_id = :categoryId)
            """;

            return handle.createQuery(countSql)
                    .bind("active", filter.active())
                    .bind("categoryId", filter.categoryId())
                    .mapTo(Long.class)
                    .one();
        });

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public ProductDetailsDTO findById(Long id) {
        Product product = jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, name, description, slug, category_id, active
                FROM products
                WHERE id = :id
            """)
                        .bind("id", id)
                        .map(new ProductRowMapper())
                        .findOne()
                        .orElseThrow(() -> new ProductNotFoundException(id))
        );

        return new ProductDetailsDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlugValue(),
                product.getCategoryId(),
                product.isActive()
        );
    }

}


