package br.com.fatec.backend.repository;

import br.com.fatec.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository responsável pelo acesso aos dados da entidade Usuario.
 *
 * O JpaRepository já fornece operações básicas como:
 * salvar, buscar, atualizar e excluir usuários.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo login.
     * Optional é utilizado porque o usuário pode não existir.
     */
    Optional<Usuario> findByLogin(String login);

    /**
     * Verifica se já existe um usuário com o login informado.
     * Retorna true se o login já estiver cadastrado.
     */
    boolean existsByLogin(String login);

    /**
     * Verifica se existe outro usuário utilizando o login informado.
     * O próprio usuário identificado pelo id é ignorado na verificação.
     * Útil para validar a alteração do login de um usuário.
     */
    boolean existsByLoginAndIdNot(String login, Long id);
}