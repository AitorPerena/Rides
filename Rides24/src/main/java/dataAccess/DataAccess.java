package dataAccess;

import java.io.File;

import java.net.NoRouteToHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import configuration.ConfigXML;
import configuration.UtilDate;
import domain.*;
import domain.Reservation;
import domain.Ride;
import domain.Traveler;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;


public class DataAccess  {
	private  EntityManager  db;
	private  EntityManagerFactory emf;

	public User getUserByEmail(String email) {
	    User user = db.find(Driver.class, email);
	    if (user == null) {
	        user = db.find(Traveler.class, email);
	    }
	    return user;
	}

	public boolean addUser(String email, String password, String name, String role) {
	    db.getTransaction().begin();
	    try {
	        User user;
	        if (role.equalsIgnoreCase("Driver")) {
	            user = new Driver(email, password, name);
	        } else if (role.equalsIgnoreCase("Traveler")) {
	            user = new Traveler(email, password, name);
	        } else {
	            throw new IllegalArgumentException("Rol no válido");
	        }

	        db.persist(user);
	        db.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        if (db.getTransaction().isActive()) {
	            db.getTransaction().rollback();
	        }
	        e.printStackTrace();
	        return false;
	    }
	}


	public boolean addReservation(Traveler traveler, Ride ride, int seats, String status) {
	    db.getTransaction().begin();
	    try {
	        Reservation reservation = new Reservation(traveler, ride, seats, status);
	        db.persist(reservation);
	        db.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        if (db.getTransaction().isActive()) {
	            db.getTransaction().rollback();
	        }
	        e.printStackTrace();
	        return false;
	    }
	}

	public List<Reservation> getReservationsByDriver(Driver driver) {
	    TypedQuery<Reservation> query = db.createQuery(
	        "SELECT r FROM Reservation r WHERE r.ride.driver = :driver", Reservation.class
	    );
	    query.setParameter("driver", driver);
	    return query.getResultList();
	}
    public List<Ride> getAvailableRides() {
        TypedQuery<Ride> query = db.createQuery("SELECT r FROM Ride r WHERE r.nPlaces > 0", Ride.class);
        return query.getResultList();
    }
	ConfigXML c=ConfigXML.getInstance();

     public DataAccess()  {
		if (c.isDatabaseInitialized()) {
			String fileName=c.getDbFilename();

			File fileToDelete= new File(fileName);
			if(fileToDelete.delete()){
				File fileToDeleteTemp= new File(fileName+"$");
				fileToDeleteTemp.delete();

				  System.out.println("File deleted");
				} else {
				  System.out.println("Operation failed");
				}
		}
		open();
		if  (c.isDatabaseInitialized())initializeDB();
		
		System.out.println("DataAccess created => isDatabaseLocal: "+c.isDatabaseLocal()+" isDatabaseInitialized: "+c.isDatabaseInitialized());

		close();

	}
     
    public DataAccess(EntityManager db) {
    	this.db=db;
    }

	
	
