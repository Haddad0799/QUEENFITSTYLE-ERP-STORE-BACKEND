package br.com.erp.api.product.domain.entity;

import br.com.erp.api.product.domain.valueobject.Dimensions;
import br.com.erp.api.product.domain.valueobject.SkuCode;

public class Sku {

    private Long id;
    private Long productId;
    private final SkuCode code;
    private final Long colorId;
    private final Long sizeId;
    private Dimensions dimensions;
    private boolean active;

    public Sku(SkuCode code,
               Long colorId,
               Long sizeId,
               Dimensions dimensions) {

        this.code = code;
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.dimensions = dimensions;
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public void attachToProduct(Long productId) {
        if (this.productId != null) {
            throw new IllegalStateException("SKU já vinculado a um produto");
        }
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public SkuCode getCode() {
        return code;
    }

    public Long getColorId() {
        return colorId;
    }

    public Long getSizeId() {
        return sizeId;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public boolean isActive() {
        return active;
    }

    public void changeDimensions(Dimensions dimensions){
        this.dimensions = dimensions;
    }
}
