package businessLogic;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.jws.WebMethod;
import javax.jws.WebService;
import configuration.ConfigXML;
import configuration.UtilDate;
import dataAccess.DataAccess;
import domain.*;
import exceptions.RideMustBeLaterThanTodayException;
import exceptions.RideAlreadyExistException;

@WebService(endpointInterface = "businessLogic.BLFacade")
public class BLFacadeImplementation implements BLFacade {
    DataAccess dbManager;
    
    public BLFacadeImplementation() {
        System.out.println("Creating BLFacadeImplementation instance");
        dbManager = new DataAccess();
    }
    
    public BLFacadeImplementation(DataAccess da) {
        System.out.println("Creating BLFacadeImplementation instance with DataAccess parameter");
        dbManager = da;
    }

    // ==================== MÉTODOS PARA VIAJES COMPUESTOS ====================

    @Override
    public Ride createMultiStopRide(String driverEmail, List<String> stops, List<Double> distances,  Date date, int totalSeats, float totalPrice)  {
        
        dbManager.open();
        try {
            if (stops.size() < 2 || distances.size() != stops.size() - 1) {
                throw new IllegalArgumentException("Número de paradas o distancias no válido");
            }

            
            Ride ride = dbManager.createMultiStopRide(driverEmail, stops, distances, date, totalSeats, totalPrice);
            
            // Notificar al conductor
            String message = "Has creado un nuevo viaje con " + (stops.size()-2) + " paradas intermedias";
            dbManager.addNotification(ride.getDriver(), message);
            
            return ride;
        } finally {
            dbManager.close();
        }
    }

    @Override
    public Reservation reserveRideSegment(Integer rideId, String travelerEmail,  int startStopIndex, int endStopIndex, int seats) {
        dbManager.open();
        try {
            Reservation reservation = dbManager.reserveSegment(rideId, travelerEmail, startStopIndex, endStopIndex, seats);
            
            // Notificar al conductor
            String message = String.format("Nueva reserva de segmento: %d asientos de %s a %s",
                seats, 
                getStopName(rideId, startStopIndex),
                getStopName(rideId, endStopIndex));
                
            dbManager.addNotification(reservation.getRide().getDriver(), message);
            
            return reservation;
        } finally {
            dbManager.close();
        }
    }

    @Override
    public List<Ride> findRidesBySegment(String from, String to, Date date) {
        dbManager.open();
        try {
            return dbManager.findRidesWithSegment(from, to, date);
        } finally {
            dbManager.close();
        }
    }
    
    

    @Override
    public List<String> getRideStops(Integer rideId) {
        dbManager.open();
        try {
            return dbManager.getRideStops(rideId);
        } finally {
            dbManager.close();
        }
    }

    @Override
    public double calculateSegmentPrice(Integer rideId, int startIndex, int endIndex) {
        dbManager.open();
        try {
            Ride ride = findRideById(rideId);
            if (ride == null) {
                throw new IllegalArgumentException("Viaje no encontrado");
            }
            return ride.calculateSegmentPrice(startIndex, endIndex);
        } finally {
            dbManager.close();
        }
    }

    // ==================== MÉTODOS EXISTENTES ====================

    @Override
    public List<Ride> getAvailableRides() {
        dbManager.open();
        List<Ride> availableRides = dbManager.getAvailableRides(); 
        dbManager.close();
        return availableRides;
    }

    @Override
    public User login(String email, String password) {
        dbManager.open();
        User user = dbManager.getUserByEmail(email); 
        dbManager.close();
        if (user != null && user.getPassword().equals(password)) {
            return user;  
        }
        return null;
    }

    @Override
    public boolean register(String email, String password, String name, String role) {
        dbManager.open();
        boolean success = dbManager.addUser(email, password, role);
        dbManager.close();
        return success;
    }
    
    @Override
    public boolean userExists(String email) {
        dbManager.open();
        try {
            User user = dbManager.getUserByEmail(email);
            return user != null;
        } finally {
            dbManager.close();
        }
    }

    @Override
    public Date parseExpirationDate(String expiration) throws ParseException {
        dbManager.open();
        Date validExpiration = dbManager.parseAndValidateExpirationDate(expiration);
        dbManager.close();
        return validExpiration;
    }
    
