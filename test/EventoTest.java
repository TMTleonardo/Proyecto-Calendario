package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import principal.Evento;

class EventoTest {

	@Test
	void modificarEvento() {
		Evento evento = new Evento("023-10-15 10:00 a 2023-10-15 11:00");
		evento.modificarEvento("2023-10-15 12:00 a 2023-10-15 13:00");
		
		assertEquals("2023-10-15 12:00 a 2023-10-15 13:00", evento.getIntervalo());
	}


}
