package br.com.erp.api.settings.presentation.controller;

import br.com.erp.api.settings.application.query.StoreSettingsQueryService;
import br.com.erp.api.settings.presentation.dto.StoreWhatsAppResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configurações expostas ao e-commerce (apenas o WhatsApp, para montar os links do footer).
 * Protegido pela autenticação de service token (client_id/secret) no SecurityConfig —
 * não é um endpoint público aberto.
 */
@RestController
@RequestMapping("/store/settings")
public class StoreSettingsStoreController {

    private final StoreSettingsQueryService queryService;

    public StoreSettingsStoreController(StoreSettingsQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public StoreWhatsAppResponse get() {
        return StoreWhatsAppResponse.from(queryService.current());
    }
}
