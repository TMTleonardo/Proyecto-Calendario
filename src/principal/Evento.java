package principal;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class Evento {

    private static JTable eventosTable;
    private static DefaultTableModel tableModel;

    public static JPanel crearPanelEventos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Gestión de Eventos"), BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Descripción", "Fecha Inicio", "Hora Inicio", "Fecha Fin", "Hora Fin"}, 0);
        eventosTable = new JTable(tableModel);
        actualizarTablaEventos();
        panel.add(new JScrollPane(eventosTable), BorderLayout.CENTER);
        JPanel botonesPanel = new JPanel(new GridLayout(1, 3));
        JButton btnAddEvent = new JButton("Agregar Evento");
        btnAddEvent.addActionListener(e -> agregarEvento());
        JButton btnEditEvent = new JButton("Modificar Evento");
        btnEditEvent.addActionListener(e -> modificarEvento());
        JButton btnDeleteEvent = new JButton("Eliminar Evento");
        btnDeleteEvent.addActionListener(e -> eliminarEvento());
        botonesPanel.add(btnAddEvent);
        botonesPanel.add(btnEditEvent);
        botonesPanel.add(btnDeleteEvent);
        panel.add(botonesPanel, BorderLayout.SOUTH);
        return panel;
    }

    public static void actualizarTablaEventos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Evento")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("idEvento"),
                        rs.getString("nombreEvento"),
                        rs.getString("descripcion"),
                        rs.getDate("fechaInicio"),
                        rs.getTime("horaInicio"),
                        rs.getDate("fechaFin"),
                        rs.getTime("horaFin")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void agregarEvento() {
        JTextField nombreEventoField = new JTextField();
        JTextField descripcionEventoField = new JTextField();
        JDateChooser fechaInicioChooser = new JDateChooser();
        JSpinner horaInicioSpinner = crearSpinnerHora();
        JDateChooser fechaFinChooser = new JDateChooser();
        JSpinner horaFinSpinner = crearSpinnerHora();

        Object[] message = {
                "Nombre del Evento:", nombreEventoField,
                "Descripción:", descripcionEventoField,
                "Fecha de Inicio:", fechaInicioChooser,
                "Hora de Inicio:", horaInicioSpinner,
                "Fecha de Fin:", fechaFinChooser,
                "Hora de Fin:", horaFinSpinner
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Agregar Evento", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Date fechaInicio = combinarFechaHora(fechaInicioChooser.getDate(), (Date) horaInicioSpinner.getValue());
            Date fechaFin = combinarFechaHora(fechaFinChooser.getDate(), (Date) horaFinSpinner.getValue());
            String nombreEvento = nombreEventoField.getText();
            String descripcionEvento = descripcionEventoField.getText();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Evento (idEvento, nombreEvento, descripcion, fechaInicio, horaInicio, fechaFin, horaFin) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, UUID.randomUUID().toString());
                pstmt.setString(2, nombreEvento);
                pstmt.setString(3, descripcionEvento);
                pstmt.setDate(4, new java.sql.Date(fechaInicioChooser.getDate().getTime())); // Fecha de Inicio
                pstmt.setTime(5, new java.sql.Time(((Date) horaInicioSpinner.getValue()).getTime())); // Hora de Inicio
                pstmt.setDate(6, new java.sql.Date(fechaFinChooser.getDate().getTime())); // Fecha de Fin
                pstmt.setTime(7, new java.sql.Time(((Date) horaFinSpinner.getValue()).getTime())); // Hora de Fin
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Evento agregado exitosamente.");
                
                // Notificar a los usuarios
                String nombreJefe = "NombreJefe"; // Asegúrate de definir o recuperar el nombre del jefe
                Notificaciones.notificarNuevoEvento(nombreJefe, nombreEvento, descripcionEvento, new Timestamp(fechaInicio.getTime()), new Timestamp(fechaFin.getTime()));

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static JSpinner crearSpinnerHora() {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(dateModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(timeEditor);
        return spinner;
    }

    private static Date combinarFechaHora(Date fecha, Date hora) {
        if (fecha == null || hora == null) return null;
        Calendar fechaCal = Calendar.getInstance();
        fechaCal.setTime(fecha);
        Calendar horaCal = Calendar.getInstance();
        horaCal.setTime(hora);

        fechaCal.set(Calendar.HOUR_OF_DAY, horaCal.get(Calendar.HOUR_OF_DAY));
        fechaCal.set(Calendar.MINUTE, horaCal.get(Calendar.MINUTE));
        return fechaCal.getTime();
    }

    public static void modificarEvento() {
        JComboBox<String> eventoBox = new JComboBox<>();
        List<String> eventoIds = cargarEventosExistentes(eventoBox);

        int option = JOptionPane.showConfirmDialog(null, eventoBox, "Selecciona un Evento para Modificar", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String selectedId = eventoIds.get(eventoBox.getSelectedIndex());
            cargarDatosEvento(selectedId);
        }
    }

    private static void cargarDatosEvento(String eventoId) {
        JTextField nombreField = new JTextField();
        JTextField descripcionField = new JTextField();
        JDateChooser fechaInicioChooser = new JDateChooser();
        JSpinner horaInicioSpinner = crearSpinnerHora();
        JDateChooser fechaFinChooser = new JDateChooser();
        JSpinner horaFinSpinner = crearSpinnerHora();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Evento WHERE idEvento = ?")) {
            pstmt.setString(1, eventoId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nombreField.setText(rs.getString("nombreEvento"));
                descripcionField.setText(rs.getString("descripcion"));
                fechaInicioChooser.setDate(rs.getTimestamp("fechaInicio"));
                horaInicioSpinner.setValue(rs.getTimestamp("fechaInicio"));
                fechaFinChooser.setDate(rs.getTimestamp("fechaFin"));
                horaFinSpinner.setValue(rs.getTimestamp("fechaFin"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Object[] message = {
                "Nombre del Evento:", nombreField,
                "Descripción:", descripcionField,
                "Fecha de Inicio:", fechaInicioChooser,
                "Hora de Inicio:", horaInicioSpinner,
                "Fecha de Fin:", fechaFinChooser,
                "Hora de Fin:", horaFinSpinner
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Modificar Evento", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Date fechaInicio = combinarFechaHora(fechaInicioChooser.getDate(), (Date) horaInicioSpinner.getValue());
            Date fechaFin = combinarFechaHora(fechaFinChooser.getDate(), (Date) horaFinSpinner.getValue());
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE Evento SET nombreEvento = ?, descripcion = ?, fechaInicio = ?, fechaFin = ? WHERE idEvento = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nombreField.getText());
                pstmt.setString(2, descripcionField.getText());
                pstmt.setTimestamp(3, new Timestamp(fechaInicio.getTime()));
                pstmt.setTimestamp(4, new Timestamp(fechaFin.getTime()));
                pstmt.setString(5, eventoId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Evento modificado exitosamente.");
                actualizarTablaEventos();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void eliminarEvento() {
        JComboBox<String> eventoBox = new JComboBox<>();
        List<String> eventoIds = cargarEventosExistentes(eventoBox);

        int option = JOptionPane.showConfirmDialog(null, eventoBox, "Selecciona un Evento para Eliminar", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String selectedId = eventoIds.get(eventoBox.getSelectedIndex());
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM Evento WHERE idEvento = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, selectedId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Evento eliminado exitosamente.");
                actualizarTablaEventos();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> cargarEventosExistentes(JComboBox<String> eventoBox) {
        List<String> eventoIds = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT idEvento, nombreEvento FROM Evento")) {
            while (rs.next()) {
                eventoBox.addItem(rs.getString("nombreEvento"));
                eventoIds.add(rs.getString("idEvento"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eventoIds;
    }
}
