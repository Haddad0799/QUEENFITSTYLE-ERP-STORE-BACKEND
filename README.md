# QueenFitStyle ERP

ERP modular para e-commerce de moda fitness, desenvolvido em **Java 21 + Spring Boot 3**, aplicando na prática os princípios de **Clean Architecture**, **Domain-Driven Design (DDD)** e **Arquitetura Hexagonal (Ports & Adapters)**.

> **Estratégia Monolith First** — um monólito modular com fronteiras claras entre módulos, pensado desde o início para uma migração fluida para microsserviços quando necessário.

---

## Arquitetura

```
queenfitstyle/
├── app/              ← Bootstrap da aplicação (Spring Boot main, configs, exception handler)
├── product/          ← Produtos, SKUs, imagens, importação em massa, publicação
├── catalog/          ← Vitrine pública (snapshots desnormalizados, filtros dinâmicos)
├── attribute/        ← Categorias, Cores, Tamanhos (dados cadastrais)
├── inventory/        ← Controle de estoque
├── pricing/          ← Precificação (custo e venda)
├── storage/          ← Upload de imagens (MinIO / S3)
└── shared/           ← Migrations Flyway, exceções base, projeções compartilhadas
```

Cada módulo segue a mesma estrutura interna de camadas:

```
module/
├── domain/           ← Entidades, Value Objects, Ports, Exceções (ZERO dependência de framework)
├── application/      ← Use Cases, Services, Events, DTOs, Providers (orquestração)
├── infrastructure/   ← Adapters JDBI, Listeners, Parsers (implementações concretas)
└── presentation/     ← Controllers REST, DTOs de request/response
```

### Por que isso importa?

- O **domínio é puro Java** — nenhuma entidade usa `@Entity`, `@Column` ou qualquer anotação de framework
- Os **Ports** (interfaces) pertencem ao domínio; os **Adapters** (implementações) ficam na infraestrutura
- Use Cases dependem apenas de abstrações — nunca de banco, HTTP ou frameworks
- Módulos se comunicam via **interfaces (Providers/Gateways)** e **eventos de domínio**, nunca por acesso direto

---

## Decisões Técnicas Relevantes

### JDBI no lugar de JPA/Hibernate

Optei por **JDBI com queries nativas** ao invés de JPA para:

- Manter o domínio **100% livre de anotações** de persistência
- Ter **controle total sobre o SQL** gerado (sem surpresas do Hibernate)
- Usar **batch inserts otimizados** para importação em massa
- Construir **queries dinâmicas** com filtros compostos no catálogo

As entidades são reconstruídas via métodos `restore()` / construtores de reconstituição, mantendo a separação entre domínio e persistência.

### Catálogo como Projeção (CQRS Simplificado)

O módulo `catalog` não é um CRUD — é uma **projeção de leitura otimizada**:

1. O `product` module publica um evento `ProductPublishedEvent` contendo um **snapshot completo**
2. O `CatalogEventListener` escuta o evento **após o commit** (`@TransactionalEventListener(AFTER_COMMIT)`)
3. O `CatalogSyncService` faz um **replace atômico** (DELETE + INSERT em transação) nas tabelas desnormalizadas
4. O catálogo de loja consulta essas tabelas com **queries otimizadas e filtros dinâmicos** (categoria, cor, tamanho, faixa de preço, busca por nome)

Isso garante que a vitrine pública seja **rápida, independente e resiliente** — sem JOINs complexos em runtime.

### Revalidação de Cache ISR (On-Demand)

Após persistir o snapshot no catálogo, o backend notifica automaticamente o frontend Next.js para invalidar o cache estático (ISR) via webhook:

```
Evento (publish/unpublish)
  → CatalogEventListener (AFTER_COMMIT, @Async)
    → CatalogSyncService (persiste snapshot)
      → CatalogRevalidationPort → NextJsRevalidationAdapter
        → POST /api/revalidate { tags: ["catalog-products", "catalog-product-{slug}"] }
          → Next.js revalidateTag()
```

