# RH Flow - Back-end Spring Boot

API REST acadêmica para candidatos, usando **Java + Spring Boot + Spring MVC + ArrayList**, sem banco de dados.

O Spring permanece responsável pelo CRUD obrigatório e pelas regras que não podem ser burladas pelo front-end. O FastAPI de currículo **não chama este backend diretamente**: o Angular revisa a análise e só então faz o POST.

## Requisitos

- Java 17+
- Maven Wrapper incluído (`mvnw` / `mvnw.cmd`)

## Executar

Linux/macOS:

```bash
./mvnw test
./mvnw spring-boot:run
```

Windows:

```powershell
mvnw.cmd test
mvnw.cmd spring-boot:run
```

API: `http://localhost:8080`.

## Endpoints

| Método | Endpoint | Função | Sucesso |
|---|---|---|---|
| POST | `/funcionarios` | cadastrar | 201 |
| GET | `/funcionarios` | listar | 200 |
| GET | `/funcionarios/{id}` | consultar por ID | 200 |
| PUT | `/funcionarios/{id}` | atualização completa | 200 |
| PATCH | `/funcionarios/{id}` | atualização parcial | 200 |
| DELETE | `/funcionarios/{id}` | excluir | 204 |
| GET | `/funcionarios/pesquisar?termo=...` | pesquisar nome/cargo/status | 200 |

## Modelo e armazenamento

`FuncionarioEntity.id` é `Integer` em entidade, Controller, Service, gerador e testes. `status` usa o enum `StatusFuncionario`:

- `EM_ANALISE`
- `APROVADO`
- `REPROVADO`
- `CONTRATADO`

Os dados ficam em `ArrayList<FuncionarioEntity>` e são perdidos ao encerrar a aplicação, como previsto pelo exercício.

Na inicialização são carregados 12 candidatos fictícios variados para facilitar Dashboard, Pipeline, busca e demonstração. O seed ocorre somente no construtor do serviço, nunca em GET.

## Cadastro e decisão humana

Todo **novo candidato**, inclusive se o cliente tentar enviar outro status, é cadastrado inicialmente como `EM_ANALISE`. Aprovar, reprovar e contratar são ações posteriores do recrutador via PATCH.

## Validações

- nome obrigatório, com trim e rejeição de espaços;
- e-mail obrigatório e formato válido;
- e-mail normalizado em `trim + lowercase`;
- e-mail duplicado -> `409 Conflict`;
- cargo obrigatório;
- salário informado deve ser >= 0;
- status limitado ao enum;
- PATCH vazio -> `400`;
- PATCH desconhecido/ID alterado -> `400`;
- PATCH é atômico e preserva campos não enviados;
- PUT exige recurso completo e preserva o ID existente;
- ID inexistente -> `404`;
- erros inesperados -> `500` sanitizado.

## Fluxo de status

- `EM_ANALISE` -> `APROVADO` / `REPROVADO`
- `APROVADO` -> `EM_ANALISE` / `REPROVADO` / `CONTRATADO`
- `REPROVADO` -> `EM_ANALISE`
- `CONTRATADO` -> estado final

Para chegar a `CONTRATADO`, departamento e salário maior que zero são obrigatórios.

## CORS

O `@CrossOrigin("*")` foi removido. Em desenvolvimento, o Angular utiliza proxy `/api`, evitando wildcard desnecessário.

## Testes

Foram adicionados testes de Service e Controller cobrindo cadastro, ID, validações, duplicidade, busca, PUT, PATCH, transições e DELETE.

Neste sandbox, o comando real `bash mvnw test` não chegou à compilação porque o wrapper não conseguiu baixar Maven 3.9.16 de `repo.maven.apache.org`. Como verificações compensatórias:

- a lógica real de `FuncionarioService` foi compilada e executada num harness Java: **16/16 verificações passaram**;
- todos os fontes Java de `src/main/java` foram compilados com stubs mínimos das anotações/classes de framework apenas para detectar erros de sintaxe/tipos internos.

Essas verificações não substituem `mvnw test` em uma máquina com acesso às dependências.
