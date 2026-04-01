package br.com.erp.api.product.application.port;

import br.com.erp.api.product.application.dto.ProductImportData;

import java.io.InputStream;
import java.util.List;

public interface ProductImportParserPort {
    List<ProductImportData> parse(InputStream input);
}