package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.dto.GroupProcessingResult;
import br.com.erp.api.product.application.dto.ImportResult;
import br.com.erp.api.product.application.dto.ProductImportData;
import br.com.erp.api.product.application.dto.ProductImportError;
import br.com.erp.api.product.application.exception.ImportFileParseException;
import br.com.erp.api.product.application.port.ProductImportParserPort;
import br.com.erp.api.product.application.service.ProductGroupProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImportProductsFromFileUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImportProductsFromFileUseCase.class);

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

        ImportResult.Builder builder = new ImportResult.Builder();

        // --- 1. Parse ---
        List<ProductImportData> rows = parseFile(file);
        builder.setTotalRows(rows.size());

        log.info("Importação iniciada: {} linhas extraídas do arquivo [{}]",
                rows.size(), file.getOriginalFilename());

        // --- 2. Validação linha a linha ---
        List<ProductImportData> validRows = new ArrayList<>();

        for (ProductImportData row : rows) {
            List<ProductImportError> rowErrors = validateRow(row);
            if (rowErrors.isEmpty()) {
                validRows.add(row);
            } else {
                rowErrors.forEach(builder::addError);
            }
        }

        builder.setValidRows(validRows.size());

        if (validRows.isEmpty()) {
            log.warn("Nenhuma linha válida encontrada. Importação abortada.");
            builder.addError(ProductImportError.validation(
                    0, null, null, null, null,
                    "Nenhuma linha válida encontrada no arquivo."
            ));
            return builder.build();
        }

        // --- 3. Agrupar por produto (nome + categoria) ---
        Map<String, List<ProductImportData>> grouped = groupByProduct(validRows);

        log.info("Linhas válidas: {}. Produtos identificados: {}", validRows.size(), grouped.size());

        // --- 4. Processar cada grupo ---
        for (var entry : grouped.entrySet()) {
            String groupKey = entry.getKey();
            List<ProductImportData> groupRows = entry.getValue();

            try {
                GroupProcessingResult result = productGroupProcessor.process(groupRows);

                // Acumular contadores
                if (result.productCreated()) {
                    builder.incrementProductsCreated();
                } else {
                    builder.incrementProductsReused();
                }

                builder.addSkusCreated(result.skusCreated());
                builder.addSkusIgnored(result.skusIgnored());
                builder.addSkusFailed(result.skusFailed());
                builder.addErrors(result.errors());

            } catch (Exception e) {
                // Falha no produto inteiro → todos os SKUs do grupo são ignorados
                ProductImportData first = groupRows.getFirst();
                builder.addError(ProductImportError.product(
                        first.rowNumber(), first.name(), first.category(),
                        e.getMessage()
                ));
                log.error("Falha ao processar grupo [{}]: {}", groupKey, e.getMessage());
            }
        }

        ImportResult result = builder.build();

        log.info("Importação finalizada: totalLinhas={}, linhasVálidas={}, " +
                        "produtosCriados={}, produtosReaproveitados={}, " +
                        "skusCriados={}, skusIgnorados={}, skusFalhos={}, erros={}",
                result.totalRows(), result.validRows(),
                result.productsCreated(), result.productsReused(),
                result.skusCreated(), result.skusIgnored(), result.skusFailed(),
                result.errors().size());

        return result;
    }

    private List<ProductImportData> parseFile(MultipartFile file) {
        try {
            return productImportParser.parse(file.getInputStream());
        } catch (ImportFileParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportFileParseException("Erro ao ler arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Validação completa de uma linha. Retorna lista vazia se válida.
     * Cada erro é um ProductImportError estruturado.
     */
    private List<ProductImportError> validateRow(ProductImportData row) {
        List<ProductImportError> errors = new ArrayList<>();
        int line = row.rowNumber();
        String name = row.name();
        String category = row.category();
        String skuCode = row.skuCode();

        // --- Campos obrigatórios de texto ---
        if (isBlank(name)) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, "name",
                    "Nome do produto é obrigatório"));
        }
        if (isBlank(row.slug())) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, "slug",
                    "Slug do produto é obrigatório"));
        }
        if (isBlank(category)) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, "category",
                    "Categoria é obrigatória"));
        }
        if (isBlank(skuCode)) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, "skuCode",
                    "Código SKU é obrigatório"));
        }
        if (isBlank(row.color())) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, "color",
                    "Cor é obrigatória"));
        }
        if (isBlank(row.size())) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, "size",
                    "Tamanho é obrigatório"));
        }

        // --- Dimensões (todas obrigatórias e positivas) ---
        validatePositive(row.width(), "width", "Largura", line, name, category, skuCode, errors);
        validatePositive(row.height(), "height", "Altura", line, name, category, skuCode, errors);
        validatePositive(row.length(), "length", "Comprimento", line, name, category, skuCode, errors);
        validatePositive(row.weight(), "weight", "Peso", line, name, category, skuCode, errors);

        // --- Preços (obrigatórios e positivos) ---
        validatePositive(row.costPrice(), "costPrice", "Preço de custo", line, name, category, skuCode, errors);
        validatePositive(row.sellingPrice(), "sellingPrice", "Preço de venda", line, name, category, skuCode, errors);

        // --- Estoque (obrigatório e não-negativo) ---
        if (row.stockQuantity() == null) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, "stockQuantity",
                    "Quantidade em estoque é obrigatória"));
        } else if (row.stockQuantity() < 0) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, "stockQuantity",
                    "Quantidade em estoque não pode ser negativa"));
        }

        return errors;
    }

    private void validatePositive(BigDecimal value, String field, String fieldLabel,
                                  int line, String name, String category, String skuCode,
                                  List<ProductImportError> errors) {
        if (value == null) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, field,
                    fieldLabel + " é obrigatório(a)"));
        } else if (value.signum() <= 0) {
            errors.add(ProductImportError.validation(line, name, category, skuCode, field,
                    fieldLabel + " deve ser positivo(a)"));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Map<String, List<ProductImportData>> groupByProduct(List<ProductImportData> rows) {
        return rows.stream()
                .collect(Collectors.groupingBy(
                        r -> normalize(r.slug()) + "|" + normalize(r.category())
                ));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}