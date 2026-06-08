package br.com.erp.api.storage.infrastructure.adapter;

import br.com.erp.api.storage.domain.port.StoragePort;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Adapter de storage para Cloudflare R2 (ativo apenas no profile {@code prod}).
 *
 * <p>O R2 é compatível com a API S3, então reaproveita o mesmo {@link MinioClient}
 * (apontado para o endpoint do R2 via {@code minio.*} no application-prod.yml).
 * As diferenças em relação ao {@link MinioStorageAdapter}:
 * <ul>
 *   <li>Não chama {@code setBucketPolicy} — o R2 não implementa {@code PutBucketPolicy};
 *       o acesso público é configurado no painel da Cloudflare (bucket público / domínio custom).</li>
 *   <li>{@link #getPublicUrl} usa a URL pública do R2 ({@code r2.public-url}), e não o
 *       endpoint da API S3 (que não serve objetos publicamente).</li>
 * </ul>
 */
@Component
@Profile("prod")
public class R2StorageAdapter implements StoragePort {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${r2.public-url}")
    private String publicUrl;

    public R2StorageAdapter(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
            }
            // R2 não suporta PutBucketPolicy: acesso público é configurado no painel da Cloudflare.
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar bucket: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUploadUrl(String imageKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(imageKey)
                            .method(Method.PUT)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar URL de upload: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPublicUrl(String imageKey) {
        String base = publicUrl.endsWith("/")
                ? publicUrl.substring(0, publicUrl.length() - 1)
                : publicUrl;
        return String.format("%s/%s", base, imageKey);
    }

    @Override
    public void deleteImages(List<String> imageKeys) {
        if (imageKeys == null || imageKeys.isEmpty()) return;

        List<DeleteObject> objects = imageKeys.stream()
                .map(DeleteObject::new)
                .collect(Collectors.toList());

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(objects)
                        .build()
        );

        // OBRIGATÓRIO: iterar para que a exclusão aconteça de fato e erros sejam detectados
        List<String> failedKeys = new ArrayList<>();
        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                failedKeys.add(error.objectName());
            } catch (Exception e) {
                throw new RuntimeException("Erro ao verificar resultado de exclusão do R2", e);
            }
        }

        if (!failedKeys.isEmpty()) {
            throw new RuntimeException(
                    "Falha ao excluir " + failedKeys.size() + " imagem(ns) do storage: " + failedKeys
            );
        }
    }
}
