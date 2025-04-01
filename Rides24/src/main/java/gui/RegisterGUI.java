package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import businessLogic.BLFacade;

public class RegisterGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;
    
    private JLabel jLabelName = new JLabel();
    private JLabel jLabelEmail = new JLabel();
    private JLabel jLabelPassword = new JLabel();
    private JLabel jLabelRole = new JLabel();
    
    private ResourceBundle bundle;

    public RegisterGUI() {
        this.bundle = ResourceBundle.getBundle("Etiquetas");
        initializeUI();
    }

    private void initializeUI() {
        setTitle(bundle.getString("RegisterGUI.Title"));
        setSize(300, 250);
        setLayout(new GridLayout(5, 2));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);


        jLabelName.setText(bundle.getString("RegisterGUI.Name"));
        nameField = new JTextField();
        
        jLabelEmail.setText(bundle.getString("RegisterGUI.Email"));
        emailField = new JTextField();
        
        jLabelPassword.setText(bundle.getString("RegisterGUI.Password"));
        passwordField = new JPasswordField();
        
        jLabelRole.setText(bundle.getString("RegisterGUI.Role"));
        roleComboBox = new JComboBox<>(new String[]{"Traveler", "Driver"});
        
        registerButton = new JButton(bundle.getString("RegisterGUI.Submit"));
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerAction();
            }
        });

 
        add(jLabelName);
        add(nameField);
        add(jLabelEmail);
        add(emailField);
        add(jLabelPassword);
        add(passwordField);
        add(jLabelRole);
        add(roleComboBox);
        add(new JLabel());
        add(registerButton);
    }

    private void registerAction() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(RegisterGUI.this, 
                bundle.getString("RegisterGUI.ErrorEmpty"),
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        BLFacade facade = MainGUI.getBusinessLogic();
        boolean success = facade.register(email, password, name, role);

        if (success) {
            JOptionPane.showMessageDialog(RegisterGUI.this, 
                bundle.getString("RegisterGUI.Success"),
                bundle.getString("Success"), 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(RegisterGUI.this, 
                bundle.getString("RegisterGUI.Error"),
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}