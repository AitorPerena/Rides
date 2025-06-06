package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Ride implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlID
    @Id
    @XmlJavaTypeAdapter(IntegerAdapter.class)
    @GeneratedValue
    private Integer rideNumber;

    private String from;
    private String to;
    private int nPlaces;
    private Date date;
    private float price;
    private boolean isMultiStop = false; // Nuevo: indica si es viaje compuesto

    // Nuevos campos para viajes compuestos
    @ElementCollection
    @OrderColumn(name="stop_order")
    private List<String> intermediateStops = new ArrayList<>(); // Paradas intermedias

    @ElementCollection
    @OrderColumn(name="price_order")
    private List<Double> segmentPrices = new ArrayList<>(); // Precios entre paradas (€)

    @ManyToOne
    @XmlIDREF
    private Driver driver;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private List<Reservation> reservations = new ArrayList<>();

    public Ride() {
        super();
    }

    public Ride(String from, String to, Date date, int nPlaces, float price, Driver driver) {
        this.from = from;
        this.to = to;
        this.date = date;
        this.nPlaces = nPlaces;
        this.price = price;
        this.driver = driver;
    }

    // Métodos para gestión de paradas intermedias
    public void addIntermediateStop(String location, double priceToNextStop) {
        if (!isMultiStop && !intermediateStops.isEmpty()) {
            isMultiStop = true;
        }
        intermediateStops.add(location);
        segmentPrices.add(priceToNextStop);
    }

    public List<String> getAllStops() {
        List<String> allStops = new ArrayList<>();
        allStops.add(from);
        allStops.addAll(intermediateStops);
        allStops.add(to);
        return allStops;
    }

    public double calculateSegmentPrice(int startStopIndex, int endStopIndex) {
        if (!isMultiStop) return price; // Viaje simple
        
        double total = 0.0;
        for (int i = startStopIndex; i < endStopIndex; i++) {
            total += segmentPrices.get(i);
        }
        return total;
    }
        

        public boolean hasAvailableSeatsForSegment(int startIdx, int endIdx) {
            return hasAvailableSeatsForSegment(startIdx, endIdx, 1);
        }
        
        public boolean hasAvailableSeatsForSegment(int startIdx, int endIdx, int requiredSeats) {
            List<String> stops = getAllStops();
            if (startIdx < 0 || endIdx >= stops.size() || startIdx >= endIdx) {
                throw new IllegalArgumentException("Índices de parada inválidos");
            }
            
            // Verificar capacidad total del viaje
            if (nPlaces < requiredSeats) {
                return false;
            }
            
            // Verificar disponibilidad en cada segmento afectado
            for (Reservation reservation : reservations) {
                if (reservationsOverlap(reservation, startIdx, endIdx)) {
                    if (reservation.getSeats() + requiredSeats > nPlaces) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        private boolean reservationsOverlap(Reservation reservation, int startIdx, int endIdx) {
            return (reservation.getStartStopIndex() < endIdx && 
                    reservation.getEndStopIndex() > startIdx);
        }
        
        public void reduceSeatsForSegment(int startIdx, int endIdx, int seats) {
            if (!hasAvailableSeatsForSegment(startIdx, endIdx, seats)) {
                throw new IllegalStateException("No hay suficientes asientos disponibles");
            }
            // En un sistema real, aquí registraríamos la reducción de asientos
            // para el segmento específico
        }
        

    // Métodos existentes
    public Integer getRideNumber() {
        return rideNumber;
    }

    public void setRideNumber(Integer rideNumber) {
        this.rideNumber = rideNumber;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getnPlaces() {
        return nPlaces;
    }

    public void setnPlaces(int nPlaces) {
        this.nPlaces = nPlaces;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

    public void reducirAsientos(int asientos) {
        if (this.nPlaces >= asientos) {
            this.nPlaces -= asientos;
        } else {
            throw new IllegalArgumentException("No hay suficientes asientos disponibles.");
        }
    }

    // Nuevos getters y setters
    public boolean isMultiStop() {
        return isMultiStop;
    }

    public List<String> getIntermediateStops() {
        return intermediateStops;
    }

    public List<Double> getSegmentPrices() {
        return segmentPrices;
    }

    @Override
    public String toString() {
        if (!isMultiStop) {
            return rideNumber + "; " + from + " → " + to + "; " + date + "; " + nPlaces + " asientos; " + price + "€";
        } else {
            return rideNumber + "; " + from + " → " + String.join(" → ", intermediateStops) + " → " + to + 
                   "; " + date + "; " + nPlaces + " asientos; " + price + "€";
        }
    }
}