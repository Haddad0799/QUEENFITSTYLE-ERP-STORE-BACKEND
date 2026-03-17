package br.com.erp.api.catalog.infrastructure.persistence;

import br.com.erp.api.catalog.application.query.CatalogQueryRepository;
import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import br.com.erp.api.catalog.infrastructure.query.CatalogFilterSqlResolver;
import br.com.erp.api.catalog.infrastructure.query.CatalogPageQuery;
import br.com.erp.api.catalog.presentation.dto.*;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CatalogJdbiQueryRepository implements CatalogQueryRepository {

    private final Jdbi jdbi;
    private final CatalogFilterSqlResolver filterResolver;

    public CatalogJdbiQueryRepository(Jdbi jdbi, CatalogFilterSqlResolver filterResolver) {
        this.jdbi = jdbi;
        this.filterResolver = filterResolver;
    }

    @Override
    public Page<CatalogProductSummaryDTO> findAll(CatalogFilter filter, Pageable pageable) {

        CatalogPageQuery pageQuery = filterResolver.build(filter, pageable);

        List<CatalogProductSummaryDTO> content = jdbi.withHandle(handle -> {
            var query = handle.createQuery(pageQuery.selectSql());
            pageQuery.params().forEach(query::bind);
            return query.map((rs, ctx) -> new CatalogProductSummaryDTO(
                    rs.getString("name"),
                    rs.getString("slug"),
                    rs.getString("category_name"),
                    rs.getString("main_image_url"),
                    rs.getBigDecimal("min_price")
            )).list();
        });

        long total = jdbi.withHandle(handle -> {
            var query = handle.createQuery(pageQuery.countSql());
            // Bind somente os params de filtro (sem limit/offset) para o count
            pageQuery.params().forEach((key, value) -> {
                if (!"limit".equals(key) && !"offset".equals(key)) {
                    query.bind(key, value);
                }
            });
            return query.mapTo(Long.class).one();
        });

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<CatalogProductDetailDTO> findBySlug(String slug) {

        return jdbi.withHandle(handle -> {

            // 1. Busca produto
            var productOpt = handle.createQuery("""
                        SELECT id, name, description, slug, category_name, main_image_url, min_price
                        FROM catalog_products
                        WHERE slug = :slug
                    """)
                    .bind("slug", slug)
                    .map((rs, ctx) -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("name", rs.getString("name"));
                        row.put("description", rs.getString("description"));
                        row.put("slug", rs.getString("slug"));
                        row.put("categoryName", rs.getString("category_name"));
                        row.put("mainImageUrl", rs.getString("main_image_url"));
                        row.put("minPrice", rs.getBigDecimal("min_price"));
                        return row;
                    })
                    .findOne();

            if (productOpt.isEmpty()) return Optional.empty();

            Map<String, Object> p = productOpt.get();
            Long catalogProductId = (Long) p.get("id");

            // 2. Busca SKUs
            List<Map<String, Object>> skuRows = handle.createQuery("""
                        SELECT cs.id, cs.code, cs.color_name, cs.color_hex, cs.size_name,
                               cs.selling_price, cs.available_stock
                        FROM catalog_skus cs
                        WHERE cs.catalog_product_id = :catalogProductId
                        ORDER BY cs.color_name, cs.size_name
                    """)
                    .bind("catalogProductId", catalogProductId)
                    .map((rs, ctx) -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("code", rs.getString("code"));
                        row.put("colorName", rs.getString("color_name"));
                        row.put("colorHex", rs.getString("color_hex"));
                        row.put("sizeName", rs.getString("size_name"));
                        row.put("sellingPrice", rs.getBigDecimal("selling_price"));
                        row.put("availableStock", rs.getInt("available_stock"));
                        return row;
                    })
                    .list();

            // 3. Busca imagens de todos os SKUs deste produto
            List<Long> catalogSkuIds = skuRows.stream()
                    .map(r -> (Long) r.get("id"))
                    .toList();

            Map<Long, List<String>> imagesBySkuId = new HashMap<>();
            if (!catalogSkuIds.isEmpty()) {
                handle.createQuery("""
                            SELECT catalog_sku_id, image_url
                            FROM catalog_sku_images
                            WHERE catalog_sku_id IN (<ids>)
                            ORDER BY "order"
                        """)
                        .bindList("ids", catalogSkuIds)
                        .map((rs, ctx) -> Map.entry(
                                rs.getLong("catalog_sku_id"),
                                rs.getString("image_url")
                        ))
                        .list()
                        .forEach(entry ->
                                imagesBySkuId
                                        .computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                                        .add(entry.getValue())
                        );
            }

            // 4. Agrupa por cor
            Map<String, List<Map<String, Object>>> byColor = skuRows.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.get("colorName") + "|" + r.get("colorHex"),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            List<CatalogColorGroupDTO> colorGroups = byColor.entrySet().stream()
                    .map(entry -> {
                        String[] parts = entry.getKey().split("\\|");
                        String colorName = parts[0];
                        String colorHex = parts.length > 1 ? parts[1] : "";

                        // Pega imagens do primeiro SKU da cor (compartilhadas)
                        Long firstSkuId = (Long) entry.getValue().getFirst().get("id");
                        List<String> images = imagesBySkuId.getOrDefault(firstSkuId, List.of());

                        List<CatalogSkuSummaryDTO> skus = entry.getValue().stream()
                                .map(r -> {
                                    int stock = (int) r.get("availableStock");
                                    return new CatalogSkuSummaryDTO(
                                            (String) r.get("code"),
                                            (String) r.get("sizeName"),
                                            (BigDecimal) r.get("sellingPrice"),
                                            stock,
                                            stock > 0
                                    );
                                })
                                .toList();

                        return new CatalogColorGroupDTO(colorName, colorHex, images, skus);
                    })
                    .sorted(Comparator.comparing(CatalogColorGroupDTO::colorName))
                    .toList();

            // Calcula maxPrice
            BigDecimal maxPrice = skuRows.stream()
                    .map(r -> (BigDecimal) r.get("sellingPrice"))
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(null);

            return Optional.of(new CatalogProductDetailDTO(
                    (String) p.get("name"),
                    (String) p.get("description"),
                    (String) p.get("slug"),
                    (String) p.get("categoryName"),
                    (String) p.get("mainImageUrl"),
                    (BigDecimal) p.get("minPrice"),
                    maxPrice,
                    colorGroups
            ));
        });
    }

    @Override
    public Optional<CatalogSkuDetailDTO> findSkuBySlugAndCode(String slug, String skuCode) {

        return jdbi.withHandle(handle -> {

            var skuOpt = handle.createQuery("""
                        SELECT cp.name AS product_name, cp.slug AS product_slug,
                               cs.code, cs.color_name, cs.color_hex, cs.size_name,
                               cs.selling_price, cs.available_stock,
                               cs.width, cs.height, cs.length, cs.weight,
                               cs.id AS catalog_sku_id
                        FROM catalog_skus cs
                        JOIN catalog_products cp ON cp.id = cs.catalog_product_id
                        WHERE cp.slug = :slug AND cs.code = :skuCode
                    """)
                    .bind("slug", slug)
                    .bind("skuCode", skuCode)
                    .map((rs, ctx) -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("productName", rs.getString("product_name"));
                        row.put("productSlug", rs.getString("product_slug"));
                        row.put("code", rs.getString("code"));
                        row.put("colorName", rs.getString("color_name"));
                        row.put("colorHex", rs.getString("color_hex"));
                        row.put("sizeName", rs.getString("size_name"));
                        row.put("sellingPrice", rs.getBigDecimal("selling_price"));
                        row.put("availableStock", rs.getInt("available_stock"));
                        row.put("width", rs.getBigDecimal("width"));
                        row.put("height", rs.getBigDecimal("height"));
                        row.put("length", rs.getBigDecimal("length"));
                        row.put("weight", rs.getBigDecimal("weight"));
                        row.put("catalogSkuId", rs.getLong("catalog_sku_id"));
                        return row;
                    })
                    .findOne();

            if (skuOpt.isEmpty()) return Optional.empty();

            Map<String, Object> s = skuOpt.get();

            List<String> images = handle.createQuery("""
                        SELECT image_url FROM catalog_sku_images
                        WHERE catalog_sku_id = :id
                        ORDER BY "order"
                    """)
                    .bind("id", (Long) s.get("catalogSkuId"))
                    .mapTo(String.class)
                    .list();

            int stock = (int) s.get("availableStock");

            return Optional.of(new CatalogSkuDetailDTO(
                    (String) s.get("productName"),
                    (String) s.get("productSlug"),
                    (String) s.get("code"),
                    (String) s.get("colorName"),
                    (String) s.get("colorHex"),
                    (String) s.get("sizeName"),
                    (BigDecimal) s.get("sellingPrice"),
                    stock,
                    stock > 0,
                    (BigDecimal) s.get("width"),
                    (BigDecimal) s.get("height"),
                    (BigDecimal) s.get("length"),
                    (BigDecimal) s.get("weight"),
                    images
            ));
        });
    }
}
