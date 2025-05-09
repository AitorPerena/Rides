package businessLogic;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import services.EmailService;
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

/**
 * It implements the business logic as a web service.
 */
@WebService(endpointInterface = "businessLogic.BLFacade")
public class BLFacadeImplementation  implements BLFacade {
	DataAccess dbManager;
	

	
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
	    if (user.isBanned()) {
	        System.out.println("Usuario baneado hasta: " + user.getBanEndDate());
	        return null;
	    }
	    return null;
	}

	@Override
    public boolean register(String email, String password, String role) {
        dbManager.open();
        boolean success = dbManager.addUser(email, password, role);
        dbManager.close();

        if (success) {
            services.EmailService.sendWelcomeEmail(email);
        }

        return success;
    }
    

    public void sendRideReminders(Date rideDate) {
        dbManager.open();
        try {
            // Usamos tu método existente getRidesBetweenDates
            List<Ride> rides = dbManager.getRidesBetweenDates(
                UtilDate.trim(rideDate), // Fecha inicio (usando tu UtilDate)
                UtilDate.addDays(rideDate, 1) // Fecha fin (día siguiente)
            );

            for (Ride ride : rides) {
                // Notificación para conductor
                String driverMsg = String.format(
                    "Recordatorio: Tienes un viaje programado para %1$te de %1$tB a las %1$tR",
                    ride.getDate()
                );
                dbManager.addNotification(ride.getDriver(), driverMsg);

                // Notificaciones para viajeros
                for (Reservation res : ride.getReservations()) {
                    String travelerMsg = String.format(
                        "Recordatorio: Viaje a %s el %1$te/%1$tm a las %1$tR.",
                        ride.getDate()
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
            // Crear la reserva
            boolean success = dbManager.addReservation(traveler, ride, seats, "Pending");
            if (success) {
	            System.out.println("Notificación enviada a: " + ride.getDriver().getEmail() + 
	                             " - Estado: Pendiente");
	            String message = String.format("%s ha enviado una solicitud de reserva.", traveler);
	        	boolean NotificationSuccess = dbManager.addNotification(ride.getDriver(), message);
	        	if(!NotificationSuccess) {
	        		System.out.println("Error al crear la notificación.");
	        	}
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
    
    
	public BLFacadeImplementation()  {		
		System.out.println("Creating BLFacadeImplementation instance");
		
		
		    dbManager=new DataAccess();
		        
		    // Verificar y crear admin si es necesario
		    dbManager.open();
		    dbManager.createAdminIfNotExists();
		    dbManager.close();
		    
		    
		

		
	}
	@Override
	public boolean confirmarReserva(Reservation reserva, String estado) {
	    dbManager.open();
	    try {
	        boolean success = dbManager.confirmarReserva(reserva, estado);
	        if (success) {
	            System.out.println("Notificación enviada a: " + reserva.getTraveler().getEmail() + 
	                             " - Estado: " + estado);
	            String message = String.format("%s ha aceptado tu solicitud de reserva.", reserva.getRide().getDriver().getEmail());
	        	boolean NotificationSuccess = dbManager.addNotification(reserva.getTraveler(), message);
	        	if(!NotificationSuccess) {
	        		System.out.println("Error al crear la notificación.");
	        	}
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
        	String message = String.format("%s te ha dejado una reseña de %d estrellas.", reviewer.getEmail(), rating);
        	boolean NotificationSuccess = dbManager.addNotification(reviewedUser, message);
        	if(!NotificationSuccess) {
        		System.out.println("Error al crear la notificación.");
        	}
        }
        return ReviewSuccess;
        } catch (Exception e) {
        	e.printStackTrace();
        	return false;
        }finally {
        	dbManager.close();
        }
    }
	
	@Override
    public boolean markNotificationAsRead(Integer notificationId) {
        dbManager.open();
        try {
            boolean success = dbManager.markNotificationAsRead(notificationId);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            dbManager.close();
        }
    }
	
	@Override
	public boolean addFunds(String userEmail, float amount) {
	    dbManager.open();
	    try {
	        boolean success = dbManager.addFunds(userEmail, amount);
	            return success;
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
			boolean success = dbManager.withdrawFunds(userEmail, amount);
			return success;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally {
			dbManager.close();
		}
	}
	
	@Override
	public boolean makePayment(String travelerEmail, String driverEmail, float amount) {
		dbManager.open();
		try {
			boolean success = dbManager.makePayment(travelerEmail, driverEmail, amount);
			return success;
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
	public List<Object[]> getUserReviews(String userEmail) {
	    dbManager.open();
	    try {
	        return dbManager.getUserReviews(userEmail);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>(); // Retorna lista vacía en caso de error
	    } finally {
	        dbManager.close();
	    }
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
	
	public Ride createMultiStopRide(String driverEmail, List<String> stops, 
            List<Double> segmentPrices, Date date, 
            int nPlaces, float totalPrice) {
		dbManager.open();
		try {
			Ride ride = dbManager.createMultiStopRide(driverEmail, stops, segmentPrices, 
                              date, nPlaces, totalPrice);

			// Notificar al conductor
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String formattedDate = sdf.format(date);
			String message = String.format("Has creado un nuevo viaje con %d paradas para %s", 
			    stops.size()-1, formattedDate);
			dbManager.addNotification(dbManager.getUserByEmail(driverEmail), message);

			return ride;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			dbManager.close();
		}
	}
	
	@Override
	public List<Ride> findRidesBySegment(String from, String to, Date date) {
	    dbManager.open();
	    try {
	        return dbManager.findRidesBySegment(from, to, date);
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
	
	@Override
	public Reservation reserveRideSegment(Integer rideId, String travelerEmail, int startStopIndex, int endStopIndex, int seats) {
	    dbManager.open();
	    try {
	        Reservation reservation = dbManager.reserveRideSegment(rideId, travelerEmail, startStopIndex, endStopIndex, seats);
	        
	        // Notificación al conductor
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
	public boolean isAdmin(User user) {
	    return user instanceof Admin;
	}
	
	@Override
	public double getAverageRating(String userEmail) {
	    dbManager.open();
	    double avg = dbManager.getAverageRating(userEmail);
	    dbManager.close();
	    return avg;
	}

	@Override
	public boolean uploadProfileImage(String userEmail, byte[] imageData) {
	    dbManager.open();
	    try {
	        // Validar datos antes de enviar a DataAccess
	        if (userEmail == null || userEmail.isEmpty() || imageData == null || imageData.length == 0) {
	            return false;
	        }
	        return dbManager.uploadProfileImage(userEmail, imageData);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        dbManager.close();
	    }
	}

	@Override
	public boolean uploadVehicleImage(String driverEmail, byte[] imageData) {
	    dbManager.open();
	    try {
	        // Validar datos antes de enviar a DataAccess
	        if (driverEmail == null || driverEmail.isEmpty() || imageData == null || imageData.length == 0) {
	            return false;
	        }
	        return dbManager.uploadVehicleImage(driverEmail, imageData);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        dbManager.close();
	    }
	}
	

	
	@Override
	public List<Report> getAllReports() {
	    dbManager.open();
	    try {
	        return dbManager.getAllReports();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    } finally {
	        dbManager.close();
	    }
	}
	
	@Override
	public boolean addReport(User reporter, User reportedUser, String description) {
	    dbManager.open();
	    try {
	        boolean success = dbManager.addReport(reporter, reportedUser, description);
	        if (success) {
	            // Notificar al admin
	            String message = String.format("Nuevo reporte de %s sobre %s", 
	                reporter.getEmail(), reportedUser.getEmail());
	            dbManager.addNotification(getAdminUser(), message);
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
	public List<Report> getUserReports(String userEmail) {
	    dbManager.open();
	    try {
	        return dbManager.getUserReports(userEmail);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    } finally {
	        dbManager.close();
	    }
	}

	@Override
	public Report getReportById(Long id) {
	    dbManager.open();
	    try {
	        return dbManager.getReportById(id);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        dbManager.close();
	    }
	}

	@Override
	public boolean respondToReport(Report report, String response, Admin admin) {
	    dbManager.open();
	    try {
	        admin.respondToReport(report, response);
	        return dbManager.updateReport(report);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        dbManager.close();
	    }
	}
	
	@Override
	public boolean banUser(String email, int days) {
	    dbManager.open();
	    try {
	        return dbManager.banUser(email, days);
	    } finally {
	        dbManager.close();
	    }
	}

	@Override
	public boolean deleteUser(String email) {
	    dbManager.open();
	    try {
	        return dbManager.deleteUser(email);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        dbManager.close();
	    }
	}

	private User getAdminUser() {
	    // Método auxiliar para obtener un usuario admin
	    dbManager.open();
	    try {
	        return dbManager.getAllUsers().stream()
	                .filter(u -> u instanceof Admin)
	                .findFirst()
	                .orElse(null);
	    } finally {
	        dbManager.close();
	    }
	}
	@Override
    public List<Notification> getNotificationsForUser(User user) {
        dbManager.open();
        List<Notification> notifications = dbManager.getNotificationsForUser(user);
        dbManager.close();
        return notifications;
    }
	
	@Override
    public List<User> getAllUsers() {
        dbManager.open();
        List<User> users = dbManager.getAllUsers();
        dbManager.close();
        return users;
    }
    public BLFacadeImplementation(DataAccess da)  {
		
		System.out.println("Creating BLFacadeImplementation instance with DataAccess parameter");
		ConfigXML c=ConfigXML.getInstance();
		
		dbManager=da;		
	}
    
    
    /**
     * {@inheritDoc}
     */
    @WebMethod public List<String> getDepartCities(){
    	dbManager.open();	
		
		 List<String> departLocations=dbManager.getDepartCities();		

		dbManager.close();
		
		return departLocations;
    	
    }
    /**
     * {@inheritDoc}
     */
	@WebMethod public List<String> getDestinationCities(String from){
		dbManager.open();	
		
		 List<String> targetCities=dbManager.getArrivalCities(from);		

		dbManager.close();
		
		return targetCities;
	}

	/**
	 * {@inheritDoc}
	 */
   @WebMethod
   public Ride createRide( String from, String to, Date date, int nPlaces, float price, String driverEmail ) throws RideMustBeLaterThanTodayException, RideAlreadyExistException{
	   
		dbManager.open();
		Ride ride=dbManager.createRide(from, to, date, nPlaces, price, driverEmail);		
		dbManager.close();
		return ride;
   };
	
   /**
    * {@inheritDoc}
    */
	@WebMethod 
	public List<Ride> getRides(String from, String to, Date date){
		dbManager.open();
		List<Ride>  rides=dbManager.getRides(from, to, date);
		dbManager.close();
		return rides;
	}
    
	/**
	 * {@inheritDoc}
	 */
	@WebMethod 
	public List<Date> getThisMonthDatesWithRides(String from, String to, Date date){
		dbManager.open();
		List<Date>  dates=dbManager.getThisMonthDatesWithRides(from, to, date);
		dbManager.close();
		return dates;
	}
	
	
	public void close() {
		DataAccess dB4oManager=new DataAccess();

		dB4oManager.close();

	}

	/**
	 * {@inheritDoc}
	 */
    @WebMethod	
	 public void initializeBD(){
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

