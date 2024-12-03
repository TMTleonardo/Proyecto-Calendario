package Test;

import org.junit.jupiter.api.Test;

import principal.DatabaseConnection;

import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    public void testConectarBaseDeDatos() {
        assertDoesNotThrow(() -> {
            Connection conn = DatabaseConnection.getConnection();
            assertNotNull(conn);
        });
    }

    @Test
    public void testDesconectarBaseDeDatos() {
        assertDoesNotThrow(() -> {
            DatabaseConnection.disconnect();
        });
    }

    @Test
    public void testConfigurarNuevaConexion() {
        assertDoesNotThrow(() -> {
            DatabaseConnection.resetConnection();
            Connection conn = DatabaseConnection.getConnection();
            assertNotNull(conn);
        });
    }
}

