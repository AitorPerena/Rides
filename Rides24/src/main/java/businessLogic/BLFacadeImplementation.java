package businessLogic;
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
	    return null;
	}

	@Override
	public boolean register(String email, String password, String name, String role) {
	    dbManager.open();
	    boolean success = dbManager.addUser(email, password, name, role);
	    dbManager.close();
	    return success;
	}

    

    public void sendRideReminders(Date rideDate) {
        dbManager.open();
        try {
            // Usamos tu método existente getRidesBetweenDates
            List<Ride> rides = dbManager.getRidesBetweenDates(
                UtilDate.trim(rideDate), // Fecha inicio (UtilDate)
                UtilDate.addDays(rideDate, 1) // Fecha fin (día siguiente)
            );

            for (Ride ride : rides) {
                // Notificación para conductor
                String driverMsg = String.format(
                    "Recordatorio: Tienes un viaje programado para %1$te de %1$tB",
                    ride.getDate()
                );
                dbManager.addNotification(ride.getDriver(), driverMsg);

                // Notificaciones para viajeros
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
            // Crear la reserva
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
    
    
	public BLFacadeImplementation()  {		
		System.out.println("Creating BLFacadeImplementation instance");
		
		
		    dbManager=new DataAccess();
		    
		//dbManager.close();

		
	}
	@Override
	public boolean confirmarReserva(Reservation reserva, String estado) {
	    dbManager.open();
	    try {
	        boolean success = dbManager.confirmarReserva(reserva, estado);
	        if (estado == "Confirmed") {
	            String message = String.format("%s ha aceptado tu solicitud de reserva.", reserva.getRide().getDriver().getEmail());
	            dbManager.addNotification(reserva.getTraveler(), message);

	        }
	        else {
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

}

