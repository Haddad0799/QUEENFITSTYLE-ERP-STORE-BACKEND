package br.com.erp.api.auth.infrastructure.persistence;

import br.com.erp.api.auth.domain.entity.User;
import br.com.erp.api.auth.domain.enumerated.UserRole;
import br.com.erp.api.auth.domain.port.UserRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserJdbiRepository implements UserRepositoryPort {

    private final Jdbi jdbi;

    public UserJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, email, password_hash, role, name, phone
                        FROM users
                        WHERE email = :email
                        """)
                        .bind("email", email)
                        .map((rs, ctx) -> User.restore(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password_hash"),
                                UserRole.valueOf(rs.getString("role")),
                                rs.getString("name"),
                                rs.getString("phone")
                        ))
                        .findOne()
        );
    }

    @Override
    public Optional<User> findById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, email, password_hash, role, name, phone
                        FROM users
                        WHERE id = :id
                        """)
                        .bind("id", id)
                        .map((rs, ctx) -> User.restore(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password_hash"),
                                UserRole.valueOf(rs.getString("role")),
                                rs.getString("name"),
                                rs.getString("phone")
                        ))
                        .findOne()
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(1) FROM users WHERE email = :email")
                        .bind("email", email)
                        .mapTo(Long.class)
                        .one()
        ) > 0;
    }

    @Override
    public void save(User user) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO users (email, password_hash, role, name, phone)
                        VALUES (:email, :passwordHash, :role, :name, :phone)
                        """)
                        .bind("email",        user.getEmail())
                        .bind("passwordHash", user.getPasswordHash())
                        .bind("role",         user.getRole().name())
                        .bind("name",         user.getName())
                        .bind("phone",        user.getPhone())
                        .execute()
        );
    }

    @Override
    public void updatePassword(Long id, String passwordHash) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE users
                        SET password_hash = :passwordHash,
                            updated_at    = NOW()
                        WHERE id = :id
                        """)
                        .bind("id",           id)
                        .bind("passwordHash", passwordHash)
                        .execute()
        );
    }

    @Override
    public void updateEmail(Long id, String email) {
        jdbi.useHandle(handle ->
                handle.createUpdate("""
                        UPDATE users
                        SET email      = :email,
                            updated_at = NOW()
                        WHERE id = :id
                        """)
                        .bind("id",    id)
                        .bind("email", email)
                        .execute()
        );
    }
}
