package br.com.erp.api.product.infrastructure.parser;

import br.com.erp.api.product.application.dto.ProductImportData;
import br.com.erp.api.product.application.exception.ImportFileParseException;
import br.com.erp.api.product.application.port.ProductImportParserPort;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ApachePoiProductParser implements ProductImportParserPort {

    private static final Logger log = LoggerFactory.getLogger(ApachePoiProductParser.class);

    @Override
    public List<ProductImportData> parse(InputStream inputStream) {

        List<ProductImportData> list = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // header

                int rowNumber = row.getRowNum() + 1; // 1-based para o usuário

                // Ignora linhas totalmente vazias
                if (isRowEmpty(row)) {
                    continue;
                }

                try {
                    ProductImportData data = new ProductImportData(
                            rowNumber,
                            getString(row, 0),   // name
                            getString(row, 1),   // slug
                            getString(row, 2),   // category
                            getString(row, 3),   // color
                            getString(row, 4),   // size
                            getString(row, 5),   // skuCode
                            getDecimal(row, 6),  // width
                            getDecimal(row, 7),  // height
                            getDecimal(row, 8),  // length
                            getDecimal(row, 9),  // weight
                            getDecimal(row, 10), // costPrice
                            getDecimal(row, 11), // sellingPrice
                            getInteger(row, 12)  // stockQuantity
                    );
                    list.add(data);
                } catch (Exception e) {
                    log.warn("Erro ao fazer parse da linha {}: {}", rowNumber, e.getMessage());
                    // Linha com tipo de célula incompatível — será tratada como erro de validação
                    // Cria uma entrada com dados parciais para que o rowNumber seja rastreável
                    list.add(new ProductImportData(
                            rowNumber, null, null, null, null, null, null,
                            null, null, null, null, null, null, null
                    ));
                }
            }

        } catch (Exception e) {
            throw new ImportFileParseException("Erro ao ler arquivo Excel: " + e.getMessage(), e);
        }

        return list;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 13; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();
                if (!value.isEmpty()) return false;
            }
        }
        return true;
    }

    private String getString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        return cell.toString().trim();
    }

    private BigDecimal getDecimal(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        try {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } catch (Exception e) {
            try {
                return new BigDecimal(cell.toString().trim());
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Coluna " + (index + 1) + ": valor numérico inválido '" + cell + "'"
                );
            }
        }
    }

    private Integer getInteger(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        try {
            return (int) cell.getNumericCellValue();
        } catch (Exception e) {
            try {
                return Integer.valueOf(cell.toString().trim());
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Coluna " + (index + 1) + ": valor inteiro inválido '" + cell + "'"
                );
            }
        }
    }
}