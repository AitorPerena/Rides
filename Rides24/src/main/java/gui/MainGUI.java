package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import domain.*;
import businessLogic.BLFacade;

public class MainGUI extends JFrame {
    private User loggedInUser; 
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;
    private JButton jButtonCreateRide = null;
    private JButton jButtonQueryRides = null;
    private JButton jButtonRequestRide = null;  // Botón para solicitar reserva
    private JButton jButtonViewReservations = null;  // Botón para ver solicitudes
    private JButton jButtonLogin = null;  // Botón para iniciar sesión
    private JButton jButtonRegister = null;  // Botón para registrarse

    protected JLabel jLabelSelectOption;
    private JRadioButton rdbtnNewRadioButton;
    private JRadioButton rdbtnNewRadioButton_1;
    private JRadioButton rdbtnNewRadioButton_2;
    private JPanel panel;
    private final ButtonGroup buttonGroup = new ButtonGroup();

    private static BLFacade appFacadeInterface;

    public static BLFacade getBusinessLogic() {
        return appFacadeInterface;
    }

    public static void setBussinessLogic(BLFacade afi) {
        appFacadeInterface = afi;
    }

    public MainGUI(User user) {
        super();
        this.loggedInUser = user; 

        jContentPane = new JPanel();
        jContentPane.setLayout(new GridLayout(8, 1, 0, 0));  

        jLabelSelectOption = new JLabel("Seleccionar opción");
        jLabelSelectOption.setFont(new Font("Tahoma", Font.BOLD, 13));
        jLabelSelectOption.setForeground(Color.BLACK);
        jLabelSelectOption.setHorizontalAlignment(SwingConstants.CENTER);

        jButtonLogin = new JButton("Iniciar Sesión");
        jButtonLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame loginGUI = new LoginGUI();
                loginGUI.setVisible(true);
            }
        });

        jButtonRegister = new JButton("Registrarse");
        jButtonRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame registerGUI = new RegisterGUI();
                registerGUI.setVisible(true);
            }
        });

        // Botón para crear un viaje (solo para conductores)
        jButtonCreateRide = new JButton(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.CreateRide"));
        jButtonCreateRide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loggedInUser instanceof Driver) {
                    JFrame createRideGUI = new CreateRideGUI((Driver) loggedInUser);
                    createRideGUI.setVisible(true);
                }
            }
        });

        jButtonQueryRides = new JButton(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.QueryRides"));
        jButtonQueryRides.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame findRidesGUI = new FindRidesGUI();
                findRidesGUI.setVisible(true);
            }
        });

        // Botón para solicitar reserva (solo para viajeros)
        jButtonRequestRide = new JButton("Solicitar Reserva");
        jButtonRequestRide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loggedInUser instanceof Traveler) {
                    JFrame requestRideGUI = new RequestRideGUI((Traveler) loggedInUser);
                    requestRideGUI.setVisible(true);
                }
            }
        });

        // Botón para ver solicitudes (solo para conductores)
        jButtonViewReservations = new JButton("Ver Solicitudes");
        jButtonViewReservations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loggedInUser instanceof Driver) {
                    JFrame viewReservationsGUI = new ViewReservationsGUI((Driver) loggedInUser);
                    viewReservationsGUI.setVisible(true);
                }
            }
        });

        // Radio buttons para cambiar el idioma
        rdbtnNewRadioButton = new JRadioButton("English");
        rdbtnNewRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Locale.setDefault(new Locale("en"));
                System.out.println("Locale: " + Locale.getDefault());
                paintAgain();
            }
        });
        buttonGroup.add(rdbtnNewRadioButton);

        rdbtnNewRadioButton_1 = new JRadioButton("Euskara");
        rdbtnNewRadioButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Locale.setDefault(new Locale("eus"));
                System.out.println("Locale: " + Locale.getDefault());
                paintAgain();
            }
        });
        buttonGroup.add(rdbtnNewRadioButton_1);

        rdbtnNewRadioButton_2 = new JRadioButton("Castellano");
        rdbtnNewRadioButton_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Locale.setDefault(new Locale("es"));
                System.out.println("Locale: " + Locale.getDefault());
                paintAgain();
            }
        });
        buttonGroup.add(rdbtnNewRadioButton_2);

        // Panel para los radio buttons
        panel = new JPanel();
        panel.add(rdbtnNewRadioButton_1);
        panel.add(rdbtnNewRadioButton_2);
        panel.add(rdbtnNewRadioButton);

        // Añadir componentes al panel principal
        jContentPane.add(jLabelSelectOption);
        jContentPane.add(jButtonLogin);  // Añadir el botón de iniciar sesión
        jContentPane.add(jButtonRegister);  // Añadir el botón de registrarse
        jContentPane.add(jButtonCreateRide);
        jContentPane.add(jButtonQueryRides);
        jContentPane.add(jButtonRequestRide);  // Añadir el botón de solicitar reserva
        jContentPane.add(jButtonViewReservations);  // Añadir el botón de ver solicitudes
        jContentPane.add(panel);  // Añadir el panel de radio buttons

        // Configurar el contenido de la ventana
        setContentPane(jContentPane);
        setTitle(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.MainTitle") + " - Usuario: " + (loggedInUser != null ? loggedInUser.getEmail() : "No autenticado"));

        // Configurar el cierre de la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
        });

        // Tamaño y visibilidad de la ventana
        this.setSize(495, 350);  // Aumentamos el tamaño para acomodar los nuevos botones
        this.setVisible(true);

        // Ajustar visibilidad de botones según el tipo de usuario
        adjustUIForUserRole();
    }

    private void adjustUIForUserRole() {
        if (loggedInUser == null) {
            jButtonCreateRide.setEnabled(false);
            jButtonRequestRide.setEnabled(false);
            jButtonViewReservations.setEnabled(false);
        } else if (loggedInUser instanceof Driver) {
            jButtonCreateRide.setEnabled(true);
            jButtonRequestRide.setEnabled(false);
            jButtonViewReservations.setEnabled(true);
        } else if (loggedInUser instanceof Traveler) {
            jButtonCreateRide.setEnabled(false);
            jButtonRequestRide.setEnabled(true);
            jButtonViewReservations.setEnabled(false);
        }
    }

    private void paintAgain() {
        jLabelSelectOption.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.SelectOption"));
        jButtonQueryRides.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.QueryRides"));
        jButtonCreateRide.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.CreateRide"));
        this.setTitle(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.MainTitle") + " - Usuario: " + (loggedInUser != null ? loggedInUser.getEmail() : "No autenticado"));
    }
}