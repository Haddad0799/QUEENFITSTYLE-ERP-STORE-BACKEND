package br.com.erp.api.notification.infrastructure.resend;

import br.com.erp.api.notification.application.port.OrderConfirmationNotifier;
import br.com.erp.api.notification.application.service.WhatsAppNotificationService;
import br.com.erp.api.notification.application.util.CustomerNameFormatter;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.Year;
import java.util.Locale;
import java.util.Map;

@Service
@Primary
@ConditionalOnProperty(name = "notification.provider", havingValue = "resend")
public class ResendOrderConfirmationNotifier implements OrderConfirmationNotifier {

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
                        <p style="margin:0 0 8px;font-size:15px;color:#1a1a1a;">
                          Olá, <strong>{customerName}</strong>!
                        </p>
                        <p style="margin:0 0 20px;font-size:14px;color:#555;line-height:1.6;">
                          Seu pagamento foi confirmado e seu pedido já está sendo preparado
                          com muito carinho para você. Em breve ele chegará até você.
                          Enquanto isso, confira o resumo da sua compra:
                        </p>

                        <!-- Order number badge -->
                        <div style="background:#fdf6f1;border:1px solid #e8d5c4;border-radius:6px;
                                    padding:10px 16px;margin-bottom:20px;">
                          <span style="font-size:14px;color:#A0673A;font-weight:bold;">
                            Pedido #{orderId}
                          </span>
                        </div>

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

                        <p style="margin:20px 0 0;font-size:14px;color:#777;line-height:1.5;">
                          Dúvidas? Fale conosco pelo
                          <a href="{whatsappUrl}" style="color:#A0673A;">WhatsApp</a>.
                        </p>

                        <p style="margin:16px 0 0;font-size:14px;color:#A0673A;line-height:1.6;
                                  text-align:center;font-style:italic;">
                          Obrigada por escolher a QueenFitStyle. Você merece se sentir incrível.
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

    /**
     * Card de item: table de duas células (imagem à esquerda, dados à direita) — único
     * padrão de colunas lado a lado confiável entre clientes de e-mail. O {imageCell} é
     * montado em Java para suportar itens sem imagem com um placeholder neutro.
     */
    private static final String ITEM_ROW_TEMPLATE = """
            <table width="100%" cellpadding="0" cellspacing="0" role="presentation"
                   style="border-collapse:separate;margin:0 0 12px;background:#fdf6f1;
                          border:1px solid #f0ebe4;border-radius:6px;">
              <tr>
                {imageCell}
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

    private static final String IMAGE_CELL_TEMPLATE = """
            <td width="76" valign="top" style="padding:12px 0 12px 12px;">
              <img src="{imageUrl}" width="64" height="64" alt="{alt}"
                   style="display:block;width:64px;height:64px;border-radius:4px;
                          border:1px solid #e8e0d8;object-fit:cover;"/>
            </td>
            """;

    private static final String IMAGE_PLACEHOLDER_CELL = """
            <td width="76" valign="top" style="padding:12px 0 12px 12px;">
              <div style="width:64px;height:64px;border-radius:4px;
                          border:1px solid #e8e0d8;background:#f0e6dc;"></div>
            </td>
            """;

    private final WhatsAppNotificationService whatsAppNotificationService;
    private final String apiKey;
    private final String from;

    public ResendOrderConfirmationNotifier(WhatsAppNotificationService whatsAppNotificationService,
                                           @Value("${resend.api-key}") String apiKey,
                                           @Value("${resend.from:onboarding@resend.dev}") String from) {
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.apiKey            = apiKey;
        this.from              = from;
    }

    @Override
    public void notify(Order order, Customer customer, Map<Long, String> imageUrls) throws Exception {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions request = CreateEmailOptions.builder()
                .from(from)
                .to(customer.getEmail())
                .subject("Pedido #" + order.getId() + " confirmado — QueenFitStyle")
                .html(buildHtmlBody(order, customer, imageUrls))
                .build();
        resend.emails().send(request);
    }

    private String buildHtmlBody(Order order, Customer customer, Map<Long, String> imageUrlsByItemId) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(PT_BR);

        return EMAIL_TEMPLATE
                .replace("{customerName}", escape(CustomerNameFormatter.toDisplayName(customer.getName())))
                .replace("{orderId}", String.valueOf(order.getId()))
                .replace("{itemRows}", buildItemRows(order, currency, imageUrlsByItemId))
                .replace("{totalAmount}", currency.format(order.getTotalAmount()))
                .replace("{whatsappUrl}", whatsAppNotificationService.buildPostPaymentUrl(order, customer))
                .replace("{year}", String.valueOf(Year.now().getValue()));
    }

    private String buildItemRows(Order order, NumberFormat currency, Map<Long, String> imageUrlsByItemId) {
        StringBuilder rows = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            String imageUrl = imageUrlsByItemId == null ? null : imageUrlsByItemId.get(item.getId());
            rows.append(ITEM_ROW_TEMPLATE
                    .replace("{imageCell}", buildImageCell(imageUrl, item.getProductName()))
                    .replace("{productName}", escape(item.getProductName()))
                    .replace("{variant}", escape(variantLabel(item)))
                    .replace("{quantity}", String.valueOf(item.getQuantity()))
                    .replace("{subtotal}", currency.format(item.getSubtotal())));
        }
        return rows.toString();
    }

    private String buildImageCell(String imageUrl, String productName) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return IMAGE_PLACEHOLDER_CELL;
        }
        return IMAGE_CELL_TEMPLATE
                .replace("{imageUrl}", escapeAttr(imageUrl))
                .replace("{alt}", escapeAttr(productName));
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
