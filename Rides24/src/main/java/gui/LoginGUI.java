package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import businessLogic.BLFacade;
import domain.User;

public class LoginGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JTextField emailField;
    private JPasswordField passwordField;
    
    private JLabel jLabelEmail = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Email"));
    private JLabel jLabelPassword = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Password"));
    private JLabel jLabelMsg = new JLabel();
    
    private JButton jButtonLogin = new JButton(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Submit"));

    public LoginGUI() {
        this.getContentPane().setLayout(null);
        this.setSize(new Dimension(350, 250));
        this.setTitle(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Title"));
        
        // Configurar posición de componentes
        jLabelEmail.setBounds(new Rectangle(30, 40, 100, 20));
        emailField = new JTextField();
        emailField.setBounds(new Rectangle(140, 40, 150, 20));
        
        jLabelPassword.setBounds(new Rectangle(30, 80, 100, 20));
        passwordField = new JPasswordField();
        passwordField.setBounds(new Rectangle(140, 80, 150, 20));
        
        jButtonLogin.setBounds(new Rectangle(110, 130, 120, 30));
        jButtonLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButtonLogin_actionPerformed(e);
            }
        });
        
        jLabelMsg.setBounds(new Rectangle(30, 170, 300, 20));
        jLabelMsg.setForeground(Color.red);

        // Añadir componentes al panel
        this.getContentPane().add(jLabelEmail);
        this.getContentPane().add(emailField);
        this.getContentPane().add(jLabelPassword);
        this.getContentPane().add(passwordField);
        this.getContentPane().add(jButtonLogin);
        this.getContentPane().add(jLabelMsg);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void jButtonLogin_actionPerformed(ActionEvent e) {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            jLabelMsg.setText(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.ErrorEmpty"));
            return;
        }

        BLFacade facade = MainGUI.getBusinessLogic();
        User user = facade.login(email, password);

        if (user != null) {
            jLabelMsg.setText(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.Success"));
            dispose();
            MainGUI mainGUI = new MainGUI(user);
            mainGUI.setVisible(true);
        } else {
            jLabelMsg.setText(ResourceBundle.getBundle("Etiquetas").getString("LoginGUI.ErrorInvalid"));
        }
    }
}