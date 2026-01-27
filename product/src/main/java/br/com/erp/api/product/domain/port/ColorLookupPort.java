package br.com.erp.api.product.domain.port;

import java.util.Set;

public interface ColorLookupPort {
    Set<Long> findAllIds();
}
