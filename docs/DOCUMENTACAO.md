# Documentação do Sistema FATEC

## 1. Visão Geral

... *(Conteúdo original a ser mantido)*

## 2. Arquitetura

... *(Conteúdo original a ser mantido)*

## 3. Banco de Dados

### 3.1 Tecnologias e Configurações **`[Sprint 1 - Manter Usuário]`**

* **SGBD:** PostgreSQL configurado via driver `org.postgresql.Driver`.
* **Gerenciamento de Schema:** Adoção oficial da ferramenta **Flyway** (`spring.flyway.enabled=true`) para versionamento estrutural do banco.
* **Diretório de Migrations:** Os scripts SQL de evolução devem ser mantidos obrigatoriamente no padrão do projeto, dentro de `src/main/resources/db/migration/`.

### 3.2 Estrutura e Extensões **`[Sprint 1 e 2 - Manter Professores]`**

* **Extensão de Busca Fuzzy:** Habilitada a extensão `pg_trgm` no PostgreSQL.
* **Índices de Performance:**
* `idx_professor_nome_trgm`: Índice do tipo **GIN** (`gin_trgm_ops`) na coluna `nome` da tabela `professor` para buscas textuais parciais de alta performance.
* `idx_professor_matricula`: Índice B-Tree na coluna `matricula` para acelerar buscas diretas.


* **Integridade Relacional e Constraints:**
* `CONSTRAINT uk_professor_cpf UNIQUE (cpf)` e `CONSTRAINT uk_professor_matricula UNIQUE (matricula)`.
* `CONSTRAINT ck_professor_cpf CHECK (cpf ~ '^[0-9]{11}$')`.
* Check constraints para os enums restritos: `regime_contrato`, `regime_juridico`, `status` e `titulacao`.



### 3.3 Tabela Período Letivo **`[Sprint 2 - Cadastrar Período Letivo]`**

* **Tabela `periodo_letivo`:** Colunas `id` (BIGINT), `semestre` (INTEGER), `ano` (INTEGER), `data_inicio` (DATE) e `data_fim` (DATE).
* **Integridade Relacional e Constraints:**
* `CONSTRAINT uk_periodo_letivo_ano_semestre UNIQUE (ano, semestre)`.
* `CONSTRAINT ck_periodo_letivo_semestre CHECK (semestre IN (1, 2))`.
* `CONSTRAINT ck_periodo_letivo_ano CHECK (ano BETWEEN 2000 AND 2050)`.
* `CONSTRAINT ck_periodo_letivo_datas CHECK (data_inicio < data_fim)`.



### 3.4 Tabela Curso **`[Sprint 2 - Manter Cursos]`**

* **Tabela `curso`:** Colunas `id` (BIGINT), `nome` (VARCHAR 150), `turno` (VARCHAR 5), `unidade` (VARCHAR 50, padrão `'FATEC Zona Leste'`) e `sigla` (VARCHAR 3).
* **Integridade Relacional e Constraints:**
* `CONSTRAINT ck_curso_turno CHECK (turno IN ('MANHA', 'TARDE', 'NOITE'))`.
* `CONSTRAINT ck_curso_sigla_letras CHECK (sigla ~ '^[A-Za-z]+$')`.
* Trava contra textos vazios ou preenchidos com espaços nas pontas (`btrim`).
* Índices únicos compostos: `uk_curso_unidade_nome_turno_lower` sobre `(unidade, lower(nome), turno)` e `uk_curso_unidade_sigla_turno_upper` sobre `(unidade, upper(sigla), turno)`, garantindo a unicidade de nome e sigla restrita ao mesmo turno dentro da unidade.



### 3.5 Tabela Disciplina **`[Sprint 2 - Manter Disciplinas]`**

