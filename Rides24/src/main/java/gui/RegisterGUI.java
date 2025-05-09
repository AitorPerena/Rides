package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Date;
import businessLogic.BLFacade;

public class RegisterGUI extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JTextField cardNumberField;
    private JTextField expirationField;
    private JPasswordField cvvField;
    private JButton registerButton;
    private ResourceBundle resourceBundle;

    public RegisterGUI() {
        this.resourceBundle = ResourceBundle.getBundle("Etiquetas");  // Cargar el ResourceBundle
        setTitle(resourceBundle.getString("RegisterGUI.Title"));
        setSize(400, 400);  // Aumentamos el tamaño para acomodar el nuevo campo
        setLayout(new GridLayout(9, 2, 5, 5));

        add(new JLabel(resourceBundle.getString("RegisterGUI.Name")));
        nameField = new JTextField();
        add(nameField);
        
        add(new JLabel(resourceBundle.getString("RegisterGUI.Email")));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel(resourceBundle.getString("RegisterGUI.Password")));
        passwordField = new JPasswordField();
        add(passwordField);
        
        add(new JLabel(resourceBundle.getString("RegisterGUI.CardNumber")));
        cardNumberField = new JTextField();
        add(cardNumberField);
        
        add(new JLabel(resourceBundle.getString("RegisterGUI.Expiration")));
        expirationField = new JTextField();
        add(expirationField);
        
        add(new JLabel(resourceBundle.getString("RegisterGUI.CVV")));
        cvvField = new JPasswordField();
        add(cvvField);

        // Selección de rol
        add(new JLabel(resourceBundle.getString("RegisterGUI.Role")));
        roleComboBox = new JComboBox<>(new String[]{"Traveler", "Driver"});
        add(roleComboBox);

        // Botón de registro
        registerButton = new JButton(resourceBundle.getString("RegisterGUI.Submit"));
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String cardNumber = cardNumberField.getText().trim();
                String expiration = expirationField.getText().trim();
                String cvv = new String(cvvField.getPassword()).trim();
                String role = (String) roleComboBox.getSelectedItem();

                if (email.isEmpty() || password.isEmpty() || cardNumber.isEmpty() || expiration.isEmpty() || cvv.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterGUI.this, resourceBundle.getString("RegisterGUI.ErrorEmpty"), 
                        resourceBundle.getString("RegisterGUI.Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Llamar a la lógica de negocio para registrar al usuario
                BLFacade facade = MainGUI.getBusinessLogic();
                boolean success = facade.register(email, password, role);
                if (!facade.validateCreditCard(cardNumber, expiration, cvv)) {
                    JOptionPane.showMessageDialog(RegisterGUI.this, 
                        resourceBundle.getString("RegisterGUI.InvalidCard"), 
                        resourceBundle.getString("RegisterGUI.Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Date expirationDate = null;
                try {
                    expirationDate = facade.parseExpirationDate(expiration);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RegisterGUI.this, resourceBundle.getString("RegisterGUI.InvalidDate"), 
                        resourceBundle.getString("RegisterGUI.Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (success) {
                    JOptionPane.showMessageDialog(RegisterGUI.this, resourceBundle.getString("RegisterGUI.Success"), 
                        resourceBundle.getString("RegisterGUI.Success"), JOptionPane.INFORMATION_MESSAGE);
                    dispose();  // Cerrar la ventana de registro
                } else {
                    JOptionPane.showMessageDialog(RegisterGUI.this, resourceBundle.getString("RegisterGUI.Error"), 
                        resourceBundle.getString("RegisterGUI.Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(registerButton);
        
        JButton cancelButton = new JButton(resourceBundle.getString("RegisterGUI.Cancel"));
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);  // Centrar la ventana en la pantalla
    }
}
