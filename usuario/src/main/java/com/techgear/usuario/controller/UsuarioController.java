package com.techgear.usuario.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techgear.usuario.model.Usuario;
import com.techgear.usuario.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = {"http://localhost:3000", "http://18.207.254.56"}) // 🔥 IMPORTANTE para CORS
@Tag(name = "Usuarios",description = "Operaciones CRUD de usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // 🔥 NUEVO ENDPOINT: LOGIN
@PostMapping("/login")
@Operation(summary="Login de usuario", description="Autentica usuario con correo y contraseña")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Login exitoso", 
        content = @Content(schema = @Schema(implementation = Usuario.class))),
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
})
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    System.out.println("🎯 ==== LOGIN ATTEMPT ====");
    System.out.println("📧 Correo: " + loginRequest.getCorreo());
    System.out.println("🔑 Contraseña: " + loginRequest.getContrasena());
    
    try {
        System.out.println("1. Obteniendo todos los usuarios...");
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        System.out.println("2. Total usuarios obtenidos: " + usuarios.size());
        
        // Mostrar primeros 3 usuarios para debug
        for (int i = 0; i < Math.min(3, usuarios.size()); i++) {
            Usuario u = usuarios.get(i);
            System.out.println("   Usuario " + i + ": " + u.getCorreo() + " - " + u.getNombre());
        }
        
        // Buscar usuario por correo y contraseña
        Usuario usuarioEncontrado = null;
        for (Usuario usuario : usuarios) {
            if (usuario.getCorreo() != null && 
                usuario.getCorreo().equals(loginRequest.getCorreo()) && 
                usuario.getContrasena() != null && 
                usuario.getContrasena().equals(loginRequest.getContrasena())) {
                
                usuarioEncontrado = usuario;
                break;
            }
        }
        
        if (usuarioEncontrado == null) {
            System.out.println("3. ❌ Credenciales incorrectas - Usuario no encontrado");
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
        
        System.out.println("3. ✅ Usuario encontrado: " + usuarioEncontrado.getNombre());
        
        // Crear respuesta simplificada
        Map<String, Object> response = new HashMap<>();
        response.put("id", usuarioEncontrado.getId());
        response.put("nombre", usuarioEncontrado.getNombre());
        response.put("correo", usuarioEncontrado.getCorreo());
        response.put("rol", usuarioEncontrado.getRol() != null ? usuarioEncontrado.getRol().getNombre() : "usuario");
        response.put("telefono", usuarioEncontrado.getTelefono());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        System.err.println("💥 ERROR en login:");
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
    }
}

    @GetMapping()
    @Operation(summary="Obtener todos los usuarios",description = "Obtiene una lista de todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Listado de usuarios",
    content = @Content(mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = Usuario.class)))
    )
    public ResponseEntity<List<Usuario>> obtenerUsuarios(){
        try {
            List<Usuario>usuarios = usuarioService.getAllUsuarios();
            if (usuarios.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary="Obtener usuario",description = "Obtiene un usuario mediante ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado", 
            content = @Content(schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable("id")Integer id){
        try {
            Usuario usuario = usuarioService.getUsuario(id);
            if (usuario==null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping()
    @Operation(summary="Ingresar usuario",description = "Ingresa usuario a la BD utilizando un JSON")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Usuario creado", 
        content = @Content(schema = @Schema(implementation = Usuario.class))),
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<Usuario> insertarUsuario(@RequestBody Usuario usuario){
        try {
            Usuario newUsuario = usuarioService.saveUsuario(usuario);
            return ResponseEntity.ok(newUsuario);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping()
    @Operation(summary="Actualizar usuario",description = "Actualiza los datos de un usuario ya registrado")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Usuario actualizado",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Usuario.class))),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Usuario> actualizarUsuario(@RequestBody Usuario usuario){
        try {
            Usuario updUser = usuarioService.updateUsuario(usuario);
            if (updUser==null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Eliminar usuario",description = "Elimina un usuario mediante ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> eliminarUsuario(@PathVariable("id")Integer id){
        try {
            usuarioService.deleteUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

// 🔥 CLASE AUXILIAR PARA LOGIN (agrégala al FINAL del mismo archivo)
class LoginRequest {
    private String correo;
    private String contrasena;
    
    // Getters y Setters
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}