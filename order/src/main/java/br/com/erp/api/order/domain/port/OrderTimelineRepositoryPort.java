package br.com.erp.api.order.domain.port;

import br.com.erp.api.order.domain.entity.OrderTimelineEvent;

import java.util.List;

public interface OrderTimelineRepositoryPort {

    OrderTimelineEvent append(OrderTimelineEvent event);

    List<OrderTimelineEvent> findByOrderId(Long orderId);
}
