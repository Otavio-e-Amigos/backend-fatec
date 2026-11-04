-- Adiciona a coluna de chave da API do Gemini opcional por usuário
ALTER TABLE usuario ADD COLUMN gemini_api_key VARCHAR(200);
