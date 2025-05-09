package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import businessLogic.BLFacade;
import domain.Admin;
import domain.Report;
import domain.User;

public class ViewUserReportsGUI extends JFrame {
    private JTable reportsTable;
    private JButton respondButton;
    
    public ViewUserReportsGUI(User targetUser) {
        setTitle(targetUser == null ? "Todos los Reportes" : "Reportes de " + targetUser.getEmail());
        setSize(800, 500);
        setLayout(new BorderLayout());
        
        BLFacade facade = MainGUI.getBusinessLogic();
        List<Report> reports = targetUser == null ? 
            facade.getAllReports() : facade.getUserReports(targetUser.getEmail());
        
        if (reports == null || reports.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                targetUser == null ? "No hay reportes en el sistema" : "El usuario no tiene reportes",
                "Información", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }
        
        String[] columnNames = {"ID", "Reportado por", "Usuario reportado", "Fecha", "Descripción", "Estado", "Respuesta"};
        Object[][] data = new Object[reports.size()][7];
        
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            data[i][0] = report.getId();
            data[i][1] = report.getReporter().getEmail();
            data[i][2] = report.getReportedUser().getEmail();
            data[i][3] = report.getReportDate();
            data[i][4] = report.getDescription();
            data[i][5] = report.isResolved() ? "Resuelto" : "Pendiente";
            data[i][6] = report.getAdminResponse() != null ? report.getAdminResponse() : "N/A";
        }
        
        reportsTable = new JTable(data, columnNames);
        reportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Solo admin puede responder
        if (MainGUI.getLoggedInUser() instanceof Admin) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            respondButton = new JButton("Responder Reporte");
            respondButton.addActionListener(e -> respondToReport(reports));
            buttonPanel.add(respondButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        add(new JScrollPane(reportsTable), BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void respondToReport(List<Report> reports) {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un reporte primero");
            return;
        }
        
        Long reportId = (Long) reportsTable.getValueAt(selectedRow, 0);
        Report selectedReport = reports.stream()
            .filter(r -> r.getId().equals(reportId))
            .findFirst()
            .orElse(null);
        
        if (selectedReport == null) {
            JOptionPane.showMessageDialog(this, "Error al obtener el reporte", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String response = JOptionPane.showInputDialog(this, 
            "Introduce tu respuesta al reporte:", 
            "Responder Reporte", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (response != null && !response.trim().isEmpty()) {
            BLFacade facade = MainGUI.getBusinessLogic();
            boolean success = facade.respondToReport(selectedReport, response, (Admin) MainGUI.getLoggedInUser());
            
            if (success) {
                reportsTable.setValueAt("Resuelto", selectedRow, 5);
                reportsTable.setValueAt(response, selectedRow, 6);
                JOptionPane.showMessageDialog(this, "Respuesta enviada con éxito");
            } else {
                JOptionPane.showMessageDialog(this, "Error al responder al reporte", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}