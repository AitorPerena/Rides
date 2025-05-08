package gui;

import javax.swing.*;
import java.util.ResourceBundle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import businessLogic.BLFacade;
import domain.*;

public class LoginGUI extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private boolean loginSuccessful = false;
    private User loggedInUser = null;

    private JLabel jLabelEmail = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Email"));
    private JLabel jLabelPassword = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Password"));
    private JLabel jLabelMsg = new JLabel();
    
    public LoginGUI() {
        super((JFrame) null, ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Title"), true);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 10, 10));
        setResizable(false);

        add(jLabelEmail);
        emailField = new JTextField();
        add(emailField);

        add(jLabelPassword);
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginButton = new JButton(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Submit"));
        loginButton.addActionListener(this::performLogin);
        add(loginButton);

        JButton cancelButton = new JButton(ResourceBundle.getBundle("Etiquetas").getString("Close"));
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);
    }

    private void performLogin(ActionEvent e) {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.ErrorEmpty"), 
                ResourceBundle.getBundle("Etiquetas").getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        BLFacade facade = MainGUI.getBusinessLogic();
        User user = facade.login(email, password);

        if (user != null) {
            loginSuccessful = true;
            loggedInUser = user;
            JOptionPane.showMessageDialog(this, 
                ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Success"), 
                ResourceBundle.getBundle("Etiquetas").getString("Success"), 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.ErrorInvalid"), 
                ResourceBundle.getBundle("Etiquetas").getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}