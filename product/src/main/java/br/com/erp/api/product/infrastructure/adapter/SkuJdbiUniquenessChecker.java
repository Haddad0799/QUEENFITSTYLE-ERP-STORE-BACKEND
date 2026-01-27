package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.product.domain.port.SkuUniquenessChecker;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SkuJdbiUniquenessChecker implements SkuUniquenessChecker {

    private final Jdbi jdbi;

    public SkuJdbiUniquenessChecker(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<Map.Entry<Long, Long>> existsBatch(
            Long productId,
            List<Map.Entry<Long, Long>> combos
    ) {
        if (combos.isEmpty()) {
            return List.of();
        }

        String sql = """
            SELECT color_id, size_id
            FROM skus
            WHERE product_id = :productId
              AND (color_id, size_id) IN (<pairs>)
        """;

        return jdbi.withHandle(handle -> {

            var query = handle.createQuery(sql)
                    .bind("productId", productId);

            // transforma (colorId, sizeId) em "(?,?) , (?,?)"
            String pairs = combos.stream()
                    .map(c -> "(" + c.getKey() + "," + c.getValue() + ")")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            query.define("pairs", pairs);

            return query.map((rs, ctx) ->
                    Map.entry(
                            rs.getLong("color_id"),
                            rs.getLong("size_id")
                    )
            ).list();
        });
    }
}