- A interface `CatalogRevalidationPort` pertence ao domínio — o adapter HTTP é infraestrutura
- Tags invalidadas: `catalog-products` (listagem) + `catalog-product-{slug}` (detalhe)
- Falhas no webhook **não propagam** para a transação principal (resiliência via try-catch)
- Secret compartilhado via header `x-revalidate-secret` para autenticação

Isso permite que páginas estáticas do Next.js sejam atualizadas em segundos após qualquer mudança no catálogo, sem rebuild completo.

### Upload de Imagens via Pre-signed URLs

O fluxo de upload é em duas etapas, garantindo segurança:

1. **Request** → `POST /erp/products/{id}/images/upload-url` retorna Pre-signed URLs do MinIO (válidas por 1h)
2. O frontend faz upload direto para o MinIO (o backend **nunca** recebe o binário)
3. **Confirm** → `POST /erp/products/{id}/images/confirm` registra as imagens e reavalia os status dos SKUs

Regras de negócio:
- Máximo de **5 imagens por cor** por produto
- A primeira imagem se torna automaticamente a **imagem principal**
- Upload dispara **reavaliação automática** de completude dos SKUs

### Importação em Massa de Produtos

O `ImportProductsFromFileUseCase` processa planilhas Excel (.xlsx) com validação inteligente:

1. **Parse** — Apache POI extrai as linhas via `ProductImportParserPort` (interface na application, implementação na infra)
2. **Validação linha a linha** — rejeita linhas inválidas sem abortar o resto
3. **Agrupamento** — agrupa SKUs por produto (nome + categoria)
4. **Processamento por grupo** — cada grupo roda em `REQUIRES_NEW` (isolamento transacional)
5. **Resolução de atributos** — categoria, cor e tamanho são resolvidos pelo nome via Providers
6. **Batch insert** — SKUs são inseridos em lote com tratamento de duplicidade pelo banco
7. **Inicialização cruzada** — estoque e preço são inicializados via Gateways para os módulos `inventory` e `pricing`

Resultado: um `ImportResult` com total processado, sucessos e lista de erros por produto.

### Máquina de Estados de Produto e SKU

O ciclo de vida é gerenciado por **regras de domínio**, não por flags:

```
SKU:       INCOMPLETE → READY → PUBLISHED → BLOCKED / DISCONTINUED
Product:   DRAFT → READY_FOR_SALE → PUBLISHED → INACTIVE / ARCHIVED
```

- `EvaluateSkuCompletenessUseCase` — reavalia se um SKU está completo (tem preço, estoque, imagem)
- `EvaluateProductStatusUseCase` — reavalia se o produto deve ser publicado, despublicado ou voltar a rascunho
- Toda mudança em preço, estoque ou imagem **dispara reavaliação automática em cascata**

### Value Objects com Validação no Domínio

```java
public record Slug(String value) {
    // Gerado automaticamente a partir do nome, sem acentos, lowercase, hifenizado
    public static Slug fromName(String name) { ... }
}

public record SkuCode(String value) {
    // Regex: ^[A-Z0-9]+(-[A-Z0-9]+)*$ — mínimo 3, máximo 50 caracteres
}

public record Dimensions(BigDecimal width, BigDecimal height, BigDecimal length, BigDecimal weight) {
    // Todos devem ser positivos — validação no construtor compacto do record
}

public record CategoryName(String displayName, String normalizedName) {
    // "Calças" → displayName: "Calças", normalizedName: "CALCAS"
    // Usado para filtros URL-safe no catálogo
}
```

### Tratamento de Erros com RFC 7807 (Problem Detail)

O `GlobalExceptionHandler` retorna erros padronizados seguindo a RFC 7807:

```json
{
  "type": "https://example.com/probs/duplicate-sku-combination",
  "title": "Combinação de SKU já existente",
  "status": 422,
  "detail": "Algumas combinações já existem.",
  "conflicts": [{ "colorName": "Preto", "sizeName": "M", "existingSkuCode": "QFS-PRETO-M" }],
  "timestamp": "2025-10-15T14:30:00",
  "path": "/erp/products/1/skus"
}
```

