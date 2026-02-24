package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.output.ColorOutput;

import java.util.List;
import java.util.Set;

public interface ColorQueryService {
    List<ColorOutput> findAll();
    List<ColorOutput> findByIds(Set<Long> ids);
}
