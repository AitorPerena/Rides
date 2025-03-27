package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import businessLogic.BLFacade;
import domain.Driver;
import domain.Reservation;

public class ViewReservationsGUI extends JFrame {
    private JTable tableReservas;
    private JButton btnConfirmar;
    private JButton btnRechazar;
    private Driver driver;
    private DefaultTableModel model; // Movemos el modelo como atributo de clase

    public ViewReservationsGUI(Driver driver) {
        this.driver = driver;
        setTitle("Solicitudes de Reserva - Conductor: " + driver.getEmail());
        setSize(800, 400);
        setLayout(new BorderLayout());

        // Inicializamos el modelo como atributo
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        model.setColumnIdentifiers(new String[]{
            "ID Reserva", "Viajero", "Ruta", "Fecha", "Asientos", "Estado"
        });

        tableReservas = new JTable(model);
        
        // Botón de confirmación
        btnConfirmar = new JButton("Confirmar Reserva");
        btnConfirmar.addActionListener(e -> confirmarReserva());
        
        // Botón de rechazo
        btnRechazar = new JButton("Rechazar Reserva");
        btnRechazar.addActionListener(e -> rechazarReserva());

        // Panel inferior
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnConfirmar);
        panelBotones.add(btnRechazar);

        add(new JScrollPane(tableReservas), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        // Llamamos a actualizarTabla después de inicializar todos los componentes
        actualizarTabla();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void actualizarTabla() {
        model.setRowCount(0); // Limpiar la tabla
        
        BLFacade facade = MainGUI.getBusinessLogic();
        List<Reservation> reservas = facade.getReservations(driver);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (Reservation r : reservas) {
            model.addRow(new Object[]{
                r.getReservationNumber(),
                r.getTraveler().getEmail(),
                r.getRide().getFrom() + " → " + r.getRide().getTo(),
                sdf.format(r.getRide().getDate()),
                r.getSeats(),
                r.getStatus()
            });
        }
    }

    private void confirmarReserva() {
        manejarReserva("Confirmed", "confirmar", "Confirmación");
    }

    private void rechazarReserva() {
        manejarReserva("Rejected", "rechazar", "Rechazo");
    }

    private void manejarReserva(String estado, String accion, String titulo) {
        int filaSeleccionada = tableReservas.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Selecciona una reserva primero", 
                "Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String estadoActual = (String) model.getValueAt(filaSeleccionada, 5);
        
        if (!"Pending".equals(estadoActual)) {
            JOptionPane.showMessageDialog(this,
                "Solo puedes " + accion + " reservas pendientes",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        BLFacade facade = MainGUI.getBusinessLogic();
        List<Reservation> reservas = facade.getReservations(driver);
        Reservation reserva = reservas.get(filaSeleccionada);

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿" + titulo + " reserva de " + reserva.getTraveler().getEmail() + 
            " para el viaje " + reserva.getRide().getFrom() + " → " + reserva.getRide().getTo() + "?",
            titulo + " de Reserva",
            JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean exito = facade.confirmarReserva(reserva, estado);
            if (exito) {
                model.setValueAt(estado, filaSeleccionada, 5);
                JOptionPane.showMessageDialog(this, 
                    "Reserva " + (estado.equals("Confirmed") ? "confirmada" : "rechazada") + " con éxito",
                    titulo + " exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla(); // Actualizar la tabla completa
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error al " + accion + " la reserva", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}