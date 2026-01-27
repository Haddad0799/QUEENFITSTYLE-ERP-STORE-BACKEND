package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.catalog.application.query.SizeQueryService;
import br.com.erp.api.catalog.application.output.SizeOutput;
import br.com.erp.api.product.domain.port.SizeLookupPort;
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
}
