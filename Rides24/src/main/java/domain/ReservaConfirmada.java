package domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity
public class ReservaConfirmada implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Traveler traveler;

    @ManyToOne
    private Ride ride;

    private Date fechaConfirmacion;
    private String estado; 

    public ReservaConfirmada() {
        super();
    }

    public ReservaConfirmada(Traveler traveler, Ride ride, Date fechaConfirmacion, String estado) {
        this.traveler = traveler;
        this.ride = ride;
        this.fechaConfirmacion = fechaConfirmacion;
        this.estado = estado;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(Date fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "ReservaConfirmada [id=" + id + ", traveler=" + traveler.getEmail() + ", ride=" + ride.getRideNumber() + ", fechaConfirmacion=" + fechaConfirmacion + ", estado=" + estado + "]";
    }
}