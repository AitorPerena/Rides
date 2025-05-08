package gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Date;

import businessLogic.BLFacade;

public class RegisterGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JTextField cardNumberField;
    private JTextField expirationField;
    private JPasswordField cvvField;
    private JButton registerButton;
    private JButton cancelButton;
    
    private JLabel jLabelName = new JLabel();
    private JLabel jLabelEmail = new JLabel();
    private JLabel jLabelPassword = new JLabel();
    private JLabel jLabelRole = new JLabel();
    private JLabel jLabelcardNumber = new JLabel();
    private JLabel jLabelexpiration = new JLabel();
    private JLabel jLabelcvv = new JLabel();
    
    private ResourceBundle bundle;

    public RegisterGUI() {
        this.bundle = ResourceBundle.getBundle("Etiquetas");
        initializeUI();
    }

    private void initializeUI() {
        setTitle(bundle.getString("RegisterGUI.Title"));
        setSize(400, 400);
        setLayout(new GridLayout(9, 2, 5, 5)); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        jLabelName.setText(bundle.getString("RegisterGUI.Name"));
        nameField = new JTextField();
        
        jLabelEmail.setText(bundle.getString("RegisterGUI.Email"));
        emailField = new JTextField();
        
        jLabelPassword.setText(bundle.getString("RegisterGUI.Password"));
        passwordField = new JPasswordField();
        
        jLabelcardNumber.setText(bundle.getString("RegisterGUI.CardNumber")); 
        cardNumberField = new JTextField();
        
        jLabelexpiration.setText(bundle.getString("RegisterGUI.Expiration")); 
        expirationField = new JTextField();
        
        jLabelcvv.setText(bundle.getString("RegisterGUI.CVV")); 
        cvvField = new JPasswordField();
       
        jLabelRole.setText(bundle.getString("RegisterGUI.Role"));
        roleComboBox = new JComboBox<>(new String[]{"Traveler", "Driver"});
        
        registerButton = new JButton(bundle.getString("RegisterGUI.Submit"));
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerAction();
            }
        });
        
        cancelButton = new JButton(bundle.getString("RegisterGUI.Cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelAction();
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
        add(jLabelcardNumber);
        add(cardNumberField);
        add(jLabelexpiration);
        add(expirationField);
        add(jLabelcvv);
        add(cvvField);
        add(registerButton);
        add(cancelButton);
    }

    private void registerAction() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String cardNumber = cardNumberField.getText().trim();
        String expiration = expirationField.getText().trim();
        String cvv = new String(cvvField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || 
            cardNumber.isEmpty() || expiration.isEmpty() || cvv.isEmpty()) {
            JOptionPane.showMessageDialog(RegisterGUI.this, 
                bundle.getString("RegisterGUI.ErrorEmpty"),
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        BLFacade facade = MainGUI.getBusinessLogic();

        // Verificar si el usuario ya existe
        if (facade.userExists(email)) {
            JOptionPane.showMessageDialog(RegisterGUI.this,
                bundle.getString("RegisterGUI.UserExists"),
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar tarjeta de crédito
        if (!facade.validateCreditCard(cardNumber, expiration, cvv)) {
            JOptionPane.showMessageDialog(RegisterGUI.this,
                bundle.getString("RegisterGUI.InvalidCard"),
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Validar fecha de expiración
            Date expirationDate = facade.parseExpirationDate(expiration);
            if (expirationDate.before(new Date())) {
                JOptionPane.showMessageDialog(RegisterGUI.this,
                    bundle.getString("RegisterGUI.ExpiredCard"),
                    bundle.getString("Error"), 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(RegisterGUI.this,
                bundle.getString("RegisterGUI.InvalidDate"),
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Registrar usuario
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
    
    private void cancelAction() {
        dispose(); 
    }
}