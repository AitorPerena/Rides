package gui;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javax.swing.*;
import businessLogic.BLFacade;
import domain.Ride;
import domain.Traveler;

public class RequestRideGUI extends JFrame {
    private JComboBox<String> jComboBoxRides; 
    private JButton jButtonRequest;
    private JSpinner jSpinnerSeats;
    private Traveler traveler;
    private ResourceBundle bundle;
    private SimpleDateFormat dateFormat;

    public RequestRideGUI(Traveler traveler) {
        this.traveler = traveler;
        this.bundle = ResourceBundle.getBundle("Etiquetas");
        this.dateFormat = new SimpleDateFormat(bundle.getString("Date.Format"));
        
        setTitle(bundle.getString("RequestRideGUI.Title"));
        setSize(400, 200);
        setLayout(new GridLayout(4, 2));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeUI();
    }

    private void initializeUI() {
        // Lista de viajes disponibles
        add(new JLabel(bundle.getString("RequestRideGUI.SelectRide")));
        jComboBoxRides = new JComboBox<>();
        BLFacade facade = MainGUI.getBusinessLogic();
        
        for (Ride ride : facade.getAvailableRides()) {
            String rideInfo = String.format("%s - %s (%s)", 
                ride.getFrom(), 
                ride.getTo(), 
                dateFormat.format(ride.getDate()));
            jComboBoxRides.addItem(rideInfo);
        }
        add(jComboBoxRides);

        // Selección de número de asientos
        add(new JLabel(bundle.getString("RequestRideGUI.Seats")));
        jSpinnerSeats = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        add(jSpinnerSeats);

        // Botón de solicitud
        jButtonRequest = new JButton(bundle.getString("RequestRideGUI.Submit"));
        jButtonRequest.addActionListener(e -> requestReservation());
        add(jButtonRequest);
    }

    private void requestReservation() {
        BLFacade facade = MainGUI.getBusinessLogic();
        int selectedRideIndex = jComboBoxRides.getSelectedIndex();

        if (selectedRideIndex >= 0) {
            Ride selectedRide = facade.getAvailableRides().get(selectedRideIndex);
            int seats = (int) jSpinnerSeats.getValue(); 

            boolean success = facade.requestReservation(selectedRide, traveler, seats);
            if (success) {
                facade.sendRideReminders(selectedRide.getDate());
                JOptionPane.showMessageDialog(this, 
                    bundle.getString("RequestRideGUI.Success"),
                    bundle.getString("Success"), 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    bundle.getString("RequestRideGUI.Error"),
                    bundle.getString("Error"), 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                bundle.getString("RequestRideGUI.InvalidSelection"),
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}