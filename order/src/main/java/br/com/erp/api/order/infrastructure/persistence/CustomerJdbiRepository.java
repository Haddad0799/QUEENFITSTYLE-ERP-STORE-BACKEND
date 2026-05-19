package br.com.erp.api.order.infrastructure.persistence;

import br.com.erp.api.order.domain.entity.Customer;
import br.com.erp.api.order.domain.port.CustomerRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerJdbiRepository implements CustomerRepositoryPort {

    private final Jdbi jdbi;

    public CustomerJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Customer upsertByPhone(Customer customer) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        INSERT INTO customers (name, phone, city, created_at, updated_at)
                        VALUES (:name, :phone, :city, now(), now())
                        ON CONFLICT (phone) DO UPDATE
                            SET name       = EXCLUDED.name,
                                city       = EXCLUDED.city,
                                updated_at = now()
                        RETURNING id, name, phone, city, created_at, updated_at
                        """)
                        .bind("name",  customer.getName())
                        .bind("phone", customer.getPhone())
                        .bind("city",  customer.getCity())
                        .map((rs, ctx) -> Customer.restore(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("phone"),
                                rs.getString("city"),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getTimestamp("updated_at").toLocalDateTime()
                        ))
                        .one()
        );
    }
}
