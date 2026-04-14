package br.com.erp.api.product.application.exception;

public class AiEmptyResponseException extends RuntimeException {

    public AiEmptyResponseException(String message) {
        super(message);
    }
}
