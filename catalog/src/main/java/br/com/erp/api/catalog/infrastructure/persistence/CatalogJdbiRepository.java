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
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CatalogJdbiRepository implements CatalogRepositoryPort {

    private final Jdbi jdbi;

    public CatalogJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
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
    public void replaceProduct(ProductSnapshot snapshot) {

        BigDecimal minPrice = snapshot.skus().stream()
                .map(SkuSnapshot::sellingPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);

        jdbi.useTransaction(handle -> {

            // 1. DELETE (seguro dentro da transação)
            handle.createUpdate("""
                DELETE FROM catalog_products
                WHERE product_id = :productId
            """)
                    .bind("productId", snapshot.productId())
                    .execute();

            // 2. INSERT product
            Long catalogProductId = handle.createUpdate("""
                INSERT INTO catalog_products
                (product_id, name, description, slug, category_name, category_normalized_name,
                 subcategory_id, subcategory_name, subcategory_normalized_name,
                 parent_category_id, parent_category_name, parent_category_normalized_name,
                 main_image_url, min_price, is_launch)
                VALUES
                (:productId, :name, :description, :slug, :categoryName, :categoryNormalizedName,
                 :subcategoryId, :subcategoryName, :subcategoryNormalizedName,
                 :parentCategoryId, :parentCategoryName, :parentCategoryNormalizedName,
                 :mainImageUrl, :minPrice, :isLaunch)
            """)
                    .bind("productId", snapshot.productId())
                    .bind("name", snapshot.name())
                    .bind("description", snapshot.description())
                    .bind("slug", snapshot.slug())
                    .bind("categoryName", snapshot.categoryName())
                    .bind("categoryNormalizedName", snapshot.categoryNormalizedName())
                    .bind("subcategoryId", snapshot.categoryId())
                    .bind("subcategoryName", snapshot.categoryName())
                    .bind("subcategoryNormalizedName", snapshot.categoryNormalizedName())
                    .bind("parentCategoryId", snapshot.parentCategoryId())
                    .bind("parentCategoryName", snapshot.parentCategoryName())
                    .bind("parentCategoryNormalizedName", snapshot.parentCategoryNormalizedName())
                    .bind("mainImageUrl", snapshot.mainImageUrl())
                    .bind("minPrice", minPrice)
                    .bind("isLaunch", snapshot.isLaunch())
                    .executeAndReturnGeneratedKeys("id")
                    .mapTo(Long.class)
                    .one();

            // 3. resto igual (color groups + skus)
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
                    INSERT INTO catalog_color_groups
                    (catalog_product_id, color_name, color_hex)
                    VALUES
                    (:catalogProductId, :colorName, :colorHex)
                """)
                        .bind("catalogProductId", catalogProductId)
                        .bind("colorName", first.colorName())
                        .bind("colorHex", first.colorHex())
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .one();

                // imagens
                if (first.imageUrls() != null && !first.imageUrls().isEmpty()) {
                    var batch = handle.prepareBatch("""
                        INSERT INTO catalog_color_images
                        (catalog_color_group_id, image_url, "order")
                        VALUES
                        (:colorGroupId, :imageUrl, :order)
                    """);

                    int order = 1;
                    for (String imageUrl : first.imageUrls()) {
                        batch.bind("colorGroupId", colorGroupId)
                                .bind("imageUrl", imageUrl)
                                .bind("order", order++)
                                .add();
                    }
                    batch.execute();
                }

                // SKUs
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
    public Optional<String> findSlugByProductId(Long productId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT slug FROM catalog_products WHERE product_id = :productId")
                        .bind("productId", productId)
                        .mapTo(String.class)
                        .findOne()
        );
    }

}
