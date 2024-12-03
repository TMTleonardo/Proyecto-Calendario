package Test;

import org.junit.jupiter.api.Test;
import principal.Login;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    @Test
    public void testIniciarSesionEmpleado() {
        assertDoesNotThrow(() -> {
            boolean result = Login.validarEmpleado("usuarioEmpleado");
            assertTrue(result);
        });
    }

    @Test
    public void testIniciarSesionJefe() {
        assertDoesNotThrow(() -> {
            boolean result = Login.validarJefe("usuarioJefe", "passwordJefe");
            assertTrue(result);
        });
    }
}

