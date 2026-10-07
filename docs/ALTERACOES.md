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

### [Sprint 2] Cadastrar Período Letivo

#### 1. Visão Geral e Mapeamento

* **Data da Conclusão:** 07/10/2026
* **Mapeamento de Escopo:** Implementação do cadastro e consulta de períodos letivos para identificação dos semestres acadêmicos (ex.: 2026/1 e 2026/2), servindo de base histórica para o vínculo com Grades e validações de datas na Folha de Frequência.

#### 2. Alterações e Decisões de Negócio

* **Decisões Tomadas:**
* **Obrigatoriedade de Datas:** Definido que `data_inicio` e `data_fim` são obrigatórias para permitir o alerta de "mês fora do período da grade" no módulo de Folha de Frequência.
* **Escopo Mínimo de Operações:** Implementados apenas o cadastro (`POST`) e as consultas (`GET`). Edição e exclusão ficaram fora de escopo para preservar a integridade referencial com futuras Grades.
* **Compatibilidade de Tipos:** Utilização do tipo `INTEGER` para `ano` e `semestre` no SQL (em vez de `SMALLINT`) para alinhamento estrito 1:1 com a entidade JPA (`Integer`) e validação do Hibernate.


* **Regras de Negócio e Validações:**
* *Regra 1 (Unicidade de Período):* Impedimento de cadastro de dois períodos com o mesmo ano e semestre (retorna HTTP 409 Conflict via `ConflitoException`).
* *Regra 2 (Consistência Temporal):* Exigência de que `data_inicio` seja estritamente anterior à `data_fim` (retorna HTTP 400 Bad Request via `RegraNegocioException`).
* *Regra 3 (Restrição de Semestre e Ano):* Semestre restrito aos valores `1` ou `2`; Ano validado no intervalo de `2000` a `2050`.
* *Regra 4 (Ordenação Cronológica):* A listagem geral retorna os períodos ordenados do mais recente para o mais antigo (`ano DESC, semestre DESC`).



#### 3. Impactos em Segurança e Perfis de Acesso

* **Permissões:**
* As rotas `/api/periodos-letivos/**` foram liberadas para os perfis `TI` e `RESPONSAVEL` (`hasAnyRole("TI", "RESPONSAVEL")`).


* **Regras de Bloqueio:**
* Requisições sem Token JWT ou provenientes de usuários inativos são rejeitadas pela camada de segurança (`401 Unauthorized` / `403 Forbidden`).



#### 4. Endpoints e Contratos da API

* `POST /api/periodos-letivos` — Cadastra um novo período letivo (`201 Created`). Exige perfil `TI` ou `RESPONSAVEL`.
* `GET /api/periodos-letivos` — Lista os períodos ordenados do mais recente ao mais antigo (`200 OK`). Exige perfil `TI` ou `RESPONSAVEL`.
* `GET /api/periodos-letivos/{id}` — Detalha um período específico por ID (`200 OK` / `404 Not Found`). Exige perfil `TI` ou `RESPONSAVEL`.

#### 5. Banco de Dados e Persistência

* **Migrations Flyway:** Script criado em `V7__criacao_periodo_letivo.sql`.
* **Alterações de Schema:**
* Criação da tabela `periodo_letivo` com colunas `id` (BIGINT), `semestre` (INTEGER), `ano` (INTEGER), `data_inicio` (DATE) e `data_fim` (DATE).
* Adição de `CONSTRAINT uk_periodo_letivo_ano_semestre UNIQUE (ano, semestre)`.
* Adição de `CHECK constraints`: `ck_periodo_letivo_semestre` (`semestre IN (1, 2)`), `ck_periodo_letivo_ano` (`ano BETWEEN 2000 AND 2050`) e `ck_periodo_letivo_datas` (`data_inicio < data_fim`).



---

## Registro de Sprints Anteriores

*(Histórico consolidado e promovido ao DOCUMENTACAO.md)*

* **[Sprint 1]** Alterações de Segurança e Usuários — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Manter Professores (Docentes) — *Promovido ao DOCUMENTACAO.md*

```

```