package br.com.fatec.backend.dto.autenticacao;

import br.com.fatec.backend.enums.Perfil;

public record AuthResponseDTO(
        String token,
        Long id,
        String nome,
        Perfil perfil
) {}
