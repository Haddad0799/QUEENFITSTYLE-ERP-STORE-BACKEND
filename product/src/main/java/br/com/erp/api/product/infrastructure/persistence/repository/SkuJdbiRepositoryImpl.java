package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import br.com.erp.api.product.domain.valueobject.Dimensions;
import br.com.erp.api.product.domain.valueobject.SkuCode;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SkuJdbiRepositoryImpl implements SkuRepositoryPort {

    private final Jdbi jdbi;

    public SkuJdbiRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }


    @Override
    public Map<String, Long> saveAll(Long productId, List<Sku> skus) {

        if (skus == null || skus.isEmpty()) {
            return Map.of();  // era List.of() — bug, já corrijo aqui
        }

        return jdbi.withHandle(handle -> {

            var batch = handle.prepareBatch("""
            INSERT INTO skus (
                product_id,
                sku_code,
                color_id,
                size_id,
                width,
                height,
                length,
                weight,
                status
            ) VALUES (
                :productId,
                :code,
                :colorId,
                :sizeId,
                :width,
                :height,
                :length,
                :weight,
                :status
            )
        """);

            for (Sku sku : skus) {
                batch.bind("productId", productId)
                        .bind("code", sku.getCode().value())
                        .bind("colorId", sku.getColorId())
                        .bind("sizeId", sku.getSizeId())
                        .bind("width", sku.getDimensions().width())
                        .bind("height", sku.getDimensions().height())
                        .bind("length", sku.getDimensions().length())
                        .bind("weight", sku.getDimensions().weight())
                        .bind("status", sku.getStatus())
                        .add();
            }

            return batch
                    .executePreparedBatch("id", "sku_code")  // retorna ambas as colunas
                    .map((rs, ctx) -> Map.entry(
                            rs.getString("sku_code"),
                            rs.getLong("id")
                    ))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
        });
    }

    @Override
    public boolean existsByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT COUNT(*) > 0
                FROM skus
                WHERE product_id = :productId
                  AND color_id = :colorId
            """)
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public List<Long> findIdsByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id
                FROM skus
                WHERE product_id = :productId
                  AND color_id = :colorId
            """)
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .mapTo(Long.class)
                        .list()
        );
    }

    @Override
    public Optional<Sku> findById(Long skuId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, product_id, sku_code, color_id, size_id,
                       width, height, length, weight, status
                FROM skus
                WHERE id = :skuId
            """)
                        .bind("skuId", skuId)
                        .map((rs, ctx) -> new Sku(
                                rs.getLong("id"),
                                rs.getLong("product_id"),
                                SkuCode.of(rs.getString("sku_code")),
                                rs.getLong("color_id"),
                                rs.getLong("size_id"),
                                Dimensions.of(
                                        rs.getBigDecimal("width"),
                                        rs.getBigDecimal("height"),
                                        rs.getBigDecimal("length"),
                                        rs.getBigDecimal("weight")
                                ),
                                SkuStatus.valueOf(rs.getString("status"))
                        ))
                        .findOne()
        );
    }

    @Override
    public void updateStatus(Sku sku) {
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                UPDATE skus
                SET status = :status
                WHERE id = :id
            """)
                        .bind("status", sku.getStatus().name())
                        .bind("id", sku.getId())
                        .execute()
        );
    }

    @Override
    public List<Sku> findByProductId(Long productId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, product_id, sku_code, color_id, size_id,
                       width, height, length, weight, status
                FROM skus
                WHERE product_id = :productId
            """)
                        .bind("productId", productId)
                        .map((rs, ctx) -> new Sku(
                                rs.getLong("id"),
                                rs.getLong("product_id"),
                                SkuCode.of(rs.getString("sku_code")),
                                rs.getLong("color_id"),
                                rs.getLong("size_id"),
                                Dimensions.of(
                                        rs.getBigDecimal("width"),
                                        rs.getBigDecimal("height"),
                                        rs.getBigDecimal("length"),
                                        rs.getBigDecimal("weight")
                                ),
                                SkuStatus.valueOf(rs.getString("status"))
                        ))
                        .list()
        );
    }

    @Override
    public void updateStatusBatch(List<Sku> skus) {
        if (skus == null || skus.isEmpty()) return;

        jdbi.withHandle(handle -> {
            var batch = handle.prepareBatch("""
            UPDATE skus
            SET status = :status
            WHERE id = :id
        """);

            for (Sku sku : skus) {
                batch.bind("status", sku.getStatus().name())
                        .bind("id", sku.getId())
                        .add();
            }

            return batch.execute();
        });
    }


}
