package com.techgear.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techgear.usuario.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
    
    // ✅ MÉTODO 1: Buscar por correo y contraseña (para login)
    Usuario findByCorreoAndContrasena(String correo, String contrasena);
    
    // ✅ MÉTODO 2: Buscar solo por correo (para verificar existencia)
    Usuario findByCorreo(String correo);
    
    // ✅ MÉTODO 3: Alternativa con @Query
    @Query("SELECT u FROM Usuario u WHERE u.correo = :correo AND u.contrasena = :contrasena")
    Usuario buscarPorLogin(@Param("correo") String correo, 
                           @Param("contrasena") String contrasena);
    
    // ✅ MÉTODO 4: Con Optional (mejor práctica)
    Optional<Usuario> findOneByCorreoAndContrasena(String correo, String contrasena);
}