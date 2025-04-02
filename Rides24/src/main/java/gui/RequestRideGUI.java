package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import businessLogic.BLFacade;
import domain.Ride;
import domain.Traveler;

public class RequestRideGUI extends JFrame {
    private JComboBox<String> jComboBoxRides; 
    private JButton jButtonRequest;
    private JSpinner jSpinnerSeats;
    private Traveler traveler;
    private ResourceBundle bundle;

    public RequestRideGUI(Traveler traveler) {
        this.bundle = ResourceBundle.getBundle("Etiquetas");
        this.traveler = traveler;
        setTitle(bundle.getString("RequestRideGUI.Title"));
        setSize(400, 200); // Tamaño ajustado
        setLayout(new GridLayout(4, 2)); 

        // Lista de viajes disponibles
        add(new JLabel(bundle.getString("RequestRideGUI.Seats")));
        jComboBoxRides = new JComboBox<>();
        BLFacade facade = MainGUI.getBusinessLogic();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); 

        
        for (Ride ride : facade.getAvailableRides()) {
            String rideInfo = ride.getFrom() + " - " + ride.getTo() + " (" + dateFormat.format(ride.getDate()) + ")";
            jComboBoxRides.addItem(rideInfo);
        }
        add(jComboBoxRides);

        // Selección de número de asientos
        add(new JLabel(bundle.getString("RequestRideGUI.Seats")));
        jSpinnerSeats = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1)); 
        add(jSpinnerSeats);

  
        jButtonRequest = new JButton(bundle.getString("RequestRideGUI.Submit"));
        jButtonRequest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                requestReservation();
            }
        });
        add(jButtonRequest);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }


    private void requestReservation() {
        BLFacade facade = MainGUI.getBusinessLogic();
        int selectedRideIndex = jComboBoxRides.getSelectedIndex();

        if (selectedRideIndex >= 0) {
            Ride selectedRide = facade.getAvailableRides().get(selectedRideIndex);
            int seats = (int) jSpinnerSeats.getValue(); 

            boolean success = facade.requestReservation(selectedRide, traveler, seats);
            if (success) {
                JOptionPane.showMessageDialog(this, bundle.getString("RequestRideGUI.Success"));
            } else {
                JOptionPane.showMessageDialog(this, bundle.getString("RequestRideGUI.Error"));
            }
        } else {
            JOptionPane.showMessageDialog(this, bundle.getString("RequestRideGUI.InvalidSelection"));
        }
    }
}