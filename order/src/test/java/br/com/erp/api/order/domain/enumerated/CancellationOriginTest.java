package br.com.erp.api.order.domain.enumerated;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CancellationOriginTest {

    @Test
    void fromActor_comActorCustomer_retornaCustomer() {
        assertThat(CancellationOrigin.fromActor("customer")).isEqualTo(CancellationOrigin.CUSTOMER);
    }

    @Test
    void fromActor_ignoraCaixa() {
        assertThat(CancellationOrigin.fromActor("Customer")).isEqualTo(CancellationOrigin.CUSTOMER);
        assertThat(CancellationOrigin.fromActor("CUSTOMER")).isEqualTo(CancellationOrigin.CUSTOMER);
    }

    @Test
    void fromActor_comActorAdmin_retornaAdmin() {
        assertThat(CancellationOrigin.fromActor("admin")).isEqualTo(CancellationOrigin.ADMIN);
    }

    @Test
    void fromActor_comActorDesconhecidoOuNulo_retornaAdmin() {
        assertThat(CancellationOrigin.fromActor("vendedora")).isEqualTo(CancellationOrigin.ADMIN);
        assertThat(CancellationOrigin.fromActor(null)).isEqualTo(CancellationOrigin.ADMIN);
    }
}
