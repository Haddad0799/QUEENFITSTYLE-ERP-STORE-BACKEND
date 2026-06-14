package br.com.erp.api.settings.application.command;

/**
 * Entrada da atualização parcial da configuração da loja. Campos nulos preservam o
 * valor atual (semântica de PATCH).
 */
public record UpdateStoreSettingsCommand(String whatsappPhone, String notificationEmail) {
}
