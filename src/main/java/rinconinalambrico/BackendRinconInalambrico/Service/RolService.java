package rinconinalambrico.BackendRinconInalambrico.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rinconinalambrico.BackendRinconInalambrico.Entity.Rol;
import rinconinalambrico.BackendRinconInalambrico.Repository.RolRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RolService {
    
    @Autowired
    private RolRepository rolRepository;
    
    // Crear nuevo rol
    public Rol crearRol(Rol rol) {
        // Validar que el nombre no exista
        if (rolRepository.existsByNombre(rol.getNombre())) {
            throw new RuntimeException("El rol ya existe");
        }
        return rolRepository.save(rol);
    }
    
    // Obtener todos los roles
    public List<Rol> obtenerTodosRoles() {
        return rolRepository.findAll();
    }
    
    // Obtener rol por ID
    public Optional<Rol> obtenerRolPorId(Long id) {
        return rolRepository.findById(id);
    }
    
    // Obtener rol por nombre
    public Optional<Rol> obtenerRolPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }
    
    // Eliminar rol
    public void eliminarRol(Long id) {
        rolRepository.deleteById(id);
    }
}