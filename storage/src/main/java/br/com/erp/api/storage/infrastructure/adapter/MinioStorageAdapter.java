package br.com.erp.api.storage.infrastructure.adapter;

import br.com.erp.api.storage.domain.port.StoragePort;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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

    @Override
    public String generatePresignedUploadUrl(String imageKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(imageKey)
                            .expiry(15, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar URL pré-assinada: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPublicUrl(String imageKey) {
        return minioUrl + "/" + bucket + "/" + imageKey;
    }
}