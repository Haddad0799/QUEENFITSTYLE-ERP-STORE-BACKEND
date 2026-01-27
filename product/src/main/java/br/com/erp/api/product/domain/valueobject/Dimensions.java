package br.com.erp.api.product.domain.valueobject;

import java.math.BigDecimal;

public record Dimensions(
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight
) {

    public Dimensions {
        requirePositive(width, "width");
        requirePositive(height, "height");
        requirePositive(length, "length");
        requirePositive(weight, "weight");
    }

    private static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " deve ser positivo");
        }
    }

    public BigDecimal volume() {
        return width.multiply(height).multiply(length);
    }

    public static Dimensions of(BigDecimal w, BigDecimal h, BigDecimal l, BigDecimal kg) {
        return new Dimensions(w, h, l, kg);
    }


}
