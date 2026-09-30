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

### 7.3 Documentação OpenAPI (Swagger) **`[Sprint 1 - Manter Usuário]`**

As rotas da API estão documentadas dinamicamente via OpenAPI, disponíveis nos endpoints públicos `/v3/api-docs/**` e `/swagger-ui/**`.

## 8. Geração de Documentos

... *(Conteúdo original a ser mantido)*

## 9. Regras de Negócio

### 9.1 Gestão e Ciclo de Vida do Usuário **`[Sprint 1 - Manter Usuário]`**

* **Geração Automática de Login:** O sistema não permite a inserção manual do login. O serviço processa o nome completo (ex: "João da Silva"), extrai o primeiro e o último nome sem acentos ou caracteres especiais, e adiciona um sufixo numérico (ex: `joao.silva.482`), garantindo unicidade no banco.
* **Proteção de Autoalteração de Perfil:** Um usuário, mesmo com perfil de `TI`, está bloqueado de rebaixar ou alterar o próprio perfil. A operação de alteração de perfil valida o ID alvo contra o ID logado.
* **Atualização de Credenciais:** O envio da senha nas respostas e listagens da API é bloqueado. Para atualizar a senha de um usuário, o campo `novaSenha` deve ser enviado explicitamente no DTO de atualização.
* **Bloqueio de Contas Inativas:** Usuários desativados pelo `TI` têm o acesso bloqueado imediatamente no Controller de autenticação, que verifica o status do usuário (`isAtivo()`) antes da checagem da senha, emitindo uma exceção amigável de restrição de acesso.

### 9.2 Inicialização de Dados (Data Seeding) **`[Sprint 1 - Manter Usuário]`**

Caso o banco de dados seja criado do zero, o sistema popula automaticamente dois usuários essenciais para evitar travamento operacional inicial: um `TI` (`admin.ti` Senha: `admin123`) e um `RESPONSAVEL` (`user.resp` Senha:`resp123`).