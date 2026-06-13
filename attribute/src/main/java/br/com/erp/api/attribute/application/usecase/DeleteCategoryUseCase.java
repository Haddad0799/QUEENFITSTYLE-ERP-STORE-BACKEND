package br.com.erp.api.attribute.application.usecase;

import br.com.erp.api.attribute.domain.entity.Category;
import br.com.erp.api.attribute.domain.exception.category.CategoryHasAssociatedProductsException;
import br.com.erp.api.attribute.domain.exception.category.CategoryHasPublishedProductsException;
import br.com.erp.api.attribute.domain.exception.category.CategoryHasSubcategoriesException;
import br.com.erp.api.attribute.domain.repository.CategoryRepository;
import br.com.erp.api.shared.application.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteCategoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteCategoryUseCase.class);

    private final CategoryRepository repository;

    public DeleteCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria", id));

        // Categoria pai: não pode excluir se tiver subcategorias
        if (category.isParent() && repository.hasSubcategories(id)) {
            throw new CategoryHasSubcategoriesException(category.getDisplayName());
        }

        // Bloqueio: não pode excluir se tiver produtos publicados
        if (repository.hasPublishedProducts(id)) {
            throw new CategoryHasPublishedProductsException(category.getDisplayName());
        }

        // Vínculo histórico com pedidos → exclusão lógica (desativa, não remove do banco)
        if (repository.hasProductsWithOrders(id)) {
            if (category.isActive()) {
                category.deactivate();
                repository.update(category);
            }
            log.info("Categoria {} desativada (exclusão lógica): possui produtos com pedidos vinculados", id);
            return;
        }

        // Produtos associados (sem pedidos e não publicados) ainda impedem a exclusão física
        if (repository.hasProductsAssociated(id)) {
            throw new CategoryHasAssociatedProductsException(category.getDisplayName());
        }

        // Sem nenhum vínculo → exclusão física
        repository.deleteById(id);
        log.info("Categoria {} excluída fisicamente", id);
    }
}
