package br.com.erp.api.auth.application.usecase;

import br.com.erp.api.auth.domain.exception.EmailAlreadyInUseException;
import br.com.erp.api.auth.domain.exception.InvalidCredentialsException;
import br.com.erp.api.auth.domain.port.UserRepositoryPort;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChangeEmailUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder    passwordEncoder;

    public ChangeEmailUseCase(UserRepositoryPort userRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void execute(Long userId, String newEmail, String currentPassword) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuária", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            return;
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyInUseException(newEmail);
        }

        user.changeEmail(newEmail);
        userRepository.updateEmail(userId, newEmail);
    }
}
