package businessLogic;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebService;
import domain.*;
import exceptions.RideMustBeLaterThanTodayException;
import exceptions.RideAlreadyExistException;

@WebService
public interface BLFacade {
    
    // ==================== AUTENTICACIÓN Y USUARIOS ====================
    
    @WebMethod
    public User login(String email, String password);
    
    @WebMethod
    public boolean register(String email, String password, String name, String role);
    
    @WebMethod
    public boolean userExists(String email);
    
    @WebMethod
    public List<User> getAllUsers();

    // ==================== GESTIÓN DE VIAJES ====================
    
    @WebMethod
    public Ride createRide(String from, String to, Date date, int nPlaces, float price, String driverEmail) 
            throws RideMustBeLaterThanTodayException, RideAlreadyExistException;
    
    @WebMethod
    public Ride createMultiStopRide(String driverEmail, List<String> stops, List<Double> distances, 
                                  Date date, int totalSeats, float totalPrice)
            throws RideMustBeLaterThanTodayException, RideAlreadyExistException;
    
    @WebMethod
    public List<Ride> getAvailableRides();
    
    @WebMethod
    public List<Ride> getRides(String from, String to, Date date);
    
    @WebMethod
    public List<Ride> findRidesBySegment(String from, String to, Date date);
    
    @WebMethod
    public List<Date> getThisMonthDatesWithRides(String from, String to, Date date);
    
    @WebMethod
    public List<String> getDepartCities();
    
    @WebMethod
    public List<String> getDestinationCities(String from);
    
    @WebMethod
    public List<String> getRideStops(Integer rideId);
    
    @WebMethod
    public double calculateSegmentPrice(Integer rideId, int startIndex, int endIndex);
    
    @WebMethod
    void sendRideReminders(Date rideDate);

    // ==================== GESTIÓN DE RESERVAS ====================
    
    @WebMethod
    public boolean requestReservation(Ride ride, Traveler traveler, int seats);
    
    @WebMethod
    public Reservation reserveRideSegment(Integer rideId, String travelerEmail, 
                                        int startStopIndex, int endStopIndex, int seats);
    
    @WebMethod
    public List<Reservation> getReservations(Driver driver);
    
    @WebMethod
    public boolean confirmarReserva(Reservation reserva, String estado);

    // ==================== GESTIÓN DE PAGOS ====================
    
    @WebMethod
    public boolean addFunds(String userEmail, float amount);
    
    @WebMethod
    public boolean withdrawFunds(String userEmail, float amount);
    
    @WebMethod
    public boolean makePayment(String travelerEmail, String driverEmail, float amount);
    
    @WebMethod
    public Date parseExpirationDate(String expiration) throws ParseException;
    
    @WebMethod
    public boolean validateCreditCard(String cardNumber, String expiration, String cvv);

    // ==================== GESTIÓN DE RESEÑAS ====================
    
    @WebMethod
    public boolean addReview(User reviewer, User reviewedUser, int rating, String comment);
    
    @WebMethod
    public List<Review> getReviewsForUser(User user);

    // ==================== GESTIÓN DE NOTIFICACIONES ====================
    
    @WebMethod
    public List<Notification> getNotificationsForUser(User user);
    
    @WebMethod
    public boolean markNotificationAsRead(Integer notificationId);

    // ==================== INICIALIZACIÓN ====================
    
    @WebMethod 
    public void initializeBD();
}