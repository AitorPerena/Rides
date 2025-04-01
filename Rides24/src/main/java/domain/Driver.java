package domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Driver extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlIDREF
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private List<Ride> rides = new Vector<Ride>();

    public Driver() {
        super();
    }

    public Driver(String email, String password, String name) {
        super(email, password,name);
    }


    public List<Ride> getRides() {
        return rides;
    }

    public String getName() {
        return name;
    }

    public void setRides(List<Ride> rides) {
        this.rides = rides;
    }

    @Override
    public String toString() {
        return getEmail() + "; " + getName() + "; " + rides;
    }

    /**
     * This method creates a ride for the driver.
     *
     * @param from the origin location of the ride
     * @param to the destination location of the ride
     * @param date the date of the ride
     * @param nPlaces available seats
     * @param price the price of the ride
     * @return the created ride
     */
    public Ride addRide(String from, String to, Date date, int nPlaces, float price) {
        Ride ride = new Ride(from, to, date, nPlaces, price, this);
        rides.add(ride);
        return ride;
    }

    /**
     * This method checks if the ride already exists for the driver.
     *
     * @param from the origin location
     * @param to the destination location
     * @param date the date of the ride
     * @return true if the ride exists, false otherwise
     */
    public boolean doesRideExists(String from, String to, Date date) {
        for (Ride ride : rides) {
            if (ride.getFrom().equals(from) && ride.getTo().equals(to) && ride.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method removes a ride from the driver's list of rides.
     *
     * @param from the origin location
     * @param to the destination location
     * @param date the date of the ride
     * @return the removed ride, or null if not found
     */
    public Ride removeRide(String from, String to, Date date) {
        for (Ride ride : rides) {
            if (ride.getFrom().equals(from) && ride.getTo().equals(to) && ride.getDate().equals(date)) {
                rides.remove(ride);
                return ride;
            }
        }
        return null;
    }
  

}