package br.com.erp.api.order.domain.enumerated;

public enum OrderStatus {
    PENDING_PAYMENT,   // criado — aguardando confirmação manual do pagamento (reserva RESERVED)
    PAID,              // pagamento confirmado — reserva consumida (estoque baixado)
    DELIVERED,         // pedido entregue manualmente
    CANCELLED,         // cancelado por falta de pagamento — reserva liberada
    EXPIRED,           // expirado após 24h sem pagamento — reserva liberada
    RETURNED           // devolvido após pago/entregue — estoque reposto
}
