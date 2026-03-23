package br.com.erp.api.catalog.infrastructure.persistence;

import br.com.erp.api.catalog.domain.port.CatalogRepositoryPort;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.dto.SkuSnapshot;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class CatalogJdbiRepository implements CatalogRepositoryPort {

    private final Jdbi jdbi;

    public CatalogJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void publishProduct(ProductSnapshot snapshot) {

        BigDecimal minPrice = snapshot.skus().stream()
                .map(SkuSnapshot::sellingPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);

        jdbi.useHandle(handle -> {

            Long catalogProductId = handle.createUpdate("""
                        INSERT INTO catalog_products (product_id, name, description, slug, category_name, main_image_url, min_price)
                        VALUES (:productId, :name, :description, :slug, :categoryName, :mainImageUrl, :minPrice)
                    """)
                    .bind("productId", snapshot.productId())
                    .bind("name", snapshot.name())
                    .bind("description", snapshot.description())
                    .bind("slug", snapshot.slug())
                    .bind("categoryName", snapshot.categoryName())
                    .bind("mainImageUrl", snapshot.mainImageUrl())
                    .bind("minPrice", minPrice)
                    .executeAndReturnGeneratedKeys("id")
                    .mapTo(Long.class)
                    .one();

            Map<String, List<SkuSnapshot>> skusByColor = snapshot.skus().stream()
                    .collect(Collectors.groupingBy(
                            SkuSnapshot::colorName,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            for (Map.Entry<String, List<SkuSnapshot>> entry : skusByColor.entrySet()) {
                List<SkuSnapshot> colorSkus = entry.getValue();
                SkuSnapshot first = colorSkus.getFirst();

                Long colorGroupId = handle.createUpdate("""
                            INSERT INTO catalog_color_groups (catalog_product_id, color_name, color_hex)
                            VALUES (:catalogProductId, :colorName, :colorHex)
                        """)
                        .bind("catalogProductId", catalogProductId)
                        .bind("colorName", first.colorName())
                        .bind("colorHex", first.colorHex())
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .one();

                if (first.imageUrls() != null && !first.imageUrls().isEmpty()) {
                    var imageBatch = handle.prepareBatch("""
                                INSERT INTO catalog_color_images (catalog_color_group_id, image_url, "order")
                                VALUES (:colorGroupId, :imageUrl, :order)
                            """);
                    int order = 1;
                    for (String imageUrl : first.imageUrls()) {
                        imageBatch.bind("colorGroupId", colorGroupId)
                                .bind("imageUrl", imageUrl)
                                .bind("order", order++)
                                .add();
                    }
                    imageBatch.execute();
                }

                for (SkuSnapshot sku : colorSkus) {
                    handle.createUpdate("""
                                INSERT INTO catalog_skus
                                    (catalog_product_id, catalog_color_group_id, sku_id, code,
                                     size_name, selling_price, available_stock,
                                     width, height, length, weight)
                                VALUES
                                    (:catalogProductId, :colorGroupId, :skuId, :code,
                                     :sizeName, :sellingPrice, :availableStock,
                                     :width, :height, :length, :weight)
                            """)
                            .bind("catalogProductId", catalogProductId)
                            .bind("colorGroupId", colorGroupId)
                            .bind("skuId", sku.skuId())
                            .bind("code", sku.code())
                            .bind("sizeName", sku.sizeName())
                            .bind("sellingPrice", sku.sellingPrice())
                            .bind("availableStock", sku.availableStock())
                            .bind("width", sku.width())
                            .bind("height", sku.height())
                            .bind("length", sku.length())
                            .bind("weight", sku.weight())
                            .execute();
                }
            }
        });
    }

    @Override
    public void unpublishByProductId(Long productId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("DELETE FROM catalog_products WHERE product_id = :productId")
                        .bind("productId", productId)
                        .execute()
        );
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(1) > 0 FROM catalog_products WHERE product_id = :productId")
                        .bind("productId", productId)
                        .mapTo(Boolean.class)
                        .one()
        );
    }

    @Override
    public void updateProductInfo(ProductSnapshot snapshot) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                            UPDATE catalog_products
                            SET name          = :name,
                                description   = :description,
                                category_name = :categoryName,
                                main_image_url = :mainImageUrl,
                                updated_at    = NOW()
                            WHERE product_id = :productId
                        """)
                        .bind("productId", snapshot.productId())
                        .bind("name", snapshot.name())
                        .bind("description", snapshot.description())
                        .bind("categoryName", snapshot.categoryName())
                        .bind("mainImageUrl", snapshot.mainImageUrl())
                        .execute()
        );
    }

    @Override
    public void updateSkuPrice(Long skuId, BigDecimal sellingPrice) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                            UPDATE catalog_skus
                            SET selling_price = :sellingPrice,
                                updated_at    = NOW()
                            WHERE sku_id = :skuId
                        """)
                        .bind("sellingPrice", sellingPrice)
                        .bind("skuId", skuId)
                        .execute()
        );
    }

    @Override
    public void recalculateMinPrice(Long productId) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                            UPDATE catalog_products cp
                            SET min_price  = (
                                SELECT MIN(cs.selling_price)
                                FROM catalog_skus cs
                                WHERE cs.catalog_product_id = cp.id
                            ),
                            updated_at = NOW()
                            WHERE cp.product_id = :productId
                        """)
                        .bind("productId", productId)
                        .execute()
        );
    }

    @Override
    public void updateSkuStock(Long skuId, int available) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                            UPDATE catalog_skus
                            SET available_stock = :available,
                                updated_at      = NOW()
                            WHERE sku_id = :skuId
                        """)
                        .bind("available", available)
                        .bind("skuId", skuId)
                        .execute()
        );
    }

    @Override
    public void updateColorGroupImages(Long productId, Long colorId, List<String> imageUrls) {
        jdbi.useHandle(handle -> {

            // Busca o grupo de cor pelo productId + colorName não é suficiente
            // precisamos do catalog_color_group_id via join
            Long colorGroupId = handle.createQuery("""
                        SELECT ccg.id
                        FROM catalog_color_groups ccg
                        JOIN catalog_products cp ON cp.id = ccg.catalog_product_id
                        WHERE cp.product_id = :productId
                          AND ccg.color_name = (
                              SELECT name FROM cores WHERE id = :colorId
                          )
                        LIMIT 1
                    """)
                    .bind("productId", productId)
                    .bind("colorId", colorId)
                    .mapTo(Long.class)
                    .findOne()
                    .orElse(null);

            if (colorGroupId == null) return;

            // Remove imagens antigas
            handle.createUpdate("DELETE FROM catalog_color_images WHERE catalog_color_group_id = :colorGroupId")
                    .bind("colorGroupId", colorGroupId)
                    .execute();

            // Insere novas imagens
            if (!imageUrls.isEmpty()) {
                var batch = handle.prepareBatch("""
                            INSERT INTO catalog_color_images (catalog_color_group_id, image_url, "order")
                            VALUES (:colorGroupId, :imageUrl, :order)
                        """);
                int order = 1;
                for (String imageUrl : imageUrls) {
                    batch.bind("colorGroupId", colorGroupId)
                            .bind("imageUrl", imageUrl)
                            .bind("order", order++)
                            .add();
                }
                batch.execute();
            }
        });
    }

    @Override
    public void addSku(Long productId, SkuSnapshot sku) {
        jdbi.useHandle(handle -> {

            // Busca catalog_product_id
            Long catalogProductId = handle.createQuery("""
                        SELECT id FROM catalog_products WHERE product_id = :productId
                    """)
                    .bind("productId", productId)
                    .mapTo(Long.class)
                    .one();

            // Busca ou cria grupo de cor
            Long colorGroupId = handle.createQuery("""
                        SELECT id FROM catalog_color_groups
                        WHERE catalog_product_id = :catalogProductId
                          AND color_name = :colorName
                        LIMIT 1
                    """)
                    .bind("catalogProductId", catalogProductId)
                    .bind("colorName", sku.colorName())
                    .mapTo(Long.class)
                    .findOne()
                    .orElseGet(() ->
                            handle.createUpdate("""
                                        INSERT INTO catalog_color_groups (catalog_product_id, color_name, color_hex)
                                        VALUES (:catalogProductId, :colorName, :colorHex)
                                    """)
                                    .bind("catalogProductId", catalogProductId)
                                    .bind("colorName", sku.colorName())
                                    .bind("colorHex", sku.colorHex())
                                    .executeAndReturnGeneratedKeys("id")
                                    .mapTo(Long.class)
                                    .one()
                    );

            // Insere o SKU
            handle.createUpdate("""
                        INSERT INTO catalog_skus
                            (catalog_product_id, catalog_color_group_id, sku_id, code,
                             size_name, selling_price, available_stock,
                             width, height, length, weight)
                        VALUES
                            (:catalogProductId, :colorGroupId, :skuId, :code,
                             :sizeName, :sellingPrice, :availableStock,
                             :width, :height, :length, :weight)
                    """)
                    .bind("catalogProductId", catalogProductId)
                    .bind("colorGroupId", colorGroupId)
                    .bind("skuId", sku.skuId())
                    .bind("code", sku.code())
                    .bind("sizeName", sku.sizeName())
                    .bind("sellingPrice", sku.sellingPrice())
                    .bind("availableStock", sku.availableStock())
                    .bind("width", sku.width())
                    .bind("height", sku.height())
                    .bind("length", sku.length())
                    .bind("weight", sku.weight())
                    .execute();

            // Se o grupo de cor é novo, insere as imagens
            long imageCount = handle.createQuery("""
                        SELECT COUNT(1) FROM catalog_color_images
                        WHERE catalog_color_group_id = :colorGroupId
                    """)
                    .bind("colorGroupId", colorGroupId)
                    .mapTo(Long.class)
                    .one();

            if (imageCount == 0 && sku.imageUrls() != null && !sku.imageUrls().isEmpty()) {
                var batch = handle.prepareBatch("""
                            INSERT INTO catalog_color_images (catalog_color_group_id, image_url, "order")
                            VALUES (:colorGroupId, :imageUrl, :order)
                        """);
                int order = 1;
                for (String imageUrl : sku.imageUrls()) {
                    batch.bind("colorGroupId", colorGroupId)
                            .bind("imageUrl", imageUrl)
                            .bind("order", order++)
                            .add();
                }
                batch.execute();
            }
        });
    }
}