package br.com.erp.api.order.domain.entity;

import br.com.erp.api.order.domain.enumerated.OrderStatus;
import br.com.erp.api.order.domain.exception.InvalidOrderStateTransitionException;
import br.com.erp.api.order.domain.valueobject.DeliveryAddress;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private Long id;
    private final Long customerId;
    private OrderStatus status;
    private final BigDecimal totalAmount;
    private final String notes;
    private String whatsappMessage;
    private final LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<OrderItem> items;
    private final DeliveryAddress deliveryAddress;

    private Order(Long customerId, BigDecimal totalAmount, String notes,
                  LocalDateTime expiresAt, List<OrderItem> items, DeliveryAddress deliveryAddress) {
        this.customerId      = customerId;
        this.status          = OrderStatus.PENDING_PAYMENT;
        this.totalAmount     = totalAmount;
        this.notes           = notes;
        this.expiresAt       = expiresAt;
        this.items           = items;
        this.deliveryAddress = deliveryAddress;
        this.createdAt       = LocalDateTime.now();
        this.updatedAt       = LocalDateTime.now();
    }

    public static Order create(Long customerId, BigDecimal totalAmount, String notes,
                               LocalDateTime expiresAt, List<OrderItem> items,
                               DeliveryAddress deliveryAddress) {
        return new Order(customerId, totalAmount, notes, expiresAt, items, deliveryAddress);
    }

    public static Order restore(Long id, Long customerId, OrderStatus status,
                                BigDecimal totalAmount, String notes, String whatsappMessage,
                                LocalDateTime expiresAt, LocalDateTime confirmedAt,
                                LocalDateTime cancelledAt, LocalDateTime createdAt,
                                LocalDateTime updatedAt, List<OrderItem> items,
                                DeliveryAddress deliveryAddress) {
        Order order = new Order(customerId, totalAmount, notes, expiresAt, items, deliveryAddress);
        order.id              = id;
        order.status          = status;
        order.whatsappMessage = whatsappMessage;
        order.confirmedAt     = confirmedAt;
        order.cancelledAt     = cancelledAt;
        return order;
    }

    /** Pagamento confirmado manualmente: a reserva passa a ser consumida (consome estoque). */
    public void markPaid() {
        if (status != OrderStatus.PENDING_PAYMENT)
            throw new InvalidOrderStateTransitionException(id, status, "markPaid");
        status      = OrderStatus.PAID;
        confirmedAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
    }

    /** Entrega manual do pedido já pago. Não altera estoque. */
    public void markDelivered() {
        if (status != OrderStatus.PAID)
            throw new InvalidOrderStateTransitionException(id, status, "markDelivered");
        status    = OrderStatus.DELIVERED;
        updatedAt = LocalDateTime.now();
    }

    /** Devolução de pedido já pago/entregue: o estoque consumido é reposto. */
    public void markReturned() {
        if (status != OrderStatus.PAID && status != OrderStatus.DELIVERED)
            throw new InvalidOrderStateTransitionException(id, status, "markReturned");
        status    = OrderStatus.RETURNED;
        updatedAt = LocalDateTime.now();
    }

    /** Cancelamento por falta de pagamento: só é possível enquanto aguarda pagamento. */
    public void cancel() {
        if (status == OrderStatus.CANCELLED) return;
        if (status != OrderStatus.PENDING_PAYMENT)
            throw new InvalidOrderStateTransitionException(id, status, "cancel");
        status      = OrderStatus.CANCELLED;
        cancelledAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
    }

    public void expire() {
        if (status == OrderStatus.EXPIRED) return;
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateTransitionException(id, status, "expire");
        }
        status    = OrderStatus.EXPIRED;
        updatedAt = LocalDateTime.now();
    }

    public void attachWhatsappMessage(String message) {
        this.whatsappMessage = message;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public Long getId()                   { return id; }
    public Long getCustomerId()           { return customerId; }
    public OrderStatus getStatus()        { return status; }
    public BigDecimal getTotalAmount()    { return totalAmount; }
    public String getNotes()              { return notes; }
    public String getWhatsappMessage()    { return whatsappMessage; }
    public LocalDateTime getExpiresAt()   { return expiresAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
    public List<OrderItem> getItems()     { return items; }
    public DeliveryAddress getDeliveryAddress() { return deliveryAddress; }
}
