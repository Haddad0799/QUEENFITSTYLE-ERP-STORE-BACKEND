package br.com.erp.api.product.application.dto;

/**
 * Erro estruturado de importação de produto.
 * Contém contexto suficiente para o frontend exibir mensagens detalhadas.
 */
public record ProductImportError(
        int rowNumber,
        String productName,
        String category,
        String skuCode,
        String field,
        String message
) {

    /**
     * Erro de validação de campo (linha do Excel).
     */
    public static ProductImportError validation(int rowNumber, String productName, String category,
                                                String skuCode, String field, String message) {
        return new ProductImportError(rowNumber, productName, category, skuCode, field, message);
    }

    /**
     * Erro no nível do produto (afeta todo o grupo).
     */
    public static ProductImportError product(int rowNumber, String productName, String category,
                                             String message) {
        return new ProductImportError(rowNumber, productName, category, null, null, message);
    }

    /**
     * Erro no nível do SKU (apenas esse SKU é ignorado).
     */
    public static ProductImportError sku(int rowNumber, String productName, String category,
                                         String skuCode, String message) {
        return new ProductImportError(rowNumber, productName, category, skuCode, null, message);
    }
}

