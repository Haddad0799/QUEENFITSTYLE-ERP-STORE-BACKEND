package br.com.erp.api.settings.domain.entity;

import br.com.erp.api.settings.domain.exception.InvalidStoreSettingsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreSettingsTest {

    @Test
    void create_comDadosValidos_constroiEntidade() {
        StoreSettings settings = StoreSettings.create("5511999998888", "loja@exemplo.com");

        assertThat(settings.getWhatsappPhone()).isEqualTo("5511999998888");
        assertThat(settings.getNotificationEmail()).isEqualTo("loja@exemplo.com");
    }

    @Test
    void create_comTelefoneNaoNumerico_lancaExcecao() {
        assertThatThrownBy(() -> StoreSettings.create("(11) 99999-8888", "loja@exemplo.com"))
                .isInstanceOf(InvalidStoreSettingsException.class);
    }

    @Test
    void create_comEmailInvalido_lancaExcecao() {
        assertThatThrownBy(() -> StoreSettings.create("5511999998888", "nao-eh-email"))
                .isInstanceOf(InvalidStoreSettingsException.class);
    }

    @Test
    void update_aplicaApenasCamposInformados() {
        StoreSettings settings = StoreSettings.restore(1L, "5511999998888", "antigo@exemplo.com");

        settings.update(null, "novo@exemplo.com");

        assertThat(settings.getWhatsappPhone()).isEqualTo("5511999998888");
        assertThat(settings.getNotificationEmail()).isEqualTo("novo@exemplo.com");
    }

    @Test
    void update_comCampoInvalido_lancaExcecaoEnaoAltera() {
        StoreSettings settings = StoreSettings.restore(1L, "5511999998888", "loja@exemplo.com");

        assertThatThrownBy(() -> settings.update("abc", null))
                .isInstanceOf(InvalidStoreSettingsException.class);
        assertThat(settings.getWhatsappPhone()).isEqualTo("5511999998888");
    }
}
