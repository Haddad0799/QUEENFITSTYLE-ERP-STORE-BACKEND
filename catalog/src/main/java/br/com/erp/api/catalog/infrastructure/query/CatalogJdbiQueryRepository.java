package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.CatalogQueryRepository;
import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import br.com.erp.api.catalog.presentation.dto.*;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(CatalogJdbiQueryRepository.class);

    public CatalogJdbiQueryRepository(Jdbi jdbi, CatalogFilterSqlResolver filterResolver) {
        this.jdbi = jdbi;
        this.filterResolver = filterResolver;
    }

    @Override
    public Page<CatalogProductSummaryDTO> findAll(CatalogFilter filter, Pageable pageable) {

        CatalogPageQuery pageQuery = filterResolver.build(filter, pageable);
        // DEBUG: log SQL + params to help diagnosing unexpected empty pages
        if (log.isDebugEnabled()) {
            log.debug("Catalog select SQL:\n{}", pageQuery.selectSql());
            log.debug("Catalog select params: {}", pageQuery.params());
        }

        List<CatalogProductSummaryDTO> content = jdbi.withHandle(handle -> {
            var query = handle.createQuery(pageQuery.selectSql());
            pageQuery.params().forEach(query::bind);

            return query.map((rs, ctx) -> new CatalogProductSummaryDTO(
                    rs.getString("name"),
                    rs.getString("slug"),
                    rs.getString("category_name"),
                    rs.getString("category_normalized_name"),
                    rs.getString("main_image_url"),
                    rs.getBigDecimal("min_price")
            )).list();
        });

        if (log.isDebugEnabled()) {
            log.debug("Catalog count SQL:\n{}", pageQuery.countSql());
            // show params excluding limit/offset for clarity
            Map<String, Object> countParams = new HashMap<>(pageQuery.params());
            countParams.remove("limit");
            countParams.remove("offset");
            log.debug("Catalog count params: {}", countParams);
        }

        long total = jdbi.withHandle(handle -> {
            var query = handle.createQuery(pageQuery.countSql());

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
                    SELECT id, name, description, slug, category_name, category_normalized_name, main_image_url, min_price
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
                        row.put("categoryNormalizedName", rs.getString("category_normalized_name"));
                        row.put("mainImageUrl", rs.getString("main_image_url"));
                        row.put("minPrice", rs.getBigDecimal("min_price"));
                        return row;
                    })
                    .findOne();

            if (productOpt.isEmpty()) return Optional.empty();

            Map<String, Object> p = productOpt.get();
            Long catalogProductId = (Long) p.get("id");

            // 2. Busca grupos de cor com suas imagens
            List<Map<String, Object>> colorGroupRows = handle.createQuery("""
                    SELECT id, color_name, color_hex
                    FROM catalog_color_groups
                    WHERE catalog_product_id = :catalogProductId
                    ORDER BY color_name
                """)
                    .bind("catalogProductId", catalogProductId)
                    .map((rs, ctx) -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("colorName", rs.getString("color_name"));
                        row.put("colorHex", rs.getString("color_hex"));
                        return row;
                    })
                    .list();

            List<Long> colorGroupIds = colorGroupRows.stream()
                    .map(r -> (Long) r.get("id"))
                    .toList();

            // 3. Busca imagens por grupo de cor
            Map<Long, List<String>> imagesByGroupId = new HashMap<>();
            if (!colorGroupIds.isEmpty()) {
                handle.createQuery("""
                        SELECT catalog_color_group_id, image_url
                        FROM catalog_color_images
                        WHERE catalog_color_group_id IN (<ids>)
                        ORDER BY "order"
                    """)
                        .bindList("ids", colorGroupIds)
                        .map((rs, ctx) -> Map.entry(
                                rs.getLong("catalog_color_group_id"),
                                rs.getString("image_url")
                        ))
                        .list()
                        .forEach(entry ->
                                imagesByGroupId
                                        .computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                                        .add(entry.getValue())
                        );
            }

            // 4. Busca SKUs com referência ao grupo de cor
            List<Map<String, Object>> skuRows = handle.createQuery("""
                    SELECT cs.id, cs.code, cs.size_name, cs.selling_price,
                           cs.available_stock, cs.catalog_color_group_id,
                           cs.width, cs.height, cs.length, cs.weight
                    FROM catalog_skus cs
                    WHERE cs.catalog_product_id = :catalogProductId
                    ORDER BY cs.catalog_color_group_id, cs.size_name
                """)
                    .bind("catalogProductId", catalogProductId)
                    .map((rs, ctx) -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("code", rs.getString("code"));
                        row.put("sizeName", rs.getString("size_name"));
                        row.put("sellingPrice", rs.getBigDecimal("selling_price"));
                        row.put("availableStock", rs.getInt("available_stock"));
                        row.put("catalogColorGroupId", rs.getLong("catalog_color_group_id"));
                        row.put("width", rs.getBigDecimal("width"));
                        row.put("height", rs.getBigDecimal("height"));
                        row.put("length", rs.getBigDecimal("length"));
                        row.put("weight", rs.getBigDecimal("weight"));
                        return row;
                    })
                    .list();

            // 5. Monta grupos de cor com SKUs e imagens já resolvidos
            Map<Long, List<Map<String, Object>>> skusByGroupId = skuRows.stream()
                    .collect(Collectors.groupingBy(
                            r -> (Long) r.get("catalogColorGroupId"),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            List<CatalogColorGroupDTO> colorGroups = colorGroupRows.stream()
                    .map(group -> {
                        Long groupId = (Long) group.get("id");
                        List<String> images = imagesByGroupId.getOrDefault(groupId, List.of());
                        List<Map<String, Object>> skus = skusByGroupId.getOrDefault(groupId, List.of());

                        List<CatalogSkuSummaryDTO> skuDTOs = skus.stream()
                                .map(r -> {
                                    int stock = (int) r.get("availableStock");
                                    return new CatalogSkuSummaryDTO(
                                            (String) r.get("code"),
                                            (String) r.get("sizeName"),
                                            (BigDecimal) r.get("sellingPrice"),
                                            stock,
                                            stock > 0,
                                            (BigDecimal) r.get("width"),
                                            (BigDecimal) r.get("height"),
                                            (BigDecimal) r.get("length"),
                                            (BigDecimal) r.get("weight")
                                    );
                                })
                                .toList();

                        return new CatalogColorGroupDTO(
                                (String) group.get("colorName"),
                                (String) group.get("colorHex"),
                                images,
                                skuDTOs
                        );
                    })
                    .toList();

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
                    (String) p.get("categoryNormalizedName"),
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
                           cs.code, cs.size_name, cs.selling_price, cs.available_stock,
                           cs.width, cs.height, cs.length, cs.weight,
                           cs.id AS catalog_sku_id,
                           ccg.color_name, ccg.color_hex, ccg.id AS color_group_id
                    FROM catalog_skus cs
                    JOIN catalog_products cp ON cp.id = cs.catalog_product_id
                    JOIN catalog_color_groups ccg ON ccg.id = cs.catalog_color_group_id
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
                        row.put("colorGroupId", rs.getLong("color_group_id"));
                        return row;
                    })
                    .findOne();

            if (skuOpt.isEmpty()) return Optional.empty();

            Map<String, Object> s = skuOpt.get();

            // Imagens vêm do grupo de cor, não do SKU
            List<String> images = handle.createQuery("""
                    SELECT image_url FROM catalog_color_images
                    WHERE catalog_color_group_id = :groupId
                    ORDER BY "order"
                """)
                    .bind("groupId", (Long) s.get("colorGroupId"))
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
