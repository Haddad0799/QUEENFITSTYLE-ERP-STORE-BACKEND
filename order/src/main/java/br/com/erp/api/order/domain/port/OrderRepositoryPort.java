package br.com.erp.api.order.domain.port;

import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.enumerated.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

    Order saveWithItems(Order order);

    void updateWhatsappMessage(Long orderId, String message);

    void updateStatus(Long orderId, OrderStatus status);

    Optional<Order> findById(Long id);

    List<Order> findExpiredPending(LocalDateTime expiredBefore);
}
