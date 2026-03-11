package br.com.erp.api.storage.infrastructure.adapter;

import br.com.erp.api.storage.domain.port.StoragePort;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class MinioStorageAdapter implements StoragePort {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.url}")
    private String minioUrl;

    public MinioStorageAdapter(MinioClient minioClient) {
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

            String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [{
                        "Effect": "Allow",
                        "Principal": {"AWS": ["*"]},
                        "Action": ["s3:GetObject"],
                        "Resource": ["arn:aws:s3:::%s/*"]
                    }]
                }
                """, bucket);

            minioClient.setBucketPolicy(
                   SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(policy)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar bucket: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUploadUrl(String imageKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
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
        return String.format("%s/%s/%s", minioUrl, bucket, imageKey);
    }

    @Override
    public void deleteImages(java.util.List<String> imageKeys) {
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
                throw new RuntimeException("Erro ao verificar resultado de exclusão do Minio", e);
            }
        }

        if (!failedKeys.isEmpty()) {
            throw new RuntimeException(
                    "Falha ao excluir " + failedKeys.size() + " imagem(ns) do storage: " + failedKeys
            );
        }
    }
}