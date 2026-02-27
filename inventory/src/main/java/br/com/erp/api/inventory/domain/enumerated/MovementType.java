package br.com.erp.api.inventory.domain.enumerated;

public enum MovementType {
    INBOUND,      // Entrada manual de estoque
    OUTBOUND,     // Saída por pedido
    RESERVATION,  // Reserva (item no carrinho)
    INITIALIZED,  // Criação do SKU — estoque zerado
    RELEASE       // Liberação de reserva (carrinho abandonado / cancelamento)
}
