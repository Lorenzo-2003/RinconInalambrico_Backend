package rinconinalambrico.BackendRinconInalambrico.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rinconinalambrico.BackendRinconInalambrico.Entity.Rol;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    
    // Buscar rol por nombre
    Optional<Rol> findByNombre(String nombre);
    
    // Verificar si existe un rol con ese nombre
    boolean existsByNombre(String nombre);
}