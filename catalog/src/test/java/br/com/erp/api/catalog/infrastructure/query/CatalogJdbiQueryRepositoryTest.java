package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.filter.ResolvedCatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogProductSummaryDTO;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogJdbiQueryRepositoryTest {

    private CatalogJdbiQueryRepository repository;

    @BeforeEach
    void setUp() {
        String jdbcUrl = "jdbc:h2:mem:catalog-query-" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Jdbi jdbi = Jdbi.create(jdbcUrl, "sa", "");
        createSchema(jdbi);
        seedData(jdbi);
        repository = new CatalogJdbiQueryRepository(jdbi, new CatalogFilterSqlResolver());
    }

    @Test
    void shouldUseShowcaseSelectionWhenThereAreNoSelectionFilters() {
        Page<CatalogProductSummaryDTO> page = repository.findAll(
                new ResolvedCatalogFilter(null, List.of(), null, null, null, null, null),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(2);

        CatalogProductSummaryDTO leggings = page.getContent().stream()
                .filter(product -> product.slug().equals("legging-power"))
                .findFirst()
                .orElseThrow();

        assertThat(leggings.selection()).isNotNull();
        assertThat(leggings.selection().skuCode()).isEqualTo("SKU-PRETO-M");
        assertThat(leggings.selection().label()).isEqualTo("M");
        assertThat(leggings.displayPrice()).isEqualByComparingTo("89.90");
        assertThat(leggings.displayImageUrl()).isEqualTo("img/preto-main.jpg");
    }

    @Test
    void shouldUseEffectiveSelectionAndExcludeIncompatibleProductsWhenSelectionFiltersAreActive() {
        Page<CatalogProductSummaryDTO> page = repository.findAll(
                new ResolvedCatalogFilter(
                        null,
                        List.of(),
                        "Amarelo",
                        "GG",
                        null,
                        new BigDecimal("100.00"),
                        null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);

        CatalogProductSummaryDTO product = page.getContent().getFirst();
        assertThat(product.slug()).isEqualTo("top-solar");
        assertThat(product.selection()).isNotNull();
        assertThat(product.selection().skuCode()).isEqualTo("SKU-AMARELO-GG");
        assertThat(product.selection().label()).isEqualTo("GG");
        assertThat(product.displayPrice()).isEqualByComparingTo("99.90");
        assertThat(product.displayImageUrl()).isEqualTo("img/amarelo-1.jpg");
    }

    private void createSchema(Jdbi jdbi) {
        jdbi.useHandle(handle -> {
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
                    CREATE TABLE catalog_color_images (
                        id BIGINT PRIMARY KEY,
                        catalog_color_group_id BIGINT NOT NULL,
                        image_url VARCHAR(500) NOT NULL,
                        "order" INT NOT NULL DEFAULT 1
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
        });
    }

    private void seedData(Jdbi jdbi) {
        jdbi.useHandle(handle -> {
            handle.execute("""
                    INSERT INTO catalog_products (
                        id, product_id, name, description, slug,
                        category_name, category_normalized_name,
                        subcategory_id, subcategory_name, subcategory_normalized_name,
                        parent_category_id, parent_category_name, parent_category_normalized_name,
                        main_image_url, main_color_name, main_color_hex,
                        default_sku_code, default_selection_label, display_price,
                        is_launch, published_at
                    ) VALUES
                    (1, 101, 'Legging Power', 'Legging preta', 'legging-power',
                     'Leggings', 'leggings',
                     11, 'Leggings', 'leggings',
                     NULL, NULL, NULL,
                     'img/preto-main.jpg', 'Preto', '#000000',
                     'SKU-PRETO-M', 'M', 89.90,
                     FALSE, TIMESTAMP '2026-04-20 10:00:00'),
                    (2, 102, 'Top Solar', 'Top amarelo', 'top-solar',
                     'Tops', 'tops',
                     12, 'Tops', 'tops',
                     NULL, NULL, NULL,
                     'img/solar-main.jpg', 'Laranja', '#FF6600',
                     'SKU-LARANJA-P', 'P', 79.90,
                     TRUE, TIMESTAMP '2026-04-20 11:00:00')
                    """);

            handle.execute("""
                    INSERT INTO catalog_color_groups (id, catalog_product_id, color_name, color_hex) VALUES
                    (10, 1, 'Preto', '#000000'),
                    (20, 2, 'Amarelo', '#F4D03F'),
                    (21, 2, 'Laranja', '#FF6600')
                    """);

            handle.execute("""
                    INSERT INTO catalog_color_images (id, catalog_color_group_id, image_url, "order") VALUES
                    (100, 10, 'img/preto-1.jpg', 1),
                    (101, 20, 'img/amarelo-1.jpg', 1),
                    (102, 21, 'img/laranja-1.jpg', 1)
                    """);

            handle.execute("""
                    INSERT INTO catalog_skus (
                        id, catalog_product_id, catalog_color_group_id, sku_id, code, size_name, selling_price, available_stock
                    ) VALUES
                    (1000, 1, 10, 10000, 'SKU-PRETO-M', 'M', 89.90, 5),
                    (2000, 2, 20, 20000, 'SKU-AMARELO-GG', 'GG', 99.90, 3),
                    (2001, 2, 21, 20001, 'SKU-LARANJA-P', 'P', 79.90, 8)
                    """);
        });
    }
}
