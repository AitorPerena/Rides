package gui;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import businessLogic.BLFacade;
import domain.Driver;
import domain.Reservation;

public class ViewReservationsGUI extends JFrame {
    private JTable jTableReservations;
    private Driver driver;

    public ViewReservationsGUI(Driver driver) {
        this.driver = driver;
        setTitle("Solicitudes de Reservas");
        setSize(500, 300);

        // Columnas de la tabla
        String[] columnNames = {"Viajero", "Origen", "Destino", "Fecha", "Asientos", "Estado"};

        // Obtener las reservas del conductor
        BLFacade facade = MainGUI.getBusinessLogic();
        List<Reservation> reservations = facade.getReservations(driver);

        // Crear los datos para la tabla
        Object[][] data = new Object[reservations.size()][6]; // 6 columnas
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); // Formato de fecha

        for (int i = 0; i < reservations.size(); i++) {
            Reservation reservation = reservations.get(i);
            data[i][0] = reservation.getTraveler().getEmail(); // email
            data[i][1] = reservation.getRide().getFrom();     // Origen del viaje
            data[i][2] = reservation.getRide().getTo();       // Destino del viaje
            data[i][3] = dateFormat.format(reservation.getRide().getDate()); // Fecha formateada
            data[i][4] = reservation.getSeats();              // Número de asientos
            data[i][5] = reservation.getStatus();             // Estado de la reserva
        }

        // Crear la tabla con los datos
        jTableReservations = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(jTableReservations);
        add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}