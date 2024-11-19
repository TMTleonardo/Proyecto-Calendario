package principal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;

public class DatabaseConnection {
    private static final String CONFIG_FILE = "db_config.properties";
    private static Connection connection = null;
    private static Interfaz interfaz;

    public static void setInterfaz(Interfaz interfaz) {
        DatabaseConnection.interfaz = interfaz;
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Properties props = loadProperties();
                if (props == null) {
                    props = requestDatabaseConfig();
                    saveProperties(props);
                }
                try {
                    String url = props.getProperty("url");
                    String user = props.getProperty("user");
                    String password = props.getProperty("password");
                    connection = DriverManager.getConnection(url, user, password);
                    if (interfaz != null) {
                        interfaz.agregarNotificacion("Conectado a la base de datos exitosamente.");
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Error al conectar a la base de datos: " + e.getMessage());
                    connection = null;
                    if (interfaz != null) {
                        interfaz.agregarNotificacion("Error al conectar a la base de datos: " + e.getMessage());
                    }
                    props = requestDatabaseConfig();
                    saveProperties(props);
                    return getConnection();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al verificar la conexión: " + e.getMessage());
            if (interfaz != null) {
                interfaz.agregarNotificacion("Error al verificar la conexión: " + e.getMessage());
            }
        }
        return connection;
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            return props;
        } catch (IOException e) {
            return null;
        }
    }

    private static Properties requestDatabaseConfig() {
        Properties props = new Properties();
        String url = JOptionPane.showInputDialog("Ingrese la URL de la base de datos (ejemplo: jdbc:postgresql://host:port/dbname):");
        String user = JOptionPane.showInputDialog("Ingrese el usuario de la base de datos:");
        String password = JOptionPane.showInputDialog("Ingrese la contraseña de la base de datos:");

        props.setProperty("url", url);
        props.setProperty("user", user);
        props.setProperty("password", password);
        return props;
    }

    private static void saveProperties(Properties props) {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuración de la Base de Datos");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar la configuración: " + e.getMessage());
            if (interfaz != null) {
                interfaz.agregarNotificacion("Error al guardar la configuración: " + e.getMessage());
            }
        }
    }

    public static void resetConnection() {
        connection = null;
        new java.io.File(CONFIG_FILE).delete();
        JOptionPane.showMessageDialog(null, "Se ha reiniciado la configuración de la base de datos.");
        if (interfaz != null) {
            interfaz.agregarNotificacion("Se ha reiniciado la configuración de la base de datos.");
        }
    }

    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                JOptionPane.showMessageDialog(null, "Desconectado de la base de datos.");
                if (interfaz != null) {
                    interfaz.agregarNotificacion("Desconectado de la base de datos.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al desconectar la base de datos: " + e.getMessage());
                if (interfaz != null) {
                    interfaz.agregarNotificacion("Error al desconectar la base de datos: " + e.getMessage());
                }
            }
        }
    }
}




