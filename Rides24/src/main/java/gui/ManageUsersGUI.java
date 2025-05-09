package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import businessLogic.BLFacade;
import domain.Admin;
import domain.Driver;
import domain.User;

public class ManageUsersGUI extends JFrame {
    private JTable usersTable;
    private DefaultTableModel tableModel;  // Añadido para almacenar el modelo
    private JButton banButton;
    private JButton unbanButton;
    private JButton deleteButton;
    private JButton viewProfileButton;
    private JButton viewReportsButton;

    public ManageUsersGUI() {
        setTitle("Gestión de Usuarios");
        setSize(800, 500);
        setLayout(new BorderLayout());

        BLFacade facade = MainGUI.getBusinessLogic();
        List<User> users = facade.getAllUsers();

        String[] columnNames = {"Email", "Rol", "Estado", "Valoración"};
        tableModel = new DefaultTableModel(columnNames, 0);  // Modelo con columnas y sin datos

        for (User user : users) {
            tableModel.addRow(new Object[]{
                user.getEmail(),
                user instanceof Admin ? "Admin" :
                user instanceof Driver ? "Driver" : "Traveler",
                user.isCurrentlyBanned() ? "Baneado (" + user.getBanRemainingTime() + ")" : "Activo",
                String.format("%.1f", facade.getAverageRating(user.getEmail()))
            });
        }

        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());

        viewProfileButton = new JButton("Ver Perfil");
        viewProfileButton.addActionListener(this::viewProfile);
        buttonPanel.add(viewProfileButton);

        banButton = new JButton("Banear Usuario");
        banButton.addActionListener(this::banUser);
        buttonPanel.add(banButton);

        unbanButton = new JButton("Quitar Ban");
        unbanButton.addActionListener(this::unbanUser);
        buttonPanel.add(unbanButton);

        deleteButton = new JButton("Eliminar Usuario");
        deleteButton.addActionListener(this::deleteUser);
        buttonPanel.add(deleteButton);

        viewReportsButton = new JButton("Ver Reportes");
        viewReportsButton.addActionListener(this::viewReports);
        buttonPanel.add(viewReportsButton);

        add(new JScrollPane(usersTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private User getSelectedUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario primero");
            return null;
        }

        String email = (String) tableModel.getValueAt(selectedRow, 0);
        BLFacade facade = MainGUI.getBusinessLogic();
        return facade.getAllUsers().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    private void viewProfile(ActionEvent e) {
        User selectedUser = getSelectedUser();
        if (selectedUser != null) {
            new ProfileGUI(selectedUser, MainGUI.getLoggedInUser()).setVisible(true);
        }
    }

    private void banUser(ActionEvent e) {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return;

        String daysStr = JOptionPane.showInputDialog(this,
                "Introduce el número de días de ban (0 para ban permanente):",
                "Banear Usuario",
                JOptionPane.QUESTION_MESSAGE);

        if (daysStr != null && !daysStr.isEmpty()) {
            try {
                int days = Integer.parseInt(daysStr);
                BLFacade facade = MainGUI.getBusinessLogic();

                if (days <= 0) {
                    selectedUser.banUser(36500);
                } else {
                    selectedUser.banUser(days);
                }

                // Actualizar tabla
                tableModel.setValueAt(
                        selectedUser.isCurrentlyBanned() ?
                                "Baneado (" + selectedUser.getBanRemainingTime() + ")" : "Activo",
                        usersTable.getSelectedRow(),
                        2
                );

                JOptionPane.showMessageDialog(this, "Usuario baneado con éxito");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Introduce un número válido", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void unbanUser(ActionEvent e) {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return;

        selectedUser.unbanUser();

        tableModel.setValueAt("Activo", usersTable.getSelectedRow(), 2);
        JOptionPane.showMessageDialog(this, "Ban eliminado con éxito");
    }

    private void deleteUser(ActionEvent e) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un usuario primero",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = (String) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar permanentemente a " + email + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            BLFacade facade = MainGUI.getBusinessLogic();
            try {
                boolean success = facade.deleteUser(email);

                if (success) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this,
                            "Usuario eliminado con éxito",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al eliminar el usuario",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al conectar con la base de datos: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void viewReports(ActionEvent e) {
        User selectedUser = getSelectedUser();
        if (selectedUser != null) {
            new ViewUserReportsGUI(selectedUser).setVisible(true);
        }
    }
}
