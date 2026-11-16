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

---

### [Sprint 5] Notificações Internas, Relatórios Gerenciais (PDF/DOCX) e Lembrete Anual do Calendário

#### 1. Visão Geral e Mapeamento

* **Data da Conclusão:** 16/11/2026
* **Mapeamento de Escopo:** Sistema de notificações internas; histórico de extrações de grade e de folhas de ponto geradas; relatórios diário, semanal e mensal de extração de grades e de folhas de ponto (individual e geral) com exportação em PDF e DOCX; lembrete anual de atualização do calendário em fevereiro. Análise e plano em `docs/PLANO_NOTIFICACOES_RELATORIOS.md`.

#### 2. Alterações e Decisões de Negócio

* **Decisões Tomadas:**
* A extração de grade e a geração da folha não gravavam nada; foram criados históricos próprios (`registro_extracao_grade`, `registro_folha_ponto`) para viabilizar os relatórios. Registros anteriores a esta versão não existem.
* O calendário continua em memória (D-03). A atualização anual passa a ser uma confirmação explícita persistida em `calendario_atualizacao`.
* OpenPDF 2.0.3 adicionado somente para os relatórios; a folha de ponto continua apenas em DOCX.
* **Regras de Negócio e Validações:**
* *Regra 1:* Histórico e notificação em transação própria; falhas nessa etapa não interrompem a extração nem o download da folha.
* *Regra 2:* Notificações deduplicadas por `(usuario_id, chave_deduplicacao)`; o lembrete do calendário expira em 1º de março e é encerrado pela confirmação do ano.
* *Regra 3:* Filtros de relatório: período obrigatório (`DIARIO`, `SEMANAL`, `MENSAL`), data opcional (padrão hoje), sem datas futuras, ano mínimo 2000, formato `PDF` ou `DOCX`.
* *Regra 4:* O CPF não é armazenado no histórico de extração nem exibido nos relatórios.

#### 3. Impactos em Segurança e Perfis de Acesso

* **Permissões:** `/api/relatorios/**` para `ROLE_TI` e `ROLE_RESPONSAVEL`; `/api/notificacoes/**` para qualquer usuário autenticado; `/api/calendario/atualizacao/**` coberto pela regra existente de `/api/calendario/**`.
* **Regras de Bloqueio:** Notificação de outro usuário responde `404`. Parâmetro com tipo inválido ou ausente passa a responder `400` padronizado (antes caía no `500`).

#### 4. Endpoints e Contratos da API

* `GET /api/notificacoes`, `GET /api/notificacoes/contagem`, `GET /api/notificacoes/{id}`, `PATCH /api/notificacoes/{id}/lida`, `PATCH /api/notificacoes/{id}/nao-lida`, `PATCH /api/notificacoes/lidas`
* `GET /api/relatorios/extracoes-grade`, `GET /api/relatorios/extracoes-grade/exportar`
* `GET /api/relatorios/folhas-ponto`, `GET /api/relatorios/folhas-ponto/exportar`
* `GET /api/calendario/atualizacao`, `GET /api/calendario/atualizacao/{ano}`, `POST /api/calendario/atualizacao/{ano}/confirmar`
* Swagger: a tag "Notificações" descreve o fluxo de uso no frontend (contagem no sininho, lista, leitura individual e "marcar todas como lidas").
* Documentos PDF/DOCX: sem as seções de evolução diária, principais disciplinas e resumo do período (os dados do resumo passaram para os indicadores); campos vazios ficam em branco.

#### 5. Banco de Dados e Persistência

* **Migrations Flyway:** `V12__criacao_notificacao.sql`, `V13__criacao_registro_extracao_grade.sql`, `V14__criacao_registro_folha_ponto.sql`, `V15__criacao_calendario_atualizacao.sql`.
* **Alterações de Schema:** quatro tabelas novas; nenhuma tabela existente foi alterada.

---

## Registro de Sprints Consolidadas

* **[Sprint 1]** Alterações de Segurança, Perfis de Acesso e Gestão de Usuários — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 1 e 2]** Manter Professores (Docentes) e Configurações Iniciais de Banco — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Cadastros Básicos (Período Letivo, Cursos e Disciplinas) — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 3]** Cadastro e Gerenciamento de Grades, Disciplinas da Grade, Aulas da Grade e Edição de Período Letivo — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 4]** Geração de Documentos — Módulo Folha de Ponto / Folha de Frequência (DOCX via Apache POI) — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 5]** Notificações Internas, Relatórios Gerenciais (PDF/DOCX) e Lembrete Anual do Calendário — *Promovido ao DOCUMENTACAO.md*