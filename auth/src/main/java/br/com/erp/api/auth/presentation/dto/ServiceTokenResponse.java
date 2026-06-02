package br.com.erp.api.auth.presentation.dto;

public record ServiceTokenResponse(String accessToken, int expiresIn) {}
