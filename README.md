# RH Flow — Sistema de Contratação de Funcionários

Projeto acadêmico de RH desenvolvido com **Java + Spring Boot + Spring MVC + API REST + HTML/CSS/JavaScript puro**.

O objetivo é demonstrar, de forma visual e fácil de explicar, os cinco métodos HTTP estudados em aula: **GET, POST, PUT, PATCH e DELETE**. Os dados são armazenados **temporariamente em memória com `ArrayList<FuncionarioEntity>`**, sem banco de dados, conforme o enunciado do desafio.

## Tecnologias utilizadas

- Java 17
- Spring Boot 4.1.1
- Spring MVC / REST
- Maven Wrapper
- HTML5
- CSS3
- JavaScript puro
- Fetch API
- JUnit 5
- MockMvc

## Como executar

### Pré-requisitos

- JDK 17 ou superior
- Acesso à internet na primeira execução do Maven Wrapper, caso as dependências ainda não estejam em cache

### Linux / macOS

```bash
./mvnw spring-boot:run
```

### Windows

```powershell
mvnw.cmd spring-boot:run
```

Depois abra:

```text
http://localhost:8080
```

A própria aplicação Spring Boot serve o front-end localizado em `src/main/resources/static`.

## Como executar os testes

### Linux / macOS

```bash
./mvnw test
```

### Windows

```powershell
mvnw.cmd test
```

Para gerar o pacote completo:

```bash
./mvnw clean package
```

## Armazenamento em memória

Os candidatos ficam em:

```java
ArrayList<FuncionarioEntity>
```

Isso significa que:

- não existe banco de dados;
- os dados são temporários;
- ao reiniciar a aplicação, as alterações feitas durante a execução são perdidas;
- uma carga inicial de candidatos fictícios é recriada para facilitar a apresentação.

A aplicação inicia com 12 candidatos fictícios distribuídos entre `EM_ANALISE`, `APROVADO`, `REPROVADO` e `CONTRATADO`.

## Endpoints

| Método | Endpoint | Objetivo | Sucesso |
|---|---|---|---|
| `POST` | `/funcionarios` | Cadastrar candidato | `201 Created` |
| `GET` | `/funcionarios` | Listar todos | `200 OK` |
| `GET` | `/funcionarios/{id}` | Consultar por ID | `200 OK` |
| `PUT` | `/funcionarios/{id}` | Atualização completa | `200 OK` |
| `PATCH` | `/funcionarios/{id}` | Atualização parcial | `200 OK` |
| `DELETE` | `/funcionarios/{id}` | Excluir candidato | `204 No Content` |
| `GET` | `/funcionarios/pesquisar?termo=...` | Pesquisar nome, cargo, status, cidade ou departamento | `200 OK` |

IDs inexistentes retornam `404 Not Found`. Dados inválidos retornam `400 Bad Request` com JSON amigável.

Exemplo de erro:

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "O salário não pode ser negativo.",
  "caminho": "/funcionarios",
  "timestamp": "..."
}
```

## Regras principais do back-end

### ID

O ID é `Integer` em toda a aplicação: entidade, service, controller, contador e testes. Isso evita incompatibilidades como comparar `Integer` com `Long`.

### POST

- gera ID único automaticamente;
- exige nome, e-mail e cargo;
- valida formato básico de e-mail;
- recusa e-mail duplicado;
- recusa salário negativo;
- usa `EM_ANALISE` quando o status não é enviado;
- retorna `201 Created`.

### GET

`GET /funcionarios` sempre retorna `200 OK`, inclusive quando a lista está vazia (`[]`).

### PUT

O `PUT` representa atualização completa dos dados editáveis. O ID da URL é preservado e o ID enviado no corpo, se existir, não substitui o ID original.

### PATCH

O `PATCH` altera somente os campos enviados. Exemplo usado pelos botões rápidos:

```json
{
  "status": "APROVADO"
}
```

Um PATCH vazio (`{}`), campo desconhecido, status inválido ou salário negativo retorna `400 Bad Request`.

### DELETE

Retorna `204 No Content`. No front-end, após o DELETE, é feita uma consulta por ID para confirmar que o candidato realmente passou a retornar `404`.

## Pesquisa

A busca oficial funciona por:

- nome;
- cargo;
- status.

Como extensão simples, também considera:

- cidade;
- departamento.

A comparação ignora maiúsculas/minúsculas e é segura para campos opcionais nulos.

## Estrutura principal

```text
src/
├── main/
│   ├── java/com/picpay/rh/
│   │   ├── RhApplication.java
│   │   ├── controller/
│   │   │   ├── ApiExceptionHandler.java
│   │   │   └── FuncionarioController.java
│   │   ├── entity/
│   │   │   ├── FuncionarioEntity.java
│   │   │   └── StatusFuncionario.java
│   │   ├── exception/
│   │   │   ├── ApiErro.java
│   │   │   └── FuncionarioNaoEncontradoException.java
│   │   └── service/
│   │       └── FuncionarioService.java
│   └── resources/
└── test/java/com/picpay/rh/
    ├── RhApplicationTests.java
    ├── FuncionarioServiceTest.java
    └── FuncionarioControllerIntegrationTest.java
```

## Testes importantes cobertos

- cadastro e geração de ID;
- status padrão;
- listagem;
- busca por ID;
- ID inexistente;
- PUT completo;
- PATCH parcial sem apagar outros campos;
- PATCH vazio;
- status inválido;
- DELETE;
- pesquisa;
- salário negativo;
- campos obrigatórios;
- e-mail inválido;
- e-mail duplicado;
- fluxo HTTP completo com MockMvc.

## Limitações conhecidas

- os dados são perdidos quando o processo Java é encerrado;
- a `ArrayList` não é adequada como persistência para produção;
- não existe autenticação ou autorização;
- não existe upload de currículo;
- não existe histórico persistente das requisições;
- o projeto foi deliberadamente mantido simples para demonstrar conceitos HTTP e atender ao desafio acadêmico.
