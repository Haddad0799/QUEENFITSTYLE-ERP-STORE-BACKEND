package br.com.erp.api.inventory.domain.entity;

import br.com.erp.api.inventory.domain.enumerated.MovementType;
import java.time.LocalDateTime;

public class StockMovement {

    private Long id;
    private Long skuId;
    private MovementType type;
    private int quantity;
    private String reason;
    private Long referenceId;
    private LocalDateTime createdAt;

    public StockMovement(Long skuId, MovementType type, int quantity, String reason, Long referenceId) {
        this.skuId = skuId;
        this.type = type;
        this.quantity = quantity;
        this.reason = reason;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId()              { return id; }
    public Long getSkuId()           { return skuId; }
    public MovementType getType()    { return type; }
    public int getQuantity()         { return quantity; }
    public String getReason()        { return reason; }
    public Long getReferenceId()     { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}