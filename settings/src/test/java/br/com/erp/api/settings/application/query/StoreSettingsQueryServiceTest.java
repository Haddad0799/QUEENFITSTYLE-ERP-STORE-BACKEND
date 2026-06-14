package br.com.erp.api.settings.application.query;

import br.com.erp.api.settings.domain.entity.StoreSettings;
import br.com.erp.api.settings.domain.port.StoreSettingsRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreSettingsQueryServiceTest {

    private final StoreSettingsRepositoryPort repository = mock(StoreSettingsRepositoryPort.class);

    @Test
    void current_comLinhaPersistida_retornaValoresDoBanco() {
        when(repository.find()).thenReturn(
                Optional.of(StoreSettings.restore(1L, "5511111111111", "banco@exemplo.com")));
        var service = new StoreSettingsQueryService(repository, "5599999999999", "env@exemplo.com");

        StoreSettingsView view = service.current();

        assertThat(view.whatsappPhone()).isEqualTo("5511111111111");
        assertThat(view.notificationEmail()).isEqualTo("banco@exemplo.com");
    }

    @Test
    void current_comTabelaVazia_caiNoFallbackDaEnv() {
        when(repository.find()).thenReturn(Optional.empty());
        var service = new StoreSettingsQueryService(repository, "5599999999999", "env@exemplo.com");

        StoreSettingsView view = service.current();

        assertThat(view.whatsappPhone()).isEqualTo("5599999999999");
        assertThat(view.notificationEmail()).isEqualTo("env@exemplo.com");
    }

    @Test
    void current_comCampoEmBranco_usaFallbackApenasNaqueleCampo() {
        when(repository.find()).thenReturn(
                Optional.of(StoreSettings.restore(1L, null, "banco@exemplo.com")));
        var service = new StoreSettingsQueryService(repository, "5599999999999", "env@exemplo.com");

        StoreSettingsView view = service.current();

        assertThat(view.whatsappPhone()).isEqualTo("5599999999999");
        assertThat(view.notificationEmail()).isEqualTo("banco@exemplo.com");
    }

    @Test
    void current_semBancoNemEnv_retornaNulo() {
        when(repository.find()).thenReturn(Optional.empty());
        var service = new StoreSettingsQueryService(repository, "", "");

        StoreSettingsView view = service.current();

        assertThat(view.whatsappPhone()).isNull();
        assertThat(view.notificationEmail()).isNull();
    }
}
