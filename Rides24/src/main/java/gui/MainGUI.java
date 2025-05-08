package gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import domain.*;
import businessLogic.BLFacade;
import java.util.ResourceBundle;
import java.util.Locale;

public class MainGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private User loggedInUser = null;

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel cardsPanel;

    // Paneles
    private JPanel guestPanel;
    private JPanel travelerPanel;
    private JPanel driverPanel;
    private JPanel adminPanel;
    private JPanel languagePanel;

    private JRadioButton rdbtnEnglish;
    private JRadioButton rdbtnEuskara;
    private JRadioButton rdbtnCastellano;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("Etiquetas");

    private static BLFacade appFacadeInterface;
    public static BLFacade getBusinessLogic() {
        return appFacadeInterface;
    }

    public static void setBussinessLogic(BLFacade afi) {
        appFacadeInterface = afi;
    }

    public MainGUI() {
        super("Ride Sharing App");
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Configurar CardLayout para cambiar entre paneles
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        
        // Crear los diferentes paneles
        createGuestPanel();
        createTravelerPanel();
        createDriverPanel();
        createAdminPanel();
        createLanguagePanel();

        // Añadir todos los paneles al CardLayout
        cardsPanel.add(guestPanel, "GUEST");
        cardsPanel.add(travelerPanel, "TRAVELER");
        cardsPanel.add(driverPanel, "DRIVER");
        cardsPanel.add(adminPanel, "ADMIN");

        // Mostrar el panel de invitado por defecto
        cardLayout.show(cardsPanel, "GUEST");

        // Configurar el panel principal
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(cardsPanel, BorderLayout.CENTER);
        mainPanel.add(languagePanel, BorderLayout.SOUTH);
        add(mainPanel);
        updateUITexts();
    }

    private JButton createResourceButton(String resourceKey) {
        JButton button = new JButton(resourceBundle.getString(resourceKey));
        button.putClientProperty("resourceKey", resourceKey);
        return button;
    }

    private void createGuestPanel() {
        guestPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        guestPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel(resourceBundle.getString("MainGUI.Welcome"), JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.putClientProperty("resourceKey", "MainGUI.Welcome");
        
        JButton loginButton = createResourceButton("MainGUI.Login");
        loginButton.addActionListener(e -> openLoginDialog());

        JButton registerButton = createResourceButton("MainGUI.Register");
        registerButton.addActionListener(e -> openRegisterDialog());

        JButton queryRidesButton = createResourceButton("MainGUI.QueryRides");
        queryRidesButton.addActionListener(e -> new FindRidesGUI().setVisible(true));

        guestPanel.add(welcomeLabel);
        guestPanel.add(loginButton);
        guestPanel.add(registerButton);
        guestPanel.add(queryRidesButton);
    }

    private void createTravelerPanel() {
        travelerPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        travelerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel(resourceBundle.getString("MainGUI.Welcome") + " " + 
                                       resourceBundle.getString("Traveler"), JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.putClientProperty("resourceKey", "MainGUI.WelcomeTraveler");
        
        JButton requestRideButton = createResourceButton("MainGUI.RequestRide");
        requestRideButton.addActionListener(e -> new RequestRideGUI((Traveler) loggedInUser).setVisible(true));

        JButton queryRidesButton = createResourceButton("MainGUI.QueryRides");
        queryRidesButton.addActionListener(e -> new FindRidesGUI().setVisible(true));
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Botón de notificaciones
        JButton notificationsButton = createResourceButton("MainGUI.ViewNotifications");
        notificationsButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        notificationsButton.addActionListener(e -> new ViewNotificationsGUI(loggedInUser).setVisible(true));
        bottomPanel.add(notificationsButton, BorderLayout.WEST);
        
        // Botón de reseñas
        JButton reviewsButton = createResourceButton("MainGUI.AddReview");
        reviewsButton.setHorizontalTextPosition(SwingConstants.LEFT);
        reviewsButton.addActionListener(e -> new AddReviewGUI(loggedInUser).setVisible(true));
        bottomPanel.add(reviewsButton, BorderLayout.EAST);

        travelerPanel.add(bottomPanel, BorderLayout.SOUTH);

        JButton walletButton = createResourceButton("MainGUI.Wallet");
        walletButton.addActionListener(e -> new WalletGUI(loggedInUser).setVisible(true));

        JButton logoutButton = createResourceButton("MainGUI.CloseSession");
        logoutButton.addActionListener(e -> logout());

        travelerPanel.add(welcomeLabel);
        travelerPanel.add(requestRideButton);
        travelerPanel.add(queryRidesButton);
        travelerPanel.add(walletButton);
        travelerPanel.add(logoutButton);
    }

    private void createDriverPanel() {
        driverPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        driverPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel(resourceBundle.getString("MainGUI.Welcome") + " " + 
                                       resourceBundle.getString("Driver"), JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.putClientProperty("resourceKey", "MainGUI.WelcomeDriver");
        
        JButton createRideButton = createResourceButton("MainGUI.CreateRide");
        createRideButton.addActionListener(e -> new CreateRideGUI((Driver) loggedInUser).setVisible(true));

        JButton viewReservationsButton = createResourceButton("MainGUI.ViewReservations");
        viewReservationsButton.addActionListener(e -> new ViewReservationsGUI((Driver) loggedInUser).setVisible(true));

        JButton queryRidesButton = createResourceButton("MainGUI.QueryRides");
        queryRidesButton.addActionListener(e -> new FindRidesGUI().setVisible(true));

        JButton walletButton = createResourceButton("MainGUI.Wallet");
        walletButton.addActionListener(e -> new WalletGUI(loggedInUser).setVisible(true));

        JButton logoutButton = createResourceButton("MainGUI.CloseSession");
        logoutButton.addActionListener(e -> logout());

        driverPanel.add(welcomeLabel);
        driverPanel.add(createRideButton);
        driverPanel.add(viewReservationsButton);
        driverPanel.add(queryRidesButton);
        driverPanel.add(walletButton);
        driverPanel.add(logoutButton);
    }

    private void createAdminPanel() {
        adminPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel(resourceBundle.getString("MainGUI.Welcome") + " " + 
                                       resourceBundle.getString("MainGUI.UserRoleAdmin"), JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.putClientProperty("resourceKey", "MainGUI.WelcomeAdmin");
        
        JButton manageUsersButton = createResourceButton("MainGUI.ManageUsers");
        //manageUsersButton.addActionListener(e -> new ManageUsersGUI().setVisible(true));

        JButton queryRidesButton = createResourceButton("MainGUI.QueryRides");
        queryRidesButton.addActionListener(e -> new FindRidesGUI().setVisible(true));

        JButton logoutButton = createResourceButton("MainGUI.CloseSession");
        logoutButton.addActionListener(e -> logout());

        adminPanel.add(welcomeLabel);
        adminPanel.add(manageUsersButton);
        adminPanel.add(queryRidesButton);
        adminPanel.add(logoutButton);
    }
    
    private void createLanguagePanel() {
        languagePanel = new JPanel();
        rdbtnEnglish = new JRadioButton("English");
        rdbtnEuskara = new JRadioButton("Euskara");
        rdbtnCastellano = new JRadioButton("Castellano");
        
        Locale currentLocale = Locale.getDefault();
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

        rdbtnEnglish.addActionListener(e -> changeLanguage(new Locale("en")));
        rdbtnEuskara.addActionListener(e -> changeLanguage(new Locale("eus")));
        rdbtnCastellano.addActionListener(e -> changeLanguage(new Locale("es")));

        languagePanel.add(rdbtnEuskara);
        languagePanel.add(rdbtnCastellano);
        languagePanel.add(rdbtnEnglish);
    }
    
    private void changeLanguage(Locale locale) {
        Locale.setDefault(locale);
        resourceBundle = ResourceBundle.getBundle("Etiquetas", locale);
        updateUITexts();
    }

    private void openLoginDialog() {
        LoginGUI loginDialog = new LoginGUI();
        loginDialog.setVisible(true);
        
        if (loginDialog.isLoginSuccessful()) {
            this.loggedInUser = loginDialog.getLoggedInUser();
            updateUIAfterLogin();
        }
    }

    private void openRegisterDialog() {
        RegisterGUI registerDialog = new RegisterGUI();
        registerDialog.setVisible(true);
    }

    private void updateUIAfterLogin() {
        if (loggedInUser instanceof Traveler) {
            cardLayout.show(cardsPanel, "TRAVELER");
        } else if (loggedInUser instanceof Driver) {
            cardLayout.show(cardsPanel, "DRIVER");
        }
        updateUITexts();
    }

    private void logout() {
        this.loggedInUser = null;
        cardLayout.show(cardsPanel, "GUEST");
        updateUITexts();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainGUI mainGUI = new MainGUI();
            mainGUI.setVisible(true);
        });
    }
    
    private void updateUITexts() {
        setTitle(resourceBundle.getString("MainGUI.MainTitle") + 
               " - " + (loggedInUser != null ? loggedInUser.getEmail() : resourceBundle.getString("MainGUI.Welcome")));
        
        updatePanelTexts(guestPanel);
        updatePanelTexts(travelerPanel);
        updatePanelTexts(driverPanel);
        updatePanelTexts(adminPanel);
    }

    private void updatePanelTexts(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) {
                updateButtonText((JButton) comp);
            } else if (comp instanceof JLabel) {
                updateLabelText((JLabel) comp);
            } else if (comp instanceof JPanel) {
                updatePanelTexts((JPanel) comp);
            }
        }
    }

    private void updateButtonText(JButton button) {
        String key = (String) button.getClientProperty("resourceKey");
        if (key != null) {
            button.setText(resourceBundle.getString(key));
        }
    }

    private void updateLabelText(JLabel label) {
        String key = (String) label.getClientProperty("resourceKey");
        if (key != null) {
            if (key.equals("MainGUI.WelcomeTraveler")) {
                label.setText(resourceBundle.getString("MainGUI.Welcome") + " " + 
                            resourceBundle.getString("Traveler"));
            } else if (key.equals("MainGUI.WelcomeDriver")) {
                label.setText(resourceBundle.getString("MainGUI.Welcome") + " " + 
                            resourceBundle.getString("Driver"));
            } else if (key.equals("MainGUI.WelcomeAdmin")) {
                label.setText(resourceBundle.getString("MainGUI.Welcome") + " " + 
                            resourceBundle.getString("MainGUI.UserRoleAdmin"));
            } else {
                label.setText(resourceBundle.getString(key));
            }
        }
    }
}