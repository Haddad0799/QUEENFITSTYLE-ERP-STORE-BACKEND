package br.com.erp.api.settings.application.query;

/**
 * Visão de leitura das configurações efetivas da loja (já com o fallback de variável
 * de ambiente aplicado). Consumida pelos controllers e pelos demais módulos.
 */
public record StoreSettingsView(String whatsappPhone, String notificationEmail) {
}
