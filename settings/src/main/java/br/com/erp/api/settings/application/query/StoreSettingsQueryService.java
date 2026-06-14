package br.com.erp.api.settings.application.query;

import br.com.erp.api.settings.domain.port.StoreSettingsRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Leitura das configurações efetivas da loja. Resolve o valor do banco (linha singleton)
 * e, caso a tabela esteja vazia ou um campo esteja em branco, cai no fallback das variáveis
 * de ambiente — assim um ambiente novo não quebra antes do seed/edição.
 */
@Service
public class StoreSettingsQueryService {

    private final StoreSettingsRepositoryPort repository;
    private final String                      fallbackWhatsappPhone;
    private final String                      fallbackNotificationEmail;

    public StoreSettingsQueryService(StoreSettingsRepositoryPort repository,
                                     @Value("${whatsapp.seller-phone:}") String fallbackWhatsappPhone,
                                     @Value("${notification.admin-email:}") String fallbackNotificationEmail) {
        this.repository                = repository;
        this.fallbackWhatsappPhone     = fallbackWhatsappPhone;
        this.fallbackNotificationEmail = fallbackNotificationEmail;
    }

    public StoreSettingsView current() {
        return repository.find()
                .map(settings -> new StoreSettingsView(
                        firstNonBlank(settings.getWhatsappPhone(), fallbackWhatsappPhone),
                        firstNonBlank(settings.getNotificationEmail(), fallbackNotificationEmail)))
                .orElseGet(() -> new StoreSettingsView(
                        blankToNull(fallbackWhatsappPhone),
                        blankToNull(fallbackNotificationEmail)));
    }

    private static String firstNonBlank(String value, String fallback) {
        if (value != null && !value.isBlank()) return value;
        return blankToNull(fallback);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
