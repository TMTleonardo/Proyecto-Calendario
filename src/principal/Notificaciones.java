package principal;

import java.sql.*;
import java.time.Instant;

public class Notificaciones {

    private static Interfaz interfaz;

    public Notificaciones(Interfaz interfaz) {
        Notificaciones.interfaz = interfaz;
    }

    public static void notificarNuevoUsuario(Integer idJefe, String nombreJefe, String tipoUsuario, String usuario, String datosUsuario) {
        String mensaje = String.format("El jefe \"%s\" ha creado un nuevo \"%s\", con el usuario \"%s\" y los datos: %s",
                nombreJefe, tipoUsuario, usuario, datosUsuario);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (idJefe != null && !existeJefe(idJefe)) {
                System.out.println("Error: idJefe no existe en la tabla Jefe.");
                return;
            }

            String sql = "INSERT INTO Notificacion (tipo, idJefe, mensaje) VALUES ('usuario', ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (idJefe != null) {
                pstmt.setInt(1, idJefe);
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            
            pstmt.setString(2, mensaje);
            pstmt.executeUpdate();
            interfaz.agregarNotificacion(mensaje);
            notificarEmpleados(mensaje);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void notificarEmpleados(String mensaje) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, nombre FROM Empleado";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idEmpleado = rs.getInt("id");
                String nombreEmpleado = rs.getString("nombre");
                String sql = "INSERT INTO Notificacion (tipo, idEmpleado, mensaje) VALUES ('empleado', ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, idEmpleado);
                pstmt.setString(2, mensaje);
                pstmt.executeUpdate();
                interfaz.agregarNotificacion("Notificación para " + nombreEmpleado + ": " + mensaje);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void notificarBienvenida(int idUsuario, String mensaje, String tipoUsuario) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Notificacion (tipo, idEmpleado, mensaje) VALUES ('bienvenida', ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idUsuario);
            pstmt.setString(2, mensaje);
            pstmt.executeUpdate();

            interfaz.agregarNotificacion("Bienvenida: " + mensaje);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void notificarNuevoEvento(String nombreJefe, String nombreEvento, String descripcionEvento, Timestamp fechaInicio, Timestamp fechaFin) {
        String mensaje = String.format("El jefe %s asignó un evento '%s' que trata de '%s' de %s a %s",
                nombreJefe, nombreEvento, descripcionEvento, fechaInicio, fechaFin);
        enviarNotificacionATodos("evento", mensaje);

        interfaz.agregarNotificacion("Evento asignado: " + mensaje);
    }

    public static void notificarTareaAsignada(String nombreJefe, String nombreTarea, String descripcionTarea, Timestamp fechaInicio, Timestamp fechaFin, int idEmpleado) {
        String mensaje = String.format("El jefe %s le asignó una tarea '%s' que trata de '%s' de %s a %s",
                nombreJefe, nombreTarea, descripcionTarea, fechaInicio, fechaFin);
        enviarNotificacionAEmpleado("tarea", mensaje, idEmpleado);

        interfaz.agregarNotificacion("Tarea asignada: " + mensaje);
    }

    public static void notificarTareaParaJefes(String nombreJefe, String nombreTarea, String descripcionTarea, Timestamp fechaInicio, Timestamp fechaFin, String nombreEmpleado) {
        String mensaje = String.format("El jefe %s asignó una tarea '%s' a %s que trata de '%s' de %s a %s",
                nombreJefe, nombreTarea, nombreEmpleado, descripcionTarea, fechaInicio, fechaFin);
        enviarNotificacionATodos("tarea", mensaje);

        interfaz.agregarNotificacion("Tarea para jefes: " + mensaje);
    }

    private static void enviarNotificacionATodos(String tipo, String mensaje) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Notificacion (tipo, mensaje, fechaCreacion) VALUES (?, ?, ?)")) {
            pstmt.setString(1, tipo);
            pstmt.setString(2, mensaje);
            pstmt.setTimestamp(3, Timestamp.from(Instant.now()));
            pstmt.executeUpdate();

            interfaz.agregarNotificacion("Notificación general: " + mensaje);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void enviarNotificacionAEmpleado(String tipo, String mensaje, int idEmpleado) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Notificacion (tipo, mensaje, idEmpleado, fechaCreacion) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, tipo);
            pstmt.setString(2, mensaje);
            pstmt.setInt(3, idEmpleado);
            pstmt.setTimestamp(4, Timestamp.from(Instant.now()));
            pstmt.executeUpdate();

            interfaz.agregarNotificacion("Notificación para empleado: " + mensaje);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean existeJefe(Integer idJefe) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM Jefe WHERE idJefe = ?")) {
            pstmt.setInt(1, idJefe);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}



