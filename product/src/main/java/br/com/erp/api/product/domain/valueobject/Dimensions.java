package br.com.erp.api.product.domain.valueobject;

import java.math.BigDecimal;

public record Dimensions(
        BigDecimal width,
        BigDecimal height,
        BigDecimal length
) {

    public Dimensions {
        requirePositive(width, "width");
        requirePositive(height, "height");
        requirePositive(length, "length");
    }

    private static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " deve ser positivo");
        }
    }

    public BigDecimal volume() {
        return width.multiply(height).multiply(length);
    }

    public static Dimensions of(BigDecimal w, BigDecimal h, BigDecimal l) {
        return new Dimensions(w, h, l);
    }

    public Dimensions merge(BigDecimal newWidth, BigDecimal newHeight, BigDecimal newLength) {
        return Dimensions.of(
                newWidth  != null ? newWidth  : this.width,
                newHeight != null ? newHeight : this.height,
                newLength != null ? newLength : this.length
        );
    }


}
