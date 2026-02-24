package br.com.erp.api.web.exception.handler;

import br.com.erp.api.product.domain.exception.DuplicateSkuCombinationException;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import br.com.erp.api.shared.domain.exception.DomainException;
import br.com.erp.api.shared.presentation.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1️⃣ Handler específico primeiro
    @ExceptionHandler(DuplicateSkuCombinationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSku(
            DuplicateSkuCombinationException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Combinação de SKU já existente",
                ex.getMessage(),
                ex.getConflicts(),
                request.getRequestURI()
        );

        return ResponseEntity.unprocessableEntity().body(error);
    }

    // 2️⃣ Depois handler genérico de regra de negócio
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Violação de Regra de Negócio",
                ex.getMessage(),
                null,
                request.getRequestURI()
        );

        return ResponseEntity.unprocessableEntity().body(error);
    }

    // 3️⃣ Not found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 4️⃣ Genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                null,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}