package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.filter.ResolvedCatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogProductSummaryDTO;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica a busca por similaridade (pg_trgm) contra um PostgreSQL real,
 * já que H2 não implementa a função similarity() nem a extensão pg_trgm.
 *
 * O esquema e os índices GIN de trigrama espelham a migration V42.
 */
@Testcontainers
class CatalogSearchPgTrgmTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15");

    private static CatalogJdbiQueryRepository repository;

    @BeforeAll
    static void setUp() {
        Jdbi jdbi = Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        createSchema(jdbi);
        seedData(jdbi);
        repository = new CatalogJdbiQueryRepository(jdbi, new CatalogFilterSqlResolver());
    }

    @Test
    void shouldFindProductsByCategoryNameEvenWithTypo() {
        Page<CatalogProductSummaryDTO> page = search("Conjutos");

        assertThat(page.getContent())
                .extracting(CatalogProductSummaryDTO::name)
                .containsExactlyInAnyOrder("Conjunto Vital", "Conjunto Run");
    }

    @Test
    void shouldFindProductByPartialName() {
        Page<CatalogProductSummaryDTO> page = search("vital");

        assertThat(page.getContent())
                .extracting(CatalogProductSummaryDTO::name)
                .containsExactly("Conjunto Vital");
    }

    @Test
    void shouldFindProductByNameWithTypo() {
        Page<CatalogProductSummaryDTO> page = search("cropd");

        assertThat(page.getContent())
                .extracting(CatalogProductSummaryDTO::name)
                .containsExactly("Cropped Energy");
    }

    @Test
    void shouldNotReturnUnrelatedProducts() {
        Page<CatalogProductSummaryDTO> page = search("Conjutos");

        assertThat(page.getContent())
                .extracting(CatalogProductSummaryDTO::name)
                .doesNotContain("Legging Power");
    }

    private Page<CatalogProductSummaryDTO> search(String term) {
        return repository.findAll(
                new ResolvedCatalogFilter(null, List.of(), null, null, null, null, term),
                PageRequest.of(0, 10)
        );
    }

    private static void createSchema(Jdbi jdbi) {
        jdbi.useHandle(handle -> {
            handle.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");

            handle.execute("""
                    CREATE TABLE catalog_products (
                        id BIGINT PRIMARY KEY,
                        product_id BIGINT NOT NULL UNIQUE,
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        slug VARCHAR(255) NOT NULL UNIQUE,
                        category_name VARCHAR(100) NOT NULL,
                        category_normalized_name VARCHAR(100),
                        subcategory_id BIGINT,
                        subcategory_name VARCHAR(100),
                        subcategory_normalized_name VARCHAR(100),
                        parent_category_id BIGINT,
                        parent_category_name VARCHAR(100),
                        parent_category_normalized_name VARCHAR(100),
                        main_image_url VARCHAR(500),
                        main_color_name VARCHAR(50),
                        main_color_hex CHAR(7),
                        default_sku_code VARCHAR(100),
                        default_selection_label VARCHAR(20),
                        display_price NUMERIC(10,2),
                        is_launch BOOLEAN NOT NULL DEFAULT FALSE,
                        published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            handle.execute("""
                    CREATE TABLE catalog_color_groups (
                        id BIGINT PRIMARY KEY,
                        catalog_product_id BIGINT NOT NULL,
                        color_name VARCHAR(50) NOT NULL,
                        color_hex CHAR(7) NOT NULL
                    )
                    """);

            handle.execute("""
                    CREATE TABLE catalog_skus (
                        id BIGINT PRIMARY KEY,
                        catalog_product_id BIGINT NOT NULL,
                        catalog_color_group_id BIGINT NOT NULL,
                        sku_id BIGINT NOT NULL UNIQUE,
                        code VARCHAR(100) NOT NULL,
                        size_name VARCHAR(20) NOT NULL,
                        selling_price NUMERIC(10,2) NOT NULL,
                        available_stock INTEGER NOT NULL DEFAULT 0
                    )
                    """);

            // Índices GIN de trigrama, espelhando a migration V42
            handle.execute("CREATE INDEX idx_catalog_products_name_trgm ON catalog_products USING gin (name gin_trgm_ops)");
            handle.execute("CREATE INDEX idx_catalog_products_subcategory_name_trgm ON catalog_products USING gin (subcategory_name gin_trgm_ops)");
            handle.execute("CREATE INDEX idx_catalog_products_parent_category_name_trgm ON catalog_products USING gin (parent_category_name gin_trgm_ops)");
        });
    }

    private static void seedData(Jdbi jdbi) {
        jdbi.useHandle(handle -> {
            handle.execute("""
                    INSERT INTO catalog_products (
                        id, product_id, name, slug,
                        category_name, category_normalized_name,
                        subcategory_id, subcategory_name, subcategory_normalized_name,
                        default_sku_code, default_selection_label, display_price,
                        is_launch
                    ) VALUES
                    (1, 101, 'Conjunto Vital', 'conjunto-vital',
                     'Conjuntos', 'conjuntos', 11, 'Conjuntos', 'conjuntos',
                     'SKU-VITAL-M', 'M', 199.90, FALSE),
                    (2, 102, 'Conjunto Run', 'conjunto-run',
                     'Conjuntos', 'conjuntos', 11, 'Conjuntos', 'conjuntos',
                     'SKU-RUN-M', 'M', 189.90, FALSE),
                    (3, 103, 'Cropped Energy', 'cropped-energy',
                     'Tops', 'tops', 12, 'Tops', 'tops',
                     'SKU-CROP-M', 'M', 79.90, FALSE),
                    (4, 104, 'Legging Power', 'legging-power',
                     'Leggings', 'leggings', 13, 'Leggings', 'leggings',
                     'SKU-LEG-M', 'M', 99.90, FALSE)
                    """);

            handle.execute("""
                    INSERT INTO catalog_color_groups (id, catalog_product_id, color_name, color_hex) VALUES
                    (10, 1, 'Preto', '#000000'),
                    (20, 2, 'Azul',  '#0000FF'),
                    (30, 3, 'Rosa',  '#FF66AA'),
                    (40, 4, 'Preto', '#000000')
                    """);

            handle.execute("""
                    INSERT INTO catalog_skus (
                        id, catalog_product_id, catalog_color_group_id, sku_id, code, size_name, selling_price, available_stock
                    ) VALUES
                    (1000, 1, 10, 10000, 'SKU-VITAL-M', 'M', 199.90, 5),
                    (2000, 2, 20, 20000, 'SKU-RUN-M',   'M', 189.90, 5),
                    (3000, 3, 30, 30000, 'SKU-CROP-M',  'M', 79.90, 5),
                    (4000, 4, 40, 40000, 'SKU-LEG-M',   'M', 99.90, 5)
                    """);
        });
    }
}
