Queenfitstyle
===============

ERP para gerenciamento de e‑commerce de roupas de academia feminina.

Resumo rápido
-------------
Projeto multi‑módulo Java (Maven) com arquitetura modular e abordagem hexagonal (ports & adapters). Foi desenvolvido para demonstrar um sistema ERP simplificado com foco em catálogo de produtos, inventário e integração entre módulos, seguindo boas práticas de observabilidade, migrações de banco e testes.

Principais tecnologias
---------------------
- Java 21
- Spring Boot 3.x (autoconfig, Actuator)
- Jdbi (acesso a banco através de SQL mapeado)
- Flyway (migrações de banco)
- PostgreSQL (produção), H2 (testes locais)
- Testcontainers (testes de integração)
- JUnit 5, Mockito (testes)
- Springdoc OpenAPI (Swagger)
- Maven (multi‑module)

Visão de arquitetura
--------------------
- Multi‑módulo Maven (monorepo) para separar responsabilidades e facilitar entrega independente de cada parte.
- Padrão arquitetural: Hexagonal (Ports & Adapters). Cada módulo define portas (interfaces) da camada de domínio e expõe adaptadores de infraestrutura (JDBI, gateways, controllers) em módulos específicos.
- Camadas claras: domain (entidades, exceções), application (use cases / services), infrastructure (persistência, gateways) e presentation (controllers no módulo `app`).
- Migrations Flyway sob `src/main/resources/db/migration` em módulos relevantes — garantem versionamento do schema.

Módulos e responsabilidades
---------------------------
- `app`
  - Ponto de entrada Spring Boot (classe principal `br.com.erp.api.QueenfitstyleApplication`).
  - Composição de módulos e beans, configuração de runtime, Actuator e documentação OpenAPI.
  - Dependências: `catalog`, `product`, `inventory`, `shared`.

- `shared`
  - Configurações e utilitários comuns (ex.: `JdbiConfig`).
  - Recursos compartilhados como migrations base e utilitários cross‑cutting.
  - Não contém lógica de negócio específica; serve para evitar duplicação.

- `catalog`
  - Funcionalidade de catálogo (categorias, tamanhos etc.).
  - Responsável por queries/outputs relacionadas a catálogo (ex.: `SizeQueryServiceImpl`).
  - Expõe portas/serviços para outros módulos (e.g., produto).

- `product`
  - Casos de uso de produto (criação, alteração, lookup).
  - Define a porta `InventoryGateway` usada para comunicação com o módulo `inventory`.
  - Contém DTOs/commands/ports para integração com inventário e catálogo.

- `inventory`
  - Gerencia o estoque por SKU, movimentos de estoque e histórico.
  - Implementa adaptadores de persistência com Jdbi (ex.: `SkuStockJdbiRepository`, `StockMovementJdbiRepository`).
  - Fornece um gateway (`InventoryGatewayImpl`) para inicialização de estoque a partir do módulo `product`.

Dependências entre módulos (resumido)
-------------------------------------
- `app` → (`shared`, `product`, `catalog`, `inventory`)
- `product` → (`catalog`, `shared`)
- `inventory` → (`product`, `shared`) (usa contratos definidos em `product` para inicialização do estoque)
- `catalog` → (`shared`)

Padrões e boas práticas aplicadas
--------------------------------
- Separação por módulos e portas (Ports & Adapters) para facilitar testes e substituição de infra.
- Uso de Flyway para versionar migrações de banco.
- Jdbi para SQL claro e mapeamento manual (previsibilidade e performance).
- Testcontainers configurado para testes de integração (containers PostgreSQL).
- Actuator e Springdoc para observabilidade e documentação.
- SpotBugs configurado no POM pai para análise de qualidade.

Como construir e executar
------------------------
(Exemplos para Windows Powershell — use `./mvnw.cmd` se preferir o wrapper do projeto)

- Build completo (compila e roda testes):

```powershell
./mvnw.cmd -B -DskipTests=false clean verify
```

- Build e empacotar apenas o módulo `app`:

```powershell
./mvnw.cmd -pl app -am -DskipTests=false clean package
```

- Rodar a aplicação localmente (módulo `app`):

```powershell
./mvnw.cmd -pl app -am spring-boot:run
```

Testes e ambiente (Testcontainers)
----------------------------------
- Unit tests: executáveis localmente sem dependências externas.
- Integration tests: usam Testcontainers para criar um PostgreSQL real. Para executar esses testes, certifique‑se de que o Docker Engine / Docker Desktop esteja em execução.
- O projeto também possui configuração de testes que usa H2 (in‑memory) para cenários onde Testcontainers não está disponível — ver `app/src/test/resources/application.yml`.

Migrações de banco
------------------
- As migrations Flyway devem ser colocadas em `src/main/resources/db/migration` de cada módulo que precise versionar seu schema.
- Em runtime, o Spring Boot + Flyway aplicam as migrations encontradas no classpath (ordenadas por versão).

Observabilidade e documentação
------------------------------
- Actuator expondo endpoints de health/metrics/loggers.
- OpenAPI/Swagger UI disponível em `/swagger-ui.html` (quando o app roda).
- Logging via SLF4J — recomenda‑se configurar rotação e formatação em produção.

Qualidade de código e CI
------------------------
- Configuração de CI (GitHub Actions) para build e execução dos testes.
- SpotBugs (non‑failing por padrão), e possibilidade de integração com Sonar/PMD/Checkstyle.

Pontos fortes para recrutadores
-------------------------------
O projeto demonstra várias habilidades importantes para engenharia de software:
- Design modular e separação de responsabilidades (maven multi‑module + hexagonal).
- Integração com infraestrutura real (Jdbi + PostgreSQL + Flyway) e uso de Testcontainers para testes de integração robustos.
- Uso de práticas modernas de observabilidade (Actuator + OpenAPI) e qualidade (SpotBugs, CI).
- Conhecimento de patterns: ports & adapters, DTOs/UseCases, transações via Spring (@Transactional nos use cases).
- Escrita de SQL explícita (Jdbi) e percepção de performance/controle sobre queries.

Como contribuir / próximos passos (sugestões)
---------------------------------------------
- Melhorar cobertura de testes unitários e de integração (ex.: testar `InitializeStockUseCase`, repositórios Jdbi e controllers).
- Adicionar pipelines de CI para build multi‑módulo com stages (unit, integration, publish).
- Integrar análise estática (Sonar) e políticas de PRs (verificações automáticas).
- Implementar containerização de produção (Docker multi‑stage) e health/readiness probes.

Contato
-------
- Repositório: (adicione link git)
- Autor: (adicione seu nome e contato profissional)

Licença
-------
- (especifique a licença que deseja; por padrão, adicione uma licença permissiva se quiser mostrar o projeto a recrutadores)


Se quiser, eu adapto esse README para uma versão em inglês, adiciono diagramas (ASCII ou imagens), ou crio um CHANGELOG + small CONTRIBUTING.md para facilitar contribuições e recrutamento.
