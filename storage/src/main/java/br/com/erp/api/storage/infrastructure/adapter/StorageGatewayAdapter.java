package br.com.erp.api.storage.infrastructure.adapter;

import br.com.erp.api.product.application.dto.PresignedUrlResult;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.storage.domain.port.StoragePort;
import br.com.erp.api.storage.domain.valueobject.ImageKey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class StorageGatewayAdapter implements StorageGateway {

    private final StoragePort storagePort;

    public StorageGatewayAdapter(StoragePort storagePort) {
        this.storagePort = storagePort;
    }

    @Override
    public List<PresignedUrlResult> generatePresignedUrls(Long productId, Long colorId, int quantity) {
        return IntStream.range(0, quantity)
                .mapToObj(i -> {
                    ImageKey imageKey = ImageKey.of(productId, colorId);
                    String uploadUrl = storagePort.generatePresignedUploadUrl(imageKey.value());
                    return new PresignedUrlResult(uploadUrl, imageKey.value());
                })
                .toList();
    }

    @Override
    public String getPublicUrl(String imageKey) {
        return storagePort.getPublicUrl(imageKey);
    }
}