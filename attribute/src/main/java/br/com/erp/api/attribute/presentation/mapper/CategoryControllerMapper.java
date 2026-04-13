package br.com.erp.api.attribute.presentation.mapper;

import br.com.erp.api.attribute.application.command.CreateCategoryCommand;
import br.com.erp.api.attribute.application.output.CategoryOutput;
import br.com.erp.api.attribute.presentation.dto.category.request.CreateCategoryDTO;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoryDetailsDTO;
import org.springframework.stereotype.Component;

@Component
public class CategoryControllerMapper {

    public CreateCategoryCommand toCreateCommand(CreateCategoryDTO dto) {
        return new CreateCategoryCommand(dto.name(), dto.parentId());
    }

    public CategoryDetailsDTO toDetailsDTO(CategoryOutput output) {
        return new CategoryDetailsDTO(
                output.id(),
                output.name(),
                output.normalizedName(),
                output.active(),
                output.parentId()
        );
    }
}
