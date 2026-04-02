package br.com.erp.api.catalog.infrastructure.webhook;

import br.com.erp.api.catalog.domain.port.CatalogRevalidationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class NextJsRevalidationAdapter implements CatalogRevalidationPort {

    private static final Logger log = LoggerFactory.getLogger(NextJsRevalidationAdapter.class);

    private final RestClient restClient;

    @Value("${nextjs.revalidate.url}")
    private String revalidateUrl;

    @Value("${nextjs.revalidate.secret}")
    private String secret;

    public NextJsRevalidationAdapter(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public void revalidate(List<String> tags) {
        try {
            log.info("Enviando revalidação para Next.js — tags: {}", tags);

            restClient.post()
                    .uri(revalidateUrl)
                    .header("x-revalidate-secret", secret)
                    .body(Map.of("tags", tags))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Revalidação enviada com sucesso para tags: {}", tags);
        } catch (Exception ex) {
            log.error("Falha ao enviar revalidação para Next.js — tags: {}", tags, ex);
        }
    }
}

