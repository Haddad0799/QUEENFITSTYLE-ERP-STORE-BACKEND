package br.com.erp.api.attribute.application.query;

import br.com.erp.api.attribute.application.output.ColorOutput;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ColorQueryService {
    List<ColorOutput> findAll();
    List<ColorOutput> findByIds(Set<Long> ids);
    Optional<ColorOutput> findByName(String name);
}

