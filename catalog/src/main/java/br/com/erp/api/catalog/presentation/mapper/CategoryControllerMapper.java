package br.com.erp.api.catalog.presentation.mapper;

import br.com.erp.api.catalog.application.command.CreateCategoryCommand;
import br.com.erp.api.catalog.application.output.CategoryOutput;
import br.com.erp.api.catalog.presentation.dto.category.request.CreateCategoryDTO;
import br.com.erp.api.catalog.presentation.dto.category.response.CategoryDetailsDTO;
import org.springframework.stereotype.Component;

@Component
public class CategoryControllerMapper {

    public CreateCategoryCommand toCreateCommand(CreateCategoryDTO dto) {
        return new CreateCategoryCommand(dto.name());
    }

    public CategoryDetailsDTO toDetailsDTO(CategoryOutput output) {
        return new CategoryDetailsDTO(
                output.id(),
                output.name(),
                output.active()
        );
    }

}

