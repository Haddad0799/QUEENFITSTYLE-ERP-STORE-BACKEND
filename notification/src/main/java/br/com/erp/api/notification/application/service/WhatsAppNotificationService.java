package br.com.erp.api.notification.application.service;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
import br.com.erp.api.settings.application.query.StoreSettingsQueryService;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class WhatsAppNotificationService {

    private final StoreSettingsQueryService storeSettingsQueryService;

    public WhatsAppNotificationService(StoreSettingsQueryService storeSettingsQueryService) {
        this.storeSettingsQueryService = storeSettingsQueryService;
    }

    public String buildUrl(Order order, Customer customer) {
        return buildUrlWithText(buildMessageText(order, customer));
    }

    public String buildMessageText(Order order, Customer customer) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        String itemLines = order.getItems().stream()
                .map(item -> "  • %s (%s) x%d — %s".formatted(
                        item.getProductName(),
                        variantLabel(item),
                        item.getQuantity(),
                        currency.format(item.getSubtotal())
                ))
                .collect(Collectors.joining("\n"));

        var sb = new StringBuilder();
        sb.append("Olá! Gostaria de finalizar meu pedido *#%d*\n\n".formatted(order.getId()));
        sb.append("*Cliente:* %s\n".formatted(customer.getName()));
        if (customer.getCity() != null && !customer.getCity().isBlank()) {
            sb.append("*Cidade:* %s\n".formatted(customer.getCity()));
        }
        sb.append("\n*Itens:*\n").append(itemLines).append("\n\n");
        sb.append("*Total:* %s".formatted(currency.format(order.getTotalAmount())));
        if (order.getNotes() != null && !order.getNotes().isBlank()) {
            sb.append("\n\n*Obs:* %s".formatted(order.getNotes()));
        }
        sb.append("\n\nAguardo confirmação!");
        return sb.toString();
    }

    /**
     * Mensagem usada no e-mail de confirmação — o pedido já foi pago, então o tom é de
     * acompanhamento da entrega, não de finalização da compra.
     */
    public String buildPostPaymentUrl(Order order, Customer customer) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        String text = ("""
                Olá! Recebi a confirmação do meu pedido *#%d* e gostaria de saber mais sobre a entrega.

                *Cliente:* %s
                *Total:* %s

                Obrigada!""").formatted(
                order.getId(),
                customer.getName(),
                currency.format(order.getTotalAmount())
        );

        return buildUrlWithText(text);
    }

    private String buildUrlWithText(String text) {
        // Lido a cada chamada para refletir edições feitas pela dona sem redeploy.
        String sellerPhone = storeSettingsQueryService.current().whatsappPhone();
        String encoded     = URLEncoder.encode(text, StandardCharsets.UTF_8);
        return "https://wa.me/" + sellerPhone + "?text=" + encoded;
    }

    private String variantLabel(OrderItem item) {
        if (item.getColorName() != null && item.getSizeLabel() != null)
            return item.getColorName() + " / " + item.getSizeLabel();
        if (item.getColorName() != null) return item.getColorName();
        if (item.getSizeLabel() != null) return item.getSizeLabel();
        return item.getSkuCode();
    }
}
