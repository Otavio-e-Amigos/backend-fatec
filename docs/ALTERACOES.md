# Histórico de Alterações da Documentação e do Sistema FATEC

Este arquivo registra as principais alterações, decisões e mudanças realizadas no projeto após a criação das documentações técnicas iniciais em PDF.

O objetivo é manter um histórico das mudanças para que, posteriormente, a documentação técnica possa ser revisada e consolidada com base nos documentos PDF existentes, neste histórico e no código atual do sistema.

---

## 1. Alterações de Segurança e Usuários

### 1.1 Perfis de acesso

**Situação anterior:**

A documentação inicial considerava os perfis:

* RESPONSAVEL
* TI
* ACOMPANHAMENTO

**Alteração definida:**

O sistema passará a utilizar somente dois perfis:

* `TI`
* `RESPONSAVEL`

O perfil `ACOMPANHAMENTO` não será implementado, pois suas funções serão consideradas parte das responsabilidades do perfil `RESPONSAVEL`.

### 1.2 Responsabilidades dos perfis

#### TI

O perfil `TI` terá acesso geral ao sistema e será o único perfil autorizado a:

* cadastrar novos usuários;
* alterar dados de usuários;
* alterar perfil/permissões de usuários;
* ativar e desativar usuários;
* realizar manutenção relacionada às contas de acesso.

#### RESPONSAVEL

O perfil `RESPONSAVEL` terá acesso às operações administrativas e acadêmicas do sistema, incluindo:

* cadastrar e gerenciar professores;
* cadastrar e gerenciar informações necessárias à Grade;
* gerar e editar Grades;
* gerar Folhas de Frequência;
* realizar as operações necessárias ao funcionamento do sistema.

Usuários `RESPONSAVEL` não poderão alterar o próprio perfil ou suas permissões.

---

## 2. Autenticação e Segurança

A autenticação e a segurança da aplicação serão implementadas utilizando **Spring Security**.

O sistema utilizará:

* login com usuário e senha;
* senha armazenada somente em formato de hash;
* **JWT** para autenticação das requisições;
* **Spring Security** para autenticação e autorização;
* controle de acesso baseado nos perfis `TI` e `RESPONSAVEL`;
* validação das permissões no backend.

O Spring Security será responsável por proteger os endpoints da API, validar a autenticação dos usuários e aplicar as regras de autorização definidas para cada perfil.

O frontend não será responsável sozinho por impedir o acesso às funcionalidades. As permissões deverão ser obrigatoriamente validadas no backend.

### Perfis de acesso

* `TI`: acesso geral e gerenciamento dos usuários e suas permissões.
* `RESPONSAVEL`: acesso às funcionalidades operacionais e administrativas permitidas pelo sistema.

### JWT

Após uma autenticação válida, o sistema utilizará um token JWT para identificar o usuário nas requisições seguintes.

As requisições protegidas deverão enviar o token no cabeçalho de autorização:

```text
Authorization: Bearer <token>
```

O Spring Security será responsável por processar a autenticação das requisições e aplicar as regras de autorização correspondentes ao usuário autenticado.

---

## 3. API de Autenticação e Usuários

As rotas relacionadas à autenticação e gerenciamento de usuários utilizarão nomenclatura em português.

### Autenticação

* `POST /api/autenticacao/login`
* `GET /api/autenticacao/me`

### Usuários

* `GET /api/usuarios`
* `GET /api/usuarios/{id}`
* `POST /api/usuarios`
* `PUT /api/usuarios/{id}`
* `PATCH /api/usuarios/{id}/ativar`
* `PATCH /api/usuarios/{id}/desativar`
* `PATCH /api/usuarios/{id}/perfil`

---

## 4. Banco de Dados e Migrations

O projeto utilizará migrations versionadas para controlar a evolução do banco de dados.

A ferramenta definida para isso é o **Flyway**.

As migrations deverão:

* ser versionadas;
* possuir nomes padronizados;
* ser armazenadas no projeto;
* permitir que o banco seja criado/evoluído de forma controlada;
* evitar a necessidade de criação manual das tabelas a cada ambiente.

Exemplo de estrutura:

```text
src/main/resources/db/migration/
├── V1__criacao_inicial.sql
├── V2__criacao_usuario.sql
└── V3__alteracao_usuario.sql
```

O Hibernate/JPA não deverá ser utilizado como mecanismo principal para controlar a evolução do schema em ambientes finais. A evolução estrutural do banco deverá ser controlada pelas migrations.

