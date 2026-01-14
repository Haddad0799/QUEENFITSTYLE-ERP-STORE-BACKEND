package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.output.ColorOutput;
import br.com.erp.api.catalog.application.query.ColorQueryService;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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
            SELECT
                id,
                name AS nome,
                hex_code AS hexaCode
            FROM colors
            ORDER BY name
        """)
                        .map(ConstructorMapper.of(ColorOutput.class))
                        .list()
        );
    }

}
