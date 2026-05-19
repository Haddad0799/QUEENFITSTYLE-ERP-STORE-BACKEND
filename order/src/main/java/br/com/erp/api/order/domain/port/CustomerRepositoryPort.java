package br.com.erp.api.order.domain.port;

import br.com.erp.api.order.domain.entity.Customer;

public interface CustomerRepositoryPort {

    Customer upsertByPhone(Customer customer);
}
