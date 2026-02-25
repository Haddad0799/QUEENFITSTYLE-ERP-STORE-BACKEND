package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SkuJdbiRepositoryImpl implements SkuRepositoryPort {

    private final Jdbi jdbi;

    public SkuJdbiRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }


    @Override
    public List<Long> saveAll(Long productId, List<Sku> skus) {

        if (skus == null || skus.isEmpty()) {
            return List.of();
        }

        return jdbi.withHandle(handle -> {

            var batch = handle.prepareBatch("""
            INSERT INTO skus (
                product_id,
                sku_code,
                color_id,
                size_id,
                width,
                height,
                length,
                weight,
                status
            ) VALUES (
                :productId,
                :code,
                :colorId,
                :sizeId,
                :width,
                :height,
                :length,
                :weight,
                :status
            )
        """);

            for (Sku sku : skus) {
                batch.bind("productId", productId)
                        .bind("code", sku.getCode().value())
                        .bind("colorId", sku.getColorId())
                        .bind("sizeId", sku.getSizeId())
                        .bind("width", sku.getDimensions().width())
                        .bind("height", sku.getDimensions().height())
                        .bind("length", sku.getDimensions().length())
                        .bind("weight", sku.getDimensions().weight())
                        .bind("status", sku.getStatus())
                        .add();
            }

            return batch
                    .executePreparedBatch("id")
                    .mapTo(Long.class)
                    .list();
        });
    }
}
