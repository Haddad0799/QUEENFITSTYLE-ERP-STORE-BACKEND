# QueenFitStyle ERP Backend

Backend de um sistema de e-commerce com ERP integrado, responsável pela gestão de produtos, estoque e sincronização com a loja online.

---

## Problema

Em sistemas de e-commerce, é comum enfrentar:

- inconsistência entre dados do ERP e da loja  
- produtos publicados com informações incompletas  
- dificuldade na gestão de SKUs (cor, tamanho, estoque)  
- processos manuais e lentos para cadastro em massa  
- alto custo para atualização do catálogo no frontend  

---

## Solução

Este projeto resolve esses problemas através de:

- validação de regras de negócio antes da publicação de produtos  
- separação clara entre gestão interna (ERP) e catálogo da loja  
- sincronização automatizada entre sistemas  
- importação em massa de produtos com validação de dados  
- upload de imagens direto para storage (S3/MinIO)  
- atualização eficiente do catálogo no frontend  

---

## Funcionalidades

- Gestão de produtos, SKUs, categorias, cores e tamanhos  
- Controle de estado do produto (rascunho, publicado, inconsistente)  
- Importação em massa via planilha (Excel)  
- Upload de imagens com pre-signed URLs  
- Sincronização entre ERP e e-commerce  
- Atualização otimizada do catálogo da loja  

---

## Minha atuação

Neste projeto, fui responsável por:

- desenvolvimento do backend com Java e Spring Boot  
- definição das principais soluções técnicas  
- implementação de APIs e regras de negócio  
- integração entre ERP e e-commerce  
- modelagem do domínio (produto, estoque, catálogo, etc.)  
- organização do código e estrutura do sistema  

---

## Organização do projeto

O sistema foi estruturado em módulos independentes por domínio:

- Product: gestão de produtos e SKUs  
- Inventory: controle de estoque  
- Pricing: precificação  
- Catalog: dados otimizados para o e-commerce  
- Attribute: categorias, cores e tamanhos  

Cada módulo segue uma organização separando:

- regras de negócio  
- casos de uso  
- acesso a dados  
- exposição via API  

---

## Fluxo do sistema

1. Produto é criado ou atualizado no ERP  
2. Sistema valida regras de negócio (ex: produto completo)  
3. Produto é publicado  
4. Evento dispara atualização do catálogo  
5. Dados são sincronizados com a loja  
6. Frontend (Next.js) é revalidado automaticamente  

---

## Stack

- Backend: Java, Spring Boot  
- Banco de Dados: PostgreSQL  
- Mensageria/Eventos: RabbitMQ (se aplicável)  
- Storage: MinIO (S3 compatible)  
- DevOps: Docker  

---

## Diferenciais e integrações

- Separação clara entre regras de negócio e infraestrutura  
- Organização modular por domínio  
- Upload direto para storage (sem passar pelo backend)  
- Tratamento de consistência entre sistemas  
- Processamento de importação em lote com validação  
- Estrutura preparada para evolução e escalabilidade  

Integração com:

- ERP (Frontend):  
  https://github.com/Haddad0799/QUEENFITSTYLE-ERP-UI  

- E-commerce (Next.js):  
  https://github.com/Haddad0799/QUEENFITSTYLE-STORE-UI  

---

## Como rodar o projeto

```bash
# Clonar repositório
git clone https://github.com/Haddad0799/QUEENFITSTYLE-ERP-STORE-BACKEND

# Entrar na pasta
cd QUEENFITSTYLE-ERP-STORE-BACKEND

# Subir dependências
docker-compose up -d

# Rodar aplicação
./mvnw spring-boot:run
