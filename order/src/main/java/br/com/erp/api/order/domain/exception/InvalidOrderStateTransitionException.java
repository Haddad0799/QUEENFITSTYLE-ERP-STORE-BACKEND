package br.com.erp.api.order.domain.exception;

import br.com.erp.api.order.domain.enumerated.OrderStatus;
import br.com.erp.api.shared.domain.exception.DomainException;

public class InvalidOrderStateTransitionException extends DomainException {

    private final Long orderId;
    private final OrderStatus currentStatus;
    private final String attemptedAction;

    public InvalidOrderStateTransitionException(Long orderId, OrderStatus currentStatus, String attemptedAction) {
        super("Pedido #%d no status %s não pode executar a ação '%s'".formatted(orderId, currentStatus, attemptedAction));
        this.orderId         = orderId;
        this.currentStatus   = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public Long getOrderId()                 { return orderId; }
    public OrderStatus getCurrentStatus()    { return currentStatus; }
    public String getAttemptedAction()       { return attemptedAction; }
}
