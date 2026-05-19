package br.com.erp.api.order.domain.entity;

import java.time.LocalDateTime;

public class Customer {

    private Long id;
    private String name;
    private final String phone;
    private String city;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer(String name, String phone, String city) {
        this.name      = name;
        this.phone     = phone;
        this.city      = city;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Customer restore(Long id, String name, String phone, String city,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        Customer c = new Customer(name, phone, city);
        c.id        = id;
        c.updatedAt = updatedAt;
        return c;
    }

    public Long getId()                 { return id; }
    public String getName()             { return name; }
    public String getPhone()            { return phone; }
    public String getCity()             { return city; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
