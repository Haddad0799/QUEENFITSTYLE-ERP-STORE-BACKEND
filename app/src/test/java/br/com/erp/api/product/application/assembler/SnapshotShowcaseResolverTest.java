package br.com.erp.api.product.application.assembler;

import br.com.erp.api.product.application.dto.SkuSnapshot;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.shared.application.projection.ColorDetailProjection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotShowcaseResolverTest {

    private final SnapshotShowcaseResolver resolver = new SnapshotShowcaseResolver();

    @Test
    void shouldPickCheapestSellableSkuWithinMainImageColor() {
        ProductColorImage primaryImage = new ProductColorImage(10L, 1L, "img/preto-1.jpg", 1);

        SnapshotShowcaseResolver.ShowcaseSnapshot result = resolver.resolve(
                primaryImage,
                colors(),
                List.of(
                        sku(1L, "SKU-BRANCO-P", 2L, "Branco", "#FFFFFF", "P", "89.90", 5),
                        sku(2L, "SKU-PRETO-M", 1L, "Preto", "#000000", "M", "99.90", 3),
                        sku(3L, "SKU-PRETO-G", 1L, "Preto", "#000000", "G", "109.90", 2)
                )
        );

        assertThat(result.mainColor()).isNotNull();
        assertThat(result.mainColor().name()).isEqualTo("Preto");
        assertThat(result.defaultSelection()).isNotNull();
        assertThat(result.defaultSelection().skuCode()).isEqualTo("SKU-PRETO-M");
        assertThat(result.defaultSelection().label()).isEqualTo("M");
        assertThat(result.displayPrice()).isEqualByComparingTo("99.90");
    }

    @Test
    void shouldKeepMainColorAndClearSelectionWhenMainImageColorHasNoSellableSku() {
        ProductColorImage primaryImage = new ProductColorImage(10L, 1L, "img/preto-1.jpg", 1);

        SnapshotShowcaseResolver.ShowcaseSnapshot result = resolver.resolve(
                primaryImage,
                colors(),
                List.of(
                        sku(1L, "SKU-PRETO-P", 1L, "Preto", "#000000", "P", "99.90", 0),
                        sku(2L, "SKU-BRANCO-P", 2L, "Branco", "#FFFFFF", "P", "79.90", 4)
                )
        );

        assertThat(result.mainColor()).isNotNull();
        assertThat(result.mainColor().name()).isEqualTo("Preto");
        assertThat(result.defaultSelection()).isNull();
        assertThat(result.displayPrice()).isNull();
    }

    @Test
    void shouldFallbackToCheapestSellableSkuWhenThereIsNoPrimaryImage() {
        SnapshotShowcaseResolver.ShowcaseSnapshot result = resolver.resolve(
                null,
                colors(),
                List.of(
                        sku(1L, "SKU-PRETO-P", 1L, "Preto", "#000000", "P", "99.90", 1),
                        sku(2L, "SKU-BRANCO-P", 2L, "Branco", "#FFFFFF", "P", "79.90", 4)
                )
        );

        assertThat(result.mainColor()).isNotNull();
        assertThat(result.mainColor().name()).isEqualTo("Branco");
        assertThat(result.defaultSelection()).isNotNull();
        assertThat(result.defaultSelection().skuCode()).isEqualTo("SKU-BRANCO-P");
        assertThat(result.displayPrice()).isEqualByComparingTo("79.90");
    }

    private Map<Long, ColorDetailProjection> colors() {
        return Map.of(
                1L, new ColorDetailProjection(1L, "Preto", "#000000"),
                2L, new ColorDetailProjection(2L, "Branco", "#FFFFFF")
        );
    }

    private SkuSnapshot sku(Long skuId,
                            String code,
                            Long colorId,
                            String colorName,
                            String colorHex,
                            String sizeName,
                            String price,
                            int availableStock) {
        return new SkuSnapshot(
                skuId,
                code,
                colorId,
                colorName,
                colorHex,
                sizeName,
                new BigDecimal(price),
                availableStock,
                null,
                null,
                null,
                null,
                List.of()
        );
    }
}
