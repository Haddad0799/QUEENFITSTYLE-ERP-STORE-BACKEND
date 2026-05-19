package br.com.erp.api.inventory.presentation.controller;

import br.com.erp.api.inventory.domain.exception.InsufficientReservationException;
import br.com.erp.api.inventory.domain.exception.InsufficientStockException;
import br.com.erp.api.inventory.domain.exception.ReservationNotFoundException;
import br.com.erp.api.inventory.domain.exception.SkuNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class InventoryControllerAdvice {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        Map<String, Object> body = Map.of(
                "code", "ESTOQUE_INSUFICIENTE",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InsufficientReservationException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientReservation(InsufficientReservationException ex) {
        Map<String, Object> body = Map.of(
                "code", "RESERVA_INSUFICIENTE",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReservationNotFound(ReservationNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "code", "RESERVA_NAO_ENCONTRADA",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(SkuNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSkuNotFound(SkuNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "code", "SKU_NAO_ENCONTRADO",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, Object> body = Map.of(
                "code", "DADO_INVALIDO",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