* **Tabela `disciplina`:** Colunas `id` (BIGINT), `codigo` (VARCHAR 30), `sigla` (VARCHAR 8) e `nome` (VARCHAR 150).
* **Integridade Relacional e Constraints:**
* `CONSTRAINT uk_disciplina_codigo UNIQUE (codigo)`.
* `CONSTRAINT ck_disciplina_sigla_letras CHECK (sigla ~ '^[A-Za-z]+$')`.
* `CONSTRAINT ck_disciplina_codigo_sigla_diferentes CHECK (upper(codigo) <> upper(sigla))`.
* Travas contra campos vazios (`btrim <> ''`).
* Índices de performance: `idx_disciplina_nome_trgm` (GIN com `pg_trgm`), `idx_disciplina_codigo` e `idx_disciplina_sigla` (B-Tree).



---

## 4. Sistema de Grade

... *(Conteúdo original a ser mantido)*

---

## 5. Folha de Frequência

... *(Conteúdo original a ser mantido)*

---

## 6. Autenticação e Autorização

### 6.1 Perfis de Acesso **`[Sprint 1 - Manter Usuário]`**

O sistema consolida a gestão de acessos em dois perfis (Roles) principais:

* **TI (Administrador):** Possui acesso geral ao sistema e controle exclusivo sobre a rota e gestão de usuários (cadastrar, atualizar, ativar, desativar e alterar perfis).
* **RESPONSAVEL (Operacional):** Assume as responsabilidades operacionais, administrativas e acadêmicas (ex: gestão de professores, períodos letivos, cursos, disciplinas, grades e folhas de frequência).

### 6.2 Segurança e JWT **`[Sprint 1 - Manter Usuário]`**

A autenticação do sistema é *Stateless*, baseada em Tokens JWT gerenciados pelo Spring Security.

* **Emissão:** Tokens gerados no momento do login com expiração rigorosa de **2 horas**.
* **Claims:** O token armazena o `login` do usuário no *Subject* e o `id` como uma *Claim* adicional, para evitar consultas repetitivas ao banco.
* **Transmissão:** Todas as requisições protegidas devem enviar o token no cabeçalho HTTP: `Authorization: Bearer <token>`.

### 6.3 Controle de Acesso a Módulos **`[Sprint 2]`**

* As rotas dos módulos `/api/professores/**`, `/api/periodos-letivos/**`, `/api/cursos/**` e `/api/disciplinas/**` exigem autenticação obrigatória via Bearer Token e permissão `ROLE_TI` ou `ROLE_RESPONSAVEL`.

### 6.4 Tratamento de Acessos Negados **`[Sprint 1 - Manter Usuário]`**

* Foram implementados *handlers* customizados (`CustomAuthenticationEntryPoint` e `CustomAccessDeniedHandler`) para padronizar erros de segurança, garantindo que retornos de Falha de Autenticação (HTTP 401) e Falha de Autorização (HTTP 403) sigam o mesmo padrão JSON da API.

---

## 7. API

### 7.1 Padrão de Respostas **`[Sprint 1 - Manter Usuário]`**

Todas as requisições da API são padronizadas pela classe `RespostaPadraoDTO` e filtradas pelo `GlobalExceptionHandler`, garantindo o seguinte formato de saída, omitindo campos nulos:

```json
{
  "mensagem": "Texto explicativo",
  "status": 200,
  "dados": { ... } 
}

```

### 7.2 Endpoints Mapeados

**Autenticação:**

* `POST /api/autenticacao/login` — Rota pública para emissão de token JWT.
* `GET /api/autenticacao/me` — Rota protegida, retorna dados do próprio usuário autenticado.

**Usuários (Acesso exclusivo para Perfil TI):**

* `GET /api/usuarios` — Listagem de usuários.
* `POST /api/usuarios` — Cadastro de usuário.
* `PUT /api/usuarios/{id}` — Atualização de cadastro e senha.
* `PATCH /api/usuarios/{id}/perfil` — Altera o perfil do usuário.
* `PATCH /api/usuarios/{id}/ativar` e `PATCH /api/usuarios/{id}/desativar` — Controle de status da conta.

**Professores (Docentes) `[Sprint 1 e 2 - Manter Professores]`:**

