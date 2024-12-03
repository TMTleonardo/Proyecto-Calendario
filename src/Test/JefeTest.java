package Test;
import org.junit.jupiter.api.Test;
import principal.Jefe;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.JPanel;

public class JefeTest {

    @Test
    public void testCrearPanelJefes() {
        JPanel panel = Jefe.crearPanelJefes();
        assertNotNull(panel);
    }

    @Test
    public void testAgregarJefe() {
        assertDoesNotThrow(() -> {
            Jefe.agregarJefe("Luis", "Martinez", "Hernandez", 40, "luis.martinez@mail.com", "Gerente", "password123");
        });
    }

    @Test
    public void testCargarJefes() {
        assertDoesNotThrow(() -> {
            Jefe.cargarJefes();
        });
    }
}
