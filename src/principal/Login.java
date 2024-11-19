package principal;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login extends JFrame {
    private JTextField usuarioField;
    private JPasswordField passwordField;
    private JButton btnLoginEmpleado;
    private JButton btnLoginJefe;
    private static String rolUsuario;
    private static int idUsuario = -1;
    public Login() {
        setTitle("Inicio de Sesión");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));

        usuarioField = new JTextField();
        passwordField = new JPasswordField();
        btnLoginEmpleado = new JButton("Iniciar sesión como Empleado");
        btnLoginJefe = new JButton("Iniciar sesión como Jefe");

        panel.add(new JLabel("Usuario:"));
        panel.add(usuarioField);
        panel.add(new JLabel("Contraseña (solo para Jefe):"));
        panel.add(passwordField);
        panel.add(btnLoginEmpleado);
        panel.add(btnLoginJefe);

        btnLoginEmpleado.addActionListener(e -> iniciarSesionEmpleado());
        btnLoginJefe.addActionListener(e -> iniciarSesionJefe());

        add(panel);
        setVisible(true);
    }

    private void iniciarSesionEmpleado() {
        String usuario = usuarioField.getText();

        if (validarEmpleado(usuario)) {
            JOptionPane.showMessageDialog(this, "Inicio de sesión exitoso como Empleado.");
            rolUsuario = "Empleado";
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario de Empleado incorrecto.");
        }
    }

    private void iniciarSesionJefe() {
        String usuario = usuarioField.getText();
        String password = new String(passwordField.getPassword());

        if (validarJefe(usuario, password)) {
            JOptionPane.showMessageDialog(this, "Inicio de sesión exitoso como Jefe.");
            rolUsuario = "Jefe";
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña de Jefe incorrectos.");
        }
    }

    private boolean validarEmpleado(String usuario) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT idEmpleado FROM Empleado WHERE usuario = ?")) {
            pstmt.setString(1, usuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                idUsuario = rs.getInt("idEmpleado");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean validarJefe(String usuario, String password) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(null, "No se pudo conectar a la base de datos. Verifique la configuración.");
            return false;
        }

        String sql = "SELECT j.idJefe FROM jefe j " +
                     "JOIN empleado e ON j.idJefe = e.idEmpleado " +
                     "WHERE e.usuario = ? AND j.contraseña = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getInt("idJefe");
                    return true;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al validar jefe: " + e.getMessage());
        }
        return false;
    }

    public static String mostrarPantallaLogin() {
        Login login = new Login();
        login.setVisible(true);

        while (login.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return rolUsuario;
    }

    public static int obtenerIdUsuario() {
        return idUsuario;
    }
}

