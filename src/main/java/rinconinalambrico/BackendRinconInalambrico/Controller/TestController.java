package rinconinalambrico.BackendRinconInalambrico.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. Endpoint básico de prueba
    @GetMapping("/test")
    public String test() {
        return "¡Backend funcionando correctamente! 🚀";
    }

    // 2. Test de conexión a base de datos
    @GetMapping("/test-db")
    public String testDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return "✅ Conexión exitosa a la BD!<br>" +
                   "URL: " + conn.getMetaData().getURL() + "<br>" +
                   "Usuario: " + conn.getMetaData().getUserName() + "<br>" +
                   "Producto: " + conn.getMetaData().getDatabaseProductName() + "<br>" +
                   "Versión: " + conn.getMetaData().getDatabaseProductVersion();
        } catch (Exception e) {
            return "❌ Error de conexión: " + e.getMessage();
        }
    }

    // 3. Listar todas las tablas de la base de datos
    @GetMapping("/test-tables")
    public String testTables() {
        try {
            List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'DATABASE_RINCONINALAMBRICO'", 
                String.class
            );
            return "✅ Tablas en la BD (" + tables.size() + "): " + String.join(", ", tables);
        } catch (Exception e) {
            return "❌ Error al listar tablas: " + e.getMessage();
        }
    }

    // 4. Contar registros en cada tabla
    @GetMapping("/test-counts")
    public String testTableCounts() {
        try {
            List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'DATABASE_RINCONINALAMBRICO'", 
                String.class
            );
            
            StringBuilder result = new StringBuilder("📊 Conteo de registros por tabla:<br>");
            for (String table : tables) {
                try {
                    Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + table, Integer.class);
                    result.append("• ").append(table).append(": ").append(count).append(" registros<br>");
                } catch (Exception e) {
                    result.append("• ").append(table).append(": Error al contar<br>");
                }
            }
            return result.toString();
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    // 5. Información del sistema
    @GetMapping("/test-system")
    public String testSystem() {
        return "🏗️ Sistema Backend Rincon Inalambrico<br>" +
               "✅ Spring Boot 3.2.0<br>" +
               "✅ Base de datos MySQL AWS RDS<br>" +
               "✅ JPA/Hibernate configurado<br>" +
               "🚀 Listo para desarrollo!";
    }

    // 6. Probar consulta específica a una tabla (ejemplo con usuario)
    @GetMapping("/test-usuarios")
    public String testUsuarios() {
        try {
            List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(
                "SELECT * FROM usuario LIMIT 5"
            );
            return "✅ Primeros 5 usuarios: " + usuarios.toString();
        } catch (Exception e) {
            return "❌ Error al consultar usuarios: " + e.getMessage() + 
                   "<br>¿La tabla 'usuario' existe?";
        }
    }

    // 7. Health check personalizado
    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        return Map.of(
            "status", "UP",
            "service", "Backend Rincon Inalambrico",
            "database", checkDatabase() ? "CONNECTED" : "DISCONNECTED",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
    }

    private boolean checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}