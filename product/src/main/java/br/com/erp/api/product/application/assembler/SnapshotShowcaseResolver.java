package br.com.erp.api.product.application.assembler;

import br.com.erp.api.product.application.dto.ColorSnapshot;
import br.com.erp.api.product.application.dto.DefaultSelectionSnapshot;
import br.com.erp.api.product.application.dto.SkuSnapshot;
import br.com.erp.api.product.domain.entity.ProductColorImage;
import br.com.erp.api.shared.application.projection.ColorDetailProjection;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class SnapshotShowcaseResolver {

    private static final Comparator<SkuSnapshot> SELLABLE_SKU_ORDER =
            Comparator.comparing(SkuSnapshot::sellingPrice, BigDecimal::compareTo)
                    .thenComparing(SkuSnapshot::sizeName, Comparator.nullsFirst(String::compareTo))
                    .thenComparing(SkuSnapshot::code, Comparator.nullsFirst(String::compareTo));

    public ShowcaseSnapshot resolve(ProductColorImage primaryImage,
                                    Map<Long, ColorDetailProjection> colors,
                                    List<SkuSnapshot> skus) {
        Long mainColorId = primaryImage != null ? primaryImage.getColorId() : null;
        ColorSnapshot mainColor = resolveMainColor(mainColorId, colors);

        Optional<SkuSnapshot> selection = selectSellableWithinMainColor(mainColorId, skus);
        if (selection.isEmpty() && mainColorId == null) {
            selection = selectSellableGlobally(skus);
            mainColor = selection.map(this::toColorSnapshot).orElse(null);
        }
        if (mainColor == null) {
            mainColor = selection.map(this::toColorSnapshot).orElse(null);
        }

        DefaultSelectionSnapshot defaultSelection = selection
                .map(this::toDefaultSelection)
                .orElse(null);

        return new ShowcaseSnapshot(
                mainColor,
                defaultSelection,
                defaultSelection != null ? defaultSelection.price() : null
        );
    }

    private Optional<SkuSnapshot> selectSellableWithinMainColor(Long mainColorId, List<SkuSnapshot> skus) {
        if (mainColorId == null) {
            return Optional.empty();
        }

        return skus.stream()
                .filter(this::isSellable)
                .filter(sku -> Objects.equals(sku.colorId(), mainColorId))
                .min(SELLABLE_SKU_ORDER);
    }

    private Optional<SkuSnapshot> selectSellableGlobally(List<SkuSnapshot> skus) {
        return skus.stream()
                .filter(this::isSellable)
                .min(SELLABLE_SKU_ORDER);
    }

    private boolean isSellable(SkuSnapshot sku) {
        return sku.sellingPrice() != null && sku.availableStock() > 0;
    }

    private ColorSnapshot resolveMainColor(Long mainColorId, Map<Long, ColorDetailProjection> colors) {
        if (mainColorId == null) {
            return null;
        }

        ColorDetailProjection color = colors.get(mainColorId);
        if (color == null) {
            return null;
        }

        return new ColorSnapshot(color.id(), color.name(), color.hexCode());
    }

    private ColorSnapshot toColorSnapshot(SkuSnapshot sku) {
        if (sku.colorName() == null || sku.colorName().isBlank()) {
            return null;
        }

        return new ColorSnapshot(sku.colorId(), sku.colorName(), sku.colorHex());
    }

    private DefaultSelectionSnapshot toDefaultSelection(SkuSnapshot sku) {
        return new DefaultSelectionSnapshot(
                sku.code(),
                sku.sizeName(),
                sku.sellingPrice()
        );
    }

    public record ShowcaseSnapshot(
            ColorSnapshot mainColor,
            DefaultSelectionSnapshot defaultSelection,
            BigDecimal displayPrice
    ) {}
}
