package br.com.erp.api.settings.presentation.dto;

import br.com.erp.api.settings.application.query.StoreSettingsView;

/**
 * Resposta da tela de configurações do ERP.
 */
public record StoreSettingsResponse(String whatsappPhone, String notificationEmail) {

    public static StoreSettingsResponse from(StoreSettingsView view) {
        return new StoreSettingsResponse(view.whatsappPhone(), view.notificationEmail());
    }
}
