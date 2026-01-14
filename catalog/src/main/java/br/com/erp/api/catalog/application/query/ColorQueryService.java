package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.output.ColorOutput;

import java.util.List;

public interface ColorQueryService {
    List<ColorOutput> findAll();
}
