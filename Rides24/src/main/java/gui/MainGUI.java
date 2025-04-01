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
    private JButton jButtonRequestRide = null;  
    private JButton jButtonViewReservations = null;  
    private JButton jButtonLogin = null; 
    private JButton jButtonRegister = null; 
    private JButton jButtonAddReview;  
    private JButton jButtonViewNotifications; 

    protected JLabel jLabelSelectOption;
    private JRadioButton rdbtnEnglish;
    private JRadioButton rdbtnEuskara;
    private JRadioButton rdbtnCastellano;
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
        jContentPane.setLayout(new GridLayout(10, 1, 0, 0)); 


        
        jLabelSelectOption = new JLabel("Seleccionar opción");
        jLabelSelectOption.setFont(new Font("Tahoma", Font.BOLD, 13));
        jLabelSelectOption.setForeground(Color.BLACK);
        jLabelSelectOption.setHorizontalAlignment(SwingConstants.CENTER);

        jButtonLogin = new JButton("Iniciar Sesión");
        jButtonLogin.addActionListener(e -> {
            JFrame loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
        });

        jButtonRegister = new JButton("Registrarse");
        jButtonRegister.addActionListener(e -> {
            JFrame registerGUI = new RegisterGUI();
            registerGUI.setVisible(true);
        });

        jButtonCreateRide = new JButton("Crear Viaje");
        jButtonCreateRide.addActionListener(e -> {
            if (loggedInUser instanceof Driver) {
                JFrame createRideGUI = new CreateRideGUI((Driver) loggedInUser);
                createRideGUI.setVisible(true);
            }
        });

        jButtonQueryRides = new JButton("Consultar Viajes");
        jButtonQueryRides.addActionListener(e -> {
            JFrame findRidesGUI = new FindRidesGUI();
            findRidesGUI.setVisible(true);
        });

        jButtonRequestRide = new JButton("Solicitar Reserva");
        jButtonRequestRide.addActionListener(e -> {
            if (loggedInUser instanceof Traveler) {
                JFrame requestRideGUI = new RequestRideGUI((Traveler) loggedInUser);
                requestRideGUI.setVisible(true);
            }
        });

        jButtonViewReservations = new JButton("Ver Solicitudes");
        jButtonViewReservations.addActionListener(e -> {
            if (loggedInUser instanceof Driver) {
                JFrame viewReservationsGUI = new ViewReservationsGUI((Driver) loggedInUser);
                viewReservationsGUI.setVisible(true);
            }
        });

        jButtonAddReview = new JButton("Añadir Reseña");
        jButtonAddReview.addActionListener(e -> {
            if (loggedInUser != null) {
                JFrame addReviewGUI = new AddReviewGUI(loggedInUser);
                addReviewGUI.setVisible(true);
            }
        });

        jButtonViewNotifications = new JButton("Ver Notificaciones");
        jButtonViewNotifications.addActionListener(e -> {
            if (loggedInUser != null) {
                JFrame viewNotificationsGUI = new ViewNotificationsGUI(loggedInUser);
                viewNotificationsGUI.setVisible(true);
            }
        });

        panel = new JPanel();
        rdbtnEnglish = new JRadioButton("English");
        rdbtnEuskara = new JRadioButton("Euskara");
        rdbtnCastellano = new JRadioButton("Castellano");
        Locale.setDefault(new Locale("es"));
        Locale currentLocale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle("Etiquetas", currentLocale);
        if (currentLocale.getLanguage().equals("eus")) {
            rdbtnEuskara.setSelected(true);
        } else if (currentLocale.getLanguage().equals("es")) {
            rdbtnCastellano.setSelected(true);
        } else {
            rdbtnEnglish.setSelected(true);
        }

        buttonGroup.add(rdbtnEnglish);
        buttonGroup.add(rdbtnEuskara);
        buttonGroup.add(rdbtnCastellano);

        rdbtnEnglish.addActionListener(e -> {
            Locale.setDefault(new Locale("en"));
            paintAgain();
        });

        rdbtnEuskara.addActionListener(e -> {
            Locale.setDefault(new Locale("eus"));
            paintAgain();
        });

        rdbtnCastellano.addActionListener(e -> {
            Locale.setDefault(new Locale("es"));
            paintAgain();
        });

        panel.add(rdbtnEuskara);
        panel.add(rdbtnCastellano);
        panel.add(rdbtnEnglish);

        jContentPane.add(jLabelSelectOption);
        jContentPane.add(jButtonLogin);
        jContentPane.add(jButtonRegister);
        jContentPane.add(jButtonCreateRide);
        jContentPane.add(jButtonQueryRides);
        jContentPane.add(jButtonRequestRide);
        jContentPane.add(jButtonViewReservations);
        jContentPane.add(jButtonAddReview);
        jContentPane.add(jButtonViewNotifications);
        jContentPane.add(panel);
 
        setContentPane(jContentPane);
        setTitle(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.MainTitle") + 
               " - Usuario: " + (loggedInUser != null ? loggedInUser.getEmail() : "No autenticado"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
        });

        this.setSize(495, 400);
        this.setVisible(true);

        adjustUIForUserRole();
    }

    private void adjustUIForUserRole() {
        boolean isLoggedIn = (loggedInUser != null);
        

        jButtonLogin.setEnabled(!isLoggedIn);
        jButtonRegister.setEnabled(!isLoggedIn);
        
    
        jButtonCreateRide.setEnabled(isLoggedIn && loggedInUser instanceof Driver);
        jButtonRequestRide.setEnabled(isLoggedIn && loggedInUser instanceof Traveler);
        jButtonViewReservations.setEnabled(isLoggedIn && loggedInUser instanceof Driver);
        
 
        jButtonAddReview.setEnabled(isLoggedIn);
        jButtonViewNotifications.setEnabled(isLoggedIn);
    }

     private void paintAgain() {
        ResourceBundle bundle = ResourceBundle.getBundle("Etiquetas");
        
        jLabelSelectOption.setText(bundle.getString("MainGUI.SelectOption"));
        jButtonCreateRide.setText(bundle.getString("MainGUI.CreateRide"));
        jButtonQueryRides.setText(bundle.getString("MainGUI.QueryRides"));
        jButtonRequestRide.setText(bundle.getString("MainGUI.RequestRide"));
        jButtonViewReservations.setText(bundle.getString("MainGUI.ViewReservations"));
        jButtonAddReview.setText(bundle.getString("MainGUI.AddReview"));
        jButtonViewNotifications.setText(bundle.getString("MainGUI.ViewNotifications"));
        jButtonLogin.setText(bundle.getString("MainGUI.Login"));
        jButtonRegister.setText(bundle.getString("MainGUI.Register"));
        
        jButtonCreateRide.setText(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.CreateRide"));
        this.setTitle(ResourceBundle.getBundle("Etiquetas").getString("MainGUI.MainTitle") + 
                     " - Usuario: " + (loggedInUser != null ? loggedInUser.getEmail() : "No autenticado"));
        
    }
}