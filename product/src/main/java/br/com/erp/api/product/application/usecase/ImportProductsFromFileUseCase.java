package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.dto.ImportResult;
import br.com.erp.api.product.application.dto.ProductImportData;
import br.com.erp.api.product.application.port.ProductImportParserPort;
import br.com.erp.api.product.application.service.ProductGroupProcessor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ImportProductsFromFileUseCase {

    private final ProductGroupProcessor productGroupProcessor;
    private final ProductImportParserPort productImportParser;

    public ImportProductsFromFileUseCase(
            ProductGroupProcessor productGroupProcessor,
            ProductImportParserPort productImportParser
    ) {
        this.productGroupProcessor = productGroupProcessor;
        this.productImportParser = productImportParser;
    }

    public ImportResult execute(MultipartFile file) {

        List<ProductImportData> rows = parseFile(file);

        List<String> errors = new ArrayList<>();
        List<ProductImportData> validRows = new ArrayList<>();

        //valida linha a linha antes de agrupar
        for (ProductImportData row : rows) {
            try {
                validateRow(row);
                validRows.add(row);
            } catch (Exception e) {
                errors.add("Linha inválida: " + e.getMessage());
            }
        }

        Map<String, List<ProductImportData>> grouped = groupByProduct(validRows);

        int success = 0;

        for (var entry : grouped.entrySet()) {
            try {
                productGroupProcessor.process(entry.getValue());
                success++;
            } catch (Exception e) {
                errors.add("Produto [" + entry.getKey() + "]: " + e.getMessage());
            }
        }

        return new ImportResult(grouped.size(), success, errors);
    }

    private List<ProductImportData> parseFile(MultipartFile file) {
        try {
            return productImportParser.parse(file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo", e);
        }
    }

    private void validateRow(ProductImportData row) {
        if (row.name() == null || row.name().isBlank()) {
            throw new RuntimeException("Nome do produto inválido");
        }

        if (row.category() == null || row.category().isBlank()) {
            throw new RuntimeException("Categoria inválida");
        }

        if (row.skuCode() == null || row.skuCode().isBlank()) {
            throw new RuntimeException("SKU inválido");
        }
    }

    private Map<String, List<ProductImportData>> groupByProduct(List<ProductImportData> rows) {
        return rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> normalize(r.name()) + "|" + normalize(r.category())
                ));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}