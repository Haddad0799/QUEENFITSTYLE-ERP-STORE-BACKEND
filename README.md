<div align="center">

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&amp;color=0:0d1117,50:161b22,100:1f2937&amp;height=180&amp;section=header&amp;text=QueenFitStyle&amp;fontSize=52&amp;fontColor=58a6ff&amp;animation=fadeIn&amp;fontAlignY=38&amp;desc=ERP%20%2B%20E-commerce%20Backend&amp;descAlignY=60&amp;descColor=8b949e"/>

<p>
  <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&amp;logo=openjdk&amp;logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot_3.3-6DB33F?style=for-the-badge&amp;logo=springboot&amp;logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&amp;logo=postgresql&amp;logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&amp;logo=docker&amp;logoColor=white"/>
  <img src="https://img.shields.io/badge/MinIO-C72E49?style=for-the-badge&amp;logo=minio&amp;logoColor=white"/>
</p>

</div>

---

## 📌 O que é este projeto

Backend de uma plataforma de moda fitness feminina que une **ERP interno** e **vitrine pública** em um único sistema modular.

O problema que ele resolve é comum em e-commerce: o produto é cadastrado no backoffice com toda sua complexidade operacional (SKUs, variações, estoque por SKU, preço por SKU, imagens por cor), mas a loja precisa de uma visão enxuta, consistente e rápida. Aqui essas duas realidades coexistem sem misturar responsabilidades.

---

## 🏗️ Arquitetura

O sistema é um **monolito modular** organizado por domínio. Cada módulo tem fronteiras claras e se comunica com os demais por meio de eventos de aplicação e portas — não por chamadas diretas entre camadas.

```
queenfitstyle-project/
├── app/          # Bootstrap, WebConfig, GlobalExceptionHandler
├── attribute/    # Categorias (árvore hierárquica), cores e tamanhos
├── product/      # Núcleo ERP: produtos, SKUs, imagens, importação, IA
├── inventory/    # Estoque, reservas e movimentações por SKU
├── pricing/      # Precificação individual por SKU
├── catalog/      # Read model da loja — snapshot desnormalizado
├── storage/      # Integração MinIO/S3 com pre-signed URLs
└── shared/       # Código comum, eventos e migrações Flyway
```

Cada módulo segue a mesma estrutura interna:

```
modulo/
├── application/    # Casos de uso, queries, assemblers, ports
├── domain/         # Entidades, value objects, exceções de domínio
├── infrastructure/ # Persistência (JDBI), adaptadores, listeners
└── presentation/   # Controllers REST e DTOs
```

---

## 🔄 Fluxo de Publicação

```
[1] Produto criado no ERP
        ↓
[2] SKUs cadastrados (cor + tamanho + dimensões)
        ↓
[3] Preço e estoque inicializados por SKU
        ↓
[4] Imagens enviadas via pre-signed URL → vinculadas por cor
        ↓
[5] EvaluateSkuCompletenessUseCase avalia cada SKU
    EvaluateProductStatusUseCase avalia o produto
        ↓
[6] PublishProductUseCase → SnapshotAssembler monta retrato completo
        ↓
[7] ProductPublishedEvent → CatalogSyncService atualiza read model
        ↓
[8] NextJsRevalidationAdapter dispara revalidação por tags no frontend
```

Esse fluxo garante que o catálogo público nunca sirva dados inconsistentes e que o frontend não precise montar regras de negócio — ele só consome o snapshot pronto.

---

## 🖼️ Lógica de Vitrine Baseada na Imagem Principal

Um diferencial da implementação é que o preço exibido na listagem respeita a cor representada pela `mainImageUrl`:

1. `mainImageUrl` → determina a `mainColor`
2. Dentro dessa cor → SKU vendável mais barato → `showcaseSelection`
3. `showcaseSelection.price` → `displayPrice` exibido na listagem

Com **filtros ativos** (cor, tamanho, faixa de preço), a API calcula uma `effectiveSelection` em tempo real:

- Se existe SKU compatível → expõe `selection`, `displayPrice` e `displayImageUrl` dessa variação
- Se não existe SKU compatível → produto não entra no resultado

Isso elimina inconsistências como: imagem de uma cor com preço de outra.

---

## 📡 Endpoints

