package br.com.erp.api.settings.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

/**
 * Violação de regra de negócio ao criar/atualizar a configuração da loja
 * (ex.: WhatsApp fora do formato esperado, e-mail inválido). Mapeada para
 * 422 Unprocessable Entity pelo GlobalExceptionHandler.
 */
public class InvalidStoreSettingsException extends DomainException {

    public InvalidStoreSettingsException(String message) {
        super(message);
    }
}
