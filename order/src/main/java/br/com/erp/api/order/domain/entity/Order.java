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
        this.status          = OrderStatus.WAITING_SELLER_CONFIRMATION;
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

    public static Order createForPayment(Long customerId, BigDecimal totalAmount, String notes,
                                         LocalDateTime expiresAt, List<OrderItem> items,
                                         DeliveryAddress deliveryAddress) {
        Order order = new Order(customerId, totalAmount, notes, expiresAt, items, deliveryAddress);
        order.status = OrderStatus.PENDING_PAYMENT;
        return order;
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

    public void confirm() {
        if (status != OrderStatus.WAITING_SELLER_CONFIRMATION)
            throw new IllegalStateException("Pedido não pode ser confirmado no status: " + status);
        status      = OrderStatus.CONFIRMED;
        confirmedAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED)
            throw new IllegalStateException("Pedido não pode ser cancelado no status: " + status);
        status      = OrderStatus.CANCELLED;
        cancelledAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
    }

    public void expire() {
        if (status == OrderStatus.EXPIRED) return;
        if (status != OrderStatus.WAITING_SELLER_CONFIRMATION
         && status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateTransitionException(id, status, "expire");
        }
        status    = OrderStatus.EXPIRED;
        updatedAt = LocalDateTime.now();
    }

    public void approvePayment() {
        if (status != OrderStatus.PENDING_PAYMENT)
            throw new InvalidOrderStateTransitionException(id, status, "approvePayment");
        status    = OrderStatus.WAITING_SELLER_CONFIRMATION;
        updatedAt = LocalDateTime.now();
    }

    public void failPayment() {
        if (status != OrderStatus.PENDING_PAYMENT)
            throw new InvalidOrderStateTransitionException(id, status, "failPayment");
        status      = OrderStatus.CANCELLED;
        cancelledAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
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
