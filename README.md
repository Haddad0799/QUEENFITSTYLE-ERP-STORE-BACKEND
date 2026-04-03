# Queenfitstyle Platform

Sistema completo de e-commerce para moda fitness, composto por três aplicações independentes:

- ERP backend (Java + Spring Boot)
- Painel administrativo (React)
- Loja virtual (Next.js com SSR e ISR)

O sistema foi projetado para resolver problemas reais de gestão de catálogo e garantir sincronização eficiente entre operação interna e vitrine pública.

---

## Problema

E-commerces de pequeno e médio porte enfrentam desafios recorrentes:

- Erros manuais no cadastro de produtos e SKUs
- Produtos publicados incompletos (sem preço, estoque ou imagem)
- Inconsistência entre ERP e loja virtual
- Baixa performance na vitrine devido a queries complexas
- Atualizações lentas que exigem rebuild completo do frontend

---

## Solução

Desenvolvi um ecossistema integrado que:

- Garante que produtos só sejam publicados quando completos
- Automatiza a sincronização entre ERP e loja via eventos
- Utiliza projeções otimizadas para leitura (sem JOINs pesados)
- Atualiza páginas do frontend em segundos via ISR (On-Demand)
- Permite importação em massa com validação inteligente

---

## Resultado

- Redução de erros operacionais no cadastro de produtos
- Atualização quase em tempo real da loja
- Melhor performance na vitrine pública
- Arquitetura preparada para evolução e escala

---

## Arquitetura (Visão Geral)

Fluxo principal do sistema:

1. Produto é criado ou atualizado no ERP
2. Evento de domínio é disparado
3. O catálogo gera um snapshot desnormalizado
4. O frontend Next.js é notificado via webhook
5. As páginas afetadas são revalidadas automaticamente (ISR)

---

## Estrutura do Sistema

O backend segue a abordagem Monolith First, com módulos bem definidos:

queenfitstyle/
├── app
├── product
├── catalog
├── attribute
├── inventory
├── pricing
├── storage
└── shared

Cada módulo possui:

- domain: regras de negócio puras (sem dependência de framework)
- application: casos de uso e orquestração
- infrastructure: implementações técnicas (banco, integrações)
- presentation: controllers REST

---

## Decisões Técnicas

### Arquitetura

- Clean Architecture
- Domain-Driven Design (DDD)
- Arquitetura Hexagonal (Ports & Adapters)
- Monolith First com separação por módulos

### Persistência

Uso de JDBI ao invés de JPA/Hibernate:

- Controle total sobre SQL
- Melhor performance em importação em massa
- Queries dinâmicas mais previsíveis
- Domínio livre de anotações de persistência

### Catálogo como Projeção (CQRS simplificado)

- Escrita ocorre no módulo de produto
- Leitura ocorre no módulo de catálogo
- Snapshot desnormalizado para performance
- Replace atômico (DELETE + INSERT)

### Comunicação entre módulos

- Interfaces (Providers / Gateways)
- Eventos de domínio
- Nenhum acesso direto entre módulos

---

## Sincronização com Frontend (ISR)

Após atualização do catálogo:

Evento de domínio  
→ CatalogEventListener (AFTER_COMMIT)  
→ Persistência do snapshot  
→ Chamada HTTP para Next.js  
→ Revalidação de cache via tags  

Tags invalidadas:
- catalog-products
- catalog-product-{slug}

Resultado:
Atualização da loja em segundos sem rebuild completo.

---

## Upload de Imagens

Fluxo baseado em Pre-signed URLs:

1. Backend gera URL temporária
2. Frontend envia direto para o storage (MinIO/S3)
3. Backend apenas confirma o upload

Benefícios:

- Redução de carga no backend
- Melhor escalabilidade
- Upload mais rápido

---

## Importação em Massa

Processamento de arquivos Excel com:

- Validação linha a linha
- Isolamento transacional por grupo
- Agrupamento de SKUs por produto
- Batch insert otimizado
- Relatório detalhado de erros

---

## Regras de Negócio

Ciclo de vida controlado por domínio:

SKU:
INCOMPLETE → READY → PUBLISHED → BLOCKED / DISCONTINUED

Produto:
DRAFT → READY_FOR_SALE → PUBLISHED → INACTIVE / ARCHIVED

Mudanças em preço, estoque ou imagem disparam reavaliação automática.

---

## Stack Tecnológica

- Java 21
- Spring Boot 3
- PostgreSQL
- JDBI
- Flyway
- MinIO (S3)
- Docker
- Maven

Frontend:

- React (ERP)
- Next.js (Loja)
- TypeScript
- Tailwind CSS

---

## Repositórios

Backend:
https://github.com/Haddad0799/queenfitstyle-erp

Painel administrativo:
https://github.com/Haddad0799/queenfitstyle-erp-frontend

Loja virtual:
https://github.com/Haddad0799/queenfitstyle-store

---

## Como executar

Pré-requisitos:

- Java 21+
- Docker
- Maven

Passos:

1. Clonar repositório
2. Configurar variáveis de ambiente
3. Subir infraestrutura com Docker Compose
4. Executar aplicação

A aplicação estará disponível em:
http://localhost:8080

---

## Considerações

Este projeto foi desenvolvido de forma independente, cobrindo desde a modelagem de domínio até decisões de arquitetura, com foco em resolver problemas reais de negócio e garantir consistência entre sistemas.

O objetivo é evoluir continuamente o sistema e aplicar esses aprendizados em ambiente profissional.
