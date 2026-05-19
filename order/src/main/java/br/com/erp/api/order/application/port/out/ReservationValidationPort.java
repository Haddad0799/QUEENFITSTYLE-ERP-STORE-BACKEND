package br.com.erp.api.order.application.port.out;

import java.util.List;
import java.util.UUID;

public interface ReservationValidationPort {

    List<ReservationDetail> fetchDetails(List<UUID> reservationIds);
}