* `GET /api/professores` — Listagem paginada (`Pageable`) suportando busca flexível simultânea por nome e matrícula (`?busca=termo`) via Spring Data Specifications.
* `GET /api/professores/todos` — Listagem completa sem paginação para uso em dropdowns/seletores do frontend.
* `GET /api/professores/{id}` — Consulta detalhada individual do professor.
* `POST /api/professores` — Cadastro de novo professor (Status inicial forçado para ATIVO).
* `PUT /api/professores/{id}` — Atualização completa dos dados cadastrais.
* `PATCH /api/professores/{id}/status` — Alteração isolada de status (ATIVO, INATIVO, AFASTADO).

**Períodos Letivos `[Sprint 2 - Cadastrar Período Letivo]`:**

* `POST /api/periodos-letivos` — Cadastra um novo período letivo (`201 Created`).
* `GET /api/periodos-letivos` — Lista os períodos ordenados do mais recente ao mais antigo (`200 OK`).
* `GET /api/periodos-letivos/{id}` — Detalha um período específico por ID (`200 OK` / `404 Not Found`).

**Cursos `[Sprint 2 - Manter Cursos]`:**

* `GET /api/cursos` — Listagem completa de todos os cursos ordenados por nome (`200 OK`).
* `GET /api/cursos/{id}` — Consulta individual detalhada do curso por ID (`200 OK` / `404 Not Found`).
* `POST /api/cursos` — Cadastro de novo curso (`201 Created`).
* `PUT /api/cursos/{id}` — Atualização completa dos dados do curso (`200 OK` / `409 Conflict`).

**Disciplinas `[Sprint 2 - Manter Disciplinas]`:**

* `GET /api/disciplinas` — Listagem paginada com suporte a busca dinâmica multicampo (`?busca=termo&page=0&size=10`).
* `GET /api/disciplinas/todas` — Listagem completa sem paginação para uso em dropdowns/seletores (`200 OK`).
* `GET /api/disciplinas/{id}` — Detalhes da disciplina por ID (`200 OK` / `404 Not Found`).
* `POST /api/disciplinas` — Cadastra nova disciplina (`201 Created`).
* `PUT /api/disciplinas/{id}` — Atualiza os dados da disciplina (`200 OK` / `409 Conflict`).
* `DELETE /api/disciplinas/{id}` — Remove uma disciplina (`200 OK` / `404 Not Found`).

### 7.3 Documentação OpenAPI (Swagger) **`[Sprint 1 - Manter Usuário]`**

* As rotas da API estão documentadas dinamicamente via OpenAPI, disponíveis nos endpoints públicos `/v3/api-docs/**` e `/swagger-ui/**`.
* A paginação do Spring Data é anotada com `@ParameterObject` para permitir o preenchimento fluido dos parâmetros (`page`, `size`, `sort`) na interface interativa.

---

## 8. Geração de Documentos

... *(Conteúdo original a ser mantido)*

---

## 9. Regras de Negócio

### 9.1 Gestão e Ciclo de Vida do Usuário **`[Sprint 1 - Manter Usuário]`**

* **Geração Automática de Login:** O sistema não permite a inserção manual do login. O serviço processa o nome completo (ex: "João da Silva"), extrai o primeiro e o último nome sem acentos ou caracteres especiais, e adiciona um sufixo numérico (ex: `joao.silva.482`), garantindo unicidade no banco.
* **Proteção de Autoalteração de Perfil:** Um usuário, mesmo com perfil de `TI`, está bloqueado de rebaixar ou alterar o próprio perfil. A operação de alteração de perfil valida o ID alvo contra o ID logado.
* **Atualização de Credenciais:** O envio da senha nas respostas e listagens da API é bloqueado. Para atualizar a senha de um usuário, o campo `novaSenha` deve ser enviado explicitamente no DTO de atualização.
* **Bloqueio de Contas Inativas:** Usuários desativados pelo `TI` têm o acesso bloqueado imediatamente no Controller de autenticação, que verifica o status do usuário (`isAtivo()`) antes da checagem da senha, emitindo uma exceção amigável de restrição de acesso.

### 9.2 Gestão de Professores **`[Sprint 1 e 2 - Manter Professores]`**

