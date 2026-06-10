package br.com.erp.api.notification.infrastructure.adapter;

import br.com.erp.api.notification.application.service.WhatsAppNotificationService;
import br.com.erp.api.order.application.port.out.WhatsAppMessagePort;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import org.springframework.stereotype.Component;

/**
 * Resolve a porta de WhatsApp do módulo order delegando ao serviço de notificação.
 */
@Component
public class WhatsAppMessageAdapter implements WhatsAppMessagePort {

    private final WhatsAppNotificationService whatsAppNotificationService;

    public WhatsAppMessageAdapter(WhatsAppNotificationService whatsAppNotificationService) {
        this.whatsAppNotificationService = whatsAppNotificationService;
    }

    @Override
    public String buildUrl(Order order, Customer customer) {
        return whatsAppNotificationService.buildUrl(order, customer);
    }

    @Override
    public String buildMessageText(Order order, Customer customer) {
        return whatsAppNotificationService.buildMessageText(order, customer);
    }
}
