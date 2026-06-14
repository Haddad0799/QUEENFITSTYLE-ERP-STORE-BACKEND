package br.com.erp.api.auth.presentation.dto;

import br.com.erp.api.auth.domain.entity.User;

public record MeResponse(
        Long id,
        String email,
        String name,
        String role
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }
}
