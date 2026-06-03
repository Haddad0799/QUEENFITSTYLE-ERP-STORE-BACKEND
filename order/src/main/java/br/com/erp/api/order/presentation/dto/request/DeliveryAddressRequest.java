package br.com.erp.api.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Endereço de entrega informado no checkout. {@code city} e {@code state}
 * não são recebidos do cliente — são fixados no backend ("Pirenópolis"/"GO"),
 * pois a loja entrega apenas nessa cidade.
 */
public record DeliveryAddressRequest(
        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "^72980.*", message = "Entregamos apenas em Pirenópolis - GO (CEP deve começar com 72980)")
        String cep,

        @NotBlank(message = "Logradouro é obrigatório")
        String street,

        @NotBlank(message = "Número é obrigatório")
        String number,

        String complement,

        @NotBlank(message = "Bairro é obrigatório")
        String neighborhood
) {}
