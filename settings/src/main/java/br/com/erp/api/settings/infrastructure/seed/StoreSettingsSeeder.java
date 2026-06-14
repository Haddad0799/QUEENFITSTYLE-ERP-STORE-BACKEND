package br.com.erp.api.settings.infrastructure.seed;

import br.com.erp.api.settings.domain.entity.StoreSettings;
import br.com.erp.api.settings.domain.port.StoreSettingsRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cria a linha singleton de {@code store_settings} no boot, a partir das variáveis de
 * ambiente atuais (WHATSAPP_SELLER_PHONE / NOTIFICATION_ADMIN_EMAIL). É idempotente:
 * se a linha já existe (ambiente já migrado/editado), não faz nada — assim edições da
 * dona nunca são sobrescritas por um redeploy.
 */
@Component
public class StoreSettingsSeeder implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(StoreSettingsSeeder.class);

    private final StoreSettingsRepositoryPort repository;

    @Value("${whatsapp.seller-phone:}")
    private String seedWhatsappPhone;

    @Value("${notification.admin-email:}")
    private String seedNotificationEmail;

    public StoreSettingsSeeder(StoreSettingsRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (repository.find().isPresent()) return;

        // restore() (e não create()) para não falhar o boot caso o ambiente não tenha
        // as variáveis configuradas; a dona preenche depois pela tela do ERP.
        StoreSettings settings = StoreSettings.restore(
                null,
                blankToNull(seedWhatsappPhone),
                blankToNull(seedNotificationEmail)
        );
        repository.insert(settings);
        log.info("Store settings seeded (whatsapp={}, email={})",
                settings.getWhatsappPhone(), settings.getNotificationEmail());
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