<details>
<summary><strong>ERP — Backoffice</strong></summary>

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/erp/products` | Listagem paginada com filtros |
| `POST` | `/erp/products` | Criar produto |
| `PATCH` | `/erp/products/{id}` | Editar produto |
| `POST` | `/erp/products/{id}/publish` | Publicar produto |
| `POST` | `/erp/products/import` | Importação em lote via Excel |
| `GET` | `/erp/products/{id}/skus` | Listar SKUs do produto |
| `POST` | `/erp/products/{id}/skus` | Adicionar SKU |
| `PUT` | `/erp/products/{id}/skus/{skuId}/price` | Atualizar preço do SKU |
| `POST` | `/erp/products/{id}/skus/{skuId}/stock/movements` | Registrar movimentação |
| `POST` | `/erp/products/{id}/colors/{colorId}/images/upload-urls` | Solicitar pre-signed URLs |
| `POST` | `/erp/products/{id}/colors/{colorId}/images` | Confirmar upload de imagens |
| `PATCH` | `/erp/products/{id}/primary-image` | Definir imagem principal |
| `GET` | `/erp/categories` | Listar categorias |
| `GET` | `/erp/categories/tree` | Árvore hierárquica de categorias |

</details>

<details>
<summary><strong>Store — Vitrine Pública</strong></summary>

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/store/products` | Listagem paginada com filtros |
| `GET` | `/store/products/{slug}` | Detalhe do produto |
| `GET` | `/store/products/{slug}/skus/{skuCode}` | Detalhe do SKU |
| `GET` | `/store/catalog/filters` | Filtros disponíveis |
| `GET` | `/store/catalog/categories` | Categorias navegáveis |
| `GET` | `/store/categories` | Categorias da loja |

</details>

---

## 🧩 Decisões Técnicas

| Decisão | Motivação |
|---------|-----------|
| **Monolito modular** | Fronteiras de domínio claras sem a complexidade operacional de microsserviços |
| **Write model separado do Read model** | Catálogo é um snapshot publicado — loja não consulta o ERP diretamente |
| **JDBI com SQL explícito** | Controle total sobre queries críticas; comportamento previsível sem ORM |
| **Pre-signed URLs para imagens** | Arquivo nunca trafega pelo backend — escala sem custo de I/O |
| **Eventos de aplicação** | Desacoplamento entre publicação de produto e atualização do catálogo |
| **Revalidação por tags no Next.js** | Frontend invalida cache seletivamente após mudanças no catálogo |
| **Flyway para migrações** | 33 versões versionadas junto ao código, rastreáveis e reversíveis |
| **Testcontainers** | Testes de integração com banco real, sem mocks frágeis |

---

## ⚙️ Ciclos de Vida

**Produto:** `DRAFT` → `READY_FOR_SALE` → `PUBLISHED` → `INACTIVE` → `ARCHIVED`

**SKU:** `INCOMPLETE` → `READY` → `PUBLISHED` → `BLOCKED` → `DISCONTINUED`

Um SKU só avança quando possui cor, tamanho, dimensões, preço, estoque e imagem associada. Alterações em qualquer um desses dados podem recalcular o status do SKU e do produto.

---

## 🚀 Como Rodar Localmente

### Pré-requisitos

- Java 21
- Docker e Docker Compose

### 1. Criar o arquivo `.env`

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

### 2. Subir as dependências

```bash
docker compose up -d
```

Sobe: PostgreSQL · PgAdmin · MinIO

### 3. Rodar a aplicação

```bash
# Linux/macOS
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

### 4. Executar os testes

```bash
./mvnw test
```

### URLs locais

| Serviço | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Actuator Health | http://localhost:8080/actuator/health |
| MinIO Console | http://localhost:9001 |

---

## 🔗 Repositórios Relacionados

| Repositório | Descrição |
|-------------|-----------|
| [QUEENFITSTYLE-ERP-UI](https://github.com/Haddad0799/QUEENFITSTYLE-ERP-UI) | Frontend do backoffice (ERP) |
| [QUEENFITSTYLE-STORE-UI](https://github.com/Haddad0799/QUEENFITSTYLE-STORE-UI) | Frontend da loja (vitrine pública) |

---

## 🛠️ Stack Completa

| Categoria | Tecnologias |
|-----------|-------------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3, Spring Web, Spring Data Commons |
| Persistência | PostgreSQL, JDBI, Flyway |
| Storage | MinIO (compatível S3) |
| IA | OpenAI API (geração de descrição comercial) |
| Infra | Docker Compose |
| Testes | JUnit 5, H2, Testcontainers |
| Documentação | OpenAPI / Swagger, Spring Boot Actuator |

---

<div align="center">

**Desenvolvido por [Lucas Haddad](https://github.com/Haddad0799)**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat-square&amp;logo=linkedin&amp;logoColor=white)](https://www.linkedin.com/in/lucas-haddad-backend-developer/)

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&amp;color=0:1f2937,50:161b22,100:0d1117&amp;height=100&amp;section=footer"/>

</div>
