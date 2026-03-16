package br.com.erp.api.attribute.presentation.controller.category;

import br.com.erp.api.attribute.application.output.CategoryOutput;
import br.com.erp.api.attribute.application.usecase.CreateCategoryUseCase;
import br.com.erp.api.attribute.application.usecase.ActivateCategoryUseCase;
import br.com.erp.api.attribute.application.usecase.DeactivateCategoryUseCase;
import br.com.erp.api.attribute.application.usecase.RenameCategoryUseCase;
import br.com.erp.api.attribute.presentation.dto.category.request.CreateCategoryDTO;
import br.com.erp.api.attribute.presentation.mapper.CategoryControllerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CategoryBackOfficeControllerTest {

    private CreateCategoryUseCase createCategoryUseCase;
    private RenameCategoryUseCase renameCategoryUseCase;
    private ActivateCategoryUseCase activateCategoryUseCase;
    private DeactivateCategoryUseCase deactivateCategoryUseCase;
    private CategoryControllerMapper mapper;
    private CategoryBackOfficeController controller;

    @BeforeEach
    void setup() {
        createCategoryUseCase = Mockito.mock(CreateCategoryUseCase.class);
        renameCategoryUseCase = Mockito.mock(RenameCategoryUseCase.class);
        activateCategoryUseCase = Mockito.mock(ActivateCategoryUseCase.class);
        deactivateCategoryUseCase = Mockito.mock(DeactivateCategoryUseCase.class);
        mapper = Mockito.mock(CategoryControllerMapper.class);

        controller = new CategoryBackOfficeController(
                createCategoryUseCase,
                renameCategoryUseCase,
                activateCategoryUseCase,
                deactivateCategoryUseCase,
                null,
                mapper
        );

        // Ensure ServletUriComponentsBuilder.fromCurrentRequest() has a request available
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void createCategory_shouldReturnCreatedAndLocationAndBody() {
        var dto = new CreateCategoryDTO("Treino");

        when(createCategoryUseCase.execute(ArgumentMatchers.any()))
                .thenReturn(new CategoryOutput(42L, "Treino", true));

        when(mapper.toDetailsDTO(ArgumentMatchers.any()))
                .thenReturn(new br.com.erp.api.attribute.presentation.dto.category.response.CategoryDetailsDTO(42L, "Treino", true));

        ResponseEntity<?> resp = controller.createCategory(dto);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getHeaders().getLocation()).isNotNull();
        assertThat(resp.getBody()).isNotNull();
    }
}

