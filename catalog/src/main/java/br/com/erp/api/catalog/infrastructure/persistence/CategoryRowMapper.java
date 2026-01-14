package br.com.erp.api.catalog.infrastructure.persistence;

import br.com.erp.api.catalog.domain.entity.Category;
import br.com.erp.api.catalog.domain.valueobject.CategoryName;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryRowMapper implements RowMapper<Category> {

    @Override
    public Category map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Category(
                rs.getLong("id"),
                new CategoryName(
                        rs.getString("display_name"),
                        rs.getString("normalized_name")
                ),
                rs.getBoolean("active")
        );
    }
}
