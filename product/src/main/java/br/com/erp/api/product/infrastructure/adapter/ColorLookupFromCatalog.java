package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.catalog.application.output.ColorOutput;
import br.com.erp.api.catalog.application.query.ColorQueryService;
import br.com.erp.api.product.domain.port.ColorLookupPort;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ColorLookupFromCatalog implements ColorLookupPort{

    private final ColorQueryService colorQueryService;

    public ColorLookupFromCatalog(ColorQueryService colorQueryService) {
        this.colorQueryService = colorQueryService;
    }

    @Override
    public Set<Long> findAllIds() {
        return colorQueryService.findAll()
                .stream()
                .map(ColorOutput::id)
                .collect(Collectors.toUnmodifiableSet());
    }

}
