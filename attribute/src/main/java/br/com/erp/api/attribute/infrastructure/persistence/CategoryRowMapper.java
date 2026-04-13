package br.com.erp.api.attribute.infrastructure.persistence;

import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.valueobject.CategoryName;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryRowMapper implements RowMapper<Category> {

    @Override
    public Category map(ResultSet rs, StatementContext ctx) throws SQLException {
        long parentIdRaw = rs.getLong("parent_id");
        Long parentId = rs.wasNull() ? null : parentIdRaw;

        return new Category(
                rs.getLong("id"),
                new CategoryName(
                        rs.getString("display_name"),
                        rs.getString("normalized_name")
                ),
                rs.getBoolean("active"),
                parentId
        );
    }
}
