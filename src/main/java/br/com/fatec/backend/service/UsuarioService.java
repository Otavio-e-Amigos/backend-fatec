package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.usuario.UsuarioCreateDTO;
import br.com.fatec.backend.dto.usuario.UsuarioResponseDTO;
import br.com.fatec.backend.dto.usuario.UsuarioUpdateDTO;
import br.com.fatec.backend.entity.Perfil;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.List;
import java.util.Random;

@Service
public class UsuarioService {

    // Repository usado para acessar os usuários no banco.
    private final UsuarioRepository repository;
    // Usado para gerar o hash das senhas.
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();
    /**
     * Recebe as dependências usadas pelo Service.
     */
    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Função auxiliar (KISS/DRY) para buscar a entidade ou lançar erro se não existir.
     * Ela é privada pois só interessa a esta classe Service.
     */
    private Usuario buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado com ID: " + id));
    }

    // Controla a operação no banco como uma única transação. (Essa operação com o banco deve ser tratada como uma única transação.)
    // Indica que a operação apenas consulta dados.
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {

        return repository.findAll()
                .stream() // O .stream() permite processar esses usuários um por um.
                .map(UsuarioResponseDTO::daEntidade) // Converte cada usuário para o DTO de resposta.
                .toList(); // Junta os DTOs em uma lista.
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioResponseDTO.daEntidade(buscarEntidadePorId(id));
    }


    // Pode consultar e alterar dados.
    @Transactional
    public UsuarioResponseDTO criar(UsuarioCreateDTO dto) {
        String loginGerado = gerarLoginAutomatico(dto.nome());
        String senhaHash = passwordEncoder.encode(dto.senha());

        Usuario usuario = new Usuario(dto.nome(), loginGerado, senhaHash, dto.perfil());
        return UsuarioResponseDTO.daEntidade(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarEntidadePorId(id);

        usuario.atualizarDados(dto.nome(), usuario.getLogin());

        return UsuarioResponseDTO.daEntidade(repository.save(usuario));
    }
    @Transactional
    public void alterarPerfil(Long idAlvo, Perfil novoPerfil, Long idUsuarioLogado) {
        if (idAlvo.equals(idUsuarioLogado)) {
            throw new RegraNegocioException("Não é permitido alterar o próprio perfil.");
        }

        Usuario usuario = buscarEntidadePorId(idAlvo);
        usuario.alterarPerfil(novoPerfil);
        repository.save(usuario);
    }

    @Transactional
    public void ativar(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        usuario.ativar();
        repository.save(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        usuario.desativar();
        repository.save(usuario);
    }

    private String gerarLoginAutomatico(String nomeCompleto) {
        String[] partes = nomeCompleto.trim().toLowerCase().split("\\s+");
        String baseLogin = partes.length > 1
                ? partes[0] + "." + partes[partes.length - 1] // Corrigido o erro de sintaxe aqui
                : partes[0];

        baseLogin = Normalizer.normalize(baseLogin, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-z0-9.]", "");

        String loginGerado;
        do {
            int numeroSufixo = secureRandom.nextInt(900) + 100;
            loginGerado = baseLogin + numeroSufixo;
        } while (repository.existsByLogin(loginGerado));

        return loginGerado;
    }

}