package br.com.erp.api.auth.application.usecase;

import br.com.erp.api.auth.domain.entity.User;
import br.com.erp.api.auth.domain.exception.EmailAlreadyInUseException;
import br.com.erp.api.auth.domain.port.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterCustomerUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder    passwordEncoder;

    public RegisterCustomerUseCase(UserRepositoryPort userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void execute(String email, String rawPassword, String name, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }

        String hash = passwordEncoder.encode(rawPassword);
        User user   = User.createCustomer(email, hash, name, phone);
        userRepository.save(user);
    }
}
