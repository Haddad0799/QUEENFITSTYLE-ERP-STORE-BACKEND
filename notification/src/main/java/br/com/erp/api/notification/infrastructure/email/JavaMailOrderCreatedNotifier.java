package br.com.erp.api.notification.infrastructure.email;

import br.com.erp.api.notification.application.port.OrderCreatedNotifier;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.Year;
import java.util.Locale;

/**
 * Notifica a dona da loja, via SMTP local (JavaMail), sempre que um novo pedido é criado
 * no e-commerce. Espelha {@link br.com.erp.api.notification.infrastructure.resend.ResendOrderCreatedNotifier}
 * para que o fluxo de novo pedido também funcione sob o provider {@code javamail} (ambiente
 * local), mantendo a simetria com a notificação de confirmação.
 *
 * O e-mail vai para {@code notification.admin-email} e traz um botão para responder a
 * cliente pela conversa de WhatsApp já montada no checkout.
 */
@Service
@ConditionalOnProperty(name = "notification.provider", havingValue = "javamail", matchIfMissing = true)
public class JavaMailOrderCreatedNotifier implements OrderCreatedNotifier {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private static final String EMAIL_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#fafafa;font-family:Arial,sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" role="presentation"
                     style="background:#fafafa;padding:16px 0;">
                <tr><td align="center">
                  <table align="center" cellpadding="0" cellspacing="0" role="presentation"
                         style="width:100%;max-width:480px;background:#ffffff;
                                border:1px solid #e8e0d8;border-radius:8px;overflow:hidden;">

                    <!-- Header -->
                    <tr>
                      <td style="background:#fdf6f1;padding:24px;text-align:center;border-bottom:1px solid #e8d5c4;">
                        <img src="https://pub-c2b5ff96dcca41228edaffcbc0e93c92.r2.dev/brand/logo-transparent.png"
                             height="72" alt="QueenFitStyle"
                             style="display:block;height:72px;margin:0 auto;border:0;"/>
                        <div style="margin-top:12px;color:#A0673A;font-size:16px;font-weight:bold;
                                    letter-spacing:0.25em;">QUEENFITSTYLE</div>
                      </td>
                    </tr>

                    <!-- Body -->
                    <tr>
                      <td style="padding:20px 24px;">
                        <p style="margin:0 0 8px;font-size:18px;color:#1a1a1a;font-weight:bold;">
                          🛍️ Novo pedido recebido!
                        </p>

                        <!-- Order number badge -->
                        <div style="background:#fdf6f1;border:1px solid #e8d5c4;border-radius:6px;
                                    padding:10px 16px;margin:12px 0 20px;">
                          <span style="font-size:14px;color:#A0673A;font-weight:bold;">
                            Pedido #{orderId}
                          </span>
                        </div>

                        <!-- Customer -->
                        <p style="margin:0 0 4px;font-size:14px;color:#1a1a1a;">
                          <strong>Cliente:</strong> {customerName}
                        </p>
                        <p style="margin:0 0 20px;font-size:14px;color:#1a1a1a;">
                          <strong>Cidade:</strong> {customerCity}
                        </p>

                        <!-- Item cards -->
                        {itemRows}

                        <!-- Total -->
                        <table width="100%" cellpadding="0" cellspacing="0" role="presentation">
                          <tr>
                            <td style="padding:16px 0 0;border-top:2px solid #e8e0d8;
                                       text-align:right;font-size:15px;font-weight:bold;color:#1a1a1a;">
                              Total: <span style="color:#A0673A;">{totalAmount}</span>
                            </td>
                          </tr>
                        </table>

                        <!-- Urgency -->
                        <p style="margin:24px 0 16px;font-size:15px;color:#1a1a1a;line-height:1.6;
                                  text-align:center;font-weight:bold;">
                          A cliente está aguardando. Quanto mais rápido você responder,
                          maior a chance de fechar a venda.
                        </p>

