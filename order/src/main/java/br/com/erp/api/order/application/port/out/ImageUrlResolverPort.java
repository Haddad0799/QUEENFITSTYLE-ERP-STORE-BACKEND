package br.com.erp.api.order.application.port.out;

/**
 * Porta de saída para resolver image_keys em URLs públicas servíveis ao front-end.
 * Mantém o módulo order desacoplado do provedor concreto (MinIO/S3/CDN).
 */
public interface ImageUrlResolverPort {

    String resolve(String imageKey);
}