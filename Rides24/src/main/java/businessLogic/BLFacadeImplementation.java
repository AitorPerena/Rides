package businessLogic;
import java.util.Date;


import java.util.List;
import java.util.ResourceBundle;

import javax.jws.WebMethod;
import javax.jws.WebService;

import configuration.ConfigXML;
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
    public boolean register(String email, String password, String role) {
        dbManager.open();
        boolean success = dbManager.addUser(email, password, role);
        dbManager.close();
        return success;
    }

    @Override
    public boolean requestReservation(Ride ride, Traveler traveler, int seats) {
        dbManager.open();
        try {
            // Crear la reserva
            boolean success = dbManager.addReservation(traveler, ride, seats, "Pending");
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

