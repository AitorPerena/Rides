package gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import businessLogic.BLFacade;
import domain.*;

public class LoginGUI extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginGUI() {
        setTitle("Iniciar Sesión");
        setSize(300, 200);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Iniciar Sesión");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                if (email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginGUI.this, "Email y contraseña son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

             
                BLFacade facade = MainGUI.getBusinessLogic();
                User user = facade.login(email, password);

                if (user != null) {
                    JOptionPane.showMessageDialog(LoginGUI.this, "Inicio de sesión exitoso", "éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); 

          
                    MainGUI mainGUI = new MainGUI(user);
                    mainGUI.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(LoginGUI.this, "Email o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(loginButton);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);  
    }
}