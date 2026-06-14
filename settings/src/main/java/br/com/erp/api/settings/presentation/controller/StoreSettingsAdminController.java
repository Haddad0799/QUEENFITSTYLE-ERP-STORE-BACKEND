package br.com.erp.api.settings.presentation.controller;

import br.com.erp.api.settings.application.query.StoreSettingsQueryService;
import br.com.erp.api.settings.application.usecase.UpdateStoreSettingsUseCase;
import br.com.erp.api.settings.presentation.dto.StoreSettingsResponse;
import br.com.erp.api.settings.presentation.dto.UpdateStoreSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configurações da loja editáveis pela dona (área administrativa do ERP). Protegido
 * por {@code /erp/**} → ROLE_ADMIN no SecurityConfig.
 */
@RestController
@RequestMapping("/erp/settings")
public class StoreSettingsAdminController {

    private final StoreSettingsQueryService  queryService;
    private final UpdateStoreSettingsUseCase updateStoreSettingsUseCase;

    public StoreSettingsAdminController(StoreSettingsQueryService queryService,
                                        UpdateStoreSettingsUseCase updateStoreSettingsUseCase) {
        this.queryService               = queryService;
        this.updateStoreSettingsUseCase = updateStoreSettingsUseCase;
    }

    @GetMapping
    public StoreSettingsResponse get() {
        return StoreSettingsResponse.from(queryService.current());
    }

    @PatchMapping
    public ResponseEntity<StoreSettingsResponse> update(@Valid @RequestBody UpdateStoreSettingsRequest request) {
        updateStoreSettingsUseCase.handle(request.toCommand());
        return ResponseEntity.ok(StoreSettingsResponse.from(queryService.current()));
    }
}
