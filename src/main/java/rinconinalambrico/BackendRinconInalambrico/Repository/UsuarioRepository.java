package rinconinalambrico.BackendRinconInalambrico.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rinconinalambrico.BackendRinconInalambrico.Entity.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo);      // ✅ mantener
    Optional<Usuario> findByCorreo(String correo); // ✅ mantener
    boolean existsByRut(String rut);
    Optional<Usuario> findByRut(String rut);
}