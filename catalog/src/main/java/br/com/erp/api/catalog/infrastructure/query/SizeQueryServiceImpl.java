package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.output.SizeOutput;
import br.com.erp.api.catalog.application.query.SizeQueryService;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SizeQueryServiceImpl implements SizeQueryService {

    private final Jdbi jdbi;

    public SizeQueryServiceImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<SizeOutput> findAll() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT
                id,
                label,
                type,
                display_order AS displayOrder
            FROM sizes
            ORDER BY type, display_order
        """)
                        .map(ConstructorMapper.of(SizeOutput.class))
                        .list()
        );
    }

    @Override
    public List<SizeOutput> findByIds(Set<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT
                    id,
                    label,
                    type,
                    display_order AS displayOrder
                FROM sizes
                WHERE id IN (<ids>)
                ORDER BY type, display_order
            """)
                        .bindList("ids", ids)
                        .map(ConstructorMapper.of(SizeOutput.class))
                        .list()
        );
    }

}
