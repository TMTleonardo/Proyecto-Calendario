package principal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JCalendar;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;

public class Calendario {

    private static JCalendar calendar;
    private static JTable displayTable;
    private static DefaultTableModel tableModel;

    public static JPanel crearPanelCalendario() {
        JPanel panel = new JPanel(new BorderLayout());
        calendar = new JCalendar();
        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Descripción", "Encargado", "Estado"}, 0);
        displayTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(displayTable);
        JLabel timeLabel = new JLabel();
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        actualizarHora(timeLabel);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                actualizarHora(timeLabel);
            }
        }, 0, 1000);
        calendar.addPropertyChangeListener("calendar", evt -> mostrarEventosYtareasParaFecha(calendar.getDate()));
        panel.add(timeLabel, BorderLayout.NORTH);
        panel.add(calendar, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        mostrarEventosYtareasParaFecha(new Date());
        return panel;
    }

    private static void actualizarHora(JLabel timeLabel) {
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        timeLabel.setText("Hora actual: " + currentTime);
    }

    private static void mostrarEventosYtareasParaFecha(Date fechaSeleccionada) {
        String fechaStr = new SimpleDateFormat("yyyy-MM-dd").format(fechaSeleccionada);
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlEventos = "SELECT idEvento, nombreEvento, descripcion, fechaInicio, fechaFin FROM Evento WHERE DATE(fechaInicio) <= ? AND DATE(fechaFin) >= ?";
            try (PreparedStatement pstmtEventos = conn.prepareStatement(sqlEventos)) {
                pstmtEventos.setString(1, fechaStr);
                pstmtEventos.setString(2, fechaStr);
                ResultSet rsEventos = pstmtEventos.executeQuery();

                while (rsEventos.next()) {
                    tableModel.addRow(new Object[]{
                        rsEventos.getString("idEvento"),
                        rsEventos.getString("nombreEvento"),
                        rsEventos.getString("descripcion"),
                        "Evento",
                        obtenerEstado(new Date(), rsEventos.getTimestamp("fechaInicio"), rsEventos.getTimestamp("fechaFin"))
                    });
                }
            }

            String sqlTareas = "SELECT idTarea, nombreTarea, descripcion, fechaInicio, fechaFin, E.nombre AS encargado FROM Tarea T LEFT JOIN Empleado E ON T.encargado = E.idEmpleado WHERE DATE(fechaInicio) <= ? AND DATE(fechaFin) >= ?";
            try (PreparedStatement pstmtTareas = conn.prepareStatement(sqlTareas)) {
                pstmtTareas.setString(1, fechaStr);
                pstmtTareas.setString(2, fechaStr);
                ResultSet rsTareas = pstmtTareas.executeQuery();

                while (rsTareas.next()) {
                    tableModel.addRow(new Object[]{
                        rsTareas.getString("idTarea"),
                        rsTareas.getString("nombreTarea"),
                        rsTareas.getString("descripcion"),
                        rsTareas.getString("encargado"),
                        obtenerEstado(new Date(), rsTareas.getTimestamp("fechaInicio"), rsTareas.getTimestamp("fechaFin"))
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String obtenerEstado(Date fechaActual, Timestamp fechaInicio, Timestamp fechaFin) {
        if (fechaActual.before(fechaInicio)) {
            return "Sin ocurrir";
        } else if (fechaActual.after(fechaFin)) {
            return "Pasado";
        } else {
            return "En actividad";
        }
    }

	public void mostrarEventosYtareasParaFecha(String fechaPrueba) {		
	}
}