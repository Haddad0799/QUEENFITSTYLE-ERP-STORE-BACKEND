package br.com.erp.api.order.domain.enumerated;

public enum OrderStatus {
    PENDING_PAYMENT,               // novo — aguardando pagamento via gateway
    WAITING_SELLER_CONFIRMATION,
    CONFIRMED,
    PREPARING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    EXPIRED
}
