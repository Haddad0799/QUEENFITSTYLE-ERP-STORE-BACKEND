package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.application.query.SkuQueryRepository;
import br.com.erp.api.product.presentation.dto.response.SkuDetailsDTO;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SkuJdbiQueryRepositoryImpl implements SkuQueryRepository {

    private final Jdbi jdbi;

    public SkuJdbiQueryRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Page<SkuDetailsDTO> findByProductId(Long productId, Pageable pageable) {

        String baseSql = """
    SELECT
        s.id,
        s.sku_code AS code,
        s.color_id AS colorId,
        c.name AS colorName,
        s.size_id
         AS sizeId,
        sz.label AS sizeName,
        s.width,
        s.height,
        s.length,
        s.weight,
        s.active
    FROM skus s
    LEFT JOIN colors c ON c.id = s.color_id
    LEFT JOIN sizes sz ON sz.id = s.size_id
    WHERE s.product_id = :productId
    ORDER BY s.id
    LIMIT :limit OFFSET :offset
""";

        String countSql = """
    SELECT COUNT(*)
    FROM skus
    WHERE product_id = :productId
""";


        List<SkuDetailsDTO> skus = jdbi.withHandle(handle ->
                handle.createQuery(baseSql)
                        .bind("productId", productId)
                        .bind("limit", pageable.getPageSize())
                        .bind("offset", pageable.getOffset())
                        .mapToBean(SkuDetailsDTO.class)
                        .list()
        );

        Long total = jdbi.withHandle(handle ->
                handle.createQuery(countSql)
                        .bind("productId", productId)
                        .mapTo(Long.class)
                        .one()
        );

        return new PageImpl<>(skus, pageable, total);
    }
}
