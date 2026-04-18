# QueenFitStyle Backend

Backend de um ecommerce de moda fitness feminina com ERP integrado, separado entre operacao interna e storefront publica.

O projeto foi desenhado para resolver um problema comum em ecommerce: o produto e cadastrado no ERP, mas a loja precisa de um catalogo muito mais enxuto, consistente e rapido para consulta. Aqui, essas duas visoes convivem no mesmo backend sem misturar responsabilidades.

## O que este projeto entrega

- API administrativa para cadastro e operacao de produtos, SKUs, categorias, imagens, preco e estoque
- API publica para vitrine, filtros, navegacao por categorias e detalhe de produto
- catalogo denormalizado para leitura rapida pela loja
- publicacao orientada a eventos entre ERP e vitrine
- upload de imagens com pre-signed URLs, sem trafegar arquivo pelo backend
- importacao em lote via planilha com validacao detalhada e processamento por grupo
- revalidacao automatica do frontend Next.js apos mudancas de catalogo
- geracao assistida por IA para descricao comercial de produtos

## Por que esse projeto e interessante

Este backend nao e apenas um CRUD de produtos. Ele modela um fluxo real de operacao de ecommerce:

- um produto nasce no ERP
- recebe SKUs, imagens, preco e estoque
- passa por validacoes de consistencia
- e publicado
- gera um snapshot otimizado para a loja
- dispara revalidacao de cache no frontend

Na pratica, isso reduz inconsistencias entre backoffice e vitrine, melhora performance de leitura e deixa o frontend focado em experiencia de compra, nao em montar regras de negocio.

## Arquitetura

O sistema segue uma abordagem de monolito modular, organizado por dominio.

Cada modulo e estruturado em camadas:

- `application`: casos de uso, servicos, queries, assemblers
- `domain`: entidades, regras centrais, value objects, portas
- `infrastructure`: persistencia, adaptadores, integracoes externas
- `presentation`: controllers e DTOs HTTP

Essa separacao deixa o codigo mais facil de evoluir, testar e manter, sem espalhar regra de negocio em controller ou SQL solto em qualquer lugar.

Tambem ha um uso claro de portas e adaptadores:

- portas para repositorios, storage, IA, catalogo e integracoes
- adaptadores para PostgreSQL/JDBI, MinIO, OpenAI e webhook de revalidacao

## Modulos do sistema

### `app`

Modulo de bootstrap da aplicacao Spring Boot.

### `attribute`

Responsavel por atributos mestres do catalogo:

- categorias
- arvores de navegacao
- cores
- tamanhos

### `product`

Nucleo do ERP de produto:

- CRUD de produtos
- criacao e manutencao de SKUs
- definicao de imagem principal
- controle de imagens por cor
- importacao em lote
- geracao de descricao com IA
- montagem de snapshot para publicacao

### `inventory`

Responsavel pelo controle de estoque e movimentos.

### `pricing`

Responsavel pela precificacao dos SKUs.

### `storage`

Integracao com MinIO/S3 para upload e publicacao de imagens.

### `catalog`

Read model da loja:

- persiste um snapshot desnormalizado
- entrega listagem, filtros e detalhe de produto
- separa a visao publica da modelagem operacional do ERP

### `shared`

Codigo compartilhado e migracoes de banco com Flyway.

## Fluxo principal de publicacao

1. O produto e criado no ERP.
2. Os SKUs sao cadastrados com cor, tamanho, dimensoes, preco e estoque.
3. As imagens sao enviadas por pre-signed URL e vinculadas por cor.
4. O sistema avalia a completude dos SKUs e o status do produto.
5. Ao publicar, o `SnapshotAssembler` monta um retrato completo do produto.
6. Um evento de publicacao atualiza o modulo de catalogo.
7. O catalogo substitui o snapshot antigo de forma idempotente.
8. O backend chama a revalidacao do Next.js por tags.

Esse fluxo garante separacao entre operacao e vitrine, sem deixar o frontend dependente de joins complexos ou logica espalhada.

## Regra de vitrine orientada pela imagem principal

Um diferencial importante da implementacao atual e a logica de vitrine baseada na imagem principal do produto.

Em vez de mostrar sempre o menor preco global do produto, o snapshot agora respeita a cor representada pela `mainImageUrl`:

- a imagem principal determina a `mainColor`
- dentro dessa cor, o sistema escolhe o SKU vendavel mais barato
- esse SKU vira a `defaultSelection`
- o preco exibido passa a ser o `displayPrice` dessa selecao

Com isso, a vitrine deixa de mostrar combinacoes incoerentes como:

- imagem principal de uma cor
- preco pertencente a outra cor

Esse tipo de detalhe parece pequeno, mas tem impacto direto em UX, consistencia comercial e confianca do catalogo.

## Regras de negocio relevantes

- produto possui ciclo de vida: `DRAFT`, `READY_FOR_SALE`, `PUBLISHED`, `INACTIVE`, `ARCHIVED`
- SKU possui ciclo de vida: `INCOMPLETE`, `READY`, `PUBLISHED`, `BLOCKED`, `DISCONTINUED`
- SKU so avanca quando possui informacoes suficientes e imagem associada
- alteracoes em imagem, estoque e preco podem recalcular status e snapshot
- catalogo publico nao consulta a modelagem operacional diretamente; consome o snapshot publicado

## Funcionalidades implementadas

### ERP / administracao

