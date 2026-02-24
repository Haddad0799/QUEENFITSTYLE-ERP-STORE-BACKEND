package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.product.domain.port.SkuUniquenessChecker;
import br.com.erp.api.product.domain.valueobject.SkuCombination;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkuJdbiUniquenessChecker implements SkuUniquenessChecker {

    private final Jdbi jdbi;

    public SkuJdbiUniquenessChecker(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<SkuCombination> existsBatch(
            Long productId,
            List<SkuCombination> combos
    ) {

        if (combos == null || combos.isEmpty()) {
            return List.of();
        }

        return jdbi.withHandle(handle -> {

            String sql = buildSql(combos.size());

            var query = handle.createQuery(sql);

            int index = 0;
            for (SkuCombination combo : combos) {
                query.bind(index++, combo.colorId());
                query.bind(index++, combo.sizeId());
            }

            query.bind(index, productId);

            return query.map((rs, ctx) ->
                    new SkuCombination(
                            rs.getLong("color_id"),
                            rs.getLong("size_id")
                    )
            ).list();
        });
    }

    private String buildSql(int batchSize) {

        StringBuilder sql = new StringBuilder("""
        SELECT s.color_id, s.size_id
        FROM skus s
        JOIN (VALUES
    """);

        for (int i = 0; i < batchSize; i++) {
            sql.append("(?, ?)");
            if (i < batchSize - 1) {
                sql.append(",");
            }
        }

        sql.append("""
        ) AS v(color_id, size_id)
        ON s.color_id = v.color_id
        AND s.size_id = v.size_id
        WHERE s.product_id = ?
    """);

        return sql.toString();
    }
}