package br.com.erp.api.product.domain.exception;

import br.com.erp.api.shared.domain.exception.DomainException;

import java.util.List;

public class DuplicateSkuCombinationException extends DomainException {

    private final List<SkuConflictDetail> conflicts;

    public DuplicateSkuCombinationException(List<SkuConflictDetail> conflicts) {
        super("Algumas combinações já existem.");
        this.conflicts = conflicts;
    }

    public List<SkuConflictDetail> getConflicts() {
        return conflicts;
    }
}