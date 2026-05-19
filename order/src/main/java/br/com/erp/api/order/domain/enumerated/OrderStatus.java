package br.com.erp.api.order.domain.enumerated;

public enum OrderStatus {
    WAITING_SELLER_CONFIRMATION,
    CONFIRMED,
    PREPARING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    EXPIRED
}
