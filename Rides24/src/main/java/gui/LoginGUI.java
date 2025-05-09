package gui;

import javax.swing.*;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;

import businessLogic.BLFacade;
import domain.User;

public class LoginGUI extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private boolean loginSuccessful = false;
    private User loggedInUser = null;

    public LoginGUI() {
        super((JFrame) null, "Iniciar Sesión", true);
        initializeComponents();
    }

    private void initializeComponents() {
        setSize(300, 200);
        setLayout(new GridLayout(3, 2, 10, 10));
        setLocationRelativeTo(null);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginButton = new JButton("Iniciar Sesión");
        loginButton.addActionListener(this::performLogin);
        add(loginButton);

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);
    }

 // En LoginGUI, modificar performLogin():

    private void performLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Email y contraseña son obligatorios", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BLFacade facade = MainGUI.getBusinessLogic();
            if (facade == null) {
                throw new IllegalStateException("No se pudo conectar con el servidor");
            }

            User user = facade.login(email, password);
            if (user != null) {
                if (user.isCurrentlyBanned()) {
                    String message = "Tu cuenta está suspendida";
                    if (user.getBanEndDate() != null) {
                        message += "\nTiempo restante: " + user.getBanRemainingTime();
                    }
                    JOptionPane.showMessageDialog(this, 
                        message, 
                        "Cuenta Suspendida", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                loginSuccessful = true;
                loggedInUser = user;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Email o contraseña incorrectos", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al iniciar sesión: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
    
}