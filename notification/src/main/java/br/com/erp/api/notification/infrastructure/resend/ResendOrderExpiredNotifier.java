package br.com.erp.api.notification.infrastructure.resend;

import br.com.erp.api.notification.application.port.OrderExpiredNotifier;
import br.com.erp.api.notification.application.util.CustomerNameFormatter;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Year;

/**
 * Notifica a cliente, via Resend, quando seu pedido expira por falta de confirmação no prazo
 * de 24h e é cancelado automaticamente. O e-mail traz um botão "Visitar a loja" apontando
 * para {@code store.url}, convidando a cliente a fazer um novo pedido.
 */
@Service
@ConditionalOnProperty(name = "notification.provider", havingValue = "resend")
public class ResendOrderExpiredNotifier implements OrderExpiredNotifier {

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
                        <p style="margin:0 0 8px;font-size:15px;color:#1a1a1a;">
                          Olá, <strong>{customerName}</strong>!
                        </p>

                        <!-- Order number badge -->
                        <div style="background:#fdf6f1;border:1px solid #e8d5c4;border-radius:6px;
                                    padding:10px 16px;margin:12px 0 20px;">
                          <span style="font-size:14px;color:#A0673A;font-weight:bold;">
                            Pedido #{orderId}
                          </span>
                        </div>

                        <p style="margin:0 0 20px;font-size:14px;color:#555;line-height:1.6;">
                          Seu pedido não foi confirmado dentro do prazo de 24 horas e foi
                          cancelado automaticamente. Se ainda tiver interesse, acesse nossa
                          loja e faça um novo pedido.
                        </p>

                        <!-- Store button -->
                        <table width="100%" cellpadding="0" cellspacing="0" role="presentation">
                          <tr>
                            <td align="center" style="padding:4px 0 8px;">
                              <a href="{storeUrl}"
                                 style="display:inline-block;background:#A0673A;color:#ffffff;
                                        text-decoration:none;font-size:16px;font-weight:bold;
                                        padding:14px 32px;border-radius:6px;">
                                Visitar a loja
                              </a>
                            </td>
                          </tr>
                        </table>

                        <p style="margin:20px 0 0;font-size:14px;color:#A0673A;line-height:1.6;
                                  text-align:center;font-style:italic;">
                          Esperamos ver você de volta em breve!
                        </p>
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

    private final String apiKey;
    private final String from;
    private final String storeUrl;

    public ResendOrderExpiredNotifier(@Value("${resend.api-key}") String apiKey,
                                      @Value("${resend.from:onboarding@resend.dev}") String from,
                                      @Value("${store.url}") String storeUrl) {
        this.apiKey   = apiKey;
        this.from     = from;
        this.storeUrl = storeUrl;
    }

    @Override
    public void notify(Order order, Customer customer) throws Exception {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions request = CreateEmailOptions.builder()
                .from(from)
                .to(customer.getEmail())
                .subject("Seu pedido #" + order.getId() + " expirou — QueenFitStyle")
                .html(buildHtmlBody(order, customer))
                .build();
        resend.emails().send(request);
    }

    private String buildHtmlBody(Order order, Customer customer) {
        return EMAIL_TEMPLATE
                .replace("{customerName}", escape(CustomerNameFormatter.toDisplayName(customer.getName())))
                .replace("{orderId}", String.valueOf(order.getId()))
                .replace("{storeUrl}", escapeAttr(storeUrl))
                .replace("{year}", String.valueOf(Year.now().getValue()));
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
