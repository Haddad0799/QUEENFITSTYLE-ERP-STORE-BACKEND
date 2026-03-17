package br.com.erp.api.product.application.provider;

import br.com.erp.api.shared.application.projection.ColorDetailProjection;
import br.com.erp.api.shared.application.projection.IdNameProjection;

import java.util.Set;

public interface ColorProvider {
    Set<Long> findAllIds();
    Set<IdNameProjection> findByIds(Set<Long> ids);
    Set<ColorDetailProjection> findWithHexByIds(Set<Long> ids);
}
