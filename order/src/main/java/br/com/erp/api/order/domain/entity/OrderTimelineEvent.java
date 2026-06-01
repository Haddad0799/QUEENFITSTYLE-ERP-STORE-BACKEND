package br.com.erp.api.order.domain.entity;

import br.com.erp.api.order.domain.enumerated.OrderEventType;

import java.time.LocalDateTime;

public class OrderTimelineEvent {

    private Long id;
    private final Long orderId;
    private final OrderEventType eventType;
    private final String description;
    private final String payload;
    private final String actor;
    private final LocalDateTime createdAt;

    private OrderTimelineEvent(Long id, Long orderId, OrderEventType eventType, String description,
                               String payload, String actor, LocalDateTime createdAt) {
        this.id          = id;
        this.orderId     = orderId;
        this.eventType   = eventType;
        this.description = description;
        this.payload     = payload;
        this.actor       = actor;
        this.createdAt   = createdAt;
    }

    public static OrderTimelineEvent create(Long orderId, OrderEventType eventType,
                                            String description, String payload, String actor) {
        return new OrderTimelineEvent(null, orderId, eventType, description, payload, actor, LocalDateTime.now());
    }

    public static OrderTimelineEvent restore(Long id, Long orderId, OrderEventType eventType,
                                             String description, String payload, String actor,
                                             LocalDateTime createdAt) {
        return new OrderTimelineEvent(id, orderId, eventType, description, payload, actor, createdAt);
    }

    public Long getId()                  { return id; }
    public Long getOrderId()             { return orderId; }
    public OrderEventType getEventType() { return eventType; }
    public String getDescription()       { return description; }
    public String getPayload()           { return payload; }
    public String getActor()             { return actor; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
}
