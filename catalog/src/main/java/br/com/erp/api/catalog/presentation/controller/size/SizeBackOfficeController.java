package br.com.erp.api.catalog.presentation.controller.size;

import br.com.erp.api.catalog.application.query.SizeQueryService;
import br.com.erp.api.catalog.presentation.dto.size.SizeAdminDetailsDTO;
import br.com.erp.api.catalog.presentation.dto.size.SizesAdminDetailsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sizes")
public class SizeBackOfficeController {
    private final SizeQueryService sizeQueryService;

    public SizeBackOfficeController(SizeQueryService sizeQueryService) {
        this.sizeQueryService = sizeQueryService;
    }

    @GetMapping
    public ResponseEntity<SizesAdminDetailsDTO> getAllSizes() {
        var output = sizeQueryService.findAll();

        var response = output
                .stream()
                .map(o-> new SizeAdminDetailsDTO(o.id(), o.label(),o.type(),o.displayOrder()))
                .toList();

        return ResponseEntity.ok(new SizesAdminDetailsDTO(response));
    }
}
