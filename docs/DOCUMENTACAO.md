# Documentação do Sistema FATEC

## 1. Visão Geral

... *(Conteúdo original a ser mantido)*

## 2. Arquitetura

... *(Conteúdo original a ser mantido)*

## 3. Banco de Dados

... *(Conteúdo original a ser mantido)*

### 3.1 Tecnologias e Configurações **`[Sprint 1 - Manter Usuário]`**

* **SGBD:** PostgreSQL configurado via driver `org.postgresql.Driver`.
* **Gerenciamento de Schema:** Adoção oficial da ferramenta **Flyway** (`spring.flyway.enabled=true`) para versionamento estrutural do banco.
* **Diretório de Migrations:** Os scripts SQL de evolução devem ser mantidos obrigatoriamente no padrão do projeto, dentro de `src/main/resources/db/migration/`.

### 3.2 Estrutura e Extensões **`[Sprint 2 - Manter Professores]`**
- **Extensão de Busca Fuzzy:** Habilitada a extensão `pg_trgm` no PostgreSQL.
- **Índices de Performance:**
    - `idx_professor_nome_trgm`: Índice do tipo **GIN** (`gin_trgm_ops`) na coluna `nome` da tabela `professor` para buscas textuais parciais de alta performance.
    - `idx_professor_matricula`: Índice B-Tree na coluna `matricula` para acelerar buscas diretas.
- **Integridade Relacional e Constraints:**
    - `CONSTRAINT uk_professor_cpf UNIQUE (cpf)` e `CONSTRAINT uk_professor_matricula UNIQUE (matricula)`.
    - `CONSTRAINT ck_professor_cpf CHECK (cpf ~ '^[0-9]{11}$')`.
    - Check constraints para os enums restritos: `regime_contrato`, `regime_juridico`, `status` e `titulacao`.

## 4. Sistema de Grade

... *(Conteúdo original a ser mantido)*

## 5. Folha de Frequência

... *(Conteúdo original a ser mantido)*

## 6. Autenticação e Autorização

### 6.1 Perfis de Acesso **`[Sprint 1 - Manter Usuário]`**

O sistema consolida a gestão de acessos em dois perfis (Roles) principais:

* **TI (Administrador):** Possui acesso geral ao sistema e controle exclusivo sobre a rota e gestão de usuários (cadastrar, atualizar, ativar, desativar e alterar perfis).
* **RESPONSAVEL (Operacional):** Assume as responsabilidades operacionais, administrativas e acadêmicas (ex: gestão de professores, grades e folhas de frequência).

### 6.2 Segurança e JWT **`[Sprint 1 - Manter Usuário]`**

A autenticação do sistema é *Stateless*, baseada em Tokens JWT gerenciados pelo Spring Security.

* **Emissão:** Tokens gerados no momento do login com expiração rigorosa de **2 horas**.
* **Claims:** O token armazena o `login` do usuário no *Subject* e o `id` como uma *Claim* adicional, para evitar consultas repetitivas ao banco.
* **Transmissão:** Todas as requisições protegidas devem enviar o token no cabeçalho HTTP: `Authorization: Bearer <token>`.

### 6.3 Controle de Acesso a Módulos **`[Sprint 2 - Manter Professores]`**
- As rotas do módulo `/api/professores/**` exigem autenticação obrigatória via Bearer Token e permissão `ROLE_TI` ou `ROLE_RESPONSAVEL`.
- 
### 6.3 Tratamento de Acessos Negados **`[Sprint 1 - Manter Usuário]`**

Foram implementados *handlers* customizados (`CustomAuthenticationEntryPoint` e `CustomAccessDeniedHandler`) para padronizar erros de segurança, garantindo que retornos de Falha de Autenticação (HTTP 401) e Falha de Autorização (HTTP 403) sigam o mesmo padrão JSON da API.

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

### 7.2 Endpoints Mapeados **`[Sprint 1 - Manter Usuário]`**

**Autenticação:**

* `POST /api/autenticacao/login` — Rota pública para emissão de token JWT.
* `GET /api/autenticacao/me` — Rota protegida, retorna dados do próprio usuário autenticado.

**Usuários (Acesso exclusivo para Perfil TI):**

