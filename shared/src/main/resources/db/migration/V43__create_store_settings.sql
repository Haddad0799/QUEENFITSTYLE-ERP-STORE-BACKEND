-- Configuração única (singleton) da loja, editável pela dona sem depender de
-- variáveis de ambiente ou redeploy. A linha é seedada no boot pelo StoreSettingsSeeder
-- a partir das variáveis WHATSAPP_SELLER_PHONE / NOTIFICATION_ADMIN_EMAIL.
CREATE TABLE store_settings (
    id                 BIGINT       PRIMARY KEY DEFAULT 1,
    whatsapp_phone     VARCHAR(20),
    notification_email VARCHAR(255),
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_store_settings_singleton CHECK (id = 1)
);
