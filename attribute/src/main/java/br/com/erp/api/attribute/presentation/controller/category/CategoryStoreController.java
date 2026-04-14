package br.com.erp.api.attribute.presentation.controller.category;

import br.com.erp.api.attribute.application.query.CategoryQueryService;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoryTreeDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/store/categories")
public class CategoryStoreController {

    private final CategoryQueryService categoryQueryService;

    public CategoryStoreController(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @GetMapping
    public List<CategoryTreeDTO> listActiveCategories() {
        return categoryQueryService.findAllActiveAsTree();
    }

}
