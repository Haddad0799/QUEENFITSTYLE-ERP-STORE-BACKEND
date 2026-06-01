package br.com.erp.api.order.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

public class OrderNotFoundException extends DomainException {

    private final Long orderId;

    public OrderNotFoundException(Long orderId) {
        super("Pedido não encontrado: " + orderId);
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
