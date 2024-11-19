package principal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TareaTest {

    @Test
    void testGetNombreTarea() {
        Tarea tarea = new Tarea("Estudiar Java", "Revisar las clases de Java", 1, "2024-10-20");
        assertEquals("Estudiar Java", tarea.getNombreTarea(), "El nombre de la tarea no coincide.");
    }

    @Test
    void testGetDescripcion() {
        Tarea tarea = new Tarea("Estudiar Java", "Revisar las clases de Java", 1, "2024-10-20");
        assertEquals("Revisar las clases de Java", tarea.getDescripcion(), "La descripción de la tarea no coincide.");
    }

    @Test
    void testGetPrioridad() {
        Tarea tarea = new Tarea("Estudiar Java", "Revisar las clases de Java", 1, "2024-10-20");
        assertEquals(1, tarea.getPrioridad(), "La prioridad de la tarea no coincide.");
    }

    @Test
    void testGetFechaLimite() {
        Tarea tarea = new Tarea("Estudiar Java", "Revisar las clases de Java", 1, "2024-10-20");
        assertEquals("2024-10-20", tarea.getFechaLimite(), "La fecha límite de la tarea no coincide.");
    }

    @Test
    void testCompletarTarea() {
        Tarea tarea = new Tarea("Estudiar Java", "Revisar las clases de Java", 1, "2024-10-20");
        tarea.completarTarea();
    }

    @Test
    void testModificarTarea() {
        Tarea tarea = new Tarea("Estudiar Java", "Revisar las clases de Java", 1, "2024-10-20");
        tarea.modificarTarea("Estudiar la aplicacion de Java y hacer ejemplos");
        assertEquals("Estudiar la aplicacion de Java y hacer ejemplos", tarea.getDescripcion(), "La descripción de la tarea no se modificó correctamente.");
    }
}