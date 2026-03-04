package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.dto.PresignedUrlResult;
import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.application.provider.ImageProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestImageUploadUseCase {

    private final ImageProvider imageProvider;
    private final StorageGateway storageGateway;

    public RequestImageUploadUseCase(
            ImageProvider imageProvider,
            StorageGateway storageGateway
    ) {
        this.imageProvider = imageProvider;
        this.storageGateway = storageGateway;
    }

    public List<PresignedUrlResult> execute(Long productId, Long colorId, int quantity) {

        if (quantity < 1 || quantity > 5) {
            throw new IllegalArgumentException("Quantidade deve ser entre 1 e 5");
        }

        int existing = imageProvider.countByProductIdAndColorId(productId, colorId);

        if (existing + quantity > 5) {
            throw new IllegalArgumentException(
                    "Limite de 5 imagens por cor. Já existem " + existing + " imagens cadastradas."
            );
        }

        return storageGateway.generatePresignedUrls(productId, colorId, quantity);
    }
}