package br.com.erp.api.attribute.presentation.controller.category;

import br.com.erp.api.attribute.application.command.CreateCategoryCommand;
import br.com.erp.api.attribute.application.output.CategoryOutput;
import br.com.erp.api.attribute.application.query.CategoryQueryService;
import br.com.erp.api.attribute.application.usecase.ActivateCategoryUseCase;
import br.com.erp.api.attribute.application.usecase.CreateCategoryUseCase;
import br.com.erp.api.attribute.application.usecase.DeactivateCategoryUseCase;
import br.com.erp.api.attribute.application.usecase.RenameCategoryUseCase;
import br.com.erp.api.attribute.presentation.dto.category.request.CreateCategoryDTO;
import br.com.erp.api.attribute.presentation.dto.category.request.RenameCategoryDTO;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoriesDetailsDTO;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoryDetailsDTO;
import br.com.erp.api.attribute.presentation.mapper.CategoryControllerMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

//Controller utilizado pelo ERP, responsável por administração do ecommerce.
@RestController
@RequestMapping("/erp/categories")
public class CategoryBackOfficeController {
    private final CreateCategoryUseCase createCategoryUseCase;
    private final RenameCategoryUseCase renameCategoryUseCase;
    private final ActivateCategoryUseCase activateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;
    private final CategoryQueryService categoryQueryService;
    private final CategoryControllerMapper mapper;

    public CategoryBackOfficeController(CreateCategoryUseCase createCategoryUseCase, RenameCategoryUseCase renameCategoryUseCase, ActivateCategoryUseCase activateCategoryUseCase, DeactivateCategoryUseCase deactivateCategoryUseCase, CategoryQueryService categoryQueryService, CategoryControllerMapper mapper) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.renameCategoryUseCase = renameCategoryUseCase;
        this.activateCategoryUseCase = activateCategoryUseCase;
        this.deactivateCategoryUseCase = deactivateCategoryUseCase;
        this.categoryQueryService = categoryQueryService;
        this.mapper = mapper;
    }
    @PostMapping
    public ResponseEntity<CategoryDetailsDTO> createCategory(@RequestBody CreateCategoryDTO dto) {
        CreateCategoryCommand command = mapper.toCreateCommand(dto);
        CategoryOutput output = createCategoryUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(output.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(mapper.toDetailsDTO(output));
    }

    @GetMapping
    public ResponseEntity<CategoriesDetailsDTO> getAllCategories() {
        return ResponseEntity.ok(new CategoriesDetailsDTO(categoryQueryService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDetailsDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryQueryService.findById(id));
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<Void> rename(
            @PathVariable Long id,
            @RequestBody RenameCategoryDTO dto
    ) {
        renameCategoryUseCase.execute(id, dto.name());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        activateCategoryUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateCategoryUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

}

