package br.com.erp.api.catalog.infrastructure.persistence;

import br.com.erp.api.catalog.domain.port.CatalogRepositoryPort;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.dto.SkuSnapshot;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Objects;

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

            // 1. Insere produto no catálogo
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

            // 2. Insere SKUs
            for (SkuSnapshot sku : snapshot.skus()) {
                Long catalogSkuId = handle.createUpdate("""
                            INSERT INTO catalog_skus
                                (catalog_product_id, sku_id, code, color_name, color_hex,
                                 size_name, selling_price, available_stock,
                                 width, height, length, weight)
                            VALUES
                                (:catalogProductId, :skuId, :code, :colorName, :colorHex,
                                 :sizeName, :sellingPrice, :availableStock,
                                 :width, :height, :length, :weight)
                        """)
                        .bind("catalogProductId", catalogProductId)
                        .bind("skuId", sku.skuId())
                        .bind("code", sku.code())
                        .bind("colorName", sku.colorName())
                        .bind("colorHex", sku.colorHex())
                        .bind("sizeName", sku.sizeName())
                        .bind("sellingPrice", sku.sellingPrice())
                        .bind("availableStock", sku.availableStock())
                        .bind("width", sku.width())
                        .bind("height", sku.height())
                        .bind("length", sku.length())
                        .bind("weight", sku.weight())
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .one();

                // 3. Insere imagens do SKU
                if (sku.imageUrls() != null && !sku.imageUrls().isEmpty()) {
                    var batch = handle.prepareBatch("""
                                INSERT INTO catalog_sku_images (catalog_sku_id, image_url, "order")
                                VALUES (:catalogSkuId, :imageUrl, :order)
                            """);

                    int order = 1;
                    for (String imageUrl : sku.imageUrls()) {
                        batch.bind("catalogSkuId", catalogSkuId)
                                .bind("imageUrl", imageUrl)
                                .bind("order", order++)
                                .add();
                    }
                    batch.execute();
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


}

