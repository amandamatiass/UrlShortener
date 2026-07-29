# URL Shortener — Java + Quarkus + AWS Lambda + DynamoDB

Encurtador de links com endpoints JSON, rodando como função AWS Lambda
(via API Gateway) e persistindo os pares `código -> URL` no DynamoDB.

## Estrutura

```
src/main/java/org/acme/shortener/
├── ShortenerResource.java     # POST /api/shorten, GET /api/{code} (metadados)
├── RedirectResource.java      # GET /{code} -> 302 redirect para a URL original
├── UrlMapping.java            # Item da tabela DynamoDB (code = PK)
├── dto/                       # Request/Response JSON
├── service/
│   ├── ShortenerService.java  # Regras de negócio (geração de código, etc.)
│   └── UrlRepository.java     # Acesso ao DynamoDB (Enhanced Client)
└── exception/                 # Exceções + mapeamento para respostas JSON
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
  "shortUrl": "https://short.example.com/meu-alias",
  "originalUrl": "https://exemplo.com/uma-pagina-com-url-bem-longa",
  "createdAt": 1753500000
}
```

### Redirecionar (link público, o que o usuário final clica)
```
GET /{code}   -> HTTP 302 Location: <url original>
```

### Consultar metadados sem redirecionar (uso por outras APIs)
```
GET /api/{code}
```

### Erros
- `404` código não encontrado
- `409` código customizado já em uso
- `400` payload inválido (ex.: URL sem http/https)

## Rodando localmente

Requer Docker (para o DynamoDB Local) e Java 17 + Maven.

```bash
# 1) Suba um DynamoDB local
docker run -p 8000:8000 amazon/dynamodb-local

# 2) Crie a tabela local (uma vez só)
aws dynamodb create-table \
  --table-name url-shortener \
  --attribute-definitions AttributeName=code,AttributeType=S \
  --key-schema AttributeName=code,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url http://localhost:8000

# 3) Rode a aplicação em modo dev (usa o perfil %dev do application.properties)
./mvnw quarkus:dev
```

Teste:
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://quarkus.io"}'

curl -i http://localhost:8080/<code-retornado>
```

## Deploy (Lambda + DynamoDB via AWS SAM)

1. Gere o pacote da função (a extensão `quarkus-amazon-lambda-http` já produz
   `target/function.zip` no build):
   ```bash
   ./mvnw clean package
   ```
   Para reduzir cold start, pode-se compilar nativo (requer Docker):
   ```bash
   ./mvnw clean package -Dnative
   ```
   Nesse caso troque o `Runtime` no `template.yaml` para `provided.al2023` e
   ajuste o `Handler` conforme a doc da extensão nativa.

2. Deploy com SAM:
   ```bash
   sam deploy --guided \
     --template-file template.yaml \
     --stack-name url-shortener \
     --capabilities CAPABILITY_IAM \
     --parameter-overrides BaseUrl=https://seu-dominio-ou-endpoint
   ```

   O `template.yaml` já cria:
   - A tabela DynamoDB (`url-shortener`, PK `code`, billing on-demand, TTL em `expiresAt`)
   - A função Lambda com permissão CRUD apenas nessa tabela
   - Uma HTTP API do API Gateway roteando tudo (`/{proxy+}`) para a função

3. Após o deploy, o SAM mostra a `ApiUrl` de saída — use-a (ou um domínio
   customizado apontado para ela) como `app.base-url` / parâmetro `BaseUrl`.

## Notas de design

- **Par chave-valor no DynamoDB**: PK = `code` (string curta), atributo
  `url` guarda o destino. `saveIfAbsent` usa `ConditionExpression
  attribute_not_exists(code)` para nunca sobrescrever um código existente
  (seja alias customizado, seja colisão rara de geração aleatória).
- **Geração de código**: 7 caracteres em base62 (a-z, A-Z, 0-9), com até 5
  tentativas em caso de colisão.
- **TTL opcional**: o atributo `expiresAt` (epoch seconds) já está mapeado
  e a tabela SAM tem TTL habilitado nele — basta preenchê-lo se quiser
  links com expiração.
- **Cliques**: contador simples incrementado a cada redirecionamento
  (via `UpdateItem`); pode virar um `ADD` atômico se o volume for alto.
