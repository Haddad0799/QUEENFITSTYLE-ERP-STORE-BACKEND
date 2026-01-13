package br.com.erp.api.catalog.presentation.controller.category;

import br.com.erp.api.catalog.application.command.category.CreateCategoryCommand;
import br.com.erp.api.catalog.application.output.category.CategoryCreatedOutput;
import br.com.erp.api.catalog.application.query.category.CategoryQueryService;
import br.com.erp.api.catalog.application.usecase.category.ActivateCategoryUseCase;
import br.com.erp.api.catalog.application.usecase.category.CreateCategoryUseCase;
import br.com.erp.api.catalog.application.usecase.category.DeactivateCategoryUseCase;
import br.com.erp.api.catalog.application.usecase.category.RenameCategoryUseCase;
import br.com.erp.api.catalog.presentation.dto.category.request.CreateCategoryDTO;
import br.com.erp.api.catalog.presentation.dto.category.request.RenameCategoryDTO;
import br.com.erp.api.catalog.presentation.dto.category.response.CategoriesDetailsDTO;
import br.com.erp.api.catalog.presentation.dto.category.response.CategoryDetailsDTO;
import br.com.erp.api.catalog.presentation.mapper.category.CategoryControllerMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final RenameCategoryUseCase renameCategoryUseCase;
    private final ActivateCategoryUseCase activateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;
    private final CategoryQueryService categoryQueryService;
    private final CategoryControllerMapper mapper;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase, RenameCategoryUseCase renameCategoryUseCase, ActivateCategoryUseCase activateCategoryUseCase, DeactivateCategoryUseCase deactivateCategoryUseCase, CategoryQueryService categoryQueryService, CategoryControllerMapper mapper) {
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
        CategoryCreatedOutput output = createCategoryUseCase.execute(command);

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
