package br.com.erp.api.attribute.infrastructure.query;

import br.com.erp.api.attribute.application.query.CategoryQueryService;
import br.com.erp.api.attribute.application.exception.CategoryNotFoundException;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoryDetailsDTO;
import br.com.erp.api.attribute.presentation.dto.category.response.StoreCategoryDTO;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public List<StoreCategoryDTO> findAllActive() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT
              display_name AS name,
              normalized_name AS normalizedName
            FROM categories
            WHERE active = true
            ORDER BY display_name
        """)
                        .map(ConstructorMapper.of(StoreCategoryDTO.class))
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

    @Override
    public Optional<CategoryDetailsDTO> findByName(String name) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT
                  id,
                  display_name AS name,
                  active
                FROM categories
                WHERE LOWER(display_name) = LOWER(:name)
                  AND active = true
            """)
                        .bind("name", name)
                        .map(ConstructorMapper.of(CategoryDetailsDTO.class))
                        .findOne()
        );
    }


}

