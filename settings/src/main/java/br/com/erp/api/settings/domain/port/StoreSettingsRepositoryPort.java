package br.com.erp.api.settings.domain.port;

import br.com.erp.api.settings.domain.entity.StoreSettings;

import java.util.Optional;

/**
 * Porta de persistência da configuração singleton da loja (sempre uma única linha, id = 1).
 */
public interface StoreSettingsRepositoryPort {

    Optional<StoreSettings> find();

    void insert(StoreSettings settings);

    void update(StoreSettings settings);
}
