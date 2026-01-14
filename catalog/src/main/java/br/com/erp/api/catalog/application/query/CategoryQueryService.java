package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.presentation.dto.category.response.CategoryDetailsDTO;

import java.util.List;

public interface CategoryQueryService {
    List<CategoryDetailsDTO> findAll();
    CategoryDetailsDTO findById(Long id);
}
