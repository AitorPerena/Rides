package domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlIDREF;

@Entity
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Integer reservationNumber;

    @ManyToOne
    @XmlIDREF
    private Traveler traveler; 

    @ManyToOne
    @XmlIDREF
    private Ride ride; 

    private int seats;  
    private String status; 
    private int startStopIndex;
    private int endStopIndex;
    private double segmentPrice;

    public Reservation() {
        super();
    }

    public Reservation(Traveler traveler, Ride ride, int seats, String status) {
        this.traveler = traveler;
        this.ride = ride;
        this.seats = seats;
        this.status = status;
    }

    // Getters y Setters
    public int getStartStopIndex() {
        return startStopIndex;
    }
    
    public void setStartStopIndex(int startStopIndex) {
        this.startStopIndex = startStopIndex;
    }
    
    public int getEndStopIndex() {
        return endStopIndex;
    }
    
    public void setEndStopIndex(int endStopIndex) {
        this.endStopIndex = endStopIndex;
    }
    
    public double getSegmentPrice() {
        return segmentPrice;
    }
    
    public void setSegmentPrice(double segmentPrice) {
        this.segmentPrice = segmentPrice;
    }
    public Integer getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(Integer reservationNumber) {
        this.reservationNumber = reservationNumber;
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
        return reservationNumber + "; " + traveler.getEmail() + "; " + ride.getRideNumber() + "; " + seats + "; " + status;
    }
}