    @Override
    public boolean validateCreditCard(String cardNumber, String expiration, String cvv) {
        dbManager.open();
        try {
            return dbManager.validateCardData(cardNumber, expiration, cvv);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }

    @Override
    public void sendRideReminders(Date rideDate) {
        dbManager.open();
        try {
            List<Ride> rides = dbManager.getRidesBetweenDates(
                UtilDate.trim(rideDate),
                UtilDate.addDays(rideDate, 1)
            );

            for (Ride ride : rides) {
                String driverMsg = String.format(
                    "Recordatorio: Tienes un viaje programado para %1$te de %1$tB",
                    ride.getDate()
                );
                dbManager.addNotification(ride.getDriver(), driverMsg);

                for (Reservation res : ride.getReservations()) {
                    String travelerMsg = String.format(
                        "Recordatorio: Viaje a %s el %1$te/%1$tm",
                        ride.getTo(),
                        ride.getFrom()
                    );
                    dbManager.addNotification(res.getTraveler(), travelerMsg);
                }
            }
        } finally {
            dbManager.close();
        }
    }
    
    @Override
    public boolean requestReservation(Ride ride, Traveler traveler, int seats) {
        dbManager.open();
        try {
            boolean success = dbManager.addReservation(traveler, ride, seats, "Pending");
            if (success) {
                String message = String.format("%s ha enviado una solicitud de reserva.", traveler);
                dbManager.addNotification(ride.getDriver(), message);
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }

    @Override
    public List<Reservation> getReservations(Driver driver) {
        dbManager.open();
        List<Reservation> reservations = dbManager.getReservationsByDriver(driver);
        dbManager.close();
        return reservations;
    }
    
    @Override
    public boolean confirmarReserva(Reservation reserva, String estado) {
        dbManager.open();
        try {
            boolean success = dbManager.confirmarReserva(reserva, estado);
            if ("Confirmed".equals(estado)) {
                String message = String.format("%s ha aceptado tu solicitud de reserva.", reserva.getRide().getDriver().getEmail());
                dbManager.addNotification(reserva.getTraveler(), message);
            } else {
                String message = String.format("%s ha rechazado tu solicitud de reserva.", reserva.getRide().getDriver().getEmail());
                dbManager.addNotification(reserva.getTraveler(), message);
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }
    
    @Override
    public boolean addReview(User reviewer, User reviewedUser, int rating, String comment) {
        dbManager.open();
        try {
            boolean ReviewSuccess = dbManager.addReview(reviewer, reviewedUser, rating, comment);
            if(ReviewSuccess) {
                String message = String.format("%s te ha dejado una reseña de %d estrellas.", reviewer.getName(), rating);
                dbManager.addNotification(reviewedUser, message);
            }
            return ReviewSuccess;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }
    
    @Override
    public boolean markNotificationAsRead(Integer notificationId) {
        dbManager.open();
        try {
            return dbManager.markNotificationAsRead(notificationId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }
    
    @Override
    public List<Review> getReviewsForUser(User user) {
        dbManager.open(); 
        List<Review> reviews = dbManager.getReviewsForUser(user);
        dbManager.close(); 
        return reviews;
    }
    
    @Override
    public List<Notification> getNotificationsForUser(User user) {
        dbManager.open();
        List<Notification> notifications = dbManager.getNotificationsForUser(user);
        dbManager.close();
        return notifications;
    }
    
    @Override
    public boolean addFunds(String userEmail, float amount) {
        dbManager.open();
        try {
            return dbManager.addFunds(userEmail, amount);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }
    
    @Override
    public boolean withdrawFunds(String userEmail, float amount) {
        dbManager.open();
        try {
            return dbManager.withdrawFunds(userEmail, amount);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }
    
    @Override
    public boolean makePayment(String travelerEmail, String driverEmail, float amount) {
        dbManager.open();
        try {
            return dbManager.makePayment(travelerEmail, driverEmail, amount);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }
    
    @Override
    public List<User> getAllUsers() {
        dbManager.open();
        List<User> users = dbManager.getAllUsers();
        dbManager.close();
        return users;
    }
    
    @Override
    public List<String> getDepartCities() {
        dbManager.open();    
        List<String> departLocations = dbManager.getDepartCities();        
        dbManager.close();
        return departLocations;
    }
    
    @Override
    public List<String> getDestinationCities(String from) {
        dbManager.open();    
        List<String> targetCities = dbManager.getArrivalCities(from);        
        dbManager.close();
        return targetCities;
    }

    @Override
    public Ride createRide(String from, String to, Date date, int nPlaces, float price, String driverEmail) 
            throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
        dbManager.open();
        Ride ride = dbManager.createRide(from, to, date, nPlaces, price, driverEmail);        
        dbManager.close();
        return ride;
    }
    
    @Override
    public List<Ride> getRides(String from, String to, Date date) {
        dbManager.open();
        List<Ride> rides = dbManager.getRides(from, to, date);
        dbManager.close();
        return rides;
    }
    
    @Override
    public List<Date> getThisMonthDatesWithRides(String from, String to, Date date) {
        dbManager.open();
        List<Date> dates = dbManager.getThisMonthDatesWithRides(from, to, date);
        dbManager.close();
        return dates;
    }
    
    public void close() {
        DataAccess dB4oManager = new DataAccess();
        dB4oManager.close();
    }

    @Override
    public void initializeBD() {
        dbManager.open();
        dbManager.initializeDB();
        dbManager.close();
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    
    private Ride findRideById(Integer rideId) {
        // Implementación alternativa ya que DataAccess no tiene getRideById
        List<Ride> allRides = dbManager.getAvailableRides();
        for (Ride ride : allRides) {
            if (ride.getRideNumber().equals(rideId)) {
                return ride;
            }
        }
        return null;
    }

    private String getStopName(Integer rideId, int stopIndex) {
        List<String> stops = getRideStops(rideId);
        if (stopIndex < 0 || stopIndex >= stops.size()) {
            throw new IllegalArgumentException("Índice de parada inválido");
        }
        return stops.get(stopIndex);
    }
}