package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import br.com.erp.api.product.domain.valueobject.Dimensions;
import br.com.erp.api.product.domain.valueobject.SkuCode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    private Sku mapSku(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Sku(
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
        );
    }

    private static final String SKU_SELECT = """
            SELECT id, product_id, sku_code, color_id, size_id,
                   width, height, length, weight, status
            FROM skus
            """;

    @Override
    public Map<String, Long> saveAll(Long productId, List<Sku> skus) {

        if (skus == null || skus.isEmpty()) return Map.of();

        return jdbi.withHandle(handle -> {

            //1. INSERT COM ON CONFLICT
            var batch = handle.prepareBatch("""
            INSERT INTO skus (
                product_id, sku_code, color_id, size_id,
                width, height, length, weight, status
            ) VALUES (
                :productId, :code, :colorId, :sizeId,
                :width, :height, :length, :weight, :status
            )
            ON CONFLICT (product_id, color_id, size_id) DO NOTHING
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

            batch.execute();

            //2. BUSCAR TODOS (novos + existentes)
            List<String> skuCodes = skus.stream()
                    .map(s -> s.getCode().value())
                    .toList();

            return handle.createQuery("""
                SELECT id, sku_code
                FROM skus
                WHERE product_id = :productId
                  AND sku_code IN (<codes>)
            """)
                    .bind("productId", productId)
                    .bindList("codes", skuCodes)
                    .map((rs, ctx) -> Map.entry(
                            rs.getString("sku_code"),
                            rs.getLong("id")
                    ))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        });
    }

    @Override
    public Optional<Sku> findById(Long skuId) {
        return jdbi.withHandle(handle ->
                handle.createQuery(SKU_SELECT + " WHERE id = :skuId")
                        .bind("skuId", skuId)
                        .map(this::mapSku)
                        .findOne()
        );
    }

    @Override
    public Optional<Sku> findByProductIdAndSkuId(Long productId, Long skuId) {
        return jdbi.withHandle(handle ->
                handle.createQuery(SKU_SELECT + " WHERE id = :skuId AND product_id = :productId")
                        .bind("skuId", skuId)
                        .bind("productId", productId)
                        .map(this::mapSku)
                        .findOne()
        );
    }

    @Override
    public List<Sku> findByProductId(Long productId) {
        return jdbi.withHandle(handle ->
                handle.createQuery(SKU_SELECT + " WHERE product_id = :productId")
                        .bind("productId", productId)
                        .map(this::mapSku)
                        .list()
        );
    }

    @Override
    public List<Sku> findByProductIdAndStatus(Long productId, SkuStatus status) {
        return jdbi.withHandle(handle ->
                handle.createQuery(SKU_SELECT + " WHERE product_id = :productId AND status = :status")
                        .bind("productId", productId)
                        .bind("status", status.name())
                        .map(this::mapSku)
                        .list()
        );
    }

    @Override
    public List<Long> findIdsByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT id FROM skus
                    WHERE product_id = :productId AND color_id = :colorId
                """)
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .mapTo(Long.class)
                        .list()
        );
    }

    @Override
    public boolean existsByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT COUNT(*) > 0 FROM skus
                    WHERE product_id = :productId AND color_id = :colorId
                """)
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public boolean existsByProductIdAndSkuId(Long productId, Long skuId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT COUNT(*) > 0 FROM skus
                    WHERE id = :skuId AND product_id = :productId
                """)
                        .bind("skuId", skuId)
                        .bind("productId", productId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public void updateStatusBatch(List<Long> skuIds, SkuStatus skuStatus) {
        if (skuIds == null || skuIds.isEmpty()) return;

        jdbi.useHandle(handle ->
                handle.createUpdate("UPDATE skus SET status = :status WHERE id IN (<ids>)")
                        .bind("status", skuStatus.name())
                        .bindList("ids", skuIds)
                        .execute()
        );
    }

    @Override
    public List<Sku> findByProductIdAndStatusIn(Long productId, List<SkuStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) return List.of();

        List<String> statusNames = statuses.stream()
                .map(SkuStatus::name)
                .toList();

        return jdbi.withHandle(handle ->
                handle.createQuery(SKU_SELECT + " WHERE product_id = :productId AND status IN (<statuses>)")
                        .bind("productId", productId)
                        .bindList("statuses", statusNames)
                        .map(this::mapSku)
                        .list()
        );
    }



    @Override
    public void updateStatus(Sku sku) {
        jdbi.useHandle(handle ->
                handle.createUpdate("UPDATE skus SET status = :status WHERE id = :id")
                        .bind("status", sku.getStatus().name())
                        .bind("id", sku.getId())
                        .execute()
        );
    }



    @Override
    public void updateStatusByProductIdAndColorId(Long productId, Long colorId, SkuStatus skuStatus) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                    UPDATE skus SET status = :status
                    WHERE product_id = :productId AND color_id = :colorId
                """)
                        .bind("status", skuStatus.name())
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .execute()
        );
    }

    @Override
    public void updateDimensions(Sku sku) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                    UPDATE skus
                    SET width = :width, height = :height,
                        length = :length, weight = :weight
                    WHERE id = :id
                """)
                        .bind("width", sku.getDimensions().width())
                        .bind("height", sku.getDimensions().height())
                        .bind("length", sku.getDimensions().length())
                        .bind("weight", sku.getDimensions().weight())
                        .bind("id", sku.getId())
                        .execute()
        );
    }
}

