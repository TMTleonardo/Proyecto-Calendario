package principal;

import javax.swing.*;

import java.awt.GridLayout;
import java.sql.*;

public class Jefe {

    public static boolean hayJefeRegistrado() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Jefe")) {
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void crearPrimerJefe() {
        JTextField nombreField = new JTextField();
        JTextField apellidoPaternoField = new JTextField();
        JTextField apellidoMaternoField = new JTextField();
        JTextField edadField = new JTextField();
        JTextField correoField = new JTextField();
        JTextField ocupacionField = new JTextField();
        JTextField usuarioField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

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
        panelRegistro.add(new JLabel("Usuario:"));
        panelRegistro.add(usuarioField);
        panelRegistro.add(new JLabel("Contraseña:"));
        panelRegistro.add(passwordField);

        int option = JOptionPane.showConfirmDialog(null, panelRegistro, "Registrar Primer Jefe", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nombre = nombreField.getText().trim();
            String apellidoPaterno = apellidoPaternoField.getText().trim();
            String apellidoMaterno = apellidoMaternoField.getText().trim();
            String edadStr = edadField.getText().trim();
            String correo = correoField.getText().trim();
            String ocupacion = ocupacionField.getText().trim();
            String usuario = usuarioField.getText().trim();
            String contraseña = new String(passwordField.getPassword());

            if (nombre.isEmpty() || apellidoPaterno.isEmpty() || apellidoMaterno.isEmpty() || edadStr.isEmpty() ||
                correo.isEmpty() || ocupacion.isEmpty() || usuario.isEmpty() || contraseña.isEmpty()) {
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

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sqlEmpleado = "INSERT INTO Empleado (nombre, apellidoPaterno, apellidoMaterno, edad, correoElectronico, ocupacion, usuario) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmtEmpleado = conn.prepareStatement(sqlEmpleado, Statement.RETURN_GENERATED_KEYS);
                pstmtEmpleado.setString(1, nombre);
                pstmtEmpleado.setString(2, apellidoPaterno);
                pstmtEmpleado.setString(3, apellidoMaterno);
                pstmtEmpleado.setInt(4, edad);
                pstmtEmpleado.setString(5, correo);
                pstmtEmpleado.setString(6, ocupacion);
                pstmtEmpleado.setString(7, usuario);
                pstmtEmpleado.executeUpdate();

                ResultSet rs = pstmtEmpleado.getGeneratedKeys();
                if (rs.next()) {
                    int idEmpleado = rs.getInt(1);

                    String sqlJefe = "INSERT INTO Jefe (idEmpleado, contraseña) VALUES (?, ?)";
                    PreparedStatement pstmtJefe = conn.prepareStatement(sqlJefe);
                    pstmtJefe.setInt(1, idEmpleado);
                    pstmtJefe.setString(2, contraseña);
                    pstmtJefe.executeUpdate();

                    String mensajeEspecial = String.format("¡Bienvenido %s! Usted es el primer jefe registrado en el sistema. Tiene acceso a todas las funcionalidades, incluyendo la gestión de empleados, tareas, eventos y notificaciones. ¡Gracias por confiar en ProyectoAgenda!", nombre);
                    Notificaciones.notificarBienvenida(idEmpleado, mensajeEspecial, "Jefe");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void registrarJefe(int idJefe, String nombreJefe, String nombre, String apellidoPaterno, String apellidoMaterno, int edad, String correo, String ocupacion, String usuario, String contraseña) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlEmpleado = "INSERT INTO Empleado (nombre, apellidoPaterno, apellidoMaterno, edad, correoElectronico, ocupacion, usuario) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmtEmpleado = conn.prepareStatement(sqlEmpleado, Statement.RETURN_GENERATED_KEYS);
            pstmtEmpleado.setString(1, nombre);
            pstmtEmpleado.setString(2, apellidoPaterno);
            pstmtEmpleado.setString(3, apellidoMaterno);
            pstmtEmpleado.setInt(4, edad);
            pstmtEmpleado.setString(5, correo);
            pstmtEmpleado.setString(6, ocupacion);
            pstmtEmpleado.setString(7, usuario);
            pstmtEmpleado.executeUpdate();

            ResultSet rs = pstmtEmpleado.getGeneratedKeys();
            if (rs.next()) {
                int idEmpleado = rs.getInt(1);

                String sqlJefe = "INSERT INTO Jefe (idEmpleado, contraseña) VALUES (?, ?)";
                PreparedStatement pstmtJefe = conn.prepareStatement(sqlJefe);
                pstmtJefe.setInt(1, idEmpleado);
                pstmtJefe.setString(2, contraseña);
                pstmtJefe.executeUpdate();
                String datos = String.format("Nombre: %s, Apellidos: %s %s, Edad: %d, Correo: %s, Ocupación: %s, Contraseña: %s", nombre, apellidoPaterno, apellidoMaterno, edad, correo, ocupacion, contraseña);
                Notificaciones.notificarNuevoUsuario(idJefe, nombreJefe, "Jefe", usuario, datos);
                Notificaciones.notificarBienvenida(idEmpleado, nombre, "Jefe");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String obtenerNombreJefe(int idJefe) {
        String nombreJefe = null;
        String sql = "SELECT E.nombre, E.apellidoPaterno, E.apellidoMaterno " +
                     "FROM Empleado E " +
                     "JOIN Jefe J ON E.idEmpleado = J.idEmpleado " +
                     "WHERE J.idJefe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idJefe);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String nombre = rs.getString("nombre");
                String apellidoPaterno = rs.getString("apellidoPaterno");
                String apellidoMaterno = rs.getString("apellidoMaterno");
                nombreJefe = nombre + " " + apellidoPaterno + " " + apellidoMaterno;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombreJefe;
    }
}


