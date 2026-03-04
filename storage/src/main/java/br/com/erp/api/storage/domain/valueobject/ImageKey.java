package br.com.erp.api.storage.domain.valueobject;

import java.util.UUID;

public class ImageKey {

    private final String value;

    private ImageKey(String value) {
        this.value = value;
    }

    public static ImageKey of(Long productId, Long colorId) {
        return new ImageKey(
                "products/" + productId + "/colors/" + colorId + "/" + UUID.randomUUID() + ".jpg"
        );
    }

    public String value() {
        return value;
    }
}