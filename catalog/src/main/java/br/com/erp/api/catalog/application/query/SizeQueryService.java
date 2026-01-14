package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.output.SizeOutput;

import java.util.List;

public interface SizeQueryService {
    List<SizeOutput> findAll();
}
