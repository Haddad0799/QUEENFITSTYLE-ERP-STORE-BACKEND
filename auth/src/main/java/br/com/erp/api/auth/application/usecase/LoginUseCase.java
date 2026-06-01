package br.com.erp.api.auth.application.usecase;

import br.com.erp.api.auth.domain.exception.InvalidCredentialsException;
import br.com.erp.api.auth.domain.port.UserRepositoryPort;
import br.com.erp.api.auth.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final JwtService         jwtService;

    public LoginUseCase(UserRepositoryPort userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
    }

    public String execute(String email, String rawPassword) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(user);
    }
}
