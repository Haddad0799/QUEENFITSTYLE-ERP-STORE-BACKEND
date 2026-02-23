package br.com.erp.api.product.domain.entity;

import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.domain.valueobject.Dimensions;
import br.com.erp.api.product.domain.valueobject.SkuCode;

public class Sku {

    private Long id;
    private Long productId;
    private final SkuCode code;
    private final Long colorId;
    private final Long sizeId;
    private Dimensions dimensions;
    private SkuStatus status;

    public Sku(SkuCode code,
               Long colorId,
               Long sizeId,
               Dimensions dimensions) {

        this.code = code;
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.dimensions = dimensions;
        this.status = SkuStatus.INCOMPLETE;
    }

    public Long getId() {
        return id;
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
        return this.status == SkuStatus.ACTIVE;
    }

    public void activate() {
        if (!isReady()) {
            throw new IllegalStateException("SKU não está pronto");
        }
        this.status = SkuStatus.ACTIVE;
    }

    public boolean isReady() {
        return this.dimensions != null;
    }

    public void changeDimensions(Dimensions dimensions){
        this.dimensions = dimensions;
    }
}
