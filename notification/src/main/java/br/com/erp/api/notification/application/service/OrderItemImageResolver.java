package br.com.erp.api.notification.application.service;

import br.com.erp.api.order.application.port.out.ImageUrlResolverPort;
import br.com.erp.api.order.application.query.OrderAdminQueryRepository;
import br.com.erp.api.order.application.query.projection.OrderItemRow;
import br.com.erp.api.order.domain.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolve as URLs públicas das imagens dos itens de um pedido, indexadas por
 * {@link OrderItem#getId()}.
 *
 * O domínio {@code OrderItem} não carrega a image_key, então a resolução passa pela mesma via
 * de leitura usada pela API admin ({@link OrderAdminQueryRepository#findItemsByOrderId} +
 * {@link ImageUrlResolverPort}). Compartilhado pelos listeners de notificação que exibem as
 * imagens dos produtos no e-mail.
 */
@Service
public class OrderItemImageResolver {

    private final OrderAdminQueryRepository orderQueryRepository;
    private final ImageUrlResolverPort imageUrlResolver;

    public OrderItemImageResolver(OrderAdminQueryRepository orderQueryRepository,
                                  ImageUrlResolverPort imageUrlResolver) {
        this.orderQueryRepository = orderQueryRepository;
        this.imageUrlResolver     = imageUrlResolver;
    }

    /**
     * Mapeia o id de cada item do pedido para a URL pública de sua imagem (cor da SKU, com
     * fallback para a imagem principal do produto — vide a query). Valores nulos são
     * preservados: o template do e-mail decide o placeholder.
     */
    public Map<Long, String> resolveByItemId(Long orderId) {
        List<OrderItemRow> rows = orderQueryRepository.findItemsByOrderId(orderId);
        Map<Long, String> urls = new HashMap<>();
        for (OrderItemRow row : rows) {
            urls.put(row.itemId(), imageUrlResolver.resolve(row.imageKey()));
        }
        return urls;
    }
}