Hierarquia de handlers: Específico → Domain → NotFound → Genérico.

---

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (Records, Sealed Classes, Pattern Matching) |
| Framework | Spring Boot 3.3 |
| Persistência | JDBI 3 (queries nativas, sem ORM) |
| Banco de Dados | PostgreSQL 15 |
| Migrations | Flyway |
| Object Storage | MinIO (compatível S3) |
| Parsing Excel | Apache POI |
| Documentação API | SpringDoc / Swagger UI |
| Infraestrutura | Docker Compose |
| Build | Maven (multi-module) |

---

## API Endpoints

### ERP (Back-office)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/erp/products` | Criar produto |
| `PATCH` | `/erp/products/{id}` | Alterar produto |
| `POST` | `/erp/products/{id}/publish` | Publicar produto no catálogo |
| `POST` | `/erp/products/import` | Importação em massa (Excel) |
| `POST` | `/erp/products/{id}/images/upload-url` | Solicitar URLs de upload |
| `POST` | `/erp/products/{id}/images/confirm` | Confirmar imagens enviadas |
| `POST` | `/erp/products/{id}/skus` | Adicionar SKU ao produto |
| `GET` | `/erp/categories` | Listar todas as categorias |
| `POST` | `/erp/categories` | Criar categoria |

### Store (Vitrine Pública)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/store/products` | Listar produtos com filtros |
| `GET` | `/store/products/{slug}` | Detalhes do produto (cores, SKUs, imagens) |
| `GET` | `/store/products/{slug}/skus/{code}` | Detalhes de um SKU específico |
| `GET` | `/store/categories` | Categorias ativas (nome + nome normalizado) |

**Filtros do catálogo:** `?category=CALCAS&color=Preto&minPrice=50&maxPrice=200&search=legging&sizeName=M`

---

## Como Executar

### Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Maven 3.9+ (ou use o wrapper `./mvnw`)

### Setup

```bash
# 1. Clone o repositório
git clone https://github.com/Haddad0799/queenfitstyle-erp.git
cd queenfitstyle-erp

# 2. Configure as variáveis de ambiente
cp .env.example .env
# Edite o .env com suas credenciais

# 3. Suba a infraestrutura
docker-compose up -d

# 4. Execute a aplicação
./mvnw spring-boot:run -pl app
```

A aplicação estará disponível em `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Estrutura de Módulos Maven

```xml
queenfitstyle (parent POM)
├── app          → Spring Boot main, configs, exception handler
├── shared       → Flyway migrations, exceções base, projeções
├── attribute    → Categorias, Cores, Tamanhos
├── product      → Produtos, SKUs, Imagens, Importação, Publicação
├── catalog      → Vitrine pública (projeção desnormalizada)
├── inventory    → Controle de estoque
├── pricing      → Precificação
└── storage      → Upload MinIO (Pre-signed URLs)
```

---

## Padrões e Práticas Aplicadas

- **Hexagonal Architecture** — Ports & Adapters com domínio independente
- **Domain-Driven Design** — Entidades ricas, Value Objects imutáveis, Aggregates
- **CQRS simplificado** — Separação de escrita (Product) e leitura (Catalog)
- **Event-Driven** — `@TransactionalEventListener(AFTER_COMMIT)` para sincronização assíncrona
- **Monolith First** — Módulos isolados com fronteiras claras para futura decomposição
- **RFC 7807** — Respostas de erro padronizadas com `ProblemDetail`
- **Batch Processing** — Importação em massa com isolamento transacional por grupo
- **Pre-signed URLs** — Upload seguro sem tráfego de binários pelo backend
- **ISR On-Demand** — Revalidação de cache do Next.js via webhook após mudanças no catálogo
- **Flyway** — Versionamento incremental do schema (19 migrations)
- **Provider Pattern** — Comunicação entre módulos via interfaces, sem acoplamento direto

---

## Licença

Este projeto é de uso pessoal e educacional.

---

Desenvolvido por [Lucas Haddad](https://github.com/Haddad0799)

