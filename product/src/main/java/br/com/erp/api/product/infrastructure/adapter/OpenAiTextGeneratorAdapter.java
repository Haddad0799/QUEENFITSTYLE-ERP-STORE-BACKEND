package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.product.application.exception.AiEmptyResponseException;
import br.com.erp.api.product.application.exception.AiIntegrationException;
import br.com.erp.api.product.application.port.AiTextGeneratorPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;

@Component
public class OpenAiTextGeneratorAdapter implements AiTextGeneratorPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiTextGeneratorAdapter.class);

    private final RestClient restClient;
    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final int maxRetries;
    private final long retryBackoffMs;

    public OpenAiTextGeneratorAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${openai.api.url:https://api.openai.com/v1/responses}") String apiUrl,
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model,
            @Value("${openai.api.connect-timeout-ms:5000}") long connectTimeoutMs,
            @Value("${openai.api.read-timeout-ms:30000}") long readTimeoutMs,
            @Value("${openai.api.max-retries:2}") int maxRetries,
            @Value("${openai.api.retry-backoff-ms:500}") long retryBackoffMs
    ) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .withReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = restClientBuilder
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.maxRetries = Math.max(0, maxRetries);
        this.retryBackoffMs = Math.max(0, retryBackoffMs);
    }

    @Override
    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiIntegrationException("A chave da OpenAI não está configurada.");
        }

        AiIntegrationException lastTransientError = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return callProvider(prompt);
            } catch (AiEmptyResponseException | AiIntegrationException ex) {
                throw ex;
            } catch (RestClientResponseException ex) {
                if (isTransient(ex) && attempt < maxRetries) {
                    log.warn("Falha transitória ao gerar descrição com OpenAI (tentativa {}/{}). status={}",
                            attempt + 1, maxRetries + 1, ex.getStatusCode());
                    lastTransientError = new AiIntegrationException("Falha ao integrar com o provedor de IA.", ex);
                    backoff(attempt);
                    continue;
                }
                log.error("Erro HTTP ao gerar descrição com OpenAI. status={}, body={}",
                        ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
                throw new AiIntegrationException("Falha ao integrar com o provedor de IA.", ex);
            } catch (ResourceAccessException ex) {
                if (attempt < maxRetries) {
                    log.warn("Erro de conexão/timeout ao gerar descrição com OpenAI (tentativa {}/{}).",
                            attempt + 1, maxRetries + 1, ex);
                    lastTransientError = new AiIntegrationException("Não foi possível conectar ao provedor de IA.", ex);
                    backoff(attempt);
                    continue;
                }
                log.error("Erro de conexão ao gerar descrição com OpenAI", ex);
                throw new AiIntegrationException("Não foi possível conectar ao provedor de IA.", ex);
            } catch (Exception ex) {
                log.error("Erro inesperado ao gerar descrição com OpenAI", ex);
                throw new AiIntegrationException("Erro inesperado ao gerar descrição com IA.", ex);
            }
        }

        throw lastTransientError != null
                ? lastTransientError
                : new AiIntegrationException("Falha ao integrar com o provedor de IA.");
    }

    private String callProvider(String prompt) {
        OpenAiResponse response = restClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(new OpenAiRequest(model, prompt, 350))
                .retrieve()
                .body(OpenAiResponse.class);

        String text = extractText(response);
        if (text == null || text.isBlank()) {
            throw new AiEmptyResponseException("O provedor de IA retornou uma resposta vazia.");
        }

        return text.trim();
    }

    private boolean isTransient(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        return status == 429 || ex.getStatusCode().is5xxServerError();
    }

    private void backoff(int attempt) {
        if (retryBackoffMs == 0) {
            return;
        }
        try {
            Thread.sleep(retryBackoffMs * (attempt + 1L));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new AiIntegrationException("Geração de descrição interrompida.", ie);
        }
    }

    private String extractText(OpenAiResponse response) {
        if (response == null) {
            throw new AiEmptyResponseException("O provedor de IA não retornou conteúdo.");
        }

        if (response.outputText() != null && !response.outputText().isBlank()) {
            return response.outputText();
        }

        if (response.output() == null) {
            return null;
        }

        return response.output().stream()
                .filter(item -> item.content() != null)
                .flatMap(item -> item.content().stream())
                .map(OpenAiContent::text)
                .filter(text -> text != null && !text.isBlank())
                .reduce((first, second) -> first + "\n\n" + second)
                .orElse(null);
    }

    private record OpenAiRequest(
            String model,
            String input,
            @JsonProperty("max_output_tokens")
            Integer maxOutputTokens
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiResponse(
            @JsonProperty("output_text")
            String outputText,
            List<OpenAiOutputItem> output
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiOutputItem(
            String type,
            List<OpenAiContent> content
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiContent(
            String type,
            String text
    ) {
    }
}
