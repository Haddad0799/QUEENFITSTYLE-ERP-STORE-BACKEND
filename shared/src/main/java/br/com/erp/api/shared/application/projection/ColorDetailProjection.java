package br.com.erp.api.shared.application.projection;

public record ColorDetailProjection(
        Long id,
        String name,
        String hexCode
) {}

