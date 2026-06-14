package br.com.erp.api.auth.domain.entity;

import br.com.erp.api.auth.domain.enumerated.UserRole;

public class User {

    private Long id;
    private String email;
    private String passwordHash;
    private final UserRole role;
    private final String name;
    private final String phone;

    private User(String email, String passwordHash, UserRole role, String name, String phone) {
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.name         = name;
        this.phone        = phone;
    }

    public static User createAdmin(String email, String passwordHash) {
        return new User(email, passwordHash, UserRole.ADMIN, null, null);
    }

    public static User createCustomer(String email, String passwordHash, String name, String phone) {
        return new User(email, passwordHash, UserRole.CUSTOMER, name, phone);
    }

    public static User restore(Long id, String email, String passwordHash,
                               UserRole role, String name, String phone) {
        User user = new User(email, passwordHash, role, name, phone);
        user.id = id;
        return user;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void changeEmail(String newEmail) {
        this.email = newEmail;
    }

    public Long getId()           { return id; }
    public String getEmail()      { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole()     { return role; }
    public String getName()       { return name; }
    public String getPhone()      { return phone; }
}
