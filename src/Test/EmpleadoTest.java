package Test;

import org.junit.jupiter.api.Test;

import principal.Empleado;

import javax.swing.*;
import static org.junit.jupiter.api.Assertions.*;

public class EmpleadoTest {

    @Test
    public void testCrearPanelEmpleados() {
        JPanel panel = Empleado.crearPanelEmpleados();
        assertNotNull(panel);
    }

    @Test
    public void testAgregarEmpleado() {
        assertDoesNotThrow(() -> {
            Empleado.agregarEmpleado("Juan", "Perez", "Lopez", 30, "juan.perez@mail.com", "Desarrollador");
        });
    }

    @Test
    public void testCargarEmpleados() {
        assertDoesNotThrow(() -> {
            Empleado.crearPanelEmpleados();
        });
    }
}
