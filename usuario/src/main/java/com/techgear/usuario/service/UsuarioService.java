package com.techgear.usuario.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techgear.usuario.model.Usuario;
import com.techgear.usuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> getAllUsuarios(){
        return usuarioRepository.findAll();
    }

    public Usuario getUsuario(int id){
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario saveUsuario(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(Usuario user){
        Usuario updUser = getUsuario(user.getId());
        if (updUser==null) {
            return null;
        }
        updUser.setNombre(user.getNombre());
        updUser.setContrasena(user.getContrasena());
        updUser.setCorreo(user.getCorreo());
        updUser.setTelefono(user.getTelefono());
        updUser.setRol(user.getRol());
        
        return usuarioRepository.save(updUser);
    }

    public void deleteUsuario(Integer id){
        usuarioRepository.deleteById(id);
    }   
}
