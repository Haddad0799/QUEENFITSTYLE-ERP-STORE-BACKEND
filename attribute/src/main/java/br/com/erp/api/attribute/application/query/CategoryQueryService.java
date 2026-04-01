package br.com.erp.api.attribute.application.query;

import br.com.erp.api.attribute.presentation.dto.category.response.CategoryDetailsDTO;

import java.util.List;
import java.util.Optional;

public interface CategoryQueryService {
    List<CategoryDetailsDTO> findAll();
    CategoryDetailsDTO findById(Long id);
    boolean existsActiveById(Long id);
    Optional<CategoryDetailsDTO> findByName(String name);
}