* `GET /api/usuarios` — Listagem de usuários.
* `POST /api/usuarios` — Cadastro de usuário.
* `PUT /api/usuarios/{id}` — Atualização de cadastro e senha.
* `PATCH /api/usuarios/{id}/perfil` — Altera o perfil do usuário.
* `PATCH /api/usuarios/{id}/ativar` e `PATCH /api/usuarios/{id}/desativar` — Controle de status da conta.

**Professores (Docentes)**

* `GET /api/professores` — Listagem paginada (Pageable) suportando busca flexível simultânea por nome e matrícula (?busca=termo) via Spring Data Specifications.
* `GET /api/professores/todos` — Listagem completa sem paginação para uso em dropdowns/seletores do frontend.
* `GET /api/professores/{id}` — Consulta detalhada individual do professor.
* `POST /api/professores` — Cadastro de novo professor (Status inicial forçado para ATIVO).
* `PUT /api/professores/{id}` — Atualização completa dos dados cadastrais.
* `PATCH /api/professores/{id}/status` — Alteração isolada de status (ATIVO, INATIVO, AFASTADO).


### 7.3 Documentação OpenAPI (Swagger) **`[Sprint 1 - Manter Usuário]`**

* As rotas da API estão documentadas dinamicamente via OpenAPI, disponíveis nos endpoints públicos `/v3/api-docs/**` e `/swagger-ui/**`.
* A paginação do Spring Data é anotada com `@ParameterObject` para permitir o preenchimento fluido dos parâmetros (page, size, sort) na interface interativa.
## 8. Geração de Documentos

... *(Conteúdo original a ser mantido)*

## 9. Regras de Negócio

### 9.1 Gestão e Ciclo de Vida do Usuário **`[Sprint 1 - Manter Usuário]`**

* **Geração Automática de Login:** O sistema não permite a inserção manual do login. O serviço processa o nome completo (ex: "João da Silva"), extrai o primeiro e o último nome sem acentos ou caracteres especiais, e adiciona um sufixo numérico (ex: `joao.silva.482`), garantindo unicidade no banco.
* **Proteção de Autoalteração de Perfil:** Um usuário, mesmo com perfil de `TI`, está bloqueado de rebaixar ou alterar o próprio perfil. A operação de alteração de perfil valida o ID alvo contra o ID logado.
* **Atualização de Credenciais:** O envio da senha nas respostas e listagens da API é bloqueado. Para atualizar a senha de um usuário, o campo `novaSenha` deve ser enviado explicitamente no DTO de atualização.
* **Bloqueio de Contas Inativas:** Usuários desativados pelo `TI` têm o acesso bloqueado imediatamente no Controller de autenticação, que verifica o status do usuário (`isAtivo()`) antes da checagem da senha, emitindo uma exceção amigável de restrição de acesso.

### 9.2 Gestão de Professores **`[Sprint 1 - Manter Professores]`**

* **Normalização de Dados:** O CPF enviado no payload (com ou sem máscara) é limpo via Regex (`\D`) para salvar no banco apenas 11 dígitos numéricos.
* **Unicidade de Registros:** Não é permitido cadastrar ou atualizar CPF e Matrícula que já estejam em uso por outro professor (retorna HTTP 409 Conflict sem expor dados sensíveis no log).
* **Sem Exclusão Física (Soft Delete / Status):** Professores não possuem endpoint de exclusão (`DELETE`) para preservar a integridade histórica de Grades e Folhas de Frequência. O ciclo de vida é alterado via status (`ATIVO`, `INATIVO`, `AFASTADO`).
* **Busca Flexível::** A busca dinâmica (`?busca=`) filtra por aproximação (`LIKE %termo%`) tanto no nome quanto na matrícula, ignorando maiúsculas/minúsculas.

### 9.3 Inicialização de Dados (Data Seeding) **`[Sprint 1 - Manter Usuário]`**

Caso o banco de dados seja criado do zero, o sistema popula automaticamente dois usuários essenciais para evitar travamento operacional inicial: um `TI` (`admin.ti` Senha: `admin123`) e um `RESPONSAVEL` (`user.resp` Senha:`resp123`).