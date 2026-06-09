package br.com.erp.api.order.application.service;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.Year;
import java.util.Locale;

@Service
public class OrderEmailService {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private static final String EMAIL_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#fafafa;font-family:Arial,sans-serif;">
              <table width="100%" cellpadding="0" cellspacing="0" style="background:#fafafa;padding:32px 0;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border:1px solid #e8e0d8;border-radius:8px;overflow:hidden;">

                    <!-- Header -->
                    <tr>
                      <td style="background:#A0673A;padding:28px 40px;text-align:center;">
                        <span style="color:#ffffff;font-size:22px;font-weight:bold;
                                     letter-spacing:0.3em;">QUEENFITSTYLE</span>
                      </td>
                    </tr>

                    <!-- Body -->
                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="margin:0 0 8px;font-size:16px;color:#1a1a1a;">
                          Olá, <strong>{customerName}</strong>!
                        </p>
                        <p style="margin:0 0 28px;font-size:14px;color:#555;">
                          Seu pedido foi confirmado. Abaixo está o resumo:
                        </p>

                        <!-- Order number badge -->
                        <div style="background:#fdf6f1;border:1px solid #e8d5c4;border-radius:6px;
                                    padding:12px 20px;margin-bottom:28px;display:inline-block;">
                          <span style="font-size:13px;color:#A0673A;font-weight:bold;">
                            Pedido #{orderId}
                          </span>
                        </div>

                        <!-- Items table -->
                        <table width="100%" cellpadding="0" cellspacing="0"
                               style="border-collapse:collapse;margin-bottom:24px;">
                          <thead>
                            <tr style="border-bottom:2px solid #e8e0d8;">
                              <th style="text-align:left;font-size:12px;color:#888;
                                         padding:8px 0;font-weight:600;">PRODUTO</th>
                              <th style="text-align:center;font-size:12px;color:#888;
                                         padding:8px 0;font-weight:600;">QTD</th>
                              <th style="text-align:right;font-size:12px;color:#888;
                                         padding:8px 0;font-weight:600;">SUBTOTAL</th>
                            </tr>
                          </thead>
                          <tbody>
                            {itemRows}
                          </tbody>
                        </table>

                        <!-- Total -->
                        <table width="100%" cellpadding="0" cellspacing="0"
                               style="border-top:2px solid #e8e0d8;margin-bottom:32px;">
                          <tr>
                            <td style="padding:14px 0;font-size:15px;
                                       font-weight:bold;color:#1a1a1a;">Total</td>
                            <td style="padding:14px 0;font-size:15px;font-weight:bold;
                                       color:#A0673A;text-align:right;">{totalAmount}</td>
                          </tr>
                        </table>

                        <p style="margin:0;font-size:13px;color:#777;line-height:1.6;">
                          Dúvidas? Fale conosco pelo
                          <a href="{whatsappUrl}" style="color:#A0673A;">WhatsApp</a>.
                        </p>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background:#f5f0eb;padding:20px 40px;text-align:center;
                                 border-top:1px solid #e8e0d8;">
                        <p style="margin:0;font-size:11px;color:#999;">
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
            <tr style="border-bottom:1px solid #f0ebe4;">
              <td style="padding:10px 0;font-size:13px;color:#1a1a1a;">
                {productName}<br/>
                <span style="font-size:11px;color:#999;">{variant}</span>
              </td>
              <td style="padding:10px 0;font-size:13px;color:#555;text-align:center;">
                x{quantity}
              </td>
              <td style="padding:10px 0;font-size:13px;color:#1a1a1a;text-align:right;">
                {subtotal}
              </td>
            </tr>
            """;

    private final JavaMailSender mailSender;
    private final WhatsAppUrlService whatsAppUrlService;
    private final String from;

    public OrderEmailService(JavaMailSender mailSender,
                             WhatsAppUrlService whatsAppUrlService,
                             @Value("${spring.mail.username:}") String from) {
        this.mailSender        = mailSender;
        this.whatsAppUrlService = whatsAppUrlService;
        this.from              = from;
    }

    public void sendConfirmation(Order order, Customer customer) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        if (from != null && !from.isBlank()) {
            helper.setFrom(from);
        }
        helper.setTo(customer.getEmail());
        helper.setSubject("Pedido #%d confirmado — QueenFitStyle".formatted(order.getId()));
        helper.setText(buildHtmlBody(order, customer), true);

        mailSender.send(message);
    }

    private String buildHtmlBody(Order order, Customer customer) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(PT_BR);

        return EMAIL_TEMPLATE
                .replace("{customerName}", escape(customer.getName()))
                .replace("{orderId}", String.valueOf(order.getId()))
                .replace("{itemRows}", buildItemRows(order, currency))
                .replace("{totalAmount}", currency.format(order.getTotalAmount()))
                .replace("{whatsappUrl}", whatsAppUrlService.buildUrl(order, customer))
                .replace("{year}", String.valueOf(Year.now().getValue()));
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
}
