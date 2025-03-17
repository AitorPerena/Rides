package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import businessLogic.BLFacade;

public class RegisterGUI extends JFrame {
	private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;

    public RegisterGUI() {
        setTitle("Registro de Usuario");
        setSize(300, 250);  // Aumentamos el tamaño para acomodar el nuevo campo
        setLayout(new GridLayout(5, 2));  // Ajustamos el layout para 5 filas

        add(new JLabel("Nombre"));
        nameField = new JTextField();
        add(nameField);
        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        add(passwordField);

        // Selección de rol
        add(new JLabel("Rol:"));
        roleComboBox = new JComboBox<>(new String[]{"Traveler", "Driver"});
        add(roleComboBox);

        // Botón de registro
        registerButton = new JButton("Registrarse");
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String name = nameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();

                if (email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterGUI.this, "Email y contraseña son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Llamar a la lógica de negocio para registrar al usuario
                BLFacade facade = MainGUI.getBusinessLogic();
                boolean success = facade.register(email, password, role);

                if (success) {
                    JOptionPane.showMessageDialog(RegisterGUI.this, "Registro exitoso", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();  // Cerrar la ventana de registro
                } else {
                    JOptionPane.showMessageDialog(RegisterGUI.this, "Error en el registro", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(registerButton);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);  // Centrar la ventana en la pantalla
    }
}