# Encurtador de URL

Aplicação web para encurtar links: cole uma URL longa e receba um link curto,
pronto para compartilhar. Acesse em produção: **[encurtadorlink.link](https://encurtadorlink.link)**

![Tela inicial da aplicação](docs/screenshot-home.png)
![Link encurtado gerado](docs/screenshot-resultado.png)

> As imagens acima ficam na pasta `docs/` do repositório — basta adicionar os
> prints correspondentes nesse caminho para que apareçam aqui.

## Stack e conceitos aplicados

- **Java 17 + Quarkus** — framework do backend REST, injeção de dependência
  (CDI), validação de payload (Hibernate Validator) e empacotamento otimizado
  para ambientes serverless.
- **AWS** — uso combinado de vários serviços gerenciados:
  - **Lambda** — execução do backend sem servidor dedicado.
  - **API Gateway (HTTP API)** — roteamento das requisições HTTP para a Lambda.
  - **DynamoDB** — banco NoSQL chave-valor (`code` → `url`), acessado via
    DynamoDB Enhanced Client.
  - **IAM** — roles e policies com permissão mínima necessária (a Lambda só
    acessa a própria tabela).
  - **ACM (Certificate Manager)** — certificado TLS para o domínio próprio.
  - **CloudFormation / AWS SAM** — toda a infraestrutura descrita em
    `backend/template.yaml` e provisionada via `sam deploy`, sem passos
    manuais no console.
- **JSON** — contrato de entrada/saída de todos os endpoints, com
  desserialização via Jackson e um exception mapper dedicado para retornar
  erros também em JSON, de forma consistente.
- **Git** — histórico versionado, deploys reproduzíveis a partir de um
  `pom.xml`/`template.yaml` versionados junto ao código.
- **Linux** — o backend roda em runtime Linux (Amazon Linux, base da AWS
  Lambda); build e deploy também testados via terminal (PowerShell/bash).
- **Monitoramento** — logs de execução e erros centralizados no Amazon
  CloudWatch, permitindo rastrear falhas de invocação da Lambda em produção.
- **React + Vite + TypeScript** — interface do usuário, hospedada na Vercel.

## Arquitetura

```
Front-end (React, Vercel)
  │  fetch (JSON)
  ▼
API Gateway (HTTP API)
  │
  ▼
AWS Lambda (Quarkus)
  │
  ▼
DynamoDB (tabela: code → url)
```

O domínio público (`encurtadorlink.link`) fica apontado direto para o Custom
Domain do API Gateway, com certificado emitido via ACM — é ele quem responde
tanto aos redirecionamentos de link curto quanto (na rota raiz) redireciona
para a interface web.

## Estrutura do backend

```
backend/src/main/java/org/acme/shortener/
├── ShortenerResource.java     # POST /api/shorten, GET /api/{code} (metadados)
├── RedirectResource.java      # GET /{code} -> 302 redirect para a URL original
├── HomeResource.java          # GET / -> redireciona para o front-end
├── UrlMapping.java            # Item da tabela DynamoDB (code = partition key)
├── dto/                       # Contratos de request/response (JSON)
├── service/
│   ├── ShortenerService.java  # Regras de negócio (geração de código, etc.)
│   └── UrlRepository.java     # Acesso ao DynamoDB (Enhanced Client)
└── exception/                 # Exceções de domínio + mapeamento para JSON
```

## Endpoints

### Criar link curto
```
POST /api/shorten
Content-Type: application/json

{
  "url": "https://exemplo.com/uma-pagina-com-url-bem-longa",
  "customCode": "meu-alias"   // opcional
}
```
Resposta `201 Created`:
```json
{
  "code": "meu-alias",
  "shortUrl": "https://encurtadorlink.link/meu-alias",
  "originalUrl": "https://exemplo.com/uma-pagina-com-url-bem-longa",
  "createdAt": 1753500000
}
```

### Redirecionar (link público)
```
GET /{code}   -> HTTP 302 Location: <url original>
```

### Consultar metadados sem redirecionar
```
GET /api/{code}
```

### Erros (JSON padronizado)
- `404` código não encontrado
- `409` código customizado já em uso
- `400` payload inválido (ex.: URL sem http/https)

## Rodando o backend localmente

Requer Java 17, Maven Wrapper (incluso) e uma conta AWS configurada
(`aws configure`) para uso da DynamoDB real em modo de desenvolvimento.

```bash
cd backend
./mvnw quarkus:dev
```

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://quarkus.io"}'
```

## Deploy do backend (infraestrutura como código)

```bash
cd backend
./mvnw clean package
sam deploy
```

O `template.yaml` provisiona, em uma única execução:
- Tabela DynamoDB (billing on-demand, TTL habilitado)
- Função Lambda com IAM role de permissão mínima (CRUD restrito à própria tabela)
- HTTP API no API Gateway, incluindo rota para a raiz do domínio
- Custom Domain associado a um certificado ACM (`encurtadorlink.link`)

## Detalhes de implementação

- **Idempotência na criação de código**: `saveIfAbsent` usa uma
  `ConditionExpression attribute_not_exists(code)` no DynamoDB, garantindo que
  nem um alias customizado nem uma colisão de geração aleatória sobrescrevam
  um registro existente.
- **Geração de código**: 7 caracteres alfanuméricos aleatórios, com até 5
  tentativas em caso de colisão.
- **CORS configurado explicitamente** para restringir quais origens podem
  consumir a API a partir do navegador.
- **TTL opcional**: campo `expiresAt` já mapeado, permitindo expiração
  automática de links sem lógica adicional de limpeza.