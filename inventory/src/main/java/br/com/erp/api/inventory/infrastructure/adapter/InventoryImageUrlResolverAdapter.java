package br.com.erp.api.inventory.infrastructure.adapter;

import br.com.erp.api.inventory.application.port.out.ImageUrlResolverPort;
import br.com.erp.api.storage.domain.port.StoragePort;
import org.springframework.stereotype.Component;

@Component
public class InventoryImageUrlResolverAdapter implements ImageUrlResolverPort {

    private final StoragePort storagePort;

    public InventoryImageUrlResolverAdapter(StoragePort storagePort) {
        this.storagePort = storagePort;
    }

    @Override
    public String resolve(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) return null;
        return storagePort.getPublicUrl(imageKey);
    }
}
