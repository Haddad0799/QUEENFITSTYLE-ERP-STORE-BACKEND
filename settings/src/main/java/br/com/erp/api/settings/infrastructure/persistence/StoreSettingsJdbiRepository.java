package br.com.erp.api.settings.infrastructure.persistence;

import br.com.erp.api.settings.domain.entity.StoreSettings;
import br.com.erp.api.settings.domain.port.StoreSettingsRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistência da configuração singleton da loja. Sempre opera sobre a única linha (id = 1).
 */
@Repository
public class StoreSettingsJdbiRepository implements StoreSettingsRepositoryPort {

    private final Jdbi jdbi;

    public StoreSettingsJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Optional<StoreSettings> find() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, whatsapp_phone, notification_email
                        FROM store_settings
                        WHERE id = 1
                        """)
                        .map((rs, ctx) -> StoreSettings.restore(
                                rs.getLong("id"),
                                rs.getString("whatsapp_phone"),
                                rs.getString("notification_email")
                        ))
                        .findOne()
        );
    }

    @Override
    public void insert(StoreSettings settings) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO store_settings (id, whatsapp_phone, notification_email, created_at, updated_at)
                        VALUES (1, :whatsappPhone, :notificationEmail, now(), now())
                        """)
                        .bind("whatsappPhone", settings.getWhatsappPhone())
                        .bind("notificationEmail", settings.getNotificationEmail())
                        .execute()
        );
    }

    @Override
    public void update(StoreSettings settings) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE store_settings
                        SET whatsapp_phone     = :whatsappPhone,
                            notification_email = :notificationEmail,
                            updated_at         = now()
                        WHERE id = 1
                        """)
                        .bind("whatsappPhone", settings.getWhatsappPhone())
                        .bind("notificationEmail", settings.getNotificationEmail())
                        .execute()
        );
    }
}
