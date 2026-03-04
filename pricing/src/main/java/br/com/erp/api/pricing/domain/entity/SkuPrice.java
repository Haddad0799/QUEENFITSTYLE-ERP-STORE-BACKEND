package br.com.erp.api.pricing.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SkuPrice {

    private Long id;
    private final Long skuId;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private LocalDateTime createdAt;

    public SkuPrice(Long skuId, BigDecimal costPrice, BigDecimal sellingPrice) {
        this.skuId = skuId;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
    }

    public Long getId() { return id; }
    public Long getSkuId() { return skuId; }
    public BigDecimal getCostPrice() { return costPrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}