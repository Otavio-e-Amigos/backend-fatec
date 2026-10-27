git commit -F - <<'EOF'
EXTRAÇÃO DE GRADES (PDF)
- Cria o endpoint POST /api/grades/importacao/extrair, que recebe o PDF da grade horária (multipart, campo "arquivo") e devolve os dados extraídos no wrapper RespostaPadraoDTO
- A leitura do PDF é feita pela API do Gemini (SDK google-genai), que apenas extrai os dados: nenhuma regra de negócio é delegada à IA
- Funcionalidade isolada: não consulta nem grava nada no banco, não usa Grade, DisciplinaDaGrade ou AulaDaGrade e não tem migration
- Retornos: 200 em caso de sucesso, 400 para arquivo vazio, não PDF, acima do limite ou JSON inválido da IA, 401/403 sem acesso e 503 quando o Gemini está indisponível
- Rota já coberta pela regra existente de /api/grades/** (TI e RESPONSAVEL), sem alteração no SecurityConfig

DEPENDÊNCIA E CONFIGURAÇÃO
- pom.xml: adiciona o google-genai 1.66.0
- application.properties: gemini.api-key (GEMINI_API_KEY), gemini.model (GEMINI_MODEL) e gemini.timeout-segundos, com valores padrão para a aplicação subir mesmo sem a chave
- application.properties: limite de upload de 10 MB (arquivo e requisição)
- Cria o GeminiProperties (@ConfigurationProperties) e registra com @ConfigurationPropertiesScan no BackendApplication
- A chave da API fica somente no backend e o modelo é configurável por variável de ambiente, sem valor fixo no código

CONTRATO DE EXTRAÇÃO (DTO)
- Cria o GradeExtraidaDTO com records aninhados: ProfessorExtraido, HorasExtraidas, DisciplinaExtraida e AulaExtraida
- Contrato independente das entidades do sistema: não possui IDs nem referências a Grade, Curso ou Disciplina
- Datas e horários trafegam como String, para que formatos fora do padrão virem aviso em vez de quebrar a leitura do JSON
- @JsonIgnoreProperties(ignoreUnknown = true), listas nulas viram listas vazias e método comAvisos() para devolver o objeto com os avisos preenchidos
- Campo avisos reservado para as pendências da extração e da futura integração com o sistema

EXTRATOR (GEMINI)
- Cria a interface ExtratorDeGrade, que permite trocar de provedor ou usar mock nos testes
- GeminiExtratorDeGrade: envia o PDF e o prompt ao Gemini e recebe a resposta em JSON (responseMimeType); a temperatura é mantida no padrão do modelo
- Prompt: o dia da semana vem da coluna do cabeçalho, cada bloco de 50 minutos é mantido separado como impresso, o período vem da coluna da tabela de disciplinas (e não do horário da aula), o sufixo "(Determinada)" é removido, o CPF vem só com dígitos, campos ausentes viram null e assinaturas são ignoradas
- O cliente do Gemini é criado apenas no primeiro uso, então a aplicação sobe normalmente sem a chave, e é fechado no encerramento
- Remove cercas de código (json) caso o modelo as inclua na resposta
- Conversão do JSON com o JsonMapper do Jackson 3, padrão do Spring Boot 4
- Chave ausente ou falha na chamada: ExtracaoIndisponivelException (503), com mensagem específica para limite de uso (429)
- Resposta vazia ou fora do formato: RegraNegocioException (400)
- O conteúdo da resposta não é registrado em log, pois contém nome e CPF do professor

VALIDAÇÃO E NORMALIZAÇÃO
- Cria o ValidadorGradeExtraida, classe sem acesso ao banco que devolve os dados normalizados e a lista de problemas encontrados
- Normalizações: CPF somente com dígitos, remoção do sufixo entre parênteses no nome da disciplina, período, contrato e status sem acento e em maiúsculas, horários H:mm convertidos para HH:mm
- Período letivo: semestre 1 ou 2, ano entre 2000 e 2100, datas em aaaa-mm-dd e validade fim não anterior à validade início
- Professor: nome e matrícula obrigatórios, CPF com 11 dígitos e contrato válido
- Disciplinas: ao menos uma, com nome, curso, período e status válidos
- Aulas: dia da semana de 1 a 7, horários válidos, término posterior ao início e quantidadeAulas igual ao número de horários
- Detecta sobreposição de horários no mesmo dia entre quaisquer disciplinas (término igual ao início não conflita)
- Na extração, os problemas viram avisos e não bloqueiam a resposta

ENDPOINT DE VALIDAÇÃO
- Cria o endpoint POST /api/grades/importacao/validar, que recebe os dados corrigidos (o mesmo JSON do /extrair, sem o wrapper de resposta), revalida e normaliza
- Aqui os problemas bloqueiam: retorna 400 com todos os problemas na mensagem; em caso de sucesso devolve os dados normalizados
- O backend nunca confia só na validação do front: o fluxo é extrair, o usuário corrigir os dados e reenviar para validação

SERVICE E CONTROLLER
- Cria o ExtracaoGradeService, que valida o arquivo (vazio, acima de 10 MB e assinatura %PDF-) antes de acionar a IA e orquestra o extrator e o validador
- Cria o ExtracaoGradeController, documentado no Swagger na tag "Extração de Grade (PDF)"

TRATAMENTO DE ERROS
- Cria a ExtracaoIndisponivelException, tratada no GlobalExceptionHandler como 503
- GlobalExceptionHandler: MaxUploadSizeExceededException e MissingServletRequestPartException passam a retornar 400 com mensagem clara, em vez de cair no erro 500 genérico

VERIFICADO MANUALMENTE (SWAGGER)
- /extrair com o Grade_Exemplo.pdf: professor, três disciplinas, sábado e quarta-feira, horas e período corretos, sem avisos
- /validar com corpo incompleto: rejeitado com 400 listando os campos ausentes

FICA PARA PRÓXIMOS COMMITS
- Testes automatizados do ValidadorGradeExtraida e do ExtracaoGradeService, com o ExtratorDeGrade mockado
- Schema de resposta do Gemini para travar o formato do JSON
- Mesclagem dos blocos de 50 minutos em bloco contínuo
- Resolução de professor, curso, disciplina e período no banco e gravação de Grade, DisciplinaDaGrade e AulaDaGrade
  EOF