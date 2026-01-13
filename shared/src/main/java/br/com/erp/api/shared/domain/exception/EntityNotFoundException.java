package br.com.erp.api.shared.domain.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, Object id) {
        super(entity + " não encontrada com o id: " + id);
    }
}
