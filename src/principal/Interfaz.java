package principal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;

public class Interfaz extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private String rolUsuario;
    private int idUsuario;  
    private JTextArea areaNotificaciones; 

    public Interfaz(String rolUsuario, int idUsuario) {
        this.rolUsuario = rolUsuario;
        this.idUsuario = idUsuario;

        setTitle("ProyectoAgenda");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if (DatabaseConnection.getConnection() == null) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos. Se solicitarán los datos nuevamente.");
            DatabaseConnection.resetConnection();
            DatabaseConnection.getConnection();
        }

        JMenuBar menuBar = new JMenuBar();
        JMenu menuBaseDatos = new JMenu("Base de datos");

        JMenuItem itemConfigurar = new JMenuItem("Configurar conexión");
        itemConfigurar.addActionListener(e -> configurarConexion());

        JMenuItem itemDesconectar = new JMenuItem("Desconectar");
        itemDesconectar.addActionListener(e -> DatabaseConnection.disconnect());

        JMenuItem itemReiniciar = new JMenuItem("Reiniciar configuración");
        itemReiniciar.addActionListener(e -> DatabaseConnection.resetConnection());

        menuBaseDatos.add(itemConfigurar);
        menuBaseDatos.add(itemDesconectar);
        menuBaseDatos.add(itemReiniciar);

        menuBar.add(menuBaseDatos);
        setJMenuBar(menuBar);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(6, 1, 5, 5));
        menuPanel.setBackground(new Color(60, 63, 65));

        JButton btnDashboard = createMenuButton("Dashboard", e -> mostrarSeccion("Dashboard"));
        JButton btnCalendar = createMenuButton("Calendario", e -> mostrarSeccion("Calendario"));
        JButton btnTasks = createMenuButton("Gestión de Tareas", e -> mostrarSeccion("Tareas"));
        JButton btnEmployees = createMenuButton("Gestión de Empleados", e -> mostrarSeccion("Empleados"));
        JButton btnEvents = createMenuButton("Gestión de Eventos", e -> mostrarSeccion("Eventos"));
        JButton btnNotifications = createMenuButton("Notificaciones", e -> mostrarSeccion("Notificaciones"));

        menuPanel.add(btnDashboard);
        menuPanel.add(btnCalendar);
        menuPanel.add(btnNotifications);

        if ("Jefe".equals(rolUsuario)) {
            menuPanel.add(btnTasks);
            menuPanel.add(btnEmployees);
            menuPanel.add(btnEvents);
        }

        add(menuPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.add(new JLabel("Bienvenido al Dashboard de ProyectoAgenda"), "Dashboard");
        mainPanel.add(Calendario.crearPanelCalendario(), "Calendario");
        mainPanel.add(Tarea.crearPanelTareas(), "Tareas");
        mainPanel.add(Empleado.crearPanelEmpleados(), "Empleados");
        mainPanel.add(Evento.crearPanelEventos(), "Eventos");
        mainPanel.add(crearPanelNotificaciones(), "Notificaciones");

        setVisible(true);
    }

    private JButton createMenuButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(80, 80, 82));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addActionListener(action);
        return button;
    }

    private void mostrarSeccion(String seccion) {
        cardLayout.show(mainPanel, seccion);
    }

    private void configurarConexion() {
        DatabaseConnection.resetConnection();
        DatabaseConnection.getConnection();
    }

    private JPanel crearPanelNotificaciones() {
        JPanel panelNotificaciones = new JPanel(new BorderLayout());

        JLabel labelTitulo = new JLabel("Notificaciones");
        labelTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panelNotificaciones.add(labelTitulo, BorderLayout.NORTH);

        areaNotificaciones = new JTextArea();
        areaNotificaciones.setEditable(false);
        areaNotificaciones.append("Bienvenido al sistema de ProyectoAgenda.\n");

        JScrollPane scrollPane = new JScrollPane(areaNotificaciones);
        panelNotificaciones.add(scrollPane, BorderLayout.CENTER);
        return panelNotificaciones;
    }

    public void agregarNotificacion(String mensaje) {
        if (areaNotificaciones != null) {
            areaNotificaciones.append(mensaje + "\n");
            areaNotificaciones.setCaretPosition(areaNotificaciones.getDocument().getLength()); // Desplazarse a la última notificación
        }
    }
    
    public static void main(String[] args) {
        if (!existeJefe()) {
            JOptionPane.showMessageDialog(null, "No hay un jefe registrado. Debe registrar el primer jefe.");
            Jefe.crearPrimerJefe();
        }

        String rolUsuario = Login.mostrarPantallaLogin();
        int idUsuario = Login.obtenerIdUsuario();

        if (rolUsuario != null && idUsuario != -1) {
            Interfaz interfaz = new Interfaz(rolUsuario, idUsuario);
            DatabaseConnection.setInterfaz(interfaz);
            Notificaciones notificaciones = new Notificaciones(interfaz);
        } else {
            System.out.println("No se pudo iniciar sesión.");
            System.exit(0);
        }
    }

    private static boolean existeJefe() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Jefe")) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}






