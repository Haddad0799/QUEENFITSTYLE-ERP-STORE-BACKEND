package br.com.erp.api.product.infrastructure.persistence.repository;

import br.com.erp.api.product.application.query.SkuQueryRepository;
import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.infrastructure.persistence.query.PageQuery;
import br.com.erp.api.product.infrastructure.persistence.query.SkuFilterSqlResolver;
import br.com.erp.api.product.application.query.filter.SkuFilter;
import br.com.erp.api.product.presentation.dto.response.SkuDetailsDTO;
import br.com.erp.api.product.presentation.dto.response.SkuSummaryDTO;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SkuJdbiQueryRepositoryImpl implements SkuQueryRepository {

    private final Jdbi jdbi;
    private final SkuFilterSqlResolver skuFilterSqlResolver;

    public SkuJdbiQueryRepositoryImpl(Jdbi jdbi,
                                      SkuFilterSqlResolver skuFilterSqlResolver) {
        this.jdbi = jdbi;
        this.skuFilterSqlResolver = skuFilterSqlResolver;
    }

    @Override
    public Page<SkuSummaryDTO> findByProductId(Long productId,
                                               SkuFilter filter,
                                               Pageable pageable) {

        PageQuery pageQuery = skuFilterSqlResolver.buildSummary(productId, filter, pageable);

        List<SkuSummaryDTO> skus = jdbi.withHandle(handle ->
                handle.createQuery(pageQuery.selectSql())
                        .bindMap(pageQuery.filterParams())
                        .bind("limit", pageable.getPageSize())
                        .bind("offset", pageable.getOffset())
                        .map((rs, ctx) -> new SkuSummaryDTO(
                                rs.getLong("id"),
                                rs.getString("code"),
                                rs.getString("color_name"),
                                rs.getString("size_name"),
                                SkuStatus.valueOf(rs.getString("status"))
                        ))
                        .list()
        );

        Long total = jdbi.withHandle(handle ->
                handle.createQuery(pageQuery.countSql())
                        .bindMap(pageQuery.filterParams())
                        .mapTo(Long.class)
                        .one()
        );

        return new PageImpl<>(skus, pageable, total);
    }
}