	/**
	 * This is the data access method that initializes the database with some events and questions.
	 * This method is invoked by the business logic (constructor of BLFacadeImplementation) when the option "initialize" is declared in the tag dataBaseOpenMode of resources/config.xml file
	 */	
	public void initializeDB(){
		
		db.getTransaction().begin();

		try {

		   Calendar today = Calendar.getInstance();
		   
		   int month=today.get(Calendar.MONTH);
		   int year=today.get(Calendar.YEAR);
		   if (month==12) { month=1; year+=1;}  
	    
		   Driver driver1 = new Driver("driver1@gmail.com", "password123", "Aitor Fernandez");
		   Driver driver2 = new Driver("driver2@gmail.com", "password456", "Ane Gaztañaga");
		   Driver driver3 = new Driver("driver3@gmail.com", "password789", "Test driver");

			//Create rides
			driver1.addRide("Donostia", "Bilbo", UtilDate.newDate(year,month,15), 4, 7);
			driver1.addRide("Donostia", "Gazteiz", UtilDate.newDate(year,month,6), 4, 8);
			driver1.addRide("Bilbo", "Donostia", UtilDate.newDate(year,month,25), 4, 4);

			driver1.addRide("Donostia", "Iruña", UtilDate.newDate(year,month,7), 4, 8);
			
			driver2.addRide("Donostia", "Bilbo", UtilDate.newDate(year,month,15), 3, 3);
			driver2.addRide("Bilbo", "Donostia", UtilDate.newDate(year,month,25), 2, 5);
			driver2.addRide("Eibar", "Gasteiz", UtilDate.newDate(year,month,6), 2, 5);

			driver3.addRide("Bilbo", "Donostia", UtilDate.newDate(year,month,14), 1, 3);

			
						
			db.persist(driver1);
			db.persist(driver2);
			db.persist(driver3);
			db.getTransaction().commit();
			System.out.println("Db initialized");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * This method returns all the cities where rides depart 
	 * @return collection of cities
	 */
	public List<String> getDepartCities(){
			TypedQuery<String> query = db.createQuery("SELECT DISTINCT r.from FROM Ride r ORDER BY r.from", String.class);
			List<String> cities = query.getResultList();
			return cities;
		
	}
	/**
	 * This method returns all the arrival destinations, from all rides that depart from a given city  
	 * 
	 * @param from the depart location of a ride
	 * @return all the arrival destinations
	 */
	public List<String> getArrivalCities(String from){
		TypedQuery<String> query = db.createQuery("SELECT DISTINCT r.to FROM Ride r WHERE r.from=?1 ORDER BY r.to",String.class);
		query.setParameter(1, from);
		List<String> arrivingCities = query.getResultList(); 
		return arrivingCities;
		
	}
	/**
	 * This method creates a ride for a driver
	 * 
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride
	 * @param date the date of the ride 
	 * @param nPlaces available seats
	 * @param driverEmail to which ride is added
	 * 
	 * @return the created ride, or null, or an exception
	 * @throws RideMustBeLaterThanTodayException if the ride date is before today 
 	 * @throws RideAlreadyExistException if the same ride already exists for the driver
	 */
	public Ride createRide(String from, String to, Date date, int nPlaces, float price, String driverEmail) throws  RideAlreadyExistException, RideMustBeLaterThanTodayException {
		System.out.println(">> DataAccess: createRide=> from= "+from+" to= "+to+" driver="+driverEmail+" date "+date);
		try {
			if(new Date().compareTo(date)>0) {
				throw new RideMustBeLaterThanTodayException(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.ErrorRideMustBeLaterThanToday"));
			}
			db.getTransaction().begin();
			
			Driver driver = db.find(Driver.class, driverEmail);
			if (driver.doesRideExists(from, to, date)) {
				db.getTransaction().commit();
				throw new RideAlreadyExistException(ResourceBundle.getBundle("Etiquetas").getString("DataAccess.RideAlreadyExist"));
			}
			Ride ride = driver.addRide(from, to, date, nPlaces, price);
			//next instruction can be obviated
			db.persist(driver); 
			db.getTransaction().commit();

			return ride;
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			db.getTransaction().commit();
			return null;
		}
		
		
	}
	
	/**
	 * This method retrieves the rides from two locations on a given date 
	 * 
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride
	 * @param date the date of the ride 
	 * @return collection of rides
	 */
	public List<Ride> getRides(String from, String to, Date date) {
		System.out.println(">> DataAccess: getRides=> from= "+from+" to= "+to+" date "+date);

		List<Ride> res = new ArrayList<>();	
		TypedQuery<Ride> query = db.createQuery("SELECT r FROM Ride r WHERE r.from=?1 AND r.to=?2 AND r.date=?3",Ride.class);   
		query.setParameter(1, from);
		query.setParameter(2, to);
		query.setParameter(3, date);
		List<Ride> rides = query.getResultList();
	 	 for (Ride ride:rides){
		   res.add(ride);
		  }
	 	return res;
	}
	
	
	/**
	 * This method retrieves from the database the dates a month for which there are events
	 * @param from the origin location of a ride
	 * @param to the destination location of a ride 
	 * @param date of the month for which days with rides want to be retrieved 
	 * @return collection of rides
	 */
	public List<Date> getThisMonthDatesWithRides(String from, String to, Date date) {
		System.out.println(">> DataAccess: getEventsMonth");
		List<Date> res = new ArrayList<>();	
		
		Date firstDayMonthDate= UtilDate.firstDayMonth(date);
		Date lastDayMonthDate= UtilDate.lastDayMonth(date);
				
		
		TypedQuery<Date> query = db.createQuery("SELECT DISTINCT r.date FROM Ride r WHERE r.from=?1 AND r.to=?2 AND r.date BETWEEN ?3 and ?4",Date.class);   
		
		query.setParameter(1, from);
		query.setParameter(2, to);
		query.setParameter(3, firstDayMonthDate);
		query.setParameter(4, lastDayMonthDate);
		List<Date> dates = query.getResultList();
	 	 for (Date d:dates){
		   res.add(d);
		  }
	 	return res;
	}
	public boolean confirmarReserva(Reservation reserva, String estado) {
	    db.getTransaction().begin();
	    try {

	        Reservation managedReserva = db.merge(reserva);
	        managedReserva.setStatus(estado);
	        
	        if ("Confirmed".equals(estado)) {
	            Ride ride = managedReserva.getRide();
	            ride.reducirAsientos(managedReserva.getSeats());
	            db.merge(ride);
	        }
	        
	        ReservaConfirmada reservaConfirmada = new ReservaConfirmada(
	            managedReserva.getTraveler(),
	            managedReserva.getRide(),
	            new Date(),
	            estado
	        );
	        db.persist(reservaConfirmada);
	        
	        db.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        if (db.getTransaction().isActive()) {
	            db.getTransaction().rollback();
	        }
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public boolean addReview(User reviewer, User reviewedUser, int rating, String comment) {
		db.getTransaction().begin();
	    if (reviewer == null || reviewedUser == null || comment == null || rating < 1 || rating > 5) {
	        throw new IllegalArgumentException("Parámetros no válidos para la reseña");
	    }

	    
	    try {
	        Review review = new Review(reviewer, reviewedUser, rating, comment);
	        db.persist(review);
	        db.getTransaction().commit();
	        return true;
	    } catch (Exception e) {
	        if (db.getTransaction().isActive()) {
	            db.getTransaction().rollback(); 
	        }
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public boolean markNotificationAsRead(Integer notificationId) {
	    db.getTransaction().begin();
	    try {
	        Notification notification = db.find(Notification.class, notificationId);
	        if (notification != null) {
	            notification.setRead(true);
	            db.merge(notification);
	            db.getTransaction().commit();
	            return true;
	        }
	        return false;
	    } catch (Exception e) {
	        if (db.getTransaction().isActive()) {
	            db.getTransaction().rollback();
	        }
	        e.printStackTrace();
	        return false;
	    }
	}

    public boolean addNotification(User user, String message) {
    	db.getTransaction().begin();
    	try {
            Notification notification = new Notification(user, message);
            db.persist(notification);
            db.getTransaction().commit();
            return true;
    	} catch (Exception e) {
	        if (db.getTransaction().isActive()) {
	            db.getTransaction().rollback(); 
	        }
	        e.printStackTrace();
	        return false;
        }
    }
    
    public List<Review> getReviewsForUser(User user) {
	    try {
	        TypedQuery<Review> query = db.createQuery(
	            "SELECT r FROM Review r WHERE r.reviewedUser = :user", Review.class
	        );
	        query.setParameter("user", user);
	        return query.getResultList();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }
	} 
public List<Notification> getNotificationsForUser(User user) {
    try {
    	TypedQuery<Notification> query = db.createQuery(
             "SELECT n FROM Notification n WHERE n.user = :user", Notification.class
        );
        query.setParameter("user", user);
        return query.getResultList();
    }catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}

public List<User> getAllUsers() {
        TypedQuery<User> query = db.createQuery(
            "SELECT u FROM User u", User.class);
        return query.getResultList();
    
}




/**
 * Obtiene los viajes entre dos fechas (inclusive)
 * @param startDate Fecha de inicio (se considera desde 00:00:00)
 * @param endDate Fecha de fin (se considera hasta 23:59:59)
 * @return Lista de viajes en ese rango de fechas
 */
public List<Ride> getRidesBetweenDates(Date startDate, Date endDate) {
    try {
        // Aseguramos que las fechas estén "recortadas" sin hora/minutos/segundos
        Date trimmedStart = UtilDate.trim(startDate);
        Date trimmedEnd = UtilDate.trim(endDate);
        
        // Ajustamos la fecha final para incluir todo el día
        Calendar cal = Calendar.getInstance();
        cal.setTime(trimmedEnd);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endOfDay = cal.getTime();
        
        // Consulta JPA
        TypedQuery<Ride> query = db.createQuery(
            "SELECT r FROM Ride r WHERE r.date BETWEEN :startDate AND :endDate ORDER BY r.date ASC",
            Ride.class
        );
        
        query.setParameter("startDate", trimmedStart);
        query.setParameter("endDate", endOfDay);
        
        return query.getResultList();
    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>(); // Devuelve lista vacía en caso de error
    }
}

	

public void open(){
		
		String fileName=c.getDbFilename();
		if (c.isDatabaseLocal()) {
			emf = Persistence.createEntityManagerFactory("objectdb:"+fileName);
			db = emf.createEntityManager();
		} else {
			Map<String, String> properties = new HashMap<>();
			  properties.put("javax.persistence.jdbc.user", c.getUser());
			  properties.put("javax.persistence.jdbc.password", c.getPassword());

			  emf = Persistence.createEntityManagerFactory("objectdb://"+c.getDatabaseNode()+":"+c.getDatabasePort()+"/"+fileName, properties);
			  db = emf.createEntityManager();
    	   }
		System.out.println("DataAccess opened => isDatabaseLocal: "+c.isDatabaseLocal());

		
	}

	public void close(){
		db.close();
		System.out.println("DataAcess closed");
	}
	
}
