package businessLogic;

import java.text.ParseException;
import java.util.Date;

import java.util.List;

//import domain.Booking;
import domain.*;
import exceptions.RideMustBeLaterThanTodayException;
import exceptions.RideAlreadyExistException;

import javax.jws.WebMethod;
import javax.jws.WebService;
 
/**
 * Interface that specifies the business logic.
 */
@WebService
public interface BLFacade  {
	
	// ==================== AUTENTICACIÓN Y USUARIOS ====================
	@WebMethod
    public User login(String email, String password);

	@WebMethod
	public boolean register(String email, String password, String role);
	
	@WebMethod
    public boolean userExists(String email);
	
	@WebMethod
    public List<User> getAllUsers();
	
	// ==================== GESTIÓN DE VIAJES ====================
	
	@WebMethod
    public Ride createMultiStopRide(String driverEmail, List<String> stops, List<Double> distances, 
                                  Date date, int totalSeats, float totalPrice)
            throws RideMustBeLaterThanTodayException, RideAlreadyExistException;
	
	@WebMethod
    public List<Ride> getAvailableRides();
	
	@WebMethod
    public List<Ride> findRidesBySegment(String from, String to, Date date);
	
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
    
 // ==================== GESTIÓN DE NOTIFICACIONES Y RESEÑAS ====================
    @WebMethod
	public List<Review> getReviewsForUser(User user);

	@WebMethod
	public List<Notification> getNotificationsForUser(User user);
	
    
    @WebMethod
    public boolean addReview(User reviewer, User reviewedUser, int rating, String comment);
    
    @WebMethod
    public boolean markNotificationAsRead(Integer notificationId);
    
    @WebMethod
    public List<Object[]> getUserReviews(String userEmail);
    
    @WebMethod
    double getAverageRating(String userEmail);
    
 // ==================== GESTIÓN DE PERFIL ====================

    @WebMethod
    public boolean uploadProfileImage(String userEmail, byte[] imageData);

    @WebMethod
    public boolean uploadVehicleImage(String driverEmail, byte[] imageData);
    
 // ==================== GESTIÓN DE REPORTES ====================
    
    @WebMethod
    public boolean addReport(User reporter, User reportedUser, String description);

    @WebMethod
    public List<Report> getUserReports(String userEmail);

    @WebMethod
    public Report getReportById(Long id);
    
    @WebMethod
    public List<Report> getAllReports();
    
    @WebMethod
    public boolean respondToReport(Report report, String response, Admin admin);

    @WebMethod
    public boolean deleteUser(String email);
    
    @WebMethod
    public boolean banUser(String email, int days);
    
    @WebMethod
    public boolean isAdmin(User user);
	
    /**
	 * This method returns all the cities where rides depart 
	 * @return collection of cities
	 */
	@WebMethod public List<String> getDepartCities();
	
	/**
	 * This method returns all the arrival destinations, from all rides that depart from a given city  
	 * 
	 * @param from the depart location of a ride
	 * @return all the arrival destinations
	 */
	@WebMethod public List<String> getDestinationCities(String from);


	/**
	 * This method creates a ride for a driver
	 * 
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride
	 * @param date the date of the ride 
	 * @param nPlaces available seats
	 * @param driver to which ride is added
	 * 
	 * @return the created ride, or null, or an exception
	 * @throws RideMustBeLaterThanTodayException if the ride date is before today 
 	 * @throws RideAlreadyExistException if the same ride already exists for the driver
	 */
   @WebMethod
   public Ride createRide( String from, String to, Date date, int nPlaces, float price, String driverEmail) throws RideMustBeLaterThanTodayException, RideAlreadyExistException;
	
	
	/**
	 * This method retrieves the rides from two locations on a given date 
	 * 
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride
	 * @param date the date of the ride 
	 * @return collection of rides
	 */
	@WebMethod public List<Ride> getRides(String from, String to, Date date);
	
	/**
	 * This method retrieves from the database the dates a month for which there are events
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride 
	 * @param date of the month for which days with rides want to be retrieved 
	 * @return collection of rides
	 */
	@WebMethod public List<Date> getThisMonthDatesWithRides(String from, String to, Date date);
	
	/**
	 * This method calls the data access to initialize the database with some events and questions.
	 * It is invoked only when the option "initialize" is declared in the tag dataBaseOpenMode of resources/config.xml file
	 */	
	@WebMethod public void initializeBD();

	

	
}
