package br.com.erp.api.attribute.infrastructure.query;

import br.com.erp.api.attribute.application.output.ColorOutput;
import br.com.erp.api.attribute.application.query.ColorQueryService;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ColorQueryServiceImpl implements ColorQueryService {

    private final Jdbi jdbi;

    public ColorQueryServiceImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<ColorOutput> findAll() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, name, hex_code
                        FROM colors
                        ORDER BY name
                        """)
                        .map((rs, ctx) -> new ColorOutput(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("hex_code")
                        ))
                        .list()
        );
    }

    @Override
    public List<ColorOutput> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return jdbi.withHandle(handle ->
                handle.createQuery("""
                        SELECT id, name, hex_code
                        FROM colors
                        WHERE id IN (<ids>)
                        ORDER BY name
                        """)
                        .bindList("ids", ids)
                        .map((rs, ctx) -> new ColorOutput(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("hex_code")
                        ))
                        .list()
        );
    }
}

