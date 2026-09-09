# Backend FATEC

Backend do projeto desenvolvido para a FATEC, responsável por
centralizar os dados necessários para a geração automatizada de
documentos oficiais de professores.

## Descrição do projeto

O projeto tem como objetivo automatizar o preenchimento e a geração de
documentos relacionados aos professores, utilizando dados estruturados
no banco de dados.

Fluxo previsto:

``` text
Selecionar professor
        ↓
Selecionar período/grade
        ↓
Escolher mês e ano
        ↓
Sistema calcula e organiza as informações
        ↓
Visualizar documento
        ↓
Gerar documento DOCX
        ↓
Possível geração de PDF
```

Nesta etapa inicial, o backend está sendo estruturado para receber a
futura implementação das entidades, regras de negócio, persistência e
automação dos documentos.

O sistema inicialmente será destinado ao responsável pelo processo e à
administração. O acesso dos professores e o controle eletrônico de ponto
não fazem parte do escopo inicial.

## Tecnologias

-   Java 21
-   Spring Boot
-   Spring Web MVC
-   Spring Data JPA
-   Hibernate
-   Jakarta Validation
-   PostgreSQL
-   Neon PostgreSQL
-   Apache POI
-   OpenAPI / Swagger UI
-   Maven
-   Docker
-   Docker Compose

## Requisitos

-   Java 21
-   IntelliJ IDEA ou outra IDE compatível com Java
-   Maven
-   Git
-   Docker e Docker Compose (para execução com Docker)
-   Instância PostgreSQL no Neon, caso seja utilizado o banco hospedado

## Como executar

### Clonar o projeto

``` bash
git clone URL_DO_REPOSITORIO
cd backend-fatec
```

### Configurar o banco

Configure as variáveis de ambiente:

``` text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Exemplo:

``` text
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require&channel_binding=require
DB_USERNAME=SEU_USUARIO
DB_PASSWORD=SUA_SENHA
```

Nunca envie senhas ou credenciais para o GitHub.

### Executar pela IDE

No IntelliJ IDEA:

1.  Abra o projeto `backend-fatec`.
2.  Verifique se o projeto utiliza Java 21.
3.  Configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` na configuração de
    execução.
4.  Execute `BackendApplication`.

O backend utiliza a porta `8080`.

Teste a API em:

``` text
http://localhost:8080/api/teste
```

## Como executar com Maven

Para testar:

``` bash
mvn test
```

Para compilar, executar os testes e gerar o JAR:

``` bash
mvn package
```

O arquivo será gerado em:

``` text
target/backend-fatec-0.0.1-SNAPSHOT.jar
```

Para executar o JAR:

``` bash
java -jar target/backend-fatec-0.0.1-SNAPSHOT.jar
```

As variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` precisam estar
disponíveis no ambiente.

## Como executar com Docker

Primeiro gere o JAR:

``` bash
mvn package
```

Crie a imagem:

``` bash
docker build -t backend-fatec .
```

Execute o container:

``` bash
docker run --name backend-fatec -p 8080:8080 backend-fatec
```

Para utilizar um banco PostgreSQL externo:

``` bash
docker run --name backend-fatec -p 8080:8080   -e DB_URL="SUA_URL"   -e DB_USERNAME="SEU_USUARIO"   -e DB_PASSWORD="SUA_SENHA"   backend-fatec
```

### Docker Compose

O `docker-compose.yml` padroniza um ambiente local com:

-   Backend Spring Boot
-   PostgreSQL local

Para iniciar:

``` bash
docker compose up --build
```

Para iniciar em segundo plano:

``` bash
docker compose up --build -d
```

Para parar:

``` bash
docker compose down
```

O PostgreSQL do Docker Compose é destinado ao desenvolvimento local. O
Neon é a opção de PostgreSQL hospedado do projeto.

## Configuração do PostgreSQL / Neon

O PostgreSQL é acessado pelo Spring Boot através de variáveis de
ambiente.

No `src/main/resources/application.properties`:

``` properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

### Neon

Para o Neon:

``` text
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require&channel_binding=require
DB_USERNAME=SEU_USUARIO
DB_PASSWORD=SUA_SENHA
```

Os valores reais não devem ser colocados no `application.properties` nem
enviados ao GitHub.

A configuração atual do Hibernate utiliza:

``` properties
spring.jpa.hibernate.ddl-auto=none
```

Isso impede que o Hibernate altere automaticamente a estrutura das
tabelas. A modelagem das entidades e a estratégia de criação/migração
das tabelas serão definidas nas próximas etapas.

## Swagger / OpenAPI

O projeto utiliza OpenAPI e Swagger UI para documentar e testar a API.

Com o backend em execução:

``` text
http://localhost:8080/swagger-ui/index.html
```

Endpoint inicial de teste:

``` text
GET /api/teste
```

Resposta esperada:

``` text
API do backend funcionando!
```

## Estrutura do projeto

``` text
backend-fatec/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/fatec/backend/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── document/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── BackendApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       │   └── grade-modelo.docx
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│           └── br/com/fatec/backend/
│               └── BackendFatecApplicationTests.java
│
├── .mvn/
├── documentos-gerados/
├── target/
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

### Responsabilidade das principais pastas

Pasta          Responsabilidade
  -------------- ----------------------------------------------
`controller`   Receber requisições HTTP da API
`service`      Concentrar as regras de negócio
`repository`   Acessar os dados do banco
`entity`       Representar as entidades persistidas
`dto`          Representar dados de entrada e saída da API
`document`     Trabalhar com a geração de documentos
`config`       Configurações do backend
`exception`    Tratamento de exceções
`templates`    Modelos de documentos usados pelo Apache POI
`test`         Testes automatizados

## Arquitetura prevista

``` text
Frontend React
      ↓
API REST
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
PostgreSQL
```

Para geração de documentos:

``` text
Frontend
   ↓
Controller
   ↓
Service
   ↓
Dados do sistema
   ↓
Serviço de documentos
   ↓
Apache POI
   ↓
Modelo DOCX
   ↓
Documento preenchido
```

## Status

-   [x] Projeto Spring Boot configurado
-   [x] Java 21 configurado
-   [x] Maven configurado
-   [x] Spring Web MVC
-   [x] Spring Data JPA
-   [x] PostgreSQL
-   [x] OpenAPI / Swagger
-   [x] Apache POI
-   [x] Estrutura inicial de pacotes
-   [x] Teste inicial da API
-   [x] Conexão com PostgreSQL/Neon validada
-   [x] Geração do JAR validada
-   [x] Dockerfile validado
-   [x] Execução do backend em container validada
-   [ ] Modelagem definitiva do banco
-   [ ] Implementação das entidades
-   [ ] Regras de negócio
-   [ ] Geração completa dos documentos
-   [ ] Integração com dados do AxioDB