---

## 5. Organização da Documentação

Os documentos técnicos existentes em PDF serão mantidos como referência durante o desenvolvimento.

A documentação editável ficará organizada da seguinte forma:

```text
docs/
├── DOCUMENTACAO.md
├── ALTERACOES.md
├── Documentacao_Tecnica_Banco_de_Dados_FATEC.pdf
├── Documentacao_Tecnica_da_Grade.pdf
└── Documentacao_Tecnica_Folha_de_Frequencia.pdf
```

O `DOCUMENTACAO.md` representa a documentação técnica evolutiva do projeto.

O `ALTERACOES.md` registra mudanças relevantes realizadas após a criação dos documentos PDF.

Os PDFs não precisam ser alterados a cada mudança durante o desenvolvimento.

---

## 6. Atualização da Documentação ao Final de Cada Sprint

Durante o desenvolvimento de uma sprint ou ciclo, as novas decisões, alterações e funcionalidades deverão ser registradas inicialmente neste arquivo (`ALTERACOES.md`).

Ao final de cada sprint ou ciclo concluído, deverá ser realizada uma revisão das alterações registradas.

As alterações que tiverem sido efetivamente implementadas e validadas deverão ser incorporadas ao `DOCUMENTACAO.md`.

O processo deverá seguir esta sequência:

1. **Durante a sprint:** registrar novas decisões e alterações no `ALTERACOES.md`.
2. **Final da sprint:** revisar o que foi realmente implementado.
3. **Validar as alterações:** verificar se a implementação corresponde ao que foi definido.
4. **Atualizar o `DOCUMENTACAO.md`:** incorporar as informações técnicas que passaram a fazer parte do sistema.
5. **Manter o histórico:** preservar o registro da alteração no `ALTERACOES.md`.
6. **Não atualizar os PDFs a cada sprint:** os PDFs permanecem como documentação inicial de referência durante o desenvolvimento.

Dessa forma, o `ALTERACOES.md` funciona como histórico das mudanças, enquanto o `DOCUMENTACAO.md` permanece como a documentação técnica atualizada do sistema.

### Exemplo

Durante uma sprint foi definida e implementada a autenticação com JWT.

Durante a sprint:

```text
ALTERACOES.md
→ registrar a decisão e as alterações realizadas.
```

Ao finalizar a sprint:

```text
ALTERACOES.md
→ mantém o histórico da mudança.

DOCUMENTACAO.md
→ recebe a documentação definitiva da autenticação com JWT.
```

---

## 7. Consolidação Final

Quando o sistema estiver próximo da finalização, a documentação deverá ser revisada considerando:

1. os três documentos PDF existentes;
2. o `DOCUMENTACAO.md`;
3. o `ALTERACOES.md`;
4. o código atual do projeto;
5. a estrutura atual do banco de dados;
6. as migrations do Flyway;
7. a documentação da API;
8. as regras de negócio implementadas.

A documentação final deverá consolidar essas informações em um único documento técnico atualizado, removendo informações obsoletas, duplicidades e inconsistências.

O objetivo é que a documentação final represente o sistema efetivamente implementado, e não apenas as decisões realizadas durante o planejamento.


# CONFIGURAÇÕES DO BANCO DE DADOS

# URL de conexão com o PostgreSQL
spring.datasource.url=${DB_URL}

# Usuário do banco
spring.datasource.username=${DB_USERNAME}

# Senha do banco
spring.datasource.password=${DB_PASSWORD}

# Driver usado para conectar ao PostgreSQL
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / HIBERNATE / FLAYWAR

# Não altera automaticamente as tabelas do banco
spring.jpa.hibernate.ddl-auto=update

# Não exibe os comandos SQL no console
spring.jpa.show-sql=false

# Fecha a.md conexão com o banco após cada operação
spring.jpa.open-in-view=false

# Ativa o Flyway para gerenciamento das migrations.
spring.flyway.enabled=true

# Só se o banco já tiver tabelas criadas manualmente:
# spring.flyway.baseline-on-migrate=true

# LOGS

# Mostra apenas erros do Hibernate
logging.level.org.hibernate=ERROR

# Esconde informações do pool de conexões
logging.level.org.hibernate.orm.connections.pooling=ERROR

#JWT

# Chave secreta usada para assinar os tokens JWT.
api.security.token.secret=${JWT_SECRET}

# SERVER

# Define a.md porta da aplicação
server.port=8080
