package br.com.erp.api.attribute.infrastructure.query;

import br.com.erp.api.attribute.application.query.CategoryQueryService;
import br.com.erp.api.attribute.application.exception.CategoryNotFoundException;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoryDetailsDTO;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoryTreeDTO;
import br.com.erp.api.attribute.presentation.dto.category.response.StoreCategoryDTO;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
              normalized_name,
              active,
              parent_id
            FROM categories
            ORDER BY parent_id NULLS FIRST, display_name
        """)
                        .map((rs, ctx) -> {
                            long parentIdRaw = rs.getLong("parent_id");
                            Long parentId = rs.wasNull() ? null : parentIdRaw;
                            return new CategoryDetailsDTO(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("normalized_name"),
                                    rs.getBoolean("active"),
                                    parentId
                            );
                        })
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
              AND parent_id IS NOT NULL
            ORDER BY display_name
        """)
                        .map((rs, ctx) -> new StoreCategoryDTO(
                                rs.getString("name"),
                                rs.getString("normalizedName")
                        ))
                        .list()
        );
    }

    @Override
    public List<CategoryTreeDTO> findAllActiveAsTree() {
        record FlatRow(Long id, String name, String normalizedName, Long parentId) {}

        List<FlatRow> rows = jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT
              id,
              display_name AS name,
              normalized_name AS normalizedName,
              parent_id
            FROM categories
            WHERE active = true
            ORDER BY parent_id NULLS FIRST, display_name
        """)
                        .map((rs, ctx) -> {
                            long parentIdRaw = rs.getLong("parent_id");
                            Long parentId = rs.wasNull() ? null : parentIdRaw;
                            return new FlatRow(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("normalizedName"),
                                    parentId
                            );
                        })
                        .list()
        );

        // Agrupa em memória: pais → filhos
        Map<Long, List<FlatRow>> childrenByParent = new LinkedHashMap<>();
        List<FlatRow> parents = new ArrayList<>();

        for (FlatRow row : rows) {
            if (row.parentId() == null) {
                parents.add(row);
            } else {
                childrenByParent.computeIfAbsent(row.parentId(), k -> new ArrayList<>()).add(row);
            }
        }

        return parents.stream()
                .map(parent -> {
                    List<CategoryTreeDTO> subcategories = childrenByParent
                            .getOrDefault(parent.id(), List.of())
                            .stream()
                            .map(child -> new CategoryTreeDTO(
                                    child.id(),
                                    child.name(),
                                    child.normalizedName(),
                                    List.of()
                            ))
                            .toList();

                    return new CategoryTreeDTO(
                            parent.id(),
                            parent.name(),
                            parent.normalizedName(),
                            subcategories
                    );
                })
                .toList();
    }

    @Override
    public CategoryDetailsDTO findById(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT
              id,
              display_name AS name,
              normalized_name,
              active,
              parent_id
            FROM categories
            WHERE id = :id
        """)
                        .bind("id", id)
                        .map((rs, ctx) -> {
                            long parentIdRaw = rs.getLong("parent_id");
                            Long parentId = rs.wasNull() ? null : parentIdRaw;
                            return new CategoryDetailsDTO(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("normalized_name"),
                                    rs.getBoolean("active"),
                                    parentId
                            );
                        })
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
    public boolean isSubcategory(Long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
            SELECT EXISTS(
                SELECT 1
                FROM categories
                WHERE id = :id
                  AND parent_id IS NOT NULL
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
                  normalized_name,
                  active,
                  parent_id
                FROM categories
                WHERE LOWER(display_name) = LOWER(:name)
                  AND active = true
            """)
                        .bind("name", name)
                        .map((rs, ctx) -> {
                            long parentIdRaw = rs.getLong("parent_id");
                            Long parentId = rs.wasNull() ? null : parentIdRaw;
                            return new CategoryDetailsDTO(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("normalized_name"),
                                    rs.getBoolean("active"),
                                    parentId
                            );
                        })
                        .findOne()
        );
    }
}

