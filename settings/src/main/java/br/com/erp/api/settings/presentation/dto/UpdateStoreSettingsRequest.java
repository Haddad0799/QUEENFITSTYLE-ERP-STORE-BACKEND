package br.com.erp.api.settings.presentation.dto;

import br.com.erp.api.settings.application.command.UpdateStoreSettingsCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * Requisição de atualização parcial das configurações da loja. Ambos os campos são
 * opcionais (semântica de PATCH): nulos preservam o valor atual. Quando informados,
 * são validados aqui (formato) e novamente no domínio.
 */
public record UpdateStoreSettingsRequest(

        @Pattern(regexp = "\\d{10,15}",
                message = "informe apenas dígitos, incluindo o código do país (ex.: 5511999998888)")
        String whatsappPhone,

        @Email(message = "e-mail de notificação inválido")
        String notificationEmail
) {

    public UpdateStoreSettingsCommand toCommand() {
        return new UpdateStoreSettingsCommand(whatsappPhone, notificationEmail);
    }
}
