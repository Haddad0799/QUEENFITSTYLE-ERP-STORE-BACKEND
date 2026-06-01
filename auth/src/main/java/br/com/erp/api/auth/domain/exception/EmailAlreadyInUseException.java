package br.com.erp.api.auth.domain.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("Email já cadastrado: " + email);
    }
}
