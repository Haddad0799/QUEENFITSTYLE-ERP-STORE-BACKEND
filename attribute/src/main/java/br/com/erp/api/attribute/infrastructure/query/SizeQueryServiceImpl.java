package br.com.erp.api.attribute.infrastructure.query;

import br.com.erp.api.attribute.application.output.SizeOutput;
import br.com.erp.api.attribute.application.query.SizeQueryService;
import org.jdbi.v3.core.Jdbi;
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
                        SELECT id, label, type, display_order
                        FROM sizes
                        ORDER BY type, display_order
                        """)
                        .map((rs, ctx) -> new SizeOutput(
                                rs.getLong("id"),
                                rs.getString("label"),
                                rs.getString("type"),
                                rs.getInt("display_order")
                        ))
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
                        SELECT id, label, type, display_order
                        FROM sizes
                        WHERE id IN (<ids>)
                        ORDER BY type, display_order
                        """)
                        .bindList("ids", ids)
                        .map((rs, ctx) -> new SizeOutput(
                                rs.getLong("id"),
                                rs.getString("label"),
                                rs.getString("type"),
                                rs.getInt("display_order")
                        ))
                        .list()
        );
    }
}

