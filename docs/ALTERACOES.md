Aqui está a versão completa e atualizada do seu arquivo **`ALTERACOES.md`**, já com as anotações do módulo de Professores adicionadas como **Sprint 2**, seguindo exatamente a estrutura e o template do seu guia.

Você pode copiar o conteúdo abaixo e substituir no seu arquivo.

---

# Histórico de Alterações da Documentação e do Sistema FATEC

Este arquivo registra as alterações, decisões arquiteturais e mudanças de regras de negócio realizadas no projeto ao longo do desenvolvimento.

O objetivo deste documento é manter o registro cronológico das mudanças por sprint para que a documentação técnica principal (`DOCUMENTACAO.md`) possa ser atualizada de forma incremental e validada.

---

## Estrutura do Fluxo de Documentação

```text
docs/
├── DOCUMENTACAO.md                         (Documentação técnica canônica e evolutiva)
├── ALTERACOES.md                           (Histórico contínuo de mudanças por Sprint)
├── Documentacao_Tecnica_Banco_de_Dados.pdf (Referência inicial - Baseline)
├── Documentacao_Tecnica_da_Grade.pdf       (Referência inicial - Baseline)
└── Documentacao_Tecnica_Folha_Frequencia.pdf (Referência inicial - Baseline)

```

1. **Durante a Sprint:** Qualquer nova regra, endpoint criado ou ajuste arquitetural deve ser anotado neste arquivo (`ALTERACOES.md`).
2. **Final da Sprint:** O que foi efetivamente entregue e testado deve ser transposto para as seções correspondentes do `DOCUMENTACAO.md` com a tag identificadora da Sprint (ex: `[Sprint X - Nome do Módulo]`).
3. **Preservação de Histórico:** As entradas neste arquivo devem permanecer registradas para fins de auditoria.

---

## Guia de Registro por Sprint (Modelo para Preenchimento)

---

* ### [Sprint X] Nome da Feature / Módulo Principal



#### 1. Visão Geral e Mapeamento

* **Data da Conclusão:** DD/MM/AAAA
* **Mapeamento de Escopo:** Breve resumo das funcionalidades cobertas.

#### 2. Alterações e Decisões de Negócio

* **Decisões Tomadas:** Descrição de regras alteradas em relação aos requisitos e PDFs iniciais.
* **Regras de Negócio e Validações:**
* *Regra 1:* Detalhar validações inseridas no Backend/Services.
* *Regra 2:* Restrições de mutação ou estados de entidades.



#### 3. Impactos em Segurança e Perfis de Acesso

* **Permissões:** Mapeamento de quem pode executar as novas rotas (`ROLE_TI`, `ROLE_RESPONSAVEL`).
* **Regras de Bloqueio:** Validações de acesso ou exceções tratadas no fluxo de autorização.

#### 4. Endpoints e Contratos da API

Listagem das novas rotas ou alterações nos contratos existentes:

* `MÉTODO /api/exemplo` — Descrição da rota e permissão exigida.

#### 5. Banco de Dados e Persistência

* **Migrations Flyway:** Lista de novos arquivos de migração criados (`V4__exemplo.sql`).
* **Alterações de Schema:** Tabelas criadas, colunas alteradas ou relacionamentos ajustados.

---

## Registro de Sprints Anteriores

---

### [Sprint 2] Manter Professores (Docentes)

#### 1. Visão Geral e Mapeamento

* **Data da Conclusão:** 02/10/2026
* **Mapeamento de Escopo:** Cadastro, edição, listagem e inativação de Professores (Docentes) do sistema, com validações rigorosas de dados sensíveis e enums.

#### 2. Alterações e Decisões de Negócio

* **Decisões Tomadas:**
* **Enums explícitos:** O `RegimeJuridico` passou a ser um `Enum` (CLT, PSS) no código para garantir consistência, substituindo o uso de `String` livre.
* **Nova Titulação:** Inclusão do valor `POS_DOUTOR` na regra de Titulação.
* **Edição Completa (PUT):** O endpoint de atualização (`PUT`) realiza a *substituição completa* dos dados (exceto o id). Não atua como `PATCH` (ignorar nulos). Todos os dados do professor devem ser reenviados no payload.
* **Status centralizado:** Em vez de dois endpoints separados (`/ativar` e `/desativar`), optou-se por um único endpoint genérico `PATCH /status` recebendo a intenção no corpo (ATIVO, INATIVO, AFASTADO).


* **Regras de Negócio e Validações:**
* *Regra 1 (Privacidade do CPF):* O CPF não é devolvido na listagem geral de professores para evitar exposição de dados pessoais. Ele é retornado apenas na busca individual (`GET /{id}`), criação (`POST`) e atualização (`PUT`).
* *Regra 2 (Tamanho de colunas):* Os campos `codigo` e `matricula` foram limitados a no máximo 10 caracteres tanto na validação (DTO) quanto no banco de dados.



#### 3. Impactos em Segurança e Perfis de Acesso

