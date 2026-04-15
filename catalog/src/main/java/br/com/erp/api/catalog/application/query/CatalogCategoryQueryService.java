package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.presentation.dto.CatalogNavigationCategoryDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogCategoryQueryService {

    private final CatalogCategoryQueryRepository queryRepository;

    public CatalogCategoryQueryService(CatalogCategoryQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public List<CatalogNavigationCategoryDTO> listNavigableCategories() {
        List<CatalogCategoryNodeView> rows = queryRepository.findNavigableCategories();
        Map<Long, CategoryNode> nodesById = new LinkedHashMap<>();
        List<CategoryNode> roots = new ArrayList<>();

        for (CatalogCategoryNodeView row : rows) {
            nodesById.put(row.id(), new CategoryNode(
                    row.id(),
                    row.name(),
                    row.slug(),
                    row.parentId(),
                    row.directProductCount()
            ));
        }

        for (CategoryNode node : nodesById.values()) {
            if (node.parentId() == null) {
                roots.add(node);
                continue;
            }

            CategoryNode parent = nodesById.get(node.parentId());
            if (parent == null) {
                roots.add(node);
                continue;
            }

            parent.children().add(node);
        }

        return roots.stream()
                .map(this::toDto)
                .toList();
    }

    private CatalogNavigationCategoryDTO toDto(CategoryNode node) {
        List<CatalogNavigationCategoryDTO> subcategories = node.children().stream()
                .map(this::toDto)
                .toList();

        long productCount = node.directProductCount();
        for (CatalogNavigationCategoryDTO subcategory : subcategories) {
            productCount += subcategory.productCount();
        }

        return new CatalogNavigationCategoryDTO(
                node.id(),
                node.name(),
                node.slug(),
                productCount,
                subcategories
        );
    }

    private static final class CategoryNode {
        private final Long id;
        private final String name;
        private final String slug;
        private final Long parentId;
        private final long directProductCount;
        private final List<CategoryNode> children = new ArrayList<>();

        private CategoryNode(Long id, String name, String slug, Long parentId, long directProductCount) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.parentId = parentId;
            this.directProductCount = directProductCount;
        }

        private Long id() {
            return id;
        }

        private String name() {
            return name;
        }

        private String slug() {
            return slug;
        }

        private Long parentId() {
            return parentId;
        }

        private long directProductCount() {
            return directProductCount;
        }

        private List<CategoryNode> children() {
            return children;
        }
    }
}
