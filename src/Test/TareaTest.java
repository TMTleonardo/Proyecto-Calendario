package Test;

import org.junit.jupiter.api.Test;
import principal.Tarea;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.JPanel;

public class TareaTest {

    @Test
    public void testCrearPanelTareas() {
        JPanel panel = Tarea.crearPanelTareas();
        assertNotNull(panel);
    }

    @Test
    public void testAgregarTarea() {
        assertDoesNotThrow(() -> {
            Tarea.agregarTarea();
        });
    }

    @Test
    public void testCargarTareas() {
        assertDoesNotThrow(() -> {
            Tarea.actualizarTablaTareas();
        });
    }
}
