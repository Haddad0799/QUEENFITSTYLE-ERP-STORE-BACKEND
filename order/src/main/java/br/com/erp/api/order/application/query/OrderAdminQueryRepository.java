package br.com.erp.api.order.application.query;

import br.com.erp.api.order.application.query.filter.OrderAdminFilter;
import br.com.erp.api.order.application.query.projection.OrderItemRow;
import br.com.erp.api.order.application.query.projection.OrderReservationRow;
import br.com.erp.api.order.presentation.dto.response.OrderCustomerDTO;
import br.com.erp.api.order.presentation.dto.response.OrderSummaryDTO;
import br.com.erp.api.order.presentation.dto.response.OrderTimelineDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderAdminQueryRepository {

    Page<OrderSummaryDTO> findAll(OrderAdminFilter filter, Pageable pageable);

    Optional<OrderCustomerDTO> findCustomerByOrderId(Long orderId);

    List<OrderItemRow> findItemsByOrderId(Long orderId);

    List<OrderReservationRow> findReservationsByOrderId(Long orderId);

    List<OrderTimelineDTO> findTimelineByOrderId(Long orderId);
}
