package rinconinalambrico.BackendRinconInalambrico.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rinconinalambrico.BackendRinconInalambrico.Entity.Usuario;
import rinconinalambrico.BackendRinconInalambrico.Entity.Rol;
import rinconinalambrico.BackendRinconInalambrico.Repository.UsuarioRepository;
import rinconinalambrico.BackendRinconInalambrico.Repository.RolRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    // Crear nuevo usuario
    public Usuario crearUsuario(Usuario usuario) {
        // Validar que el correo no exista
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {  // ✅ CORRECTO
            throw new RuntimeException("El correo ya está registrado");
        }
        
        // Validar que el RUT no exista
        if (usuarioRepository.existsByRut(usuario.getRut())) {
            throw new RuntimeException("El RUT ya está registrado");
        }
        
        // Validar que tenga rol asignado
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new RuntimeException("El rol es obligatorio");
        }
        
        // Verificar que el rol exista en la base de datos
        Rol rolExistente = rolRepository.findById(usuario.getRol().getId())
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + usuario.getRol().getId()));
        
        // Asignar el rol completo
        usuario.setRol(rolExistente);
        
        return usuarioRepository.save(usuario);
    }
    
    // Método alternativo para crear usuario con nombre de rol
    public Usuario crearUsuarioConRolNombre(Usuario usuario, String nombreRol) {
        // Validaciones de correo y RUT
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {  // ✅ CORRECTO
            throw new RuntimeException("El correo ya está registrado");
        }
        if (usuarioRepository.existsByRut(usuario.getRut())) {
            throw new RuntimeException("El RUT ya está registrado");
        }
        
        // Buscar rol por nombre
        Rol rol = rolRepository.findByNombre(nombreRol)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nombreRol));
        
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }
    
    // Los demás métodos se mantienen igual...
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }
    
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    public Optional<Usuario> obtenerUsuarioPorCorreo(String correo) {  // ✅ CORRECTO
        return usuarioRepository.findByCorreo(correo);  // ✅ CORRECTO
    }
    
    public Optional<Usuario> obtenerUsuarioPorRut(String rut) {
        return usuarioRepository.findByRut(rut);
    }
    
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id)
            .map(usuario -> {
                usuario.setNombre(usuarioActualizado.getNombre());
                usuario.setCorreo(usuarioActualizado.getCorreo());  // ✅ CORRECTO
                usuario.setRut(usuarioActualizado.getRut());
                usuario.setContrasena(usuarioActualizado.getContrasena());  // ✅ CORRECTO
                usuario.setTelefono(usuarioActualizado.getTelefono());
                
                // Actualizar rol si viene en el actualizado
                if (usuarioActualizado.getRol() != null && usuarioActualizado.getRol().getId() != null) {
                    Rol rol = rolRepository.findById(usuarioActualizado.getRol().getId())
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                    usuario.setRol(rol);
                }
                
                return usuarioRepository.save(usuario);
            })
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
    
    public long contarUsuarios() {
        return usuarioRepository.count();
    }
}