                        <!-- WhatsApp button -->
                        <table width="100%" cellpadding="0" cellspacing="0" role="presentation">
                          <tr>
                            <td align="center" style="padding:4px 0 8px;">
                              <a href="{customerWhatsAppUrl}"
                                 style="display:inline-block;background:#25D366;color:#ffffff;
                                        text-decoration:none;font-size:16px;font-weight:bold;
                                        padding:14px 32px;border-radius:6px;">
                                Abrir WhatsApp
                              </a>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background:#f5f0eb;padding:14px 24px;text-align:center;
                                 border-top:1px solid #e8e0d8;">
                        <p style="margin:0;font-size:12px;color:#999;">
                          © {year} QueenFitStyle. Todos os direitos reservados.
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """;

    private static final String ITEM_ROW_TEMPLATE = """
            <table width="100%" cellpadding="0" cellspacing="0" role="presentation"
                   style="border-collapse:separate;margin:0 0 12px;background:#fdf6f1;
                          border:1px solid #f0ebe4;border-radius:6px;">
              <tr>
                <td valign="top" style="padding:12px;">
                  <div style="font-size:15px;color:#1a1a1a;font-weight:bold;line-height:1.3;">
                    {productName}
                  </div>
                  <div style="font-size:14px;color:#999;margin-top:4px;">{variant}</div>
                  <div style="font-size:14px;color:#555;margin-top:8px;">
                    x{quantity} &middot; <strong style="color:#A0673A;">{subtotal}</strong>
                  </div>
                </td>
              </tr>
            </table>
            """;

    private final JavaMailSender mailSender;
    private final String from;
    private final String adminEmail;

    public JavaMailOrderCreatedNotifier(JavaMailSender mailSender,
                                        @Value("${spring.mail.username:}") String from,
                                        @Value("${notification.admin-email}") String adminEmail) {
        this.mailSender = mailSender;
        this.from       = from;
        this.adminEmail = adminEmail;
    }

    @Override
    public void notify(Order order, Customer customer, String customerPhone) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        if (from != null && !from.isBlank()) {
            helper.setFrom(from);
        }
        helper.setTo(adminEmail);
        helper.setSubject("Novo pedido #%d — responda pelo WhatsApp".formatted(order.getId()));
        helper.setText(buildHtmlBody(order, customer, customerPhone), true);

        mailSender.send(message);
    }

    private String buildHtmlBody(Order order, Customer customer, String customerPhone) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(PT_BR);

        return EMAIL_TEMPLATE
                .replace("{orderId}", String.valueOf(order.getId()))
                .replace("{customerName}", escape(customer.getName()))
                .replace("{customerCity}", escape(cityLabel(customer)))
                .replace("{itemRows}", buildItemRows(order, currency))
                .replace("{totalAmount}", currency.format(order.getTotalAmount()))
                .replace("{customerWhatsAppUrl}", escapeAttr(customerWhatsAppUrl(customerPhone)))
                .replace("{year}", String.valueOf(Year.now().getValue()));
    }

    /**
     * Link {@code wa.me} para a vendedora abrir a conversa direta com a cliente — sem texto
     * pré-definido, para ela responder como quiser. Mantém apenas os dígitos do telefone
     * (removendo parênteses, espaços e hífens); o número já inclui o código do país, então
     * nenhum prefixo é adicionado.
     */
    private String customerWhatsAppUrl(String customerPhone) {
        String digits = customerPhone == null ? "" : customerPhone.replaceAll("\\D", "");
        return "https://wa.me/" + digits;
    }

    private String buildItemRows(Order order, NumberFormat currency) {
        StringBuilder rows = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            rows.append(ITEM_ROW_TEMPLATE
                    .replace("{productName}", escape(item.getProductName()))
                    .replace("{variant}", escape(variantLabel(item)))
                    .replace("{quantity}", String.valueOf(item.getQuantity()))
                    .replace("{subtotal}", currency.format(item.getSubtotal())));
        }
        return rows.toString();
    }

    private String cityLabel(Customer customer) {
        return customer.getCity() != null && !customer.getCity().isBlank()
                ? customer.getCity()
                : "não informada";
    }

    private String variantLabel(OrderItem item) {
        if (item.getColorName() != null && item.getSizeLabel() != null)
            return item.getColorName() + " / " + item.getSizeLabel();
        if (item.getColorName() != null) return item.getColorName();
        if (item.getSizeLabel() != null) return item.getSizeLabel();
        return item.getSkuCode();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String escapeAttr(String value) {
        return escape(value).replace("\"", "&quot;");
    }
}
