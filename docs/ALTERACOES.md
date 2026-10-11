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

### [Sprint X] Nome da Feature / Módulo Principal

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

* **Migrations Flyway:** Lista de novos arquivos de migração criados (`VX__exemplo.sql`).
* **Alterações de Schema:** Tabelas criadas, colunas alteradas ou relacionamentos ajustados.

### [Sprint 3] Cadastro e Gerenciamento de Grades

1. #### Visão Geral e Mapeamento

- **Data da Conclusão:** 10/10/2026
- **Mapeamento de Escopo:** Implementação do módulo completo de Grades para organização da carga horária docente, vinculando professores a períodos letivos, com suporte a histórico acadêmico, consultas paginadas, métricas de carga horária opcionais e validações estritas de unicidade.

#### 2. Alterações e Decisões de Negócio

- **Decisões Tomadas:**
    - **Métricas de Carga Horária Opcionais:** Os campos `semanal`, `mensal` e `total` aceitam valores nulos (`null`) no cadastro e na edição, permitindo o registro do vínculo docente mesmo sem o preenchimento detalhado das horas (essencial para geração de documentos e cenários iniciais).
    - **Preservação de Histórico (Soft / Sem Delete):** Ausência intencional de exclusão física (`DELETE /api/grades`) para proteger o histórico acadêmico de períodos letivos passados. O ciclo de vida se baseia na criação de novas grades por período.

- **Regras de Negócio e Validações:**
    - *Regra 1 (Unicidade por Professor e Período):* Impedimento de cadastro ou atualização de mais de uma grade para o mesmo professor no mesmo período letivo (retorna HTTP 409 Conflict via `ConflitoException`).
    - *Regra 2 (Validação de Carga Horária):* Caso informadas, as métricas numéricas devem ser obrigatoriamente maiores ou iguais a zero (`@PositiveOrZero`).
    - *Regra 3 (Integridade Referencial Estrita):* As chaves estrangeiras (`professor_id` e `periodo_letivo_id`) contam com `ON DELETE RESTRICT` no banco de dados para bloquear exclusões acidentais de registros vinculados.

#### 3. Impactos em Segurança e Perfis de Acesso

- **Permissões:** As rotas `/api/grades/**` e `/api/professores/{id}/grades` exigem autenticação obrigatória via Bearer Token e permissão exclusiva para os perfis `TI` ou `RESPONSAVEL` (`hasAnyRole("TI", "RESPONSAVEL")`).
- **Regras de Bloqueio:** Tentativas de acesso sem token (`401`) ou com credenciais insuficientes/inativas (`403`) são interceptadas pelos handlers customizados.

#### 4. Endpoints e Contratos da API

- `POST /api/grades` — Cadastra uma nova grade vinculando professor e período letivo (`201 Created`).
- `GET /api/grades` — Listagem paginada de grades utilizando `@ParameterObject` para paginação e ordenação (`200 OK`).
- `GET /api/professores/{id}/grades` — Retorna o histórico completo de grades de um professor específico (`200 OK`).
- `GET /api/grades/{id}` — Consulta detalhada de uma grade específica por ID (`200 OK` / `404 Not Found`).
- `PUT /api/grades/{id}` — Atualiza dados da grade (carga horária e vínculos), atualizando automaticamente o `updated_at` (`200 OK` / `409 Conflict`).

#### 5. Banco de Dados e Persistência

- **Migrations Flyway:** Script criado em `V8__criacao_grade.sql`.
- **Alterações de Schema:**
    - Criação da tabela `grade` com colunas `id` (BIGINT), `professor_id` (BIGINT, FK), `periodo_letivo_id` (BIGINT, FK), `semanal`, `mensal`, `total` (`NUMERIC(6,2)`, opcionais), `created_at` e `updated_at` (`TIMESTAMP`).
    - Adição das constraints de chave estrangeira `fk_grade_professor` e `fk_grade_periodo` com a regra `ON DELETE RESTRICT`.

---

### [Sprint 3] Cadastro e Gerenciamento de Disciplina da Grade

1. #### Visão Geral e Mapeamento

- **Data da Conclusão:** 11/10/2026
- **Mapeamento de Escopo:** Implementação da entidade de associação `DisciplinaDaGrade`, conectando Grades, Disciplinas e Cursos para definir o planejamento acadêmico, turnos, semestres, quantidade de aulas e estados operacionais.

#### 2. Alterações e Decisões de Negócio

- **Decisões Tomadas:**
  - **Exclusão Livre:** Remoção de travas históricas complexas na exclusão, permitindo que a associação seja removida livremente via serviço conforme a necessidade administrativa.
  - **Padronização de Status via Enum:** Utilização do Enum `StatusDisciplinaGrade` contendo rigidamente os estados `MINISTRANDO` e `CANCELADA`, substituindo validações por texto livre.

- **Regras de Negócio e Validações:**
  - *Regra 1 (Quantidade de Aulas Obrigatória):* O campo `quantidade_aulas` é obrigatório e validado por banco de dados através de `CHECK (quantidade_aulas > 0)`.
  - *Regra 2 (Unicidade de Vínculo):* Validação no serviço utilizando `existsByGradeIdAndDisciplinaIdAndCursoId` para evitar duplicidade da mesma disciplina para o mesmo curso na mesma grade (lançando HTTP 409 Conflict via `ConflitoException`).
  - *Regra 3 (Integridade Referencial Estrita):* As chaves estrangeiras (`grade_id`, `disciplina_id`, `curso_id`) contam com restrição `ON DELETE RESTRICT`.
  - *Regra 4 (Normalização de Texto):* Campos de texto opcionais como `periodo` passam por normalização de espaços e conversão para maiúsculo (`.trim().toUpperCase()`).

