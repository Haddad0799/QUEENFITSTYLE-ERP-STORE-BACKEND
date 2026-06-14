package br.com.erp.api.notification.listener;

import br.com.erp.api.notification.application.port.OrderCancelledStoreOwnerNotifier;
import br.com.erp.api.notification.application.service.OrderItemImageResolver;
import br.com.erp.api.order.application.event.OrderCancelledEvent;
import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.enumerated.CancellationOrigin;
import br.com.erp.api.order.domain.enumerated.OrderStatus;
import br.com.erp.api.order.domain.port.CustomerRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderCancelledStoreOwnerNotificationListenerTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<OrderCancelledStoreOwnerNotifier> notifierProvider = mock(ObjectProvider.class);
    private final OrderCancelledStoreOwnerNotifier notifier = mock(OrderCancelledStoreOwnerNotifier.class);
    private final CustomerRepositoryPort customerRepository = mock(CustomerRepositoryPort.class);
    private final OrderItemImageResolver imageResolver = mock(OrderItemImageResolver.class);

    private final OrderCancelledStoreOwnerNotificationListener listener =
            new OrderCancelledStoreOwnerNotificationListener(notifierProvider, customerRepository, imageResolver);

    @Test
    void onOrderCancelled_origemCliente_avisaDona() throws Exception {
        Order order = order();
        Customer customer = customer();
        when(notifierProvider.getIfAvailable()).thenReturn(notifier);
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));
        when(imageResolver.resolveByItemId(7L)).thenReturn(Map.of());

        listener.onOrderCancelled(new OrderCancelledEvent(order, CancellationOrigin.CUSTOMER));

        verify(notifier, times(1)).notify(order, customer, Map.of());
    }

    @Test
    void onOrderCancelled_origemVendedora_naoAvisaDona() {
        listener.onOrderCancelled(new OrderCancelledEvent(order(), CancellationOrigin.ADMIN));

        verifyNoInteractions(notifierProvider, customerRepository, imageResolver);
    }

    @Test
    void onOrderCancelled_semNotifierConfigurado_naoBuscaClienteNemFalha() throws Exception {
        when(notifierProvider.getIfAvailable()).thenReturn(null);

        listener.onOrderCancelled(new OrderCancelledEvent(order(), CancellationOrigin.CUSTOMER));

        verifyNoInteractions(customerRepository, imageResolver);
        verify(notifier, never()).notify(any(), any(), any());
    }

    @Test
    void onOrderCancelled_falhaNoEnvio_naoPropaga() throws Exception {
        when(notifierProvider.getIfAvailable()).thenReturn(notifier);
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer()));
        when(imageResolver.resolveByItemId(anyLong())).thenReturn(Map.of());
        org.mockito.Mockito.doThrow(new RuntimeException("Resend fora do ar"))
                .when(notifier).notify(any(), any(), any());

        // Não deve lançar: o cancelamento já está consolidado quando o listener roda.
        listener.onOrderCancelled(new OrderCancelledEvent(order(), CancellationOrigin.CUSTOMER));

        verify(notifier, times(1)).notify(any(), any(), any());
    }

    private Order order() {
        return Order.restore(7L, 42L, OrderStatus.CANCELLED, BigDecimal.TEN, null, null,
                null, null, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(), null);
    }

    private Customer customer() {
        return Customer.restore(42L, "Maria Silva", "5511999998888", "São Paulo",
                "maria@exemplo.com", LocalDateTime.now(), LocalDateTime.now());
    }
}
