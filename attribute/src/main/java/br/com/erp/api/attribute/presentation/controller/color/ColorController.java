package br.com.erp.api.attribute.presentation.controller.color;

import br.com.erp.api.attribute.application.query.ColorQueryService;
import br.com.erp.api.attribute.presentation.dto.color.ColorAdminDetailsDTO;
import br.com.erp.api.attribute.presentation.dto.color.ColorsAdminDetailsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/erp/colors")
public class ColorController {
    private final ColorQueryService queryService;

    public ColorController(ColorQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<ColorsAdminDetailsDTO> getAllColors() {

        var output = queryService.findAll();

        var respose = output
                .stream()
                .map(o-> new ColorAdminDetailsDTO(o.id(),
                        o.name(),
                        o.hexaCode()))
                .toList();

        return ResponseEntity.ok(new ColorsAdminDetailsDTO(respose));
    }

}

