package br.com.erp.api.auth.application.usecase;

import br.com.erp.api.auth.domain.entity.User;
import br.com.erp.api.auth.domain.port.UserRepositoryPort;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUserUseCase {

    private final UserRepositoryPort userRepository;

    public GetCurrentUserUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuária", userId));
    }
}
