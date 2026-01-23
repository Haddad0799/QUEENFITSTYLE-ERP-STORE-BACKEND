package br.com.erp.api.catalog.presentation.dto.size;

import java.util.List;

public record SizesAdminDetailsDTO(
        List<SizeAdminDetailsDTO> tamanhosDisponiveis
) {
}
