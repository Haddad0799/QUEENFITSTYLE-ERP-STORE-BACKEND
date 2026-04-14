package br.com.erp.api.product.application.exception;

public class AiIntegrationException extends RuntimeException {

    public AiIntegrationException(String message) {
        super(message);
    }

    public AiIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
