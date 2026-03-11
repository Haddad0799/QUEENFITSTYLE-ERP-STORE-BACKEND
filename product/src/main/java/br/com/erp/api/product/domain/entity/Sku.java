package br.com.erp.api.product.domain.entity;

import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.domain.valueobject.Dimensions;
import br.com.erp.api.product.domain.valueobject.SkuCode;

import java.math.BigDecimal;

public class Sku {

    private Long id;
    private Long productId;
    private final SkuCode code;
    private final Long colorId;
    private final Long sizeId;
    private Dimensions dimensions;
    private SkuStatus status;

    // construtor de criação
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

    // construtor de reconstituição — usado pelo repositório
    public Sku(Long id, Long productId, SkuCode code, Long colorId, Long sizeId, Dimensions dimensions, SkuStatus status) {
        this.id = id;
        this.productId = productId;
        this.code = code;
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.dimensions = dimensions;
        this.status = status;
    }

    public Long getId() {
        return id;
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

    public SkuStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return this.status == SkuStatus.ACTIVE;
    }

    public boolean isReady() {
        return this.status == SkuStatus.READY;
    }

    public void markAsReady() {
        this.status = SkuStatus.READY;
    }

    public void markAsIncomplete() {
        this.status = SkuStatus.INCOMPLETE;
    }

    public void activate() {
        if (!isReady()) {
            throw new IllegalStateException("SKU não está pronto para ser ativado");
        }
        this.status = SkuStatus.ACTIVE;
    }

    public void changeDimensions(BigDecimal width, BigDecimal height, BigDecimal length, BigDecimal weight) {
        this.dimensions = this.dimensions.merge(width, height, length, weight);
    }
}