package br.com.erp.api.order.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderItem {

    private Long id;
    private Long orderId;
    private final Long skuId;
    private final UUID reservationId;
    private final String productName;
    private final String skuCode;
    private final String colorName;
    private final String sizeLabel;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;
    private final LocalDateTime createdAt;

    public OrderItem(Long skuId, UUID reservationId, String productName, String skuCode,
                     String colorName, String sizeLabel, int quantity, BigDecimal unitPrice) {
        this.skuId         = skuId;
        this.reservationId = reservationId;
        this.productName   = productName;
        this.skuCode       = skuCode;
        this.colorName     = colorName;
        this.sizeLabel     = sizeLabel;
        this.quantity      = quantity;
        this.unitPrice     = unitPrice;
        this.subtotal      = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.createdAt     = LocalDateTime.now();
    }

    public static OrderItem restore(Long id, Long orderId, Long skuId, UUID reservationId,
                                    String productName, String skuCode, String colorName,
                                    String sizeLabel, int quantity, BigDecimal unitPrice,
                                    BigDecimal subtotal, LocalDateTime createdAt) {
        OrderItem item = new OrderItem(skuId, reservationId, productName, skuCode,
                colorName, sizeLabel, quantity, unitPrice);
        item.id      = id;
        item.orderId = orderId;
        return item;
    }

    public Long getId()                 { return id; }
    public Long getOrderId()            { return orderId; }
    public Long getSkuId()              { return skuId; }
    public UUID getReservationId()      { return reservationId; }
    public String getProductName()      { return productName; }
    public String getSkuCode()          { return skuCode; }
    public String getColorName()        { return colorName; }
    public String getSizeLabel()        { return sizeLabel; }
    public int getQuantity()            { return quantity; }
    public BigDecimal getUnitPrice()    { return unitPrice; }
    public BigDecimal getSubtotal()     { return subtotal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
