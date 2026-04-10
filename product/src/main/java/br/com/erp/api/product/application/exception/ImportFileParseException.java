package br.com.erp.api.product.application.exception;

public class ImportFileParseException extends RuntimeException {

    public ImportFileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

