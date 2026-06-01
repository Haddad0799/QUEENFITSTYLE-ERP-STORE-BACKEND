package br.com.erp.api.order.presentation.controller;

import br.com.erp.api.order.domain.exception.InvalidOrderStateTransitionException;
import br.com.erp.api.order.domain.exception.OrderNotFoundException;
import br.com.erp.api.order.domain.exception.ReservationOperationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "br.com.erp.api.order")
public class OrderAdminControllerAdvice {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "code",    "PEDIDO_NAO_ENCONTRADO",
                "message", ex.getMessage(),
                "orderId", ex.getOrderId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidOrderStateTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidOrderStateTransitionException ex) {
        Map<String, Object> body = Map.of(
                "code",            "TRANSICAO_INVALIDA",
                "message",         ex.getMessage(),
                "orderId",         ex.getOrderId(),
                "currentStatus",   ex.getCurrentStatus().name(),
                "attemptedAction", ex.getAttemptedAction()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(ReservationOperationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleReservationFailure(ReservationOperationFailedException ex) {
        Map<String, Object> body = Map.of(
                "code",          "FALHA_RESERVA",
                "message",       ex.getMessage(),
                "reservationId", ex.getReservationId().toString(),
                "operation",     ex.getOperation()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
