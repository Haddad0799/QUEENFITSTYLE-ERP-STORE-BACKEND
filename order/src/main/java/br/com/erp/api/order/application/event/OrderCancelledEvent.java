package br.com.erp.api.order.application.event;

import br.com.erp.api.order.domain.entity.Order;
import br.com.erp.api.order.domain.enumerated.CancellationOrigin;

/**
 * Evento publicado quando um pedido é cancelado (CancelOrderUseCase). Consumido de forma
 * assíncrona após o commit para disparar efeitos colaterais desacoplados:
 * <ul>
 *   <li>avisar a cliente, por e-mail, que o pedido foi cancelado;</li>
 *   <li>avisar a dona da loja quando o cancelamento parte da própria cliente
 *       ({@link CancellationOrigin#CUSTOMER}).</li>
 * </ul>
 *
 * O {@code origin} carrega quem disparou o cancelamento para que cada listener decida se deve
 * reagir.
 */
public record OrderCancelledEvent(Order order, CancellationOrigin origin) {}
