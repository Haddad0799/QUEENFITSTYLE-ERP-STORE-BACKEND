package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.attribute.application.output.ColorOutput;
import br.com.erp.api.attribute.application.query.ColorQueryService;
import br.com.erp.api.product.domain.port.ColorLookupPort;
import br.com.erp.api.shared.application.projection.ColorDetailProjection;
import br.com.erp.api.shared.application.projection.IdNameProjection;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ColorLookupFromAttribute implements ColorLookupPort{

    private final ColorQueryService colorQueryService;

    public ColorLookupFromAttribute(ColorQueryService colorQueryService) {
        this.colorQueryService = colorQueryService;
    }

    @Override
    public Set<Long> findAllIds() {
        return colorQueryService.findAll()
                .stream()
                .map(ColorOutput::id)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<IdNameProjection> findByIds(Set<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        return colorQueryService.findByIds(ids)
                .stream()
                .map(color -> new IdNameProjection(
                        color.id(),
                        color.name()
                ))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ColorDetailProjection> findWithHexByIds(Set<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        return colorQueryService.findByIds(ids)
                .stream()
                .map(color -> new ColorDetailProjection(
                        color.id(),
                        color.name(),
                        color.hexaCode()
                ))
                .collect(Collectors.toUnmodifiableSet());
    }
}

