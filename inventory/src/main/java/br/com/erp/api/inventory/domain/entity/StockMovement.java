package br.com.erp.api.inventory.domain.entity;

import br.com.erp.api.inventory.domain.enumerated.MovementType;
import java.time.LocalDateTime;
import java.util.UUID;

public class StockMovement {

    private Long id;
    private final Long skuId;
    private final MovementType type;
    private final int quantity;
    private final String reason;
    private final UUID referenceUuid;
    private final LocalDateTime createdAt;

    public StockMovement(Long skuId, MovementType type, int quantity, String reason, UUID referenceUuid) {
        this.skuId = skuId;
        this.type = type;
        this.quantity = quantity;
        this.reason = reason;
        this.referenceUuid = referenceUuid;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId()              { return id; }
    public Long getSkuId()           { return skuId; }
    public MovementType getType()    { return type; }
    public int getQuantity()         { return quantity; }
    public String getReason()        { return reason; }
    public UUID getReferenceUuid()   { return referenceUuid; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}