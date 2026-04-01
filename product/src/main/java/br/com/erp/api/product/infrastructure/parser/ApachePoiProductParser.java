package br.com.erp.api.product.infrastructure.parser;

import br.com.erp.api.product.application.dto.ProductImportData;
import br.com.erp.api.product.application.port.ProductImportParserPort;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ApachePoiProductParser implements ProductImportParserPort {

    @Override
    public List<ProductImportData> parse(InputStream inputStream) {

        List<ProductImportData> list = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                ProductImportData data = new ProductImportData(
                        getString(row, 0),
                        getString(row, 1),
                        getString(row, 2),
                        getString(row, 3),
                        getString(row, 4),
                        getString(row, 5),
                        getDecimal(row, 6),
                        getDecimal(row, 7),
                        getDecimal(row, 8),
                        getDecimal(row, 9),
                        getDecimal(row, 10),
                        getDecimal(row, 11),
                        getInteger(row, 12)
                );

                list.add(data);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler Excel", e);
        }

        return list;
    }

    private String getString(Row row, int index) {
        if (row.getCell(index) == null) return null;
        return row.getCell(index).toString().trim();
    }

    private BigDecimal getDecimal(Row row, int index) {
        if (row.getCell(index) == null) return null;

        try {
            return BigDecimal.valueOf(row.getCell(index).getNumericCellValue());
        } catch (Exception e) {
            return new BigDecimal(row.getCell(index).toString());
        }
    }

    private Integer getInteger(Row row, int index) {
        if (row.getCell(index) == null) return null;

        try {
            return (int) row.getCell(index).getNumericCellValue();
        } catch (Exception e) {
            return Integer.valueOf(row.getCell(index).toString());
        }
    }
}