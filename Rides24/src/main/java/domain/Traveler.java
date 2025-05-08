package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlIDREF;

@Entity
public class Traveler extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlIDREF
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private List<Reservation> reservations = new ArrayList<>();

    public Traveler() {
        super();
    }

    public Traveler(String email, String password) {
        super(email, password);
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

    @Override
    public String toString() {
        return super.toString() + "; " + reservations;
    }
}