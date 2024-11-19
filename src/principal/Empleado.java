package principal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Empleado {

    private static JTable empleadosTable;
    private static DefaultTableModel tableModel;

    public static JPanel crearPanelEmpleados() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Gestión de Empleados"), BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Rol"}, 0);
        empleadosTable = new JTable(tableModel);
        actualizarTablaEmpleados();
        panel.add(new JScrollPane(empleadosTable), BorderLayout.CENTER);
        JPanel botonesPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JButton btnAddEmployee = new JButton("Agregar Empleado");
        btnAddEmployee.addActionListener(e -> agregarEmpleado(null));
        JButton btnDeleteEmployee = new JButton("Eliminar Empleado");
        btnDeleteEmployee.addActionListener(e -> eliminarEmpleado());
        JButton btnAddJefe = new JButton("Agregar Jefe");
        btnAddJefe.addActionListener(e -> agregarJefe());
        JButton btnDeleteJefe = new JButton("Eliminar Jefe");
        btnDeleteJefe.addActionListener(e -> eliminarJefe());
        botonesPanel.add(btnAddEmployee);
        botonesPanel.add(btnDeleteEmployee);
        botonesPanel.add(btnAddJefe);
        botonesPanel.add(btnDeleteJefe);
        panel.add(botonesPanel, BorderLayout.SOUTH);
        return panel;
    }

    private static void actualizarTablaEmpleados() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT E.idEmpleado, E.nombre, IF(J.idJefe IS NOT NULL, 'Jefe', 'Empleado') AS rol " +
                 "FROM Empleado E LEFT JOIN Jefe J ON E.idEmpleado = J.idEmpleado")) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("idEmpleado"),
                        rs.getString("nombre"),
                        rs.getString("rol")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void agregarEmpleado(DefaultTableModel tableModel) {
        JTextField nombreField = new JTextField();
        JTextField apellidoPaternoField = new JTextField();
        JTextField apellidoMaternoField = new JTextField();
        JTextField edadField = new JTextField();
        JTextField correoField = new JTextField();
        JTextField ocupacionField = new JTextField();

        JPanel panelRegistro = new JPanel(new GridLayout(0, 2));
        panelRegistro.add(new JLabel("Nombre:"));
        panelRegistro.add(nombreField);
        panelRegistro.add(new JLabel("Apellido Paterno:"));
        panelRegistro.add(apellidoPaternoField);
        panelRegistro.add(new JLabel("Apellido Materno:"));
        panelRegistro.add(apellidoMaternoField);
        panelRegistro.add(new JLabel("Edad:"));
        panelRegistro.add(edadField);
        panelRegistro.add(new JLabel("Correo Electrónico:"));
        panelRegistro.add(correoField);
        panelRegistro.add(new JLabel("Ocupación:"));
        panelRegistro.add(ocupacionField);

        int option = JOptionPane.showConfirmDialog(null, panelRegistro, "Registrar Empleado", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nombre = nombreField.getText().trim();
            String apellidoPaterno = apellidoPaternoField.getText().trim();
            String apellidoMaterno = apellidoMaternoField.getText().trim();
            String edadStr = edadField.getText().trim();
            String correo = correoField.getText().trim();
            String ocupacion = ocupacionField.getText().trim();
            if (nombre.isEmpty() || apellidoPaterno.isEmpty() || apellidoMaterno.isEmpty() || edadStr.isEmpty() || correo.isEmpty() || ocupacion.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int edad;
            try {
                edad = Integer.parseInt(edadStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "La edad debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String usuario = generarUsuario(nombre, apellidoPaterno, apellidoMaterno, edad);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Empleado (nombre, apellidoPaterno, apellidoMaterno, edad, correoElectronico, ocupacion, usuario) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, nombre);
                pstmt.setString(2, apellidoPaterno);
                pstmt.setString(3, apellidoMaterno);
                pstmt.setInt(4, edad);
                pstmt.setString(5, correo);
                pstmt.setString(6, ocupacion);
                pstmt.setString(7, usuario);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int idEmpleado = rs.getInt(1);

                    JOptionPane.showMessageDialog(null, "Empleado registrado exitosamente. Usuario: " + usuario);
                    String datos = String.format("Nombre: %s, Apellidos: %s %s, Edad: %d, Correo: %s, Ocupación: %s", nombre, apellidoPaterno, apellidoMaterno, edad, correo, ocupacion);
                    Notificaciones.notificarNuevoUsuario(null, nombre, "Empleado", usuario, datos);
                    Notificaciones.notificarBienvenida(idEmpleado, "Bienvenido al sistema, " + nombre, "Empleado");
                }

                if (tableModel != null) {
                    cargarEmpleados(tableModel);
                } else {
                    System.out.println("Error: tableModel es null.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String generarUsuario(String nombre, String apellidoPaterno, String apellidoMaterno, int edad) {
        String usuario = "";
        if (nombre.length() >= 2) usuario += nombre.substring(0, 2).toUpperCase();
        if (apellidoPaterno.length() >= 2) usuario += apellidoPaterno.substring(0, 2).toUpperCase();
        if (apellidoMaterno.length() >= 2) usuario += apellidoMaterno.substring(0, 2).toUpperCase();
        usuario += edad;
        return usuario;
    }

    private static void cargarEmpleados(DefaultTableModel tableModel) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT idEmpleado, nombre, apellidoPaterno, apellidoMaterno, ocupacion, correoElectronico FROM Empleado")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("idEmpleado"),
                        rs.getString("nombre"),
                        rs.getString("apellidoPaterno") + " " + rs.getString("apellidoMaterno"),
                        rs.getString("ocupacion"),
                        rs.getString("correoElectronico"),
                        "Empleado"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void eliminarEmpleado() {
        JTextField idField = new JTextField();
        Object[] message = {"ID del Empleado:", idField};

        int option = JOptionPane.showConfirmDialog(null, message, "Eliminar Empleado", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM Empleado WHERE idEmpleado = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(idField.getText()));
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Empleado eliminado exitosamente.");
                actualizarTablaEmpleados();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static void agregarJefe() {
        JTextField nombre = new JTextField();
        JTextField apellidoPaterno = new JTextField();
        JTextField apellidoMaterno = new JTextField();
        JTextField edad = new JTextField();
        JTextField correo = new JTextField();
        JTextField ocupacion = new JTextField();
        JTextField usuario = new JTextField();
        JPasswordField password = new JPasswordField();

        Object[] fields = {
            "Nombre:", nombre,
            "Apellido Paterno:", apellidoPaterno,
            "Apellido Materno:", apellidoMaterno,
            "Edad:", edad,
            "Correo:", correo,
            "Ocupación:", ocupacion,
            "Usuario:", usuario,
            "Contraseña:", password
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "Agregar Jefe", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sqlEmpleado = "INSERT INTO Empleado (nombre, apellidoPaterno, apellidoMaterno, edad, correoElectronico, ocupacion, usuario) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmtEmpleado = conn.prepareStatement(sqlEmpleado, Statement.RETURN_GENERATED_KEYS);
                pstmtEmpleado.setString(1, nombre.getText());
                pstmtEmpleado.setString(2, apellidoPaterno.getText());
                pstmtEmpleado.setString(3, apellidoMaterno.getText());
                pstmtEmpleado.setInt(4, Integer.parseInt(edad.getText()));
                pstmtEmpleado.setString(5, correo.getText());
                pstmtEmpleado.setString(6, ocupacion.getText());
                pstmtEmpleado.setString(7, usuario.getText());
                pstmtEmpleado.executeUpdate();
                ResultSet rs = pstmtEmpleado.getGeneratedKeys();
                if (rs.next()) {
                    int idEmpleado = rs.getInt(1);
                    String sqlJefe = "INSERT INTO Jefe (idEmpleado, contraseña) VALUES (?, ?)";
                    PreparedStatement pstmtJefe = conn.prepareStatement(sqlJefe);
                    pstmtJefe.setInt(1, idEmpleado);
                    pstmtJefe.setString(2, new String(password.getPassword()));
                    pstmtJefe.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Jefe agregado exitosamente.");
                    actualizarTablaEmpleados();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void eliminarJefe() {
        JTextField idEmpleadoField = new JTextField();
        Object[] message = {"ID del Jefe:", idEmpleadoField};
        int option = JOptionPane.showConfirmDialog(null, message, "Eliminar Jefe", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM Jefe WHERE idEmpleado = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(idEmpleadoField.getText()));
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Jefe eliminado exitosamente.");
                actualizarTablaEmpleados();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}