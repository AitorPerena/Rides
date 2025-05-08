package dataAccess;

import java.io.File;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.*;

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

	public boolean addUser(String email, String password, String role) {
	    db.getTransaction().begin();
	    try {
	        User user;
	        if (role.equals("Driver")) {
	            user = new Driver(email, password);
	        } else if (role.equals("Traveler")) {
	            user = new Traveler(email, password);
	        } else {
	            throw new IllegalArgumentException("Rol no válido");
	        }
	        
	        Wallet wallet = new Wallet(user);
	        user.setWallet(wallet);
	        
	        db.persist(user);
	        db.persist(wallet);
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
	
	public Ride createMultiStopRide(String driverEmail, List<String> stops, List<Double> distances, 
	                                  Date date, int nPlaces, float totalPrice) 
	           {
	        
	        
	        db.getTransaction().begin();
	        try {
	            Driver driver = db.find(Driver.class, driverEmail);
	            if(driver == null) {
	                throw new IllegalArgumentException("Conductor no encontrado");
	            }
	            
	            // Verificar si ya existe un viaje con las mismas paradas en la misma fecha
	            if(doesMultiStopRideExist(driver, stops.get(0), stops.get(stops.size()-1), date)) {
	                throw new RideAlreadyExistException("Ya existe un viaje con estas paradas en esta fecha");
	            }
	            
	            Ride ride = new Ride(stops.get(0), stops.get(stops.size()-1), date, nPlaces, totalPrice, driver);
	            
	            // Añadir paradas intermedias
	            for(int i = 1; i < stops.size() - 1; i++) {
	                ride.addIntermediateStop(stops.get(i), distances.get(i-1));
	            }
	            
	            db.persist(ride);
	            db.getTransaction().commit();
	            return ride;
	        } catch (Exception e) {
	            if (db.getTransaction().isActive()) {
	                db.getTransaction().rollback();
	            }
	            throw new RuntimeException("Error al crear el viaje con múltiples paradas", e);
	        }
	    }
	    
	    private boolean doesMultiStopRideExist(Driver driver, String origin, String destination, Date date) {
	        TypedQuery<Long> query = db.createQuery(
	            "SELECT COUNT(r) FROM Ride r WHERE r.driver = :driver AND r.from = :origin " +
	            "AND r.to = :destination AND r.date = :date", Long.class);
	        query.setParameter("driver", driver);
	        query.setParameter("origin", origin);
	        query.setParameter("destination", destination);
	        query.setParameter("date", date);
	        return query.getSingleResult() > 0;
	    }
	    
	    /**
	     * Reserva un segmento de un viaje
	     */
	    public Reservation reserveSegment(Integer rideId, String travelerEmail, 
	                                    int startIdx, int endIdx, int seats) {
	        db.getTransaction().begin();
	        try {
	            Ride ride = db.find(Ride.class, rideId);
	            Traveler traveler = db.find(Traveler.class, travelerEmail);
	            
	            if(ride == null || traveler == null) {
	                throw new IllegalArgumentException("Viaje o viajero no encontrado");
	            }
	            
	            List<String> stops = ride.getAllStops();
	            if(startIdx < 0 || endIdx >= stops.size() || startIdx >= endIdx) {
	                throw new IllegalArgumentException("Índices de parada inválidos");
	            }
	            
	            if(ride.getnPlaces() < seats) {
	                throw new IllegalArgumentException("No hay suficientes asientos disponibles");
	            }
	            
	            Reservation reservation = new Reservation();
	            reservation.setRide(ride);
	            reservation.setTraveler(traveler);
	            reservation.setSeats(seats);
	            reservation.setStartStopIndex(startIdx);
	            reservation.setEndStopIndex(endIdx);
	            reservation.setSegmentPrice(ride.calculateSegmentPrice(startIdx, endIdx));
	            reservation.setStatus("Pending");
	            
	            db.persist(reservation);
	            ride.reducirAsientos(seats);
	            
	            // Notificar al conductor
	            Notification notification = new Notification(
	                ride.getDriver(), 
	                "Nueva reserva de segmento: " + stops.get(startIdx) + " → " + stops.get(endIdx)
	            );
	            db.persist(notification);
	            
	            db.getTransaction().commit();
	            return reservation;
	        } catch(Exception e) {
	            if(db.getTransaction().isActive()) {
	                db.getTransaction().rollback();
	            }
	            throw e;
	        }
	    }
	    
	    /**
	     * Busca viajes que contengan un segmento específico
	     */
	    public List<Ride> findRidesWithSegment(String from, String to, Date date) {
	        // Primero obtenemos todos los viajes en la fecha especificada
	        List<Ride> rides = getRidesByDate(date);
	        
	        // Filtramos los que contengan el segmento from→to
	        List<Ride> result = new ArrayList<>();
	        for(Ride ride : rides) {
	            List<String> stops = ride.getAllStops();
	            int fromIndex = stops.indexOf(from);
	            int toIndex = stops.indexOf(to);
	            
	            if(fromIndex != -1 && toIndex != -1 && fromIndex < toIndex) {
	                result.add(ride);
	            }
	        }
	        
	        return result;
	    }
	    
	    private List<Ride> getRidesByDate(Date date) {
	        TypedQuery<Ride> query = db.createQuery(
	            "SELECT r FROM Ride r WHERE r.date = :date", Ride.class);
	        query.setParameter("date", date);
	        return query.getResultList();
	    }
	    
	    /**
	     * Obtiene todas las paradas de un viaje
	     */
	    public List<String> getRideStops(Integer rideId) {
	        Ride ride = db.find(Ride.class, rideId);
	        return ride != null ? ride.getAllStops() : Collections.emptyList();
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
	    
		   Driver driver1 = new Driver("driver1@gmail.com", "Aitor Fernandez");
		   Driver driver2 = new Driver("driver2@gmail.com", "Ane Gaztañaga");
		   Driver driver3 = new Driver("driver3@gmail.com", "Test driver");

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

public boolean addFunds(String userEmail, float amount) {
    db.getTransaction().begin();
    try {
        User user = db.find(User.class, userEmail);
        if (user != null && user.getWallet() != null) {
            user.getWallet().addFunds(amount);
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

public boolean withdrawFunds(String userEmail, float amount) {
	db.getTransaction().begin();
	try {
		User user = db.find(User.class,  userEmail);
		if(user != null && user.getWallet() != null) {
			user.getWallet().deductFunds(amount);
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

public boolean makePayment(String travelerEmail, String driverEmail, float amount) {
	db.getTransaction().begin();
	try {
		User traveler = db.find(User.class,  travelerEmail);
		User driver = db.find(User.class,  driverEmail);
		if(traveler != null && traveler.getWallet() != null && driver != null && driver.getWallet() != null) {
			//Verificar que el traveler tiene suficiente dinero
			if(traveler.getWallet().getBalance() >= amount) {
				//Completar la acción
				traveler.getWallet().deductFunds(amount);
				driver.getWallet().addFunds(amount);
				db.getTransaction().commit();
				return true;
			}
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

public Date parseAndValidateExpirationDate(String expiration) throws ParseException {
	 SimpleDateFormat sdf = new SimpleDateFormat("MM/YY");
	 sdf.setLenient(false);
	 Date date = sdf.parse(expiration);
	 
	 // Asegurar que es fecha futura
	 Date today = new Date();
	 if (date.before(today)) {
	     throw new IllegalArgumentException("La fecha de expiración debe ser futura");
	 }
	 return date;
	}

	public boolean validateCardData(String cardNumber, String expiration, String cvv) {
	 try {
	     // Validar formato del número de tarjeta
	     if (!cardNumber.matches("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$")) {
	         return false;
	     }
	     
	     // Validar CVV
	     if (!cvv.matches("^\\d{3,4}$")) {
	         return false;
	     }
	     
	     // Validar fecha de expiración
	     parseAndValidateExpirationDate(expiration);
	     
	     return true;
	 } catch (ParseException e) {
	     return false;
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
