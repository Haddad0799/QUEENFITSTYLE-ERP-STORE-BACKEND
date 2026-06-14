package br.com.erp.api.settings.presentation.dto;

import br.com.erp.api.settings.application.query.StoreSettingsView;

/**
 * Resposta consumida pelo e-commerce para montar os links de WhatsApp do footer.
 */
public record StoreWhatsAppResponse(String whatsappPhone) {

    public static StoreWhatsAppResponse from(StoreSettingsView view) {
        return new StoreWhatsAppResponse(view.whatsappPhone());
    }
}
