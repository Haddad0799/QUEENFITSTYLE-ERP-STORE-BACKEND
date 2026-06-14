package br.com.erp.api.settings.application.usecase;

import br.com.erp.api.settings.application.command.UpdateStoreSettingsCommand;
import br.com.erp.api.settings.domain.entity.StoreSettings;
import br.com.erp.api.settings.domain.port.StoreSettingsRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Atualiza a configuração singleton da loja (WhatsApp e/ou e-mail de avisos). Aplica
 * apenas os campos informados, validando-os no domínio. Caso a linha ainda não exista
 * (ambiente novo sem seed), cria-a a partir do fallback de variável de ambiente antes
 * de aplicar o patch.
 */
@Service
public class UpdateStoreSettingsUseCase {

    private final StoreSettingsRepositoryPort repository;
    private final String                      fallbackWhatsappPhone;
    private final String                      fallbackNotificationEmail;

    public UpdateStoreSettingsUseCase(StoreSettingsRepositoryPort repository,
                                      @Value("${whatsapp.seller-phone:}") String fallbackWhatsappPhone,
                                      @Value("${notification.admin-email:}") String fallbackNotificationEmail) {
        this.repository                = repository;
        this.fallbackWhatsappPhone     = fallbackWhatsappPhone;
        this.fallbackNotificationEmail = fallbackNotificationEmail;
    }

    @Transactional
    public StoreSettings handle(UpdateStoreSettingsCommand command) {
        return repository.find()
                .map(existing -> applyUpdate(existing, command))
                .orElseGet(() -> createFromFallback(command));
    }

    private StoreSettings applyUpdate(StoreSettings settings, UpdateStoreSettingsCommand command) {
        settings.update(command.whatsappPhone(), command.notificationEmail());
        repository.update(settings);
        return settings;
    }

    private StoreSettings createFromFallback(UpdateStoreSettingsCommand command) {
        StoreSettings settings = StoreSettings.restore(null, fallbackWhatsappPhone, fallbackNotificationEmail);
        settings.update(command.whatsappPhone(), command.notificationEmail());
        repository.insert(settings);
        return settings;
    }
}
