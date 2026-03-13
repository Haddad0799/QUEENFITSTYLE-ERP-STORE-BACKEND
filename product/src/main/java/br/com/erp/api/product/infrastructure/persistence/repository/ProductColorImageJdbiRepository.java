package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.application.provider.ImageProvider;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductColorImageJdbiRepository implements ProductColorImageRepositoryPort, ImageProvider {

    private final Jdbi jdbi;

    public ProductColorImageJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void saveAll(List<ProductColorImage> images) {

        if (images == null || images.isEmpty()) return;

        jdbi.withHandle(handle -> {

            var batch = handle.prepareBatch("""
                INSERT INTO product_color_images (
                    product_id,
                    color_id,
                    image_key,
                    "order"
                ) VALUES (
                    :productId,
                    :colorId,
                    :imageKey,
                    :order
                )
            """);

            for (ProductColorImage image : images) {
                batch.bind("productId", image.getProductId())
                        .bind("colorId", image.getColorId())
                        .bind("imageKey", image.getImageKey())
                        .bind("order", image.getOrder())
                        .add();
            }

            return batch.execute();
        });
    }

    @Override
    public int countByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT COUNT(1)
                FROM product_color_images
                WHERE product_id = :productId
                  AND color_id = :colorId
            """)
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .mapTo(Integer.class)
                        .one()
        );
    }

    @Override
    public boolean hasImage(Long skuId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT COUNT(*) > 0
                FROM product_color_images pci
                JOIN skus s ON s.color_id = pci.color_id
                           AND s.product_id = pci.product_id
                WHERE s.id = :skuId
            """)
                        .bind("skuId", skuId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public List<ProductColorImage> findByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, product_id, color_id, image_key, "order"
                FROM product_color_images
                WHERE product_id = :productId
                  AND color_id = :colorId
                ORDER BY "order"
            """)
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .map((rs, ctx) -> new ProductColorImage(
                                rs.getLong("id"),
                                rs.getLong("product_id"),
                                rs.getLong("color_id"),
                                rs.getString("image_key"),
                                rs.getInt("order")
                        ))
                        .list()
        );
    }

    @Override
    public List<Integer> findOrdersByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT "order"
                FROM product_color_images
                WHERE product_id = :productId
                  AND color_id = :colorId
                ORDER BY "order"
            """)
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .mapTo(Integer.class)
                        .list()
        );
    }

    @Override
    public List<String> findKeysByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT image_key
                FROM product_color_images
                WHERE product_id = :productId
                  AND color_id = :colorId
            """)
                        .bind("productId", productId)
                        .bind("colorId", colorId)
                        .mapTo(String.class)
                        .list()
        );
    }

    @Override
    public List<ProductColorImage> findAllByIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return List.of();

        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, product_id, color_id, image_key, "order"
                FROM product_color_images
                WHERE id IN (<ids>)
            """)
                        .defineList("ids", imageIds)
                        .map((rs, ctx) -> new ProductColorImage(
                                rs.getLong("id"),
                                rs.getLong("product_id"),
                                rs.getLong("color_id"),
                                rs.getString("image_key"),
                                rs.getInt("order")
                        ))
                        .list()
        );
    }

    @Override
    public void deleteAllByIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;

        jdbi.useHandle(handle ->
                handle.createUpdate("""
                DELETE FROM product_color_images
                WHERE id IN (<ids>)
            """)
                        .defineList("ids", imageIds)
                        .execute()
        );
    }

    @Override
    public boolean existsByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT COUNT(1) > 0
                FROM product_color_images
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
    public List<Long> saveAllReturningIds(List<ProductColorImage> images) {
        if (images == null || images.isEmpty()) return List.of();

        return jdbi.withHandle(handle -> {
            List<Long> ids = new ArrayList<>();
            for (ProductColorImage image : images) {
                Long id = handle.createUpdate("""
                    INSERT INTO product_color_images (product_id, color_id, image_key, "order")
                    VALUES (:productId, :colorId, :imageKey, :order)
                """)
                        .bind("productId", image.getProductId())
                        .bind("colorId", image.getColorId())
                        .bind("imageKey", image.getImageKey())
                        .bind("order", image.getOrder())
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .one();
                ids.add(id);
            }
            return ids;
        });
    }

    @Override
    public Optional<ProductColorImage> findFirstByProductIdExcluding(Long productId, List<Long> excludedIds) {
        return jdbi.withHandle(handle -> {
            String sql;
            if (excludedIds == null || excludedIds.isEmpty()) {
                sql = """
                    SELECT id, product_id, color_id, image_key, "order"
                    FROM product_color_images
                    WHERE product_id = :productId
                    ORDER BY "order"
                    LIMIT 1
                """;
                return handle.createQuery(sql)
                        .bind("productId", productId)
                        .map((rs, ctx) -> new ProductColorImage(
                                rs.getLong("id"),
                                rs.getLong("product_id"),
                                rs.getLong("color_id"),
                                rs.getString("image_key"),
                                rs.getInt("order")
                        ))
                        .findOne();
            } else {
                sql = """
                    SELECT id, product_id, color_id, image_key, "order"
                    FROM product_color_images
                    WHERE product_id = :productId
                      AND id NOT IN (<excludedIds>)
                    ORDER BY "order"
                    LIMIT 1
                """;
                return handle.createQuery(sql)
                        .bind("productId", productId)
                        .defineList("excludedIds", excludedIds)
                        .map((rs, ctx) -> new ProductColorImage(
                                rs.getLong("id"),
                                rs.getLong("product_id"),
                                rs.getLong("color_id"),
                                rs.getString("image_key"),
                                rs.getInt("order")
                        ))
                        .findOne();
            }
        });
    }

    @Override
    public List<ProductColorImage> findAllByProductIdGroupedByColor(Long productId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT id, product_id, color_id, image_key, "order"
                FROM product_color_images
                WHERE product_id = :productId
                ORDER BY color_id, "order"
            """)
                        .bind("productId", productId)
                        .map((rs, ctx) -> new ProductColorImage(
                                rs.getLong("id"),
                                rs.getLong("product_id"),
                                rs.getLong("color_id"),
                                rs.getString("image_key"),
                                rs.getInt("order")
                        ))
                        .list()
        );
    }

    @Override
    public void updateOrders(Map<Long, Integer> imageIdToOrder) {
        if (imageIdToOrder == null || imageIdToOrder.isEmpty()) return;

        jdbi.useHandle(handle -> {
            var batch = handle.prepareBatch("""
                UPDATE product_color_images
                SET "order" = :order
                WHERE id = :id
            """);

            for (var entry : imageIdToOrder.entrySet()) {
                batch.bind("id", entry.getKey())
                     .bind("order", entry.getValue())
                     .add();
            }

            batch.execute();
        });
    }
}