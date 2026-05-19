package br.com.erp.api.order.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @Valid
        @NotNull(message = "Dados do cliente são obrigatórios")
        CustomerRequest customer,

        @NotEmpty(message = "Pelo menos uma reserva é necessária")
        List<@NotBlank String> reservations,

        String notes
) {}
