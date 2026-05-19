package br.com.erp.api.order.application.service;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.entity.OrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class WhatsAppUrlService {

    private final String sellerPhone;

    public WhatsAppUrlService(@Value("${whatsapp.seller-phone}") String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String buildUrl(Order order, Customer customer) {
        String encoded = URLEncoder.encode(buildMessageText(order, customer), StandardCharsets.UTF_8);
        return "https://wa.me/" + sellerPhone + "?text=" + encoded;
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

    private String variantLabel(OrderItem item) {
        if (item.getColorName() != null && item.getSizeLabel() != null)
            return item.getColorName() + " / " + item.getSizeLabel();
        if (item.getColorName() != null) return item.getColorName();
        if (item.getSizeLabel() != null) return item.getSizeLabel();
        return item.getSkuCode();
    }
}