* **Normalização de Dados:** O CPF enviado no payload (com ou sem máscara) é limpo via Regex (`\D`) para salvar no banco apenas 11 dígitos numéricos.
* **Unicidade de Registros:** Não é permitido cadastrar ou atualizar CPF e Matrícula que já estejam em uso por outro professor (retorna HTTP 409 Conflict sem expor dados sensíveis no log).
* **Sem Exclusão Física (Soft Delete / Status):** Professores não possuem endpoint de exclusão (`DELETE`) para preservar a integridade histórica de Grades e Folhas de Frequência. O ciclo de vida é alterado via status (`ATIVO`, `INATIVO`, `AFASTADO`).
* **Busca Flexível:** A busca dinâmica (`?busca=`) filtra por aproximação (`LIKE %termo%`) tanto no nome quanto na matrícula, ignorando maiúsculas/minúsculas.

### 9.3 Gestão de Período Letivo **`[Sprint 2 - Cadastrar Período Letivo]`**

* **Obrigatoriedade e Validação de Datas:** `data_inicio` e `data_fim` são obrigatórias para servirem de base na validação da Folha de Frequência. A `data_inicio` deve ser estritamente anterior à `data_fim` (retorna HTTP 400 Bad Request em caso de inconsistência).
* **Unicidade de Período:** Não é permitido cadastrar dois períodos com o mesmo `ano` e `semestre` (retorna HTTP 409 Conflict via `ConflitoException`).
* **Escopo Mínimo Operacional:** Implementados apenas o cadastro (`POST`) e consultas (`GET`), sem suporte a edição ou exclusão para preservar a integridade histórica de Grades associadas.
* **Ordenação Cronológica:** A listagem geral retorna os períodos ordenados do mais recente para o mais antigo (`ano DESC, semestre DESC`).

### 9.4 Gestão de Cursos **`[Sprint 2 - Manter Cursos]`**

* **Unicidade Relativizada por Turno:** O mesmo nome de curso (ex: "ADS") ou mesma sigla podem existir na mesma unidade, desde que em turnos diferentes (ex: ADS no turno MANHÃ e ADS no turno NOITE). Cadastros duplicados no mesmo turno retornam HTTP 409 Conflict.
* **Formato Estrito da Sigla:** A sigla é limitada a no máximo 3 caracteres e restrita exclusivamente a letras (`^[a-zA-Z]+$`), rejeitando números e caracteres especiais.
* **Sem Exclusão Física (Soft Delete / Integridade):** Não possui endpoint `DELETE` para evitar inconsistências em Grades Horárias associadas.
* **Listagem Direta:** Retorna todos os cursos ordenados por nome em uma única lista sem paginação.

### 9.5 Gestão de Disciplinas **`[Sprint 2 - Manter Disciplinas]`**

* **Sanitização de Texto:** Todos os campos de texto passam por remoção automática de espaços em branco sobressalentes (`.trim()`) antes do salvamento.
* **Sigla Alfabética:** A sigla aceita até 8 caracteres e exige exclusivamente letras (`^[a-zA-Z]+$`).
* **Divergência entre Código e Sigla:** Bloqueio caso o `codigo` seja idêntico à `sigla` (retorna HTTP 400 Bad Request via `RegraNegocioException`).
* **Unicidade de Código:** Impedimento de cadastrar ou alterar disciplinas com códigos já existentes (retorna HTTP 409 Conflict via `ConflitoException`).
* **Busca Multicampo Flexível:** A pesquisa por termo (`?busca=`) filtra simultaneamente nos campos `nome`, `codigo` e `sigla`.
* **Exclusão Física:** Suporte ao endpoint `DELETE /api/disciplinas/{id}` para remoção de disciplinas.

### 9.6 Inicialização de Dados (Data Seeding) **`[Sprint 1 - Manter Usuário]`**

Caso o banco de dados seja criado do zero, o sistema popula automaticamente dois usuários essenciais para evitar travamento operacional inicial: um `TI` (`admin.ti` Senha: `admin123`) e um `RESPONSAVEL` (`user.resp` Senha: `resp123`).