#### 3. Impactos em Segurança e Perfis de Acesso

- **Permissões:** As rotas de manipulação de disciplinas da grade exigem autenticação via Bearer Token e permissão exclusiva para os perfis `TI` ou `RESPONSAVEL` (`hasAnyRole("TI", "RESPONSAVEL")`).
- **Regras de Bloqueio:** Tentativas de acesso sem token (`401`) ou credenciais inválidas/insuficientes (`403`) são tratadas pelos handlers globais.

#### 4. Endpoints e Contratos da API

- `POST /api/disciplinas-grade` — Associa uma disciplina a uma grade e curso (`201 Created`).
- `GET /api/grades/{id}/disciplinas` — Retorna a lista de disciplinas vinculadas a uma Grade específica (`200 OK`).
- `GET /api/disciplinas-grade/{id}` — Consulta os detalhes de uma associação específica por ID (`200 OK` / `404 Not Found`).
- `PUT /api/disciplinas-grade/{id}` — Atualiza os dados da associação (`200 OK`).
- `DELETE /api/disciplinas-grade/{id}` — Remove a vinculação da disciplina à grade (`200 OK`).

#### 5. Banco de Dados e Persistência

- **Migrations Flyway:** Script criado em `V9__criacao_disciplina_da_grade.sql`.
- **Alterações de Schema:**
  - Criação da tabela `disciplina_da_grade` com colunas `id`, `grade_id`, `disciplina_id`, `curso_id`, `semestre_curso`, `periodo`, `quantidade_aulas`, `op`, `status`, `created_at` e `updated_at`.
  - Configuração de `ON DELETE RESTRICT` nas FKs e `CHECK` constraint em `quantidade_aulas`.

---

### [Sprint 4] Cadastro e Gerenciamento de Aula da Grade

1. #### Visão Geral e Mapeamento

- **Data da Conclusão:** 11/10/2026
- **Mapeamento de Escopo:** Implementação do módulo de alocação de horários e blocos de aula (`AULA_DA_GRADE`), permitindo o registro de dias da semana (ISO-8601) e intervalos temporais vinculados às disciplinas da grade, servindo como base essencial para o planejamento acadêmico e futura geração da Folha de Frequência.

#### 2. Alterações e Decisões de Negócio

- **Decisões Tomadas:**
  - **Abordagem de Bloco Contínuo:** Períodos longos de aula (ex: 13:00 às 18:20) são gravados em um único registro contínuo por sessão, ficando a expansão programática para slots de 50 minutos sob responsabilidade da camada de serviço.
  - **Validação de Conflito em Camada de Serviço:** Como a tabela de aulas aponta indiretamente para a Grade através de `DISCIPLINA_DA_GRADE`, a regra complexa de prevenção de sobreposição de horários na mesma grade é processada de forma centralizada no `AulaDaGradeService` utilizando comparação temporal matemática (`(StartA < EndB) && (StartB < EndA)`).

- **Regras de Negócio e Validações:**
  - *Regra 1 (Validação Temporal):* A `hora_fim` deve ser obrigatoriamente posterior à `hora_inicio`, garantida tanto via `CHECK` no banco de dados quanto por validação no serviço (lançando `RegraNegocioException` / `400 Bad Request`).
  - *Regra 2 (Bloqueio de Sobreposição):* Tentativas de alocar horários conflitantes no mesmo dia para a mesma grade bloqueiam a operação imediatamente (lançando `ConflitoException` / `409 Conflict`).
  - *Regra 3 (Domínio de Dias da Semana):* O campo `dia_semana` valida rigorosamente a faixa numérica de 1 a 7 conforme o padrão ISO-8601.
  - *Regra 4 (Integridade Referencial Estrita):* A chave estrangeira `disciplina_grade_id` utiliza restrição `ON DELETE RESTRICT`.

#### 3. Impactos em Segurança e Perfis de Acesso

- **Permissões:** As rotas do módulo `/api/aulas-grade/**` e associadas exigem autenticação obrigatória via Bearer Token e permissão exclusiva para os perfis **`TI`** ou **`RESPONSAVEL`** (`hasAnyRole("TI", "RESPONSAVEL")`).
- **Regras de Bloqueio:** Tentativas de acesso sem token (`401`) ou credenciais insuficientes (`403`) são tratadas pelos handlers globais.

#### 4. Endpoints e Contratos da API

- `POST /api/disciplinas-grade/{id}/aulas` — Registra os horários de aula em bloco contínuo para uma disciplina da grade (`201 Created` / `400 Bad Request` / `409 Conflict`).
- `GET /api/disciplinas-grade/{id}/aulas` — Retorna todas as aulas vinculadas a uma disciplina da grade específica (`200 OK`).
- `DELETE /api/aulas-grade/{id}` — Remove um horário de aula cadastrado (`200 OK` / `404 Not Found`).

#### 5. Banco de Dados e Persistência

- **Migrations Flyway:** Script criado em `V10__criacao_aula_da_grade.sql`.
- **Alterações de Schema:**
  - Criação da tabela `aula_da_grade` com colunas `id`, `disciplina_grade_id`, `dia_semana` (SMALLINT), `hora_inicio` (TIME) e `hora_fim` (TIME).
  - Adição de índice na FK `disciplina_grade_id` e constraints `CHECK` para validação de horários e dias da semana.


---
## Registro de Sprints Anteriores

*(Histórico consolidado e promovido ao DOCUMENTACAO.md)*

* **[Sprint 1]** Alterações de Segurança e Usuários — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 1 e 2]** Manter Professores (Docentes) — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Cadastrar Período Letivo — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Manter Cursos — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Manter Disciplinas — *Promovido ao DOCUMENTACAO.md*

```

