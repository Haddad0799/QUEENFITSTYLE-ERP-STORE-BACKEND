package br.com.erp.api.catalog.infrastructure.persistence.category;

import br.com.erp.api.catalog.domain.entity.Category;
import br.com.erp.api.catalog.domain.valueobject.Name;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryRowMapper implements RowMapper<Category> {

    @Override
    public Category map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Category(
                rs.getLong("id"),
                new Name(
                        rs.getString("display_name"),
                        rs.getString("normalized_name")
                ),
                rs.getBoolean("active")
        );
    }
}
