package gui;

import javax.swing.*;
import java.awt.*;
import domain.User;
import domain.Admin;
import domain.Driver;
import businessLogic.BLFacade;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ProfileGUI extends JFrame {
    private User displayedUser;
    private User viewerUser;
    private BLFacade facade;

    public ProfileGUI(User displayedUser, User viewerUser) {
        this.displayedUser = displayedUser;
        this.viewerUser = viewerUser;
        this.facade = MainGUI.getBusinessLogic();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Perfil de " + displayedUser.getEmail());
        setSize(700, 600);
        setLayout(new BorderLayout());

        // Panel superior con información básica
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Foto de perfil con manejo seguro
        ImageIcon profileIcon = loadImageSafe(displayedUser.getProfileImagePath(), "/images/default_profile.png");
        JLabel profileImage = new JLabel(new ImageIcon(profileIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
        infoPanel.add(profileImage);

        // Información básica
        JPanel userInfo = new JPanel(new GridLayout(4, 1));
        userInfo.add(new JLabel("Email: " + displayedUser.getEmail()));
        double averageRating = facade.getAverageRating(displayedUser.getEmail());
        userInfo.add(new JLabel("Valoración promedio: " + String.format("%.1f", averageRating) + " ★"));
        
        if (displayedUser instanceof Driver) {
            Driver driver = (Driver) displayedUser;
            ImageIcon vehicleIcon = loadImageSafe(driver.getVehicleImagePath(), "/images/default_vehicle.png");
            JLabel vehicleImage = new JLabel(new ImageIcon(vehicleIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
            infoPanel.add(vehicleImage);
            userInfo.add(new JLabel("Rol: Conductor"));
        } else {
            userInfo.add(new JLabel("Rol: Viajero"));
        }
        
        infoPanel.add(userInfo);
        add(infoPanel, BorderLayout.NORTH);

        // Tabla de reseñas
        BLFacade facade = MainGUI.getBusinessLogic();
        List<Object[]> reviews = facade.getUserReviews(displayedUser.getEmail());
        
        String[] columnNames = {"Usuario", "Valoración", "Comentario", "Fecha"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        for (Object[] review : reviews) {
            model.addRow(review);
        }
        
        JTable reviewsTable = new JTable(model);
        reviewsTable.getColumnModel().getColumn(1).setCellRenderer(new StarRatingRenderer());
        add(new JScrollPane(reviewsTable), BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        // Solo mostrar botón de reporte si el usuario visualizado no es el mismo que el que está viendo
        if (!displayedUser.equals(viewerUser) && !(viewerUser instanceof Admin)) {
            JButton reportButton = new JButton("Reportar Usuario");
            reportButton.addActionListener(e -> new ReportUserGUI(viewerUser).setVisible(true));
            buttonPanel.add(reportButton);
        }
        
        add(buttonPanel, BorderLayout.SOUTH);
        
    }

    private ImageIcon loadImageSafe(String imagePath, String defaultImagePath) {
        try {
            // First try to load the specified image
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                ImageIcon icon = new ImageIcon(getClass().getResource("/images/" + imagePath));
                if (icon.getImage() != null) {
                    return icon;
                }
            }
            // Fall back to default image if specified image fails to load
            return new ImageIcon(getClass().getResource(defaultImagePath));
        } catch (Exception e) {
            // If everything fails, return empty icon
            return new ImageIcon(); // Returns a blank icon
        }
    }

    // Renderer para mostrar estrellas en lugar de números
    private class StarRatingRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof Number) {
                int rating = ((Number) value).intValue();
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < rating; i++) stars.append("★");
                for (int i = rating; i < 5; i++) stars.append("☆");
                setText(stars.toString());
            }
            return this;
        }
    }

}