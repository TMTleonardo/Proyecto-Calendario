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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Tarea {

    private static JTable tareasTable;
    private static DefaultTableModel tableModel;
    private static Map<String, Integer> empleadosMap = new HashMap<>(); // Mapa para almacenar nombre y ID de empleados

    public static JPanel crearPanelTareas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Gestión de Tareas"), BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Descripción", "Fecha Inicio", "Hora Inicio", "Fecha Fin", "Hora Fin", "Encargado"}, 0);
        tareasTable = new JTable(tableModel);
        actualizarTablaTareas();
        panel.add(new JScrollPane(tareasTable), BorderLayout.CENTER);

        JPanel botonesPanel = new JPanel(new GridLayout(1, 3));
        JButton btnAddTask = new JButton("Agregar Tarea");
        btnAddTask.addActionListener(e -> agregarTarea());
        JButton btnEditTask = new JButton("Modificar Tarea");
        btnEditTask.addActionListener(e -> modificarTarea());
        JButton btnDeleteTask = new JButton("Eliminar Tarea");
        btnDeleteTask.addActionListener(e -> eliminarTarea());

        botonesPanel.add(btnAddTask);
        botonesPanel.add(btnEditTask);
        botonesPanel.add(btnDeleteTask);
        panel.add(botonesPanel, BorderLayout.SOUTH);

        return panel;
    }

    public static void actualizarTablaTareas() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT T.idTarea, T.nombreTarea, T.descripcion, T.fechaInicio, T.fechaFin, E.nombre AS encargado " +
                "FROM Tarea T LEFT JOIN Empleado E ON T.encargado = E.idEmpleado")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("idTarea"),
                        rs.getString("nombreTarea"),
                        rs.getString("descripcion"),
                        rs.getTimestamp("fechaInicio"),
                        rs.getTimestamp("fechaFin"),
                        rs.getString("encargado")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void agregarTarea() {
        JTextField nombreField = new JTextField();
        JTextField descripcionField = new JTextField();
        JDateChooser fechaInicioChooser = new JDateChooser();
        JSpinner horaInicioSpinner = crearSpinnerHora();
        JDateChooser fechaFinChooser = new JDateChooser();
        JSpinner horaFinSpinner = crearSpinnerHora();

        JComboBox<String> encargadoBox = new JComboBox<>();
        cargarEncargados(encargadoBox);

        Object[] message = {
                "Nombre de Tarea:", nombreField,
                "Descripción:", descripcionField,
                "Fecha de Inicio:", fechaInicioChooser,
                "Hora de Inicio:", horaInicioSpinner,
                "Fecha de Fin:", fechaFinChooser,
                "Hora de Fin:", horaFinSpinner,
                "Encargado:", encargadoBox
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Agregar Tarea", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Date fechaInicio = combinarFechaHora(fechaInicioChooser.getDate(), (Date) horaInicioSpinner.getValue());
            Date fechaFin = combinarFechaHora(fechaFinChooser.getDate(), (Date) horaFinSpinner.getValue());
            if (fechaInicio != null && fechaFin != null) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO Tarea (idTarea, nombreTarea, descripcion, fechaInicio, fechaFin, encargado) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    String idTarea = UUID.randomUUID().toString();
                    pstmt.setString(1, idTarea);
                    pstmt.setString(2, nombreField.getText());
                    pstmt.setString(3, descripcionField.getText());
                    pstmt.setTimestamp(4, new Timestamp(fechaInicio.getTime()));
                    pstmt.setTimestamp(5, new Timestamp(fechaFin.getTime()));

                    // Obtener ID del empleado seleccionado
                    String encargadoSeleccionado = (String) encargadoBox.getSelectedItem();
                    Integer idEncargado = empleadosMap.get(encargadoSeleccionado);
                    pstmt.setInt(6, idEncargado);

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Tarea agregada exitosamente.");
                    actualizarTablaTareas();

                    // Enviar notificación
                    String nombreJefe = "idJefe"; // Suponiendo que tienes el nombre del jefe
                    String nombreTarea = nombreField.getText();
                    String descripcionTarea = descripcionField.getText();

                    Notificaciones.notificarTareaAsignada(nombreJefe, nombreTarea, descripcionTarea, new Timestamp(fechaInicio.getTime()), new Timestamp(fechaFin.getTime()), idEncargado);
                    Notificaciones.notificarTareaParaJefes(nombreJefe, nombreTarea, descripcionTarea, new Timestamp(fechaInicio.getTime()), new Timestamp(fechaFin.getTime()), encargadoSeleccionado);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

    private static void cargarEncargados(JComboBox<String> encargadoBox) {
        empleadosMap.clear(); // Limpiar mapa de empleados antes de recargar
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT idEmpleado, nombre FROM Empleado")) {
            while (rs.next()) {
                String nombreEmpleado = rs.getString("nombre");
                int idEmpleado = rs.getInt("idEmpleado");
                encargadoBox.addItem(nombreEmpleado);
                empleadosMap.put(nombreEmpleado, idEmpleado);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void modificarTarea() {
        JComboBox<String> tareaBox = new JComboBox<>();
        List<String> tareaIds = cargarTareasExistentes(tareaBox);

        int option = JOptionPane.showConfirmDialog(null, tareaBox, "Selecciona una Tarea para Modificar", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String selectedId = tareaIds.get(tareaBox.getSelectedIndex());
            cargarDatosTarea(selectedId);
        }
    }

    private static void cargarDatosTarea(String tareaId) {
        JTextField nombreField = new JTextField();
        JTextField descripcionField = new JTextField();
        JComboBox<String> encargadoBox = new JComboBox<>();
        JDateChooser fechaInicioChooser = new JDateChooser();
        JSpinner horaInicioSpinner = crearSpinnerHora();
        JDateChooser fechaFinChooser = new JDateChooser();
        JSpinner horaFinSpinner = crearSpinnerHora();

        cargarEncargados(encargadoBox);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Tarea WHERE idTarea = ?")) {
            pstmt.setString(1, tareaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nombreField.setText(rs.getString("nombreTarea"));
                descripcionField.setText(rs.getString("descripcion"));
                fechaInicioChooser.setDate(rs.getTimestamp("fechaInicio"));
                horaInicioSpinner.setValue(rs.getTimestamp("fechaInicio"));
                fechaFinChooser.setDate(rs.getTimestamp("fechaFin"));
                horaFinSpinner.setValue(rs.getTimestamp("fechaFin"));
                encargadoBox.setSelectedIndex(rs.getInt("encargado") - 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Object[] message = {
                "Nombre de Tarea:", nombreField,
                "Descripción:", descripcionField,
                "Encargado:", encargadoBox,
                "Fecha de Inicio:", fechaInicioChooser,
                "Hora de Inicio:", horaInicioSpinner,
                "Fecha de Fin:", fechaFinChooser,
                "Hora de Fin:", horaFinSpinner
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Modificar Tarea", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Date fechaInicio = combinarFechaHora(fechaInicioChooser.getDate(), (Date) horaInicioSpinner.getValue());
            Date fechaFin = combinarFechaHora(fechaFinChooser.getDate(), (Date) horaFinSpinner.getValue());
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE Tarea SET nombreTarea = ?, descripcion = ?, fechaInicio = ?, fechaFin = ?, encargado = ? WHERE idTarea = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nombreField.getText());
                pstmt.setString(2, descripcionField.getText());
                pstmt.setTimestamp(3, new Timestamp(fechaInicio.getTime()));
                pstmt.setTimestamp(4, new Timestamp(fechaFin.getTime()));
                pstmt.setInt(5, encargadoBox.getSelectedIndex() + 1);
                pstmt.setString(6, tareaId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Tarea modificada exitosamente.");
                actualizarTablaTareas();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void eliminarTarea() {
        JComboBox<String> tareaBox = new JComboBox<>();
        List<String> tareaIds = cargarTareasExistentes(tareaBox);

        int option = JOptionPane.showConfirmDialog(null, tareaBox, "Selecciona una Tarea para Eliminar", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String selectedId = tareaIds.get(tareaBox.getSelectedIndex());
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM Tarea WHERE idTarea = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, selectedId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Tarea eliminada exitosamente.");
                actualizarTablaTareas();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> cargarTareasExistentes(JComboBox<String> tareaBox) {
        List<String> tareaIds = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT idTarea, nombreTarea FROM Tarea")) {
            while (rs.next()) {
                tareaBox.addItem(rs.getString("nombreTarea"));
                tareaIds.add(rs.getString("idTarea"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tareaIds;
    }
}