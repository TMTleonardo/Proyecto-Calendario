package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import principal.Empleado;

class EmpleadoTest {

	@Test
	void testEmpleado() {
		Empleado empleado = new Empleado("Juan", "Pérez", "1234", "juan.perez@example.com");

		assertEquals("Juan", empleado.getNombre());
		assertEquals("Pérez", empleado.getApellido());
		assertEquals("1234", empleado.getCodigo());
		assertEquals("juan.perez@example.com", empleado.getCorreoElectronico());
	}
}
