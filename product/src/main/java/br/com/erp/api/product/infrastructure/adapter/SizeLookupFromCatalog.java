package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.catalog.application.output.SizeOutput;
import br.com.erp.api.catalog.application.query.SizeQueryService;
import br.com.erp.api.product.domain.port.SizeLookupPort;
import br.com.erp.api.shared.application.projection.IdNameProjection;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SizeLookupFromCatalog implements SizeLookupPort {

    private final SizeQueryService sizeQueryService;

    public SizeLookupFromCatalog(SizeQueryService sizeQueryService) {
        this.sizeQueryService = sizeQueryService;
    }

    @Override
    public Set<Long> findAllIds() {
        return sizeQueryService.findAll()
                .stream()
                .map(SizeOutput::id)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<IdNameProjection> findByIds(Set<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        return sizeQueryService.findByIds(ids)
                .stream()
                .map(size -> new IdNameProjection(
                        size.id(),
                        size.label()
                ))
                .collect(Collectors.toUnmodifiableSet());
    }
}
