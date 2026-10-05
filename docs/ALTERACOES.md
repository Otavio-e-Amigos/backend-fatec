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

## Registro de Sprints Anteriores

*(Histórico consolidado e promovido ao DOCUMENTACAO.md)*

* **[Sprint 1]** Alterações de Segurança e Usuários — *Promovido ao DOCUMENTACAO.md*
* **[Sprint 2]** Manter Professores (Docentes) — *Promovido ao DOCUMENTACAO.md*

```
