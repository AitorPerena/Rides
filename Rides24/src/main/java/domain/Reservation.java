package domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlIDREF;
import java.util.List;

@Entity
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Integer reservationNumber;

    // Campos para reservas de segmentos
    private Integer startStopIndex; // Índice de parada inicial (null si es reserva completa)
    private Integer endStopIndex;   // Índice de parada final (null si es reserva completa)
    private Double segmentPrice;    // Precio calculado para el segmento

    @ManyToOne
    @XmlIDREF
    private Traveler traveler;

    @ManyToOne
    @XmlIDREF
    private Ride ride;

    private int seats;
    private String status; // "Pending", "Confirmed", "Rejected"

    public Reservation() {
        super();
        this.status = "Pending"; // Valor por defecto
    }

    public Reservation(Traveler traveler, Ride ride, int seats, String status) {
        this();
        this.traveler = traveler;
        this.ride = ride;
        this.seats = seats;
        this.status = status;
    }

    // Método para verificar si es reserva de segmento
    public boolean isSegmentReservation() {
        return startStopIndex != null && endStopIndex != null;
    }

    // Método para obtener descripción del segmento
    public String getSegmentDescription() {
        if (!isSegmentReservation()) {
            return ride.getFrom() + " → " + ride.getTo();
        }
        
        List<String> allStops = ride.getAllStops();
        return allStops.get(startStopIndex) + " → " + allStops.get(endStopIndex);
    }

    // Getters y setters
    public Integer getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(Integer reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public Integer getStartStopIndex() {
        return startStopIndex;
    }

    public void setStartStopIndex(Integer startStopIndex) {
        this.startStopIndex = startStopIndex;
    }

    public Integer getEndStopIndex() {
        return endStopIndex;
    }

    public void setEndStopIndex(Integer endStopIndex) {
        this.endStopIndex = endStopIndex;
    }

    public Double getSegmentPrice() {
        return segmentPrice;
    }

    public void setSegmentPrice(Double segmentPrice) {
        this.segmentPrice = segmentPrice;
    }

    public Traveler getTraveler() {
        return traveler;
    }

    public void setTraveler(Traveler traveler) {
        this.traveler = traveler;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        if (isSegmentReservation()) {
            return "Reserva #" + reservationNumber + " - Segmento: " + getSegmentDescription() + 
                   " - " + seats + " asientos - Estado: " + status + 
                   (segmentPrice != null ? " - Precio: " + segmentPrice + "€" : "");
        } else {
            return "Reserva #" + reservationNumber + " - " + ride.getFrom() + " → " + ride.getTo() + 
                   " - " + seats + " asientos - Estado: " + status + 
                   " - Precio: " + ride.getPrice() + "€";
        }
    }
}