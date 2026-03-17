package br.com.erp.api.product.application.provider;

import br.com.erp.api.shared.application.projection.IdNameProjection;

import java.util.Set;

public interface SizeProvider {
    Set<Long> findAllIds();
    Set<IdNameProjection> findByIds(Set<Long> ids);
}
