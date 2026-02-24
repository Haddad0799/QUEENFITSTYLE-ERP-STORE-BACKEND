package br.com.erp.api.product.domain.port;

import br.com.erp.api.shared.application.projection.IdNameProjection;

import java.util.Set;

public interface SizeLookupPort {
    Set<Long> findAllIds();
    Set<IdNameProjection> findByIds(Set<Long> ids);
}