* **Permissões:**
* Toda a rota `/api/professores/**` foi protegida.
* O acesso é permitido exclusivamente aos perfis `TI` e `RESPONSAVEL` (`hasAnyRole("TI", "RESPONSAVEL")`).



#### 4. Endpoints e Contratos da API

* `GET /api/professores` — Lista todos os professores. CPF é omitido (`null`) no payload de retorno. Exige `TI` ou `RESPONSAVEL`.
* `GET /api/professores/{id}` — Detalha um professor específico. Retorna o CPF. Exige `TI` ou `RESPONSAVEL`.
* `POST /api/professores` — Cria um novo professor (Status inicial é forçado como `ATIVO`). Exige `TI` ou `RESPONSAVEL`.
* `PUT /api/professores/{id}` — Atualização completa dos dados do professor. Exige `TI` ou `RESPONSAVEL`.
* `PATCH /api/professores/{id}/status` — Altera apenas o status do professor (ATIVO, INATIVO, AFASTADO). Exige `TI` ou `RESPONSAVEL`.

#### 5. Banco de Dados e Persistência

* **Migrations Flyway:** O script da tabela `professor` foi criado  `V4__criacao_professor.sql`.
* **Alterações de Schema:**
* Tabela `professor` criada com mapeamento estrito.
* Adição de `CONSTRAINT UNIQUE` para `cpf` e `matricula`.
* Adição de `CONSTRAINT CHECK` no banco para validar o formato do CPF (`^[0-9]{11}$`) e os valores textuais restritos dos enums `regime_contrato`, `regime_juridico`, `status` e `titulacao`.



---

### [Sprint 1] Alterações de Segurança e Usuários

#### 1.1 Perfis de acesso

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

#### 1.2 Responsabilidades dos perfis

**TI**
O perfil `TI` terá acesso geral ao sistema e será o único perfil autorizado a:

* cadastrar novos usuários;
* alterar dados de usuários;
* alterar perfil/permissões de usuários;
* ativar e desativar usuários;
* realizar manutenção relacionada às contas de acesso.

**RESPONSAVEL**
O perfil `RESPONSAVEL` terá acesso às operações administrativas e acadêmicas do sistema, incluindo:

* cadastrar e gerenciar professores;
* cadastrar e gerenciar informações necessárias à Grade;
* gerar e editar Grades;
* gerar Folhas de Frequência;
* realizar as operações necessárias ao funcionamento do sistema.

Usuários `RESPONSAVEL` não poderão alterar o próprio perfil ou suas permissões.

#### 1.3 Autenticação e Segurança

A autenticação e a segurança da aplicação serão implementadas utilizando **Spring Security**.

O sistema utilizará:

* login com usuário e senha;
* senha armazenada somente em formato de hash;
* **JWT** para autenticação das requisições;
* **Spring Security** para autenticação e autorização;
* controle de acesso baseado nos perfis `TI` e `RESPONSAVEL`;
* validação das permissões no backend.

O Spring Security será responsável por proteger os endpoints da API, validar a autenticação dos usuários e aplicar as regras de autorização definidas para cada perfil. O frontend não será responsável sozinho por impedir o acesso às funcionalidades. As permissões deverão ser obrigatoriamente validadas no backend.

**JWT**
Após uma autenticação válida, o sistema utilizará um token JWT para identificar o usuário nas requisições seguintes. As requisições protegidas deverão enviar o token no cabeçalho de autorização:
`Authorization: Bearer <token>`

#### 1.4 API de Autenticação e Usuários

As rotas relacionadas à autenticação e gerenciamento de usuários utilizarão nomenclatura em português.

**Autenticação**

* `POST /api/autenticacao/login`
* `GET /api/autenticacao/me`

**Usuários**

* `GET /api/usuarios`
* `GET /api/usuarios/{id}`
* `POST /api/usuarios`
* `PUT /api/usuarios/{id}`
* `PATCH /api/usuarios/{id}/ativar`
* `PATCH /api/usuarios/{id}/desativar`
* `PATCH /api/usuarios/{id}/perfil`

#### 1.5 Banco de Dados e Migrations

O projeto utilizará migrations versionadas para controlar a evolução do banco de dados utilizando **Flyway**.
As migrations deverão:

* ser versionadas;
* possuir nomes padronizados;
* ser armazenadas no projeto;
* permitir que o banco seja criado/evoluído de forma controlada;
* evitar a necessidade de criação manual das tabelas a cada ambiente.

O Hibernate/JPA não deverá ser utilizado como mecanismo principal para controlar a evolução do schema em ambientes finais. A evolução estrutural do banco deverá ser controlada pelas migrations.

#### 1.6 Organização da Documentação

Os documentos técnicos existentes em PDF serão mantidos como referência durante o desenvolvimento. A documentação editável ficará organizada da seguinte forma:

```text
docs/
├── DOCUMENTACAO.md
├── ALTERACOES.md
├── Documentacao_Tecnica_Banco_de_Dados_FATEC.pdf
├── Documentacao_Tecnica_da_Grade.pdf
└── Documentacao_Tecnica_Folha_de_Frequencia.pdf

```

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