- cadastro, edicao, listagem e exclusao de produtos
- publicacao manual de produto
- importacao em lote via arquivo Excel
- listagem e gerenciamento de SKUs por produto
- atualizacao de dimensoes
- atualizacao de preco
- registro de movimentacoes de estoque
- exclusao em lote de SKUs
- upload de imagens por cor
- confirmacao de upload sem trafegar binario pelo backend
- remocao e reordenacao de imagens
- definicao da imagem principal do produto
- geracao de descricao comercial com IA
- CRUD de categorias com ativacao, desativacao e arvore
- consulta de cores e tamanhos

### Storefront / loja

- listagem paginada de produtos
- filtro por categoria, cor, tamanho, faixa de preco e busca textual
- endpoint de filtros disponiveis
- endpoint de categorias navegaveis
- detalhe de produto com grupos de cor, imagens e SKUs
- detalhe de SKU por slug do produto + codigo do SKU

## API em alto nivel

### ERP

- `GET /erp/products`
- `POST /erp/products`
- `PATCH /erp/products/{id}`
- `POST /erp/products/{id}/publish`
- `POST /erp/products/import`
- `GET /erp/products/{productId}/skus`
- `POST /erp/products/{productId}/skus`
- `POST /erp/products/{productId}/skus/{skuId}/stock/movements`
- `PUT /erp/products/{productId}/skus/{skuId}/price`
- `POST /erp/products/{productId}/colors/{colorId}/images/upload-urls`
- `POST /erp/products/{productId}/colors/{colorId}/images`
- `PATCH /erp/products/{productId}/primary-image`
- `GET /erp/categories`
- `GET /erp/categories/tree`

### Store

- `GET /store/products`
- `GET /store/products/{slug}`
- `GET /store/products/{slug}/skus/{skuCode}`
- `GET /store/catalog/filters`
- `GET /store/catalog/categories`
- `GET /store/categories`

## Diferenciais tecnicos

- separacao clara entre write model do ERP e read model do catalogo
- monolito modular com fronteiras de dominio bem definidas
- uso de eventos de aplicacao para sincronizacao entre modulos
- snapshot de catalogo pensado para performance de leitura
- persistencia com JDBI e SQL explicito, sem esconder regra critica
- migracoes com Flyway versionadas junto ao codigo
- upload direto em storage via URLs assinadas
- revalidacao por tags no Next.js apos publicacao e despublicacao
- suporte a OpenAPI/Swagger e Actuator
- testes automatizados com JUnit e suporte a H2/Testcontainers

## Stack

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Data Commons
- PostgreSQL
- JDBI
- Flyway
- MinIO
- OpenAPI / Swagger
- Spring Boot Actuator
- Docker Compose
- JUnit 5
- H2
- Testcontainers

## Estrutura do repositorio

```text
queenfitstyle-project/
|- app/
|- attribute/
|- catalog/
|- inventory/
|- pricing/
|- product/
|- shared/
|- storage/
|- docker-compose.yml
|- pom.xml
```

## Como rodar localmente

### Pre-requisitos

- Java 21
- Docker e Docker Compose

### 1. Criar o arquivo `.env`

Exemplo minimo:

```env
POSTGRES_DB=queenfitstyle
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432

PGADMIN_EMAIL=admin@queenfitstyle.local
PGADMIN_PASSWORD=admin123
PGADMIN_PORT=5050

MINIO_ROOT_USER=admin
MINIO_ROOT_PASSWORD=admin123
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001

NEXTJS_REVALIDATE_URL=http://localhost:3000/api/revalidate
NEXTJS_REVALIDATE_SECRET=local-secret

OPENAI_API_KEY=
OPENAI_MODEL=gpt-4.1-mini
```

### 2. Subir dependencias

```bash
docker compose up -d
```

Isso sobe:

- PostgreSQL
- PgAdmin
- MinIO

### 3. Rodar a aplicacao

No Windows:

```bash
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

### 4. Executar testes

No Windows:

```bash
.\mvnw.cmd test
```

No Linux/macOS:

```bash
./mvnw test
```

## URLs uteis em ambiente local

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Actuator Health: `http://localhost:8080/actuator/health`
- MinIO Console: `http://localhost:9001`

## Integracoes

### Frontend ERP

<https://github.com/Haddad0799/QUEENFITSTYLE-ERP-UI>

### Frontend Store

<https://github.com/Haddad0799/QUEENFITSTYLE-STORE-UI>

## O que faz esse projeto ser forte em portfolio

- resolve um problema de negocio real de ecommerce, nao apenas um exemplo academico
- demonstra modelagem de dominio com regras de consistencia
- mostra separacao entre operacao interna e experiencia publica de loja
- usa integracoes reais: banco, storage, IA e frontend externo
- implementa catalogo orientado a leitura, algo muito presente em cenarios de escala
- cobre fluxo completo de produto: cadastro, enrichment, publicacao, vitrine e revalidacao

## Proximos passos naturais

- autenticacao e autorizacao por perfis
- fila para eventos de catalogo em maior escala
- observabilidade com tracing
- testes de integracao mais amplos para catalogo e publicacao
- automacao de CI/CD

---

Se voce quer vender este projeto, a melhor leitura e esta: nao e so um backend Spring Boot. E uma base solida para operar um ecommerce com ERP acoplado, separando o que e cadastro interno do que realmente precisa chegar na vitrine com performance, consistencia e clareza comercial.
