package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import businessLogic.BLFacade;
import domain.User;

public class ReportUserGUI extends JFrame {
    private JComboBox<User> userComboBox;
    private JTextArea reportDescription;
    private JButton submitButton;
    private User reporter;
    
    public ReportUserGUI(User reporter) {
        this.reporter = reporter;
        setTitle("Reportar Usuario");
        setSize(500, 300);
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior - Selección de usuario
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.add(new JLabel("Usuario a reportar:"));
        
        BLFacade facade = MainGUI.getBusinessLogic();
        userComboBox = new JComboBox<>();
        for (User user : facade.getAllUsers()) {
            if (!user.getEmail().equals(reporter.getEmail())) {
                userComboBox.addItem(user);
            }
        }
        userPanel.add(userComboBox);
        mainPanel.add(userPanel, BorderLayout.NORTH);
        
        // Panel central - Descripción del reporte
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Descripción del problema:"), BorderLayout.NORTH);
        
        reportDescription = new JTextArea();
        reportDescription.setLineWrap(true);
        reportDescription.setWrapStyleWord(true);
        descPanel.add(new JScrollPane(reportDescription), BorderLayout.CENTER);
        mainPanel.add(descPanel, BorderLayout.CENTER);
        
        // Panel inferior - Botón de enviar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitButton = new JButton("Enviar Reporte");
        submitButton.addActionListener(this::submitReport);
        buttonPanel.add(submitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void submitReport(ActionEvent e) {
        User reportedUser = (User) userComboBox.getSelectedItem();
        String description = reportDescription.getText().trim();
        
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor describe el problema", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        BLFacade facade = MainGUI.getBusinessLogic();
        boolean success = facade.addReport(reporter, reportedUser, description);
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Reporte enviado con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error al enviar el reporte", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}