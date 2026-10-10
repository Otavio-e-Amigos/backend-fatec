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

## Registro de Sprints Anteriores

*(Histórico consolidado e promovido ao DOCUMENTACAO.md)*

* **[Sprint 1]** Alterações de Segurança e Usuários — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 1 e 2]** Manter Professores (Docentes) — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Cadastrar Período Letivo — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Manter Cursos — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Manter Disciplinas — *Promovido ao DOCUMENTACAO.md*

```

