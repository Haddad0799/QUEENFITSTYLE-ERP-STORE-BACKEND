package br.com.erp.api.auth.infrastructure.seed;

import br.com.erp.api.auth.domain.entity.User;
import br.com.erp.api.auth.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder    passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminUserSeeder(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (userRepository.existsByEmail(adminEmail)) return;

        String hash  = passwordEncoder.encode(adminPassword);
        User   admin = User.createAdmin(adminEmail, hash);
        userRepository.save(admin);
        log.info("Admin user created: {}", adminEmail);
    }
}
