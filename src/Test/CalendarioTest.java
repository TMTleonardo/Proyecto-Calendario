package Test;

import org.junit.jupiter.api.Test;
import principal.Calendario;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class CalendarioTest {

    @Test
    public void testMostrarEventosYTareasParaFecha() {
        Calendario calendario = new Calendario();
        String fechaPrueba = "2024-12-01";
        
        assertDoesNotThrow(() -> {
            calendario.mostrarEventosYtareasParaFecha(fechaPrueba);
        });
    }

    @Test
    public void testCrearPanelCalendario() {
        assertNotNull(Calendario.crearPanelCalendario());
    }
}
