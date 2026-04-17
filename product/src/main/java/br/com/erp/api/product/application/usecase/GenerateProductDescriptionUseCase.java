package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.AiEmptyResponseException;
import br.com.erp.api.product.application.port.AiTextGeneratorPort;
import br.com.erp.api.product.presentation.dto.request.GenerateProductDescriptionRequest;
import br.com.erp.api.product.presentation.dto.response.GenerateProductDescriptionResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenerateProductDescriptionUseCase {

    private final AiTextGeneratorPort aiTextGeneratorPort;

    public GenerateProductDescriptionUseCase(AiTextGeneratorPort aiTextGeneratorPort) {
        this.aiTextGeneratorPort = aiTextGeneratorPort;
    }

    public GenerateProductDescriptionResponse execute(GenerateProductDescriptionRequest request) {
        validateRequest(request);

        String prompt = buildPrompt(request);
        String generatedDescription = aiTextGeneratorPort.generateText(prompt);

        if (generatedDescription == null || generatedDescription.isBlank()) {
            throw new AiEmptyResponseException("A IA retornou uma descrição vazia.");
        }

        return new GenerateProductDescriptionResponse(generatedDescription.trim());
    }

    private void validateRequest(GenerateProductDescriptionRequest request) {
        if (!hasText(request.productName())) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }

        if (!hasText(request.categoryName())) {
            throw new IllegalArgumentException("A categoria do produto é obrigatória.");
        }

        boolean hasContext = hasText(request.subcategoryName())
                || hasText(request.brand())
                || hasText(request.material())
                || hasText(request.color())
                || hasText(request.fit())
                || hasText(request.targetAudience())
                || hasHighlights(request.highlights())
                || hasText(request.additionalDetails());

        if (!hasContext) {
            throw new IllegalArgumentException(
                    "Informe ao menos um atributo adicional do produto para gerar a descrição."
            );
        }
    }

    private String buildPrompt(GenerateProductDescriptionRequest request) {
        String highlights = formatHighlights(request.highlights());

        return """
                Você é um assistente especializado em escrever descrições de produtos para e-commerce de moda fitness em português do Brasil.
                
                Sua tarefa é gerar uma descrição comercial clara, atrativa e profissional com base exclusivamente nos dados informados pelo usuário no ERP.
                
                Dados do produto:
                - Nome do produto: %s
                - Categoria: %s
                - Subcategoria: %s
                - Marca: %s
                - Material: %s
                - Cor: %s
                - Modelagem: %s
                - Público-alvo: %s
                - Destaques: %s
                - Detalhes adicionais: %s
                
                Objetivo:
                Gerar uma descrição que ajude o cliente final a entender os principais benefícios do produto durante o uso em treinos e atividades físicas, aumentando a atratividade da página no e-commerce.
                
                Regras obrigatórias:
                - Escreva em português do Brasil.
                - Use tom profissional, natural e comercial.
                - Produza exatamente 2 parágrafos curtos.
                - Destaque apenas benefícios reais do produto com base nas informações fornecidas.
                - Priorize benefícios relacionados a treino, como conforto durante o movimento, liberdade, respirabilidade, ajuste ao corpo, praticidade e segurança.
                - Relacione as características do produto com o desempenho em atividades físicas sempre que fizer sentido (ex: mobilidade, leveza, ventilação, firmeza).
                - Valorize estilo e estética, mas com foco no contexto de academia e treino.
                - Não invente características, tecnologias, funcionalidades ou diferenciais não informados.
                - Não mencione tecnologias específicas (como "tecido tecnológico", "compressão", "dry fit") se não estiverem nos dados.
                - Não use emojis.
                - Não use listas, títulos, aspas ou frases introdutórias.
                - Evite frases genéricas como "produto de alta qualidade" sem justificar com os dados recebidos.
                - Se alguma informação não tiver sido informada, não mencione esse atributo.
                - O texto deve ficar pronto para uso direto na página de produto de um e-commerce.
                
                Retorne apenas a descrição final do produto.
                """
                .formatted(
                        safe(request.productName()),
                        safe(request.categoryName()),
                        safe(request.subcategoryName()),
                        safe(request.brand()),
                        safe(request.material()),
                        safe(request.color()),
                        safe(request.fit()),
                        safe(request.targetAudience()),
                        highlights,
                        safe(request.additionalDetails())
                );
    }

    private String formatHighlights(List<String> highlights) {
        if (!hasHighlights(highlights)) {
            return "não informado";
        }

        return highlights.stream()
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private String safe(String value) {
        return hasText(value) ? value.trim() : "não informado";
    }

    private boolean hasHighlights(List<String> highlights) {
        return highlights != null && highlights.stream().anyMatch(this::hasText);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}