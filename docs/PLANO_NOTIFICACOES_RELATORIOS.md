# Notificações, Relatórios e Lembrete Anual do Calendário — Análise e Plano

Sprint 5 do Appointer. Este documento registra a análise feita antes da implementação, as decisões tomadas e a ordem em que as partes foram construídas.

---

## 1. Análise do que já existia

| Item | Situação encontrada | Impacto |
|---|---|---|
| Extração de grade (`POST /api/grades/importacao/extrair`) | Isolada: lê o PDF pelo Gemini e devolve o JSON. **Não grava nada no banco.** | Não havia como contar "grades extraídas por período". Foi preciso criar um histórico do processamento. |
| Geração da folha de ponto (`/api/folhas-ponto/gerar`) | Gera o DOCX em memória, transação `readOnly`. **Não registra nada.** | Mesma situação: foi criado um histórico de geração. A gravação usa transação própria (`REQUIRES_NEW`), porque a transação original é somente leitura. |
| Calendário acadêmico | Eventos **em memória** (`CalendarioAcademicoService`), sem tabela. Decisão D-03 (armazenamento definitivo) segue pendente na documentação. | Não dá para saber, a partir dos eventos, se o calendário "foi atualizado" (são perdidos ao reiniciar). Foi criado um marco persistente de confirmação anual, sem mexer no CRUD em memória. |
| Autenticação | JWT stateless; o `principal` é o ID do usuário. Perfis `TI` e `RESPONSAVEL`. | Notificações identificam o destinatário pelo ID do usuário autenticado. |
| Padrões | `RespostaPadraoDTO`, `GlobalExceptionHandler`, Flyway, entidades sem Lombok nos cadastros básicos, testes com Mockito e MockMvc standalone. | Seguidos em todo o código novo. |
| Documentos | Apache POI já presente (DOCX). Nenhuma biblioteca de PDF (a geração em PDF da folha foi revertida no commit `32ca780`). | Adicionado OpenPDF 2.0.3 apenas para os relatórios. A folha de ponto continua só em DOCX. |

Dados disponíveis para os relatórios (sem inventar informação):

- **Professor:** nome, código, matrícula, titulação, regimes, status. O CPF nunca aparece nos relatórios.
- **Grades:** período letivo (ano/semestre, datas), carga semanal/mensal/total.
- **Disciplinas da grade:** disciplina (sigla, nome, código), curso e turno, quantidade de aulas, status (`MINISTRANDO`/`CANCELADA`).
- **Novos históricos:** data/hora, usuário que executou, resultado da extração (disciplinas, aulas, avisos, tempo, motivo da falha), competência da folha, grade usada, dias sem aula no mês.

Limitação assumida: extrações e folhas geradas **antes** desta versão não aparecem nos relatórios, porque não havia registro. Os documentos exportados informam isso no rodapé da última página.

---

## 2. Modelo de dados (migrations V12 a V15)

| Tabela | Finalidade | Pontos de integridade |
|---|---|---|
| `notificacao` | Uma linha por usuário destinatário. Guarda categoria, tipo, título, mensagem, referência, leitura (`lida`, `lida_em`), pendência (`pendente`, `resolvida_em`), `chave_deduplicacao`, `expira_em`, `created_at`. | `UNIQUE (usuario_id, chave_deduplicacao)` impede duplicidade; índices por usuário/data e parcial para não lidas. |
| `registro_extracao_grade` | Histórico de cada extração tentada (sucesso, sucesso com avisos, falha). | Sem CPF. `professor_id` preenchido quando a matrícula extraída já existe no cadastro. FKs com `SET NULL`. |
| `registro_folha_ponto` | Histórico de cada folha gerada (download do DOCX). | `grade_id` com `SET NULL` para **não bloquear** a exclusão de grades (regra existente); ano/semestre do período ficam copiados. |
| `calendario_atualizacao` | Confirmação de que o calendário do ano foi revisado (PK = ano). | `CHECK (ano BETWEEN 2000 AND 2050)`. |

Notificações destinadas a um perfil (lembrete do calendário) são gravadas uma por usuário. Isso permite controlar leitura individualmente e manter histórico sem tabela de associação.

---

## 3. Etapas de implementação e dependências

```text
Etapa 1  Migrations + enums + entidades + repositórios
   │
Etapa 2  NotificacaoService / NotificacaoController  ─────────────┐
   │                                                              │
Etapa 3  Históricos (extração e folha) integrados aos fluxos ─────┤ dependem da 2
   │                                                              │
Etapa 4  Lembrete anual do calendário (serviço + agendador) ──────┘
   │
Etapa 5  Relatórios (extração e folha)              ← dependem da 3
   │
Etapa 6  Exportação PDF/DOCX (modelo único + 2 renderizadores) ← depende da 5
   │
Etapa 7  Segurança, tratamento de erros, testes e documentação
```

### Arquivos criados

| Camada | Arquivos |
|---|---|
| Migrations | `V12__criacao_notificacao.sql`, `V13__criacao_registro_extracao_grade.sql`, `V14__criacao_registro_folha_ponto.sql`, `V15__criacao_calendario_atualizacao.sql` |
| Enums | `CategoriaNotificacao`, `TipoNotificacao`, `StatusExtracaoGrade`, `TipoPeriodoRelatorio`, `FormatoRelatorio` |
| Entidades | `Notificacao`, `RegistroExtracaoGrade`, `RegistroFolhaPonto`, `CalendarioAtualizacao` |
| Repositórios | `NotificacaoRepository`, `RegistroExtracaoGradeRepository`, `RegistroFolhaPontoRepository`, `CalendarioAtualizacaoRepository`; `specification/NotificacaoSpecs` |
| Serviços | `NotificacaoService`, `HistoricoExtracaoGradeService`, `HistoricoFolhaPontoService`, `CalendarioAtualizacaoService`, `CalendarioLembreteAgendador`, `UsuarioAutenticadoService`; `service/relatorio/*` (`PeriodoRelatorio`, `RelatorioExtracaoGradeService`, `RelatorioFolhaPontoService`, `RelatorioDocumentoMontador`, `RelatorioExportacaoService`, `ArquivoRelatorio`) |
| Documentos | `document/relatorio/DocumentoRelatorio`, `RelatorioPdfRenderer`, `RelatorioDocxRenderer` |
| Controllers | `NotificacaoController`, `RelatorioController`, `CalendarioAtualizacaoController` |
| DTOs | `dto/notificacao/*`, `dto/relatorio/*`, `dto/calendario/StatusAtualizacaoCalendarioDTO`, `ConfirmacaoAtualizacaoCalendarioDTO` |
| Config | `AgendamentoConfig` (`@EnableScheduling` + `Clock` em America/Sao_Paulo) |

### Arquivos alterados (somente o necessário)

| Arquivo | Alteração |
|---|---|
| `pom.xml` | Dependência `com.github.librepdf:openpdf:2.0.3`. |
| `SecurityConfig` | `/api/relatorios/**` para TI e RESPONSAVEL; `/api/notificacoes/**` para qualquer usuário autenticado. |
| `GlobalExceptionHandler` | 400 padronizado para parâmetro com tipo inválido (ex: data mal formatada) e parâmetro obrigatório ausente. Antes esses casos caíam no 500 genérico. |
| `ExtracaoGradeService` | Registra sucesso/falha da extração e o tempo de processamento. O comportamento da extração não mudou. |
| `FolhaPontoService` | Após gerar o DOCX, registra o histórico. Nada mais mudou. |
| `UsuarioRepository`, `GradeRepository`, `DisciplinaDaGradeRepository`, `ProfessorRepository` | Novas consultas usadas pelos relatórios e pelas notificações por perfil. |
| `application.properties` | `appointer.calendario.lembrete.habilitado` e `appointer.calendario.lembrete.cron`. |
| `FolhaPontoServiceTest` | Mock do novo colaborador e verificação do registro do histórico. |

---

## 4. Decisões

1. **Falha no histórico nunca derruba a operação principal.** Registro e notificação rodam em transação própria e qualquer erro é só logado. O download da folha e a resposta da extração continuam iguais.
2. **Notificação de extração também em caso de falha.** A extração pode levar até 60 s e o usuário pode sair da tela; a notificação informa o motivo.
3. **Folha de ponto com aviso** quando a grade usada é de outro período letivo (o serviço atual usa a última grade do professor como alternativa). A mensagem também informa os dias sem aula do mês.
4. **Calendário "atualizado" = confirmação explícita** (`POST /api/calendario/atualizacao/{ano}/confirmar`). Adicionar um evento não basta para considerar o calendário revisado, e os eventos em memória se perdem ao reiniciar.
5. **Lembrete só em fevereiro.** Verificação diária às 07:00 (cron configurável) e também na inicialização da aplicação. A notificação expira em 1º de março e deixa de aparecer na listagem padrão (continua no histórico com `incluirExpiradas=true`). Se o ano já estiver confirmado — antes ou durante fevereiro — nada é criado e pendências abertas são encerradas.
6. **Semana do relatório = segunda a domingo (ISO-8601)**, a mesma convenção de `dia_semana` usada nas aulas.
7. **Um modelo de documento, dois renderizadores.** Os relatórios são convertidos uma vez em `DocumentoRelatorio`; o PDF (OpenPDF) e o DOCX (POI) recebem exatamente o mesmo conteúdo.

---

## 5. Melhorias sugeridas (fora do escopo desta sprint)

- Persistir os eventos do calendário no PostgreSQL (decisão D-03). Com isso, a confirmação anual poderia ser inferida pelos próprios eventos.
- Registrar também as gravações de grade quando a extração passar a persistir `Grade`/`DisciplinaDaGrade`/`AulaDaGrade` (próximo passo previsto no commit da extração).
- Política de retenção para notificações lidas antigas (ex: remover após 12 meses).
