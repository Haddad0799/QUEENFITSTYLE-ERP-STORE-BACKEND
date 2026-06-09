package br.com.erp.api.order.domain.port;

import br.com.erp.api.order.domain.entity.Customer;

import java.util.Optional;

public interface CustomerRepositoryPort {

    Customer upsertByPhone(Customer customer);

    Optional<Customer> findById(Long id);
}
