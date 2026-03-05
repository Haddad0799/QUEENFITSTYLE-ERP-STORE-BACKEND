package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.application.provider.ImageProvider;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;

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
                    SELECT COUNT(*)
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
    public List<ProductColorImage> findByProductIdAndColorId(Long productId, Long colorId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT id, product_id, color_id, image_key, "order"
            FROM product_color_images
            WHERE product_id = :productId
              AND color_id = :colorId
            ORDER BY "order" ASC
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
}