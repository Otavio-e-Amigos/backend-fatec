package br.com.fatec.backend.config;

import br.com.fatec.backend.entity.Perfil;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Se a tabela de usuários estiver vazia, cadastra os usuários padrão para testes
        if (usuarioRepository.count() == 0) {

            // 1. Criando Usuário TI (Administrador)
            Usuario admin = new Usuario(
                    "Administrador TI",
                    "admin.ti",
                    passwordEncoder.encode("admin123"),
                    Perfil.TI
            );
            admin.ativar();
            usuarioRepository.save(admin);

            // 2. Criando Usuário RESPONSAVEL (Operacional)
            Usuario responsavel = new Usuario(
                    "Coordenador Responsável",
                    "user.resp",
                    passwordEncoder.encode("resp123"),
                    Perfil.RESPONSAVEL
            );
            responsavel.ativar();
            usuarioRepository.save(responsavel);

            System.out.println("\n==================================================");
            System.out.println(">>> USUÁRIOS DE TESTE CRIADOS COM SUCESSO! <<<");
            System.out.println("1. PERFIL TI:");
            System.out.println("   Login: admin.ti");
            System.out.println("   Senha: admin123");
            System.out.println("2. PERFIL RESPONSAVEL:");
            System.out.println("   Login: user.resp");
            System.out.println("   Senha: resp123");
            System.out.println("==================================================\n");
        }
    }
}