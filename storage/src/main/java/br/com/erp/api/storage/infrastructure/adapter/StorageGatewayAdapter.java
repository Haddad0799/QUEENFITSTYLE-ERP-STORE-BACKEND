package br.com.erp.api.storage.infrastructure.adapter;

import br.com.erp.api.product.application.dto.PresignedUrlResult;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.storage.domain.port.StoragePort;
import br.com.erp.api.storage.domain.valueobject.ImageKey;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StorageGatewayAdapter implements StorageGateway {

    private final StoragePort storagePort;

    public StorageGatewayAdapter(StoragePort storagePort) {
        this.storagePort = storagePort;
    }

    @Override
    public List<PresignedUrlResult> generatePresignedUrls(Long productId, Long colorId, List<String> filenames) {
        return filenames.stream()
                .map(filename -> {
                    ImageKey imageKey = ImageKey.of(productId, colorId, filename);
                    String uploadUrl = storagePort.generatePresignedUploadUrl(imageKey.getValue());
                    return new PresignedUrlResult(uploadUrl, imageKey.getValue());
                })
                .toList();
    }

    @Override
    public String getPublicUrl(String imageKey) {
        return storagePort.getPublicUrl(imageKey);
    }

    @Override
    public void deleteImages(List<String> imageKeys) {
        storagePort.deleteImages(imageKeys);
    }
}