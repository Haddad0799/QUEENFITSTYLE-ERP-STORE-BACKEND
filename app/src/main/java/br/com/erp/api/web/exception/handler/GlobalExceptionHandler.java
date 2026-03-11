package br.com.erp.api.web.exception.handler;

import br.com.erp.api.product.domain.exception.DuplicateSkuCombinationException;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import br.com.erp.api.shared.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEM_JSON = "application/problem+json";

    // 1️⃣ Handler específico primeiro
    @ExceptionHandler(DuplicateSkuCombinationException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateSku(
            DuplicateSkuCombinationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Combinação de SKU já existente");
        problem.setType(URI.create("https://example.com/probs/duplicate-sku-combination"));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("conflicts", ex.getConflicts());
        problem.setProperty("path", request.getRequestURI());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, PROBLEM_JSON);

        return ResponseEntity.unprocessableEntity().headers(headers).body(problem);
    }

    // 2️⃣ Depois handler genérico de regra de negócio
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(
            DomainException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Violação de Regra de Negócio");
        problem.setType(URI.create("https://example.com/probs/domain-exception"));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("path", request.getRequestURI());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, PROBLEM_JSON);

        return ResponseEntity.unprocessableEntity().headers(headers).body(problem);
    }

    // 3️⃣ Not found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Recurso não encontrado");
        problem.setType(URI.create("https://example.com/probs/not-found"));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("path", request.getRequestURI());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, PROBLEM_JSON);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(problem);
    }

    // 4️⃣ Genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );
        problem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        problem.setType(URI.create("https://example.com/probs/internal-server-error"));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("path", request.getRequestURI());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, PROBLEM_JSON);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos na requisição"
        );
        problem.setTitle("Erro de Validação");
        problem.setType(URI.create("https://example.com/probs/validation-error"));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("errors", errors);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, PROBLEM_JSON);

        return ResponseEntity.badRequest().headers(headers).body(problem);
    }
}