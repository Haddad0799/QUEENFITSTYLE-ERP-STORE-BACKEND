package br.com.erp.api.catalog.domain.port;

import java.util.List;

public interface CatalogRevalidationPort {

    void revalidate(List<String> tags);
}

