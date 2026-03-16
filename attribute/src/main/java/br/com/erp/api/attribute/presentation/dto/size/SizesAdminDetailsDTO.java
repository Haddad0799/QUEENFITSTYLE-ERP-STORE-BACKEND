package br.com.erp.api.attribute.presentation.dto.size;

import java.util.List;

public record SizesAdminDetailsDTO(
        List<SizeAdminDetailsDTO> tamanhosDisponiveis
) {
}

