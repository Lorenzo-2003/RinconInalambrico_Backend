package rinconinalambrico.BackendRinconInalambrico.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import rinconinalambrico.BackendRinconInalambrico.Entity.Rol;
import rinconinalambrico.BackendRinconInalambrico.Repository.RolRepository;

@Component  // ← Esta anotación hace que Spring la detecte automáticamente
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    // Este método se ejecuta SOLO cuando la aplicación inicia
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Iniciando carga de datos iniciales...");
        
        // Crear roles básicos si no existen
        crearRolSiNoExiste("cliente");
        crearRolSiNoExiste("admin");
        crearRolSiNoExiste("vendedor");
        
        System.out.println("✅ Carga de datos iniciales completada!");
    }

    private void crearRolSiNoExiste(String nombreRol) {
        if (!rolRepository.existsByNombre(nombreRol)) {
            Rol rol = new Rol(nombreRol);
            rolRepository.save(rol);
            System.out.println("✅ Rol creado: " + nombreRol);
        } else {
            System.out.println("ℹ️ Rol ya existía: " + nombreRol);
        }
    }
}