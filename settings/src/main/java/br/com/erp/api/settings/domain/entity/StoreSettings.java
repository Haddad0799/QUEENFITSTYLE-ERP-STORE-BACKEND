package br.com.erp.api.settings.domain.entity;

import br.com.erp.api.settings.domain.exception.InvalidStoreSettingsException;

import java.util.regex.Pattern;

/**
 * Configuração única (singleton) da loja. Guarda os dados que a dona pode editar sem
 * redeploy: o WhatsApp de atendimento e o e-mail destinatário dos avisos de pedido.
 */
public class StoreSettings {

    /** Apenas dígitos, incluindo o código do país (ex.: 5511999998888). */
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{10,15}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Long   id;
    private String whatsappPhone;
    private String notificationEmail;

    private StoreSettings(Long id, String whatsappPhone, String notificationEmail) {
        this.id                = id;
        this.whatsappPhone     = whatsappPhone;
        this.notificationEmail = notificationEmail;
    }

    public static StoreSettings create(String whatsappPhone, String notificationEmail) {
        validatePhone(whatsappPhone);
        validateEmail(notificationEmail);
        return new StoreSettings(null, whatsappPhone, notificationEmail);
    }

    public static StoreSettings restore(Long id, String whatsappPhone, String notificationEmail) {
        return new StoreSettings(id, whatsappPhone, notificationEmail);
    }

    /**
     * Atualização parcial: aplica apenas os campos informados (não-nulos), validando-os.
     * Campos nulos preservam o valor atual.
     */
    public void update(String whatsappPhone, String notificationEmail) {
        if (whatsappPhone != null) {
            validatePhone(whatsappPhone);
            this.whatsappPhone = whatsappPhone;
        }
        if (notificationEmail != null) {
            validateEmail(notificationEmail);
            this.notificationEmail = notificationEmail;
        }
    }

    private static void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidStoreSettingsException(
                    "WhatsApp inválido: informe apenas dígitos, incluindo o código do país (ex.: 5511999998888).");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidStoreSettingsException("E-mail de notificação inválido.");
        }
    }

    public Long   getId()                { return id; }
    public String getWhatsappPhone()     { return whatsappPhone; }
    public String getNotificationEmail() { return notificationEmail; }
}
