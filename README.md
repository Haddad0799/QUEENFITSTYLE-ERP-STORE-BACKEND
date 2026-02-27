Queenfitstyle

ERP para gerenciamento de ecommerce de roupas de academia feminina.

Quickstart

- Build completo e testes:

```powershell
mvn -B -DskipTests=false clean verify
```

- Build e empacotar somente o módulo de aplicação:

```powershell
mvn -pl app -am -DskipTests=false clean package
```

- Rodar localmente (módulo app):

```powershell
mvn -pl app -am spring-boot:run
```

- Endpoints úteis (quando a aplicação estiver rodando):
  - Actuator: /actuator/health
  - Swagger UI: /swagger-ui.html
  - OpenAPI: /v3/api-docs

Boas práticas aplicadas

- Classe principal posicionada no pacote raiz `br.com.erp.api` para garantir component scan.
- Actuator adicionado para observabilidade.
- Springdoc OpenAPI para documentação automática.
- GlobalExceptionHandler padroniza erros no formato RFC7807 `application/problem+json`.
- SpotBugs adicionado no POM pai (não-falha por padrão) para análise progressiva.
- Workflow GitHub Actions básico para build + testes.

