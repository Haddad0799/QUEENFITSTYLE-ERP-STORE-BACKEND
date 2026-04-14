package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.product.application.exception.AiEmptyResponseException;
import br.com.erp.api.product.application.exception.AiIntegrationException;
import br.com.erp.api.product.application.port.AiTextGeneratorPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class OpenAiTextGeneratorAdapter implements AiTextGeneratorPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiTextGeneratorAdapter.class);

    private final RestClient restClient;

    @Value("${openai.api.url:https://api.openai.com/v1/responses}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    public OpenAiTextGeneratorAdapter(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiIntegrationException("A chave da OpenAI não está configurada.");
        }

        try {
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
        } catch (AiEmptyResponseException | AiIntegrationException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.error("Erro HTTP ao gerar descrição com OpenAI. status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new AiIntegrationException("Falha ao integrar com o provedor de IA.", ex);
        } catch (ResourceAccessException ex) {
            log.error("Erro de conexão ao gerar descrição com OpenAI", ex);
            throw new AiIntegrationException("Não foi possível conectar ao provedor de IA.", ex);
        } catch (Exception ex) {
            log.error("Erro inesperado ao gerar descrição com OpenAI", ex);
            throw new AiIntegrationException("Erro inesperado ao gerar descrição com IA.", ex);
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
