package br.com.erp.api.auth.presentation.controller;

import br.com.erp.api.auth.application.usecase.ChangePasswordUseCase;
import br.com.erp.api.auth.application.usecase.LoginUseCase;
import br.com.erp.api.auth.application.usecase.RegisterCustomerUseCase;
import br.com.erp.api.auth.presentation.dto.ChangePasswordRequest;
import br.com.erp.api.auth.presentation.dto.LoginRequest;
import br.com.erp.api.auth.presentation.dto.LoginResponse;
import br.com.erp.api.auth.presentation.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase            loginUseCase;
    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final ChangePasswordUseCase   changePasswordUseCase;

    public AuthController(LoginUseCase loginUseCase,
                          RegisterCustomerUseCase registerCustomerUseCase,
                          ChangePasswordUseCase changePasswordUseCase) {
        this.loginUseCase            = loginUseCase;
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.changePasswordUseCase   = changePasswordUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        registerCustomerUseCase.execute(
                request.email(), request.password(), request.name(), request.phone()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        changePasswordUseCase.execute(Long.parseLong(userId),
                request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
