# Backend FATEC

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-DOCX-D22128?style=for-the-badge&logo=apache&logoColor=white)](https://poi.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)
[![Neon](https://img.shields.io/badge/Neon-PostgreSQL-00E599?style=for-the-badge&logo=postgresql&logoColor=black)](https://neon.com/)

> Backend do projeto desenvolvido para a FATEC, responsável por centralizar os dados necessários para a geração automatizada de documentos oficiais de professores.

---

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Configuração do Banco de Dados](#configuração-do-banco-de-dados)
- [Documentação e Testes da API](#documentação-e-testes-da-api)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

---

## Sobre o Projeto

O projeto tem como objetivo automatizar o preenchimento e a geração de documentos relacionados aos professores, utilizando dados estruturados no banco de dados.

```text
Selecionar professor
        ↓
Selecionar semestre/período letivo
        ↓
Sistema identifica a grade correspondente
        ↓
Sistema preenche automaticamente o documento
        ↓
Visualizar documento preenchido
        ↓
Gerar documento DOCX
        ↓
Gerar PDF, se necessário
````

Nesta etapa inicial, o backend está sendo estruturado para receber a implementação das entidades, regras de negócio, persistência de dados e automação dos documentos.

O sistema inicialmente será destinado ao responsável pelo processo e à administração. O acesso dos professores e o controle eletrônico de ponto não fazem parte do escopo inicial.

---

## Tecnologias

| Tecnologia               | Uso                                         |
| ------------------------ | ------------------------------------------- |
| **Java 21**              | Linguagem principal                         |
| **Spring Boot**          | Framework principal do backend              |
| **Spring Web MVC**       | Desenvolvimento da API REST                 |
| **Spring Data JPA**      | Persistência e acesso aos dados             |
| **Hibernate**            | ORM utilizado pelo JPA                      |
| **Jakarta Validation**   | Validação dos dados recebidos pela API      |
| **PostgreSQL**           | Banco de dados relacional                   |
| **Neon**                 | PostgreSQL hospedado                        |
| **Apache POI**           | Manipulação e geração de documentos `.docx` |
| **OpenAPI / Swagger UI** | Documentação e testes da API                |
| **Maven**                | Gerenciamento de dependências e build       |
| **Docker**               | Containerização da aplicação                |
| **Docker Compose**       | Orquestração do ambiente local              |

---

## Pré-requisitos

Antes de executar o projeto, é necessário possuir:

* [Java 21](https://www.oracle.com/java/)
* [IntelliJ IDEA](https://www.jetbrains.com/idea/) ou outra IDE compatível com Java
* [Git](https://git-scm.com/)
* [Docker](https://www.docker.com/) e Docker Compose, caso utilize Docker
* Conta no [Neon](https://neon.com/), caso utilize PostgreSQL hospedado

> O projeto possui Maven Wrapper (`mvnw` e `mvnw.cmd`), portanto não é necessário instalar o Maven separadamente.

---




---
## Como executar

### IntelliJ IDEA

A forma mais simples de executar o projeto durante o desenvolvimento é pelo IntelliJ IDEA.

1. Abra o projeto no IntelliJ IDEA.
2. Abra o arquivo `BackendApplication.java`.
3. Execute pelo botão **Run ▶**.
4. A API estará disponível em:

```text
http://localhost:8080
```

Endpoint de teste:

```text
http://localhost:8080/api/teste
```

### Maven

O projeto possui Maven Wrapper, portanto não é necessário instalar o Maven globalmente.

#### Windows

Para executar os testes:

```bash
mvnw.cmd test
```

Para gerar o JAR:

```bash
mvnw.cmd package
```

#### Linux/macOS

Para executar os testes:

```bash
./mvnw test
```

Para gerar o JAR:

```bash
./mvnw package
```

### Docker

O Docker pode ser utilizado para padronizar o ambiente, executar o backend em um container e facilitar um futuro deploy.

Primeiro, gere o JAR:

```bash
mvnw.cmd package
```

Depois, crie a imagem:

```bash
docker build -t backend-fatec .
```

Para executar o container, é necessário informar as credenciais do banco por meio das variáveis de ambiente.

Exemplo:

```bash
docker run --name backend-fatec -p 8080:8080 -e DB_URL="jdbc:postgresql://HOST/neondb?sslmode=require&channel_binding=require" -e DB_USERNAME="SEU_USUARIO" -e DB_PASSWORD="SUA_SENHA" backend-fatec
```

As variáveis utilizadas são:

```text
DB_URL       → URL de conexão com o PostgreSQL
DB_USERNAME  → usuário do banco
DB_PASSWORD  → senha do banco
```

Exemplo utilizando o Neon:

```text
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require&channel_binding=require
DB_USERNAME=SEU_USUARIO
DB_PASSWORD=SUA_SENHA
```

> As credenciais utilizadas no exemplo são apenas ilustrativas. Substitua pelos dados do seu banco.

> As variáveis configuradas no IntelliJ IDEA não são utilizadas automaticamente pelo container Docker. Quando executar o backend pelo Docker, as variáveis precisam ser informadas no próprio `docker run` ou configuradas no Docker Compose.

### Docker Compose

Para iniciar o backend junto com um PostgreSQL local:

```bash
docker compose up --build
```

Para parar os containers:

```bash
docker compose down
```

O `docker-compose.yml` já possui as configurações necessárias para executar um PostgreSQL local para desenvolvimento.

> O PostgreSQL do Docker e o PostgreSQL do Neon são bancos diferentes. O Docker oferece uma opção de banco local, enquanto o Neon pode ser utilizado como banco PostgreSQL hospedado para desenvolvimento e testes.

---

## Configuração do Banco de Dados

O projeto utiliza PostgreSQL.

A conexão com o banco é feita por meio de variáveis de ambiente:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

### Configurando as credenciais no IntelliJ IDEA

As credenciais do banco devem ser configuradas na configuração de execução do `BackendApplication`.

No IntelliJ IDEA:

1. Clique em **Run**.
2. Clique em **Edit Configurations...**.
3. Selecione **BackendApplication**.
4. Caso a opção **Environment variables** não esteja aparecendo, clique em **Modify options**.
5. Dentro de **Modify options**, marque **Environment variables**.
6. A opção **Environment variables** aparecerá na configuração.
7. Clique no campo de **Environment variables** para adicionar as variáveis.

Na tabela de variáveis do IntelliJ:

```text
Nome da variável       Valor da variável
```

Ou seja:

```text
DB_URL                 jdbc:postgresql://...
DB_USERNAME            seu_usuario
DB_PASSWORD            sua_senha
```

Adicione as seguintes variáveis:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Preencha os valores de acordo com o banco utilizado.

Exemplo:

```text
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require&channel_binding=require
DB_USERNAME=SEU_USUARIO
DB_PASSWORD=SUA_SENHA
```

Depois de adicionar as variáveis, clique em **Apply** e depois em **OK**.

Ao executar o `BackendApplication`, o Spring Boot utilizará essas informações para realizar a conexão com o banco de dados.

### Exemplo com Neon

```text
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require&channel_binding=require
DB_USERNAME=SEU_USUARIO
DB_PASSWORD=SUA_SENHA
```

As variáveis devem ser configuradas no ambiente de execução da aplicação.

O Hibernate está configurado para não alterar automaticamente a estrutura do banco:

```properties
spring.jpa.hibernate.ddl-auto=none
```

> Cada desenvolvedor deve utilizar suas próprias credenciais. Nunca coloque senhas ou outras informações sensíveis diretamente no código ou no GitHub.

---

## Documentação e Testes da API

O projeto utiliza **OpenAPI / Swagger UI** para documentar e testar os endpoints da API.

Com o backend em execução, acesse:

```text
http://localhost:8080/swagger-ui/index.html
```

### Endpoint de teste

```text
GET /api/teste
```

Resposta esperada:

```text
API do backend funcionando!
```

### Insomnia

Também é possível utilizar o **Insomnia** para realizar testes dos endpoints da API.

Exemplo de teste:

```text
GET http://localhost:8080/api/teste
```

Resposta esperada:

```text
API do backend funcionando!
```

O Insomnia pode ser utilizado para testar requisições como:

```text
GET
POST
PUT
DELETE
```

---

## Estrutura de Pastas

```text
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

| Pasta        | Responsabilidade                                 |
| ------------ | ------------------------------------------------ |
| `config`     | Configurações do backend                         |
| `controller` | Recebimento das requisições HTTP                 |
| `document`   | Geração e manipulação dos documentos             |
| `dto`        | Objetos de entrada e saída da API                |
| `entity`     | Entidades persistidas no banco                   |
| `exception`  | Tratamento de exceções                           |
| `repository` | Acesso aos dados do banco                        |
| `service`    | Regras de negócio                                |
| `templates`  | Modelos de documentos utilizados pelo Apache POI |
| `test`       | Testes automatizados                             |

---

## Contribuindo

Para manter o projeto organizado, todas as alterações devem ser feitas em uma branch própria e relacionadas a uma Issue do GitHub.

O fluxo utilizado pelo projeto é:

**main → atualizar projeto → selecionar Issue → criar branch → desenvolver → testar → Pull Request → revisão → merge**

### 1. Atualize a `main`

Primeiro, certifique-se de estar na branch `main`:

```bash
git checkout main
```

Depois, atualize sua cópia local:

```bash
git pull origin main
```

> Sempre faça isso antes de iniciar uma nova tarefa.

---

### 2. Escolha a tarefa no GitHub Projects

As tarefas do projeto são organizadas pelo **GitHub Projects**.

Para começar uma tarefa:

1. Acesse o GitHub Projects do projeto.
2. Localize a tarefa que deseja realizar.
3. Abra a Issue e leia os requisitos.
4. Utilize a opção **Development → Create a branch**, quando disponível.
5. Caso não queira utilizar essa opção, a branch também pode ser criada manualmente seguindo a convenção definida abaixo.

---

### 3. Criar uma branch pela Issue

Dentro da Issue, no lado direito, acesse:

```text
Development
    ↓
Create a branch
```

O GitHub mostrará opções como:

```text
Branch name
Repository destination
Branch source
```

Depois de criar a branch, ela ficará associada à Issue.

Exemplo:

```text
30-feat-teste
```

Depois que a branch for criada, o GitHub poderá mostrar:

```text
Checkout in your local repository
```

Com os comandos:

```bash
git fetch origin
git checkout 30-feat-teste
```

#### `git fetch origin`

```bash
git fetch origin
```

Atualiza as informações do seu Git local sobre as branches existentes no repositório remoto.

Esse comando **não altera os arquivos do projeto**.

#### `git checkout`

```bash
git checkout 30-feat-teste
```

Muda o seu projeto local para a branch:

```text
30-feat-teste
```

Assim, você poderá trabalhar nessa branch.

> A opção **Create a branch** do GitHub já cria a branch no repositório remoto. Os comandos `git fetch origin` e `git checkout` servem para acessar essa branch no seu computador.

Também é possível trocar de branch diretamente pelo IntelliJ IDEA ou pelo GitHub Desktop.

---

### 4. Convenção de branches

O nome da branch deve indicar o tipo de alteração realizada.

* `feat/nome-da-feature` — novas funcionalidades
* `fix/nome-do-bug` — correções de bugs
* `chore/nome-da-tarefa` — manutenção, configurações e tarefas técnicas
* `docs/nome-do-documento` — alterações na documentação
* `refactor/nome-da-alteracao` — refatoração de código
* `test/nome-do-teste` — criação ou alteração de testes
* `style/nome-da-alteracao` — formatação ou estilo do código
* `perf/nome-da-melhoria` — melhorias de desempenho

### Exemplos

```text
feat/cadastro-professor
fix/erro-geracao-documento
chore/configuracao-docker
docs/atualizar-readme
refactor/organizar-service-professor
test/teste-cadastro-professor
```

---

### 5. Crie ou utilize a branch

A criação manual de uma branch é **opcional**.

Se você utilizou a opção **Development → Create a branch** dentro da Issue do GitHub, **não precisa executar `git checkout -b`**.

Nesse caso, basta acessar a branch criada no GitHub:

```bash
git fetch origin
git checkout nome-da-branch
```

Exemplo:

```bash
git fetch origin
git checkout 30-feat-teste
```

Caso você **não queira utilizar a opção Development → Create a branch**, pode criar uma branch manualmente pelo Git:

```bash
git checkout -b feat/nome-da-feature
```

Exemplo:

```bash
git checkout -b feat/cadastro-professor
```

Confirme a branch atual:

```bash
git branch
```

A branch atual será identificada com `*`.

Exemplo:

```text
  main
* feat/cadastro-professor
```

> A criação manual de uma branch serve para quando você quiser criar uma branch separada diretamente pelo Git, sem utilizar a opção de criação de branch da Issue.

> Todo o desenvolvimento da tarefa deve ser realizado nessa branch. Não faça alterações diretamente na `main`.

---

### 6. Desenvolva a tarefa

Durante o desenvolvimento:

* Siga os requisitos descritos na Issue.
* Mantenha o código organizado.
* Utilize os padrões adotados no projeto.
* Evite alterar arquivos que não sejam necessários para a tarefa.
* Crie ou atualize os testes quando necessário.
* Verifique se sua alteração não quebra funcionalidades existentes.

---

### 7. Teste antes do Pull Request

O Pull Request deve ser criado somente depois que a tarefa estiver concluída e testada.

Antes de abrir o Pull Request, verifique:

* O projeto inicia normalmente.
* A funcionalidade desenvolvida funciona corretamente.
* Os testes necessários foram realizados.
* Não existem erros importantes relacionados à alteração.
* As funcionalidades existentes continuam funcionando.
* A Issue foi atendida.

Os testes devem ser realizados de acordo com o tipo de alteração. Podem ser utilizados, por exemplo:

```text
Swagger
Insomnia
IntelliJ IDEA
Testes automatizados
```

> Não abra o Pull Request apenas porque terminou o código. Primeiro finalize, teste e valide a alteração.

---

### 8. Envie a branch para o GitHub

Adicione os arquivos:

```bash
git add .
```

Faça o commit:

```bash
git commit -m "feat: descrição da alteração"
```

Envie a branch:

```bash
git push origin feat/nome-da-feature
```

Exemplo:

```bash
git push origin feat/cadastro-professor
```

---

### 9. Crie o Pull Request

Depois de concluir e testar a implementação, abra um **Pull Request** da sua branch para a `main`.

A descrição deve informar:

* O que foi alterado.
* Por que a alteração foi realizada.
* Quais funcionalidades foram implementadas.
* Quais testes foram executados.
* Possíveis impactos.
* A Issue relacionada.

Sempre que possível, vincule o Pull Request à Issue correspondente.

---

## Modelo de Pull Request

Utilize o modelo abaixo como referência:

```markdown
## Descrição

Descreva de forma objetiva o que foi desenvolvido ou alterado.

## Motivo da alteração

Explique por que essa alteração foi necessária
e qual problema ou requisito ela atende.

## Funcionalidades implementadas

- Funcionalidade 1
- Funcionalidade 2
- Funcionalidade 3

## Testes realizados

- [x] Teste da funcionalidade principal
- [x] Testes automatizados
- [x] Verificação da inicialização do projeto
- [x] Verificação de possíveis impactos em funcionalidades existentes

## Possíveis impactos

Informe se a alteração pode afetar outras partes do sistema.

Caso não exista nenhum impacto conhecido, informe:

"Nenhum impacto conhecido."

## Issue relacionada

Closes #XX
```

---

### Prompt para criar a descrição do Pull Request

Caso tenha dificuldade para escrever a descrição do Pull Request, utilize o seguinte prompt:

```text
Preciso criar a descrição de um Pull Request para este projeto.

Com base nas informações que vou fornecer, escreva uma descrição clara,
objetiva e profissional para o Pull Request.

Não invente informações que eu não fornecer.

Organize a descrição exatamente nos seguintes tópicos:

## Descrição
Explique o que foi alterado ou desenvolvido.

## Motivo da alteração
Explique qual problema foi resolvido ou qual requisito da Issue foi atendido.

## Funcionalidades implementadas
Liste de forma objetiva as funcionalidades, alterações ou arquivos
importantes implementados.

## Testes realizados
Liste somente os testes que realmente foram executados.
Não diga que um teste foi realizado se eu não informar que ele foi executado.

## Possíveis impactos
Informe quais partes do sistema podem ser afetadas pela alteração.
Se não houver impacto conhecido, informe isso claramente.

## Issue relacionada
Informe a Issue relacionada no formato:

Closes #XX

Regras:
- Não invente testes.
- Não invente funcionalidades.
- Não invente impactos.
- Não use linguagem exageradamente técnica.
- Escreva de forma profissional, mas simples.
- Mantenha a descrição objetiva.
- Se alguma informação estiver faltando, indique que preciso informar essa
  informação em vez de inventá-la.

Informações da minha alteração:

Issue:
[COLE AQUI O NÚMERO E O TÍTULO DA ISSUE]

O que foi alterado:
[DESCREVA AQUI]

Funcionalidades implementadas:
[DESCREVA AQUI]

Testes realizados:
[DESCREVA AQUI]

Possíveis impactos:
[DESCREVA AQUI]
```

---

## Licença

Este projeto é desenvolvido para fins acadêmicos no contexto da **FATEC**.

A utilização, modificação e distribuição do código devem respeitar os termos definidos no arquivo [`LICENSE`](LICENSE) presente neste repositório.
