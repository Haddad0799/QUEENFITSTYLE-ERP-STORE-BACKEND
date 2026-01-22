package br.com.erp.api.product.infrastructure.mapper;

import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.valueobject.CategoryId;
import br.com.erp.api.product.domain.valueobject.Slug;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductRowMapper implements RowMapper<Product> {

    @Override
    public Product map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Product.restore(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                Slug.fromValue(rs.getString("slug")),
                new CategoryId(rs.getLong("category_id")),
                rs.getBoolean("active")
        );
    }

}
