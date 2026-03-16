package br.com.erp.api.attribute.application.query;

import br.com.erp.api.attribute.application.output.SizeOutput;

import java.util.List;
import java.util.Set;

public interface SizeQueryService {
    List<SizeOutput> findAll();
    List<SizeOutput> findByIds(Set<Long> ids);
}

