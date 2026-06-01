package br.com.erp.api.auth.application.usecase;

import br.com.erp.api.auth.domain.exception.InvalidCredentialsException;
import br.com.erp.api.auth.domain.port.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChangePasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder    passwordEncoder;

    public ChangePasswordUseCase(UserRepositoryPort userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void execute(Long userId, String currentPassword, String newPassword) {
        var user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String newHash = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(userId, newHash);
    }
}
