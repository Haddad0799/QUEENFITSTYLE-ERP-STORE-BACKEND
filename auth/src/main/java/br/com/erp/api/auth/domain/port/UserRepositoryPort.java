package br.com.erp.api.auth.domain.port;

import br.com.erp.api.auth.domain.entity.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    void save(User user);

    void updatePassword(Long id, String passwordHash);

    void updateEmail(Long id, String email);
}
