
# Backend FATEC

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-DOCX%20%7C%20PDF-D22128?style=for-the-badge&logo=apache&logoColor=white)](https://poi.apache.org/)
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
- [Instalação e Execução](#instalação-e-execução)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Documentação e Testes da API](#documentação-e-testes-da-api)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Contribuindo](#contribuindo)

---

## Sobre o Projeto

O projeto tem como objetivo automatizar o preenchimento e a geração de documentos relacionados aos professores, utilizando dados estruturados no banco de dados.

### Fluxo previsto

```text
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
Gerar o PDF Final.
````

Nesta etapa inicial, o backend está sendo estruturado para receber a implementação das entidades, regras de negócio, persistência de dados e automação dos documentos.

O sistema inicialmente será destinado ao responsável pelo processo e à administração. O acesso dos professores e o controle eletrônico de ponto não fazem parte do escopo inicial.

---

## Tecnologias

### Backend

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
* [Maven](https://maven.apache.org/)
* [Git](https://git-scm.com/)
* [Docker](https://www.docker.com/) e Docker Compose
* [PostgreSQL](https://www.postgresql.org/), caso seja utilizado um banco local
* Conta no [Neon](https://neon.com/), caso seja utilizado o PostgreSQL hospedado

---

## Instalação e Execução

> Esta seção será detalhada conforme o fluxo definitivo de execução e configuração do projeto for definido.

---

## Variáveis de Ambiente

As informações de conexão com o banco de dados são configuradas por meio de variáveis de ambiente.

Variáveis utilizadas:

```
DB_URL
DB_USERNAME
DB_PASSWORD
```

Exemplo:

```text
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require&channel_binding=require
DB_USERNAME=SEU_USUARIO
DB_PASSWORD=SUA_SENHA
```

**Nunca coloque senhas ou outras credenciais diretamente no código ou no GitHub.**

---

## Documentação e Testes da API

O projeto utiliza **OpenAPI / Swagger UI** para documentação e testes dos endpoints da API.

Com o backend em execução:

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

Para manter o projeto organizado, todas as alterações devem ser feitas em uma
branch própria e relacionadas a uma Issue do GitHub.

O fluxo utilizado pelo projeto é:

**main → atualizar projeto → selecionar Issue → criar branch → desenvolver → testar → Pull Request → revisão → merge**

### 1. Antes de começar

Primeiro, certifique-se de estar na branch `main`.

```bash
git checkout main
````

Depois, atualize sua cópia local com as alterações mais recentes do GitHub:

```bash
git pull origin main
```

> **Importante:** sempre faça isso antes de iniciar uma nova tarefa.
> Dessa forma, sua branch será criada a partir da versão mais atual da `main`.

---

### 2. Escolha a tarefa no GitHub Projects

As tarefas do projeto são organizadas pelo **GitHub Projects**.

Para começar uma tarefa:

1. Acesse o **GitHub Projects** do projeto.
2. Localize a tarefa que você deseja realizar.
3. Abra a tarefa.
4. Verifique a descrição e os requisitos da Issue.
5. No próprio GitHub, utilize a opção **Create a branch**.
6. O GitHub apresentará as informações necessárias para criar a branch.
7. Utilize o nome da branch seguindo as convenções definidas abaixo.

> **Não é necessário criar uma branch aleatória antes de escolher a tarefa.**
> A branch deve estar relacionada à Issue que você está desenvolvendo.

---

### 3. Convenção de branches

O nome da branch deve indicar o tipo de alteração que será realizada.

Utilize uma das opções abaixo:

* `feat/nome-da-feature` — novas funcionalidades
* `fix/nome-do-bug` — correções de bugs
* `chore/nome-da-tarefa` — manutenção, configurações e tarefas técnicas
* `docs/nome-do-documento` — alterações na documentação
* `refactor/nome-da-alteracao` — refatoração de código sem alterar o comportamento
* `test/nome-do-teste` — criação ou alteração de testes
* `style/nome-da-alteracao` — alterações de formatação ou estilo do código
* `perf/nome-da-melhoria` — melhorias de desempenho

### Exemplos

```text
feat/cadastro-professor
fix/erro-geracao-documento
chore/configuracao-docker
docs/atualizar-readme
refactor/organizar-service-professor
test/teste-cadastro-professor
style/formatacao-controller
perf/melhorar-consulta-professor
```

> **Siga sempre a categoria que melhor representa a alteração realizada.**
> Evite nomes genéricos como `teste`, `alteracao`, `branch1` ou `minha-feature`.

---

### 4. Crie ou utilize a branch da tarefa

Caso o GitHub Projects tenha criado a branch automaticamente, utilize essa
branch localmente.

Caso seja necessário criar a branch manualmente, utilize:

```bash
git checkout -b feat/nome-da-feature
```

Substitua `feat/nome-da-feature` pelo nome relacionado à tarefa.

Por exemplo:

```bash
git checkout -b feat/cadastro-professor
```

Depois de criar a branch, confirme que está nela:

```bash
git branch
```

A branch atual será identificada com `*`.

Exemplo:

```text
  main
* feat/cadastro-professor
```

> **A partir desse momento, todo o desenvolvimento da tarefa deve ser feito
> nessa branch. Não faça alterações diretamente na `main`.**

---

### 5. Desenvolva a tarefa

Faça as alterações necessárias para resolver a Issue.

Durante o desenvolvimento:

* Siga os requisitos descritos na Issue.
* Mantenha o código organizado.
* Utilize os padrões já adotados no projeto.
* Evite alterar arquivos que não sejam necessários para a tarefa.
* Crie ou atualize os testes quando necessário.
* Verifique se sua alteração não quebra funcionalidades existentes.

---

### 6. Faça os testes antes do Pull Request

**O Pull Request só deve ser criado depois que a tarefa estiver concluída e
os testes tiverem sido realizados.**

Antes de enviar a alteração, verifique:

* O projeto inicia normalmente.
* A funcionalidade desenvolvida funciona corretamente.
* Os testes automatizados passam.
* Não existem erros ou warnings importantes relacionados à alteração.
* As funcionalidades existentes continuam funcionando.
* O código está organizado.
* A Issue foi realmente atendida.

No backend, por exemplo, execute:

```bash
mvn test
```

Caso o projeto utilize outras formas de teste ou validação, execute também
os testes necessários para a alteração.

> **Não abra o Pull Request apenas porque terminou de escrever o código.**
> Primeiro finalize, teste e valide a alteração.

---

### 7. Envie a branch para o GitHub

Depois que a implementação estiver concluída e testada, adicione os arquivos:

```bash
git add .
```

Faça o commit:

```bash
git commit -m "feat: descrição da alteração"
```

Depois, envie a branch para o GitHub:

```bash
git push origin feat/nome-da-feature
```

Exemplo:

```bash
git push origin feat/cadastro-professor
```

---

### 8. Crie o Pull Request

Somente após concluir os testes, abra um **Pull Request** da sua branch
para a branch `main`.

O Pull Request deve possuir uma descrição clara, permitindo que outra pessoa
entenda o que foi alterado, por que a alteração foi feita e como ela foi
validada.

Sempre que possível, vincule o Pull Request à **Issue correspondente**.

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

### Prompt para utilizar com uma IA

Caso tenha dificuldade para escrever a descrição do Pull Request, você pode
utilizar o seguinte prompt:

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
Liste os testes que foram realmente executados e informe o resultado.
Não diga que um teste foi realizado se eu não informar que ele foi executado.

## Possíveis impactos
Informe quais partes do sistema podem ser afetadas pela alteração.
Se não houver impacto conhecido, informe isso claramente.

## Issue relacionada
Informe a Issue relacionada ao Pull Request no formato:
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

### Exemplo de Pull Request preenchido

```markdown
## Descrição

Foi realizada a configuração inicial do backend da aplicação,
utilizando Java 21 e Spring Boot.

Também foram adicionadas as configurações iniciais para acesso ao
PostgreSQL, documentação da API e geração de documentos DOCX.

## Motivo da alteração

Essa alteração foi realizada para preparar a estrutura inicial do backend
e permitir o desenvolvimento das próximas funcionalidades do sistema.

## Funcionalidades implementadas

- Configuração inicial do Spring Boot.
- Configuração do PostgreSQL.
- Configuração do JPA/Hibernate.
- Configuração do Swagger/OpenAPI.
- Configuração do Apache POI.
- Criação da estrutura inicial de pacotes.
- Criação de endpoint para teste da API.
- Configuração do Docker.
- Configuração do Docker Compose.

## Testes realizados

- [x] Inicialização do backend.
- [x] Teste do endpoint `/api/teste`.
- [x] Conexão com o banco de dados.
- [x] Execução dos testes automatizados.
- [x] Geração do arquivo JAR.
- [x] Teste da aplicação utilizando Docker.

## Possíveis impactos

Nenhum impacto conhecido nas funcionalidades existentes,
pois esta alteração corresponde à configuração inicial do backend.

## Issue relacionada

Closes #27
```

**Regra principal:** `main` deve permanecer estável. O desenvolvimento é feito na branch da tarefa, e o **Pull Request só é aberto depois que a implementação estiver finalizada e testada**.

---

## Licença

Este projeto foi desenvolvido para fins acadêmicos no projeto da FATEC.
