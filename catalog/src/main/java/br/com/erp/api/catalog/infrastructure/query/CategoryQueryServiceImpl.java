package br.com.erp.api.catalog.infrastructure.query;

import br.com.erp.api.catalog.application.query.CategoryQueryService;
import br.com.erp.api.catalog.application.exception.CategoryNotFoundException;
import br.com.erp.api.catalog.presentation.dto.category.response.CategoryDetailsDTO;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final Jdbi jdbi;

    public CategoryQueryServiceImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public List<CategoryDetailsDTO> findAll() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT
              id,
              display_name AS name,
              active
            FROM categories
        """)
                        .map(ConstructorMapper.of(CategoryDetailsDTO.class))
                        .list()
        );
    }

    @Override
    public CategoryDetailsDTO findById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT
              id,
              display_name AS name,
              active
            FROM categories
            WHERE id = :id
        """)
                        .bind("id", id)
                        .map(ConstructorMapper.of(CategoryDetailsDTO.class))
                        .findOne()
                        .orElseThrow(() -> new CategoryNotFoundException("Categoria", id))
        );
    }

    @Override
    public boolean existsActiveById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT EXISTS(
                SELECT 1
                FROM categories
                WHERE id = :id
                  AND active = true
            )
        """)
                        .bind("id", id)
                        .mapTo(Boolean.class)
                        .one()
        );
    }


}
