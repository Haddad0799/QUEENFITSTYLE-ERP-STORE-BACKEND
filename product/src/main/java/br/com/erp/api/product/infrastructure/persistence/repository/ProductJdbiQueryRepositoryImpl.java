package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.query.ProductQueryRepository;
import br.com.erp.api.product.application.query.filter.ProductFilter;
import br.com.erp.api.product.domain.enumerated.ProductStatus;
import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.infrastructure.persistence.query.PageQuery;
import br.com.erp.api.product.infrastructure.persistence.query.ProductFilterSqlResolver;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import br.com.erp.api.product.presentation.dto.response.ProductSummaryDTO;
import br.com.erp.api.product.presentation.dto.response.SkuSummaryDTO;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductJdbiQueryRepositoryImpl implements ProductQueryRepository {

    private final Jdbi jdbi;
    private final ProductFilterSqlResolver resolver = new ProductFilterSqlResolver();

    public ProductJdbiQueryRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    @Override
    public Page<ProductSummaryDTO> findAll(ProductFilter filter, Pageable pageable) {

        PageQuery pageQuery = resolver.build(filter, pageable);

        //SELECT (usa filtros + paginação)
        List<ProductSummaryDTO> content = jdbi.withHandle(handle -> {

            var query = handle.createQuery(pageQuery.selectSql());

            pageQuery.filterParams().forEach(query::bind);
            pageQuery.pageParams().forEach(query::bind);

            return query.map((rs, ctx) ->
                    new ProductSummaryDTO(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("slug"),
                            rs.getString("category_name"),
                            rs.getString("status")
                    )
            ).list();
        });

        //COUNT (usa apenas filtros)
        Long total = jdbi.withHandle(handle -> {

            var query = handle.createQuery(pageQuery.countSql());

            pageQuery.filterParams().forEach(query::bind);

            return query.mapTo(Long.class).one();
        });

        return new PageImpl<>(content, pageable, total);
    }
    @Override
    public ProductDetailsDTO findById(Long id) {
        //buscar produtos + categorias
        var productData = jdbi.withHandle(handle ->
                handle.createQuery("""
        SELECT
            p.id,
            p.name,
            p.description,
            p.slug,
            p.status,
            c.id AS category_id,
            c.display_name AS category_name
        FROM products p
        JOIN categories c ON c.id = p.category_id
        WHERE p.id = :id
    """)
                        .bind("id", id)
                        .map((rs, ctx) -> new ProductDetailsDTO(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getString("slug"),
                                rs.getLong("category_id"),
                                rs.getString("category_name"),
                                ProductStatus.valueOf(rs.getString("status")),
                                List.of() // temporário
                        ))
                        .findOne()
                        .orElseThrow(() -> new ProductNotFoundException(id))
        );
        //buscar variaçoes do produto
        List<SkuSummaryDTO> skus = jdbi.withHandle(handle ->
                handle.createQuery("""
        SELECT
            s.id,
            s.sku_code AS code,
            c.name AS color_name,
            sz.label AS size_name,
            s.status
        FROM skus s
        JOIN colors c ON c.id = s.color_id
        JOIN sizes sz ON sz.id = s.size_id
        WHERE s.product_id = :productId
    """)
                        .bind("productId", id)
                        .map((rs, ctx) -> new SkuSummaryDTO(
                                rs.getLong("id"),
                                rs.getString("code"),
                                rs.getString("color_name"),
                                rs.getString("size_name"),
                                SkuStatus.valueOf(rs.getString("status"))
                        ))
                        .list()
        );

        //montar dto de resposta
        return new ProductDetailsDTO(
                productData.id(),
                productData.name(),
                productData.description(),
                productData.slug(),
                productData.categoryId(),
                productData.categoryName(),
                productData.status(),
                skus
        );
    }
}


