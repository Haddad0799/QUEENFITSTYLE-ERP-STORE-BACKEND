package br.com.erp.api.storage.domain.valueobject;

import java.util.Set;
import java.util.UUID;

public class ImageKey {

    private final String value;

    private ImageKey(String value) {
        this.value = value;
    }

    public static ImageKey of(Long productId, Long colorId, String filename) {

        Set<String> allowed = Set.of("jpg", "jpeg", "png", "webp");

        String ext = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
                : "jpg";

        if (!allowed.contains(ext)) {
            throw new IllegalArgumentException("Formato não suportado: " + ext);
        }

        return new ImageKey(
                "products/" + productId + "/colors/" + colorId + "/" + UUID.randomUUID() + "." + ext
        );
    }

    public String getValue() {
        return value;
    }
}