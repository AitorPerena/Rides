package dataAccess;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.NoRouteToHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
		 User user = db.find(Admin.class, email);
		    if (user == null) {
		        // Si no es Admin, busca como Driver
		        user = db.find(Driver.class, email);
		    }
		    if (user == null) {
		        // Si no es Driver, busca como Traveler
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
	        }else if(role.equals("Admin")) {
	        	user = new Admin(email, password);
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
	
	public void createAdminIfNotExists() {
	    db.getTransaction().begin();
	    try {
	        // Verificar si ya existe algún admin
	        TypedQuery<Admin> query = db.createQuery("SELECT a FROM Admin a", Admin.class);
	        List<Admin> admins = query.getResultList();
	        
	        if (admins.isEmpty()) {
	            // Crear admin por defecto solo si no existe ninguno
	            Admin admin = new Admin("admin@ridesharing.com", "admin123");
	            
	            // Crear wallet para el admin
	            Wallet wallet = new Wallet(admin);
	            admin.setWallet(wallet);
	            
	            db.persist(admin);
	            db.persist(wallet);
	            System.out.println("Cuenta de administrador creada");
	        }
	        db.getTransaction().commit();
	    } catch (Exception e) {
	        if (db.getTransaction().isActive()) {
	            db.getTransaction().rollback();
	        }
	        e.printStackTrace();
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
		
		public void initializeDB() {
		    db.getTransaction().begin();
		    try {
		        Calendar today = Calendar.getInstance();
		        int month = today.get(Calendar.MONTH);
		        int year = today.get(Calendar.YEAR);
		        if (month == 12) {
		            month = 1;
		            year += 1;
		        }

		        // Crear admin por defecto (solo si no existe)

		         Admin admin = new Admin("admin@gmail.com", "admin123");
		         db.persist(admin);
		         System.out.println("Cuenta de administrador creada");
		        

		        // Crear conductores
		        Driver driver1 = new Driver("driver1@gmail.com", "Aitor Fernandez");
		        Driver driver2 = new Driver("driver2@gmail.com", "Ane Gaztañaga");
		        Driver driver3 = new Driver("driver3@gmail.com", "Test driver");

		        // Crear viajes con valores válidos
		        driver1.addRide("Donostia", "Bilbo", UtilDate.newDate(year, month, 15), 4, 7.0f);
		        driver1.addRide("Donostia", "Gazteiz", UtilDate.newDate(year, month, 6), 4, 8.0f);
		        driver1.addRide("Bilbo", "Donostia", UtilDate.newDate(year, month, 25), 4, 4.0f);
		        driver1.addRide("Donostia", "Iruña", UtilDate.newDate(year, month, 7), 4, 8.0f);

		        driver2.addRide("Donostia", "Bilbo", UtilDate.newDate(year, month, 15), 3, 3.0f);
		        driver2.addRide("Bilbo", "Donostia", UtilDate.newDate(year, month, 25), 2, 5.0f);
		        driver2.addRide("Eibar", "Gasteiz", UtilDate.newDate(year, month, 6), 2, 5.0f);

		        driver3.addRide("Bilbo", "Donostia", UtilDate.newDate(year, month, 14), 1, 3.0f);

		        db.persist(driver1);
		        db.persist(driver2);
		        db.persist(driver3);

		        db.getTransaction().commit();
		        System.out.println("Db initialized");
		    } catch (Exception e) {
		        if (db.getTransaction().isActive()) {
		            db.getTransaction().rollback();
		        }
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
	        throw new IllegalArgumentException("Par�metros no v�lidos para la rese�a");
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
    
    public List<Object[]> getUserReviews(String userEmail) {
        try {
            TypedQuery<Object[]> query = db.createQuery(
                "SELECT r.reviewer.email, r.rating, r.comment, r.date FROM Review r " +
                "WHERE r.reviewedUser.email = :email ORDER BY r.date DESC", Object[].class);
            query.setParameter("email", userEmail);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public double getAverageRating(String userEmail) {
        TypedQuery<Double> query = db.createQuery(
            "SELECT AVG(r.rating) FROM Review r WHERE r.reviewedUser.email = :email", Double.class);
        query.setParameter("email", userEmail);
        Double avg = query.getSingleResult();
        return avg != null ? avg : 0.0;
    }

    public boolean uploadProfileImage(String userEmail, byte[] imageData) {
        db.getTransaction().begin();
        try {
            User user = db.find(User.class, userEmail);
            if (user != null) {
                // Generar nombre único para la imagen
                String fileName = "profile_" + userEmail.hashCode() + "_" + System.currentTimeMillis() + ".png";
                
                // Guardar la imagen en el sistema de archivos (o en la base de datos si prefieres)
                String imagePath = saveImageToFileSystem(imageData, fileName);
                
                // Actualizar la referencia en el usuario
                user.setProfileImagePath(imagePath);
                db.persist(user);
                db.getTransaction().commit();
                return true;
            }
            db.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean uploadVehicleImage(String driverEmail, byte[] imageData) {
        db.getTransaction().begin();
        try {
            Driver driver = db.find(Driver.class, driverEmail);
            if (driver != null) {
                // Generar nombre único para la imagen del vehículo
                String fileName = "vehicle_" + driverEmail.hashCode() + "_" + System.currentTimeMillis() + ".png";
                
                // Guardar la imagen en el sistema de archivos
                String imagePath = saveImageToFileSystem(imageData, fileName);
                
                // Actualizar la referencia en el conductor
                driver.setVehicleImagePath(imagePath);
                db.persist(driver);
                db.getTransaction().commit();
                return true;
            }
            db.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        }
    }


    public boolean addReport(User reporter, User reportedUser, String description) {
        db.getTransaction().begin();
        try {
            Report report = new Report(reporter, reportedUser, description);
            db.persist(report);
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
    
    public List<Report> getAllReports() {
        try {
            TypedQuery<Report> query = db.createQuery(
                "SELECT r FROM Report r ORDER BY r.reportDate DESC", 
                Report.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Report> getUserReports(String userEmail) {
        try {
            TypedQuery<Report> query = db.createQuery(
                "SELECT r FROM Report r WHERE r.reportedUser.email = :email ORDER BY r.reportDate DESC", 
                Report.class);
            query.setParameter("email", userEmail);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Report getReportById(Long id) {
        return db.find(Report.class, id);
    }

    public boolean updateReport(Report report) {
        db.getTransaction().begin();
        try {
            db.merge(report);
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
    
    public boolean banUser(String email, int days) {
        try {
            db.getTransaction().begin();
            User user = db.find(User.class, email);
            
            if (user != null) {
                // Configurar baneo
                user.setBanned(true);
                
                if (days > 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, days);
                    user.setBanEndDate(cal.getTime());
                } else {
                    user.setBanEndDate(null); // Baneo permanente
                }
                
                db.merge(user);
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

    public boolean deleteUser(String email) {
        db.getTransaction().begin();
        try {
            User user = db.find(User.class, email);
            if (user != null) {
                db.remove(user);
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
    
    public Ride createMultiStopRide(String driverEmail, List<String> stops, List<Double> segmentPrices, Date date, int nPlaces, float totalPrice) {
		db.getTransaction().begin();
		try {
			// 1. Validaciones básicas
			if (driverEmail == null || driverEmail.isEmpty()) {
				throw new IllegalArgumentException("El email del conductor no puede estar vacío");
			}

			if (stops == null || stops.size() < 2) {
				throw new IllegalArgumentException("Debe haber al menos 2 paradas (origen y destino)");
			}

			if (segmentPrices == null || segmentPrices.size() != stops.size() - 1) {
				throw new IllegalArgumentException(
						String.format("Se necesitan %d precios para %d segmentos", 
						stops.size() - 1, stops.size() - 1)
						);
			}

			// 2. Verificar conductor
			Driver driver = db.find(Driver.class, driverEmail);
			if (driver == null) {
				throw new IllegalArgumentException("Conductor no encontrado");
			}
			
			// 3. Verificar que no exista un viaje similar
			if (doesMultiStopRideExist(driver, stops.get(0), stops.get(stops.size()-1), date)) {
				throw new RideAlreadyExistException("Ya existe un viaje con estas paradas en esta fecha");
			}

			// 4. Verificar consistencia de precios
			double sumPrices = segmentPrices.stream().mapToDouble(Double::doubleValue).sum();
			if (Math.abs(sumPrices - totalPrice) > 0.01) {
				throw new IllegalArgumentException(
						String.format("La suma de precios de segmentos (%.2f) no coincide con el precio total (%.2f)",
								sumPrices, totalPrice)
						);
			}

			// 5. Crear el viaje
			Ride ride = new Ride(stops.get(0), stops.get(stops.size()-1), date, nPlaces, totalPrice, driver);

			// 6. Añadir paradas intermedias con sus precios
			for (int i = 1; i < stops.size(); i++) {
				ride.addIntermediateStop(stops.get(i), segmentPrices.get(i-1));
			}

			// 7. Persistir el viaje
			db.persist(ride);

			// 8. Crear notificación
			String routeDescription = String.join(" → ", stops);
			Notification notification = new Notification(
					driver,
					String.format("Nuevo viaje creado: %s (%.2f€)", routeDescription, totalPrice)
					);
			db.persist(notification);
	
			db.getTransaction().commit();
			return ride;

		} catch (Exception e) {
			if (db.getTransaction().isActive()) {
				db.getTransaction().rollback();
			}
			throw new RuntimeException("Error al crear el viaje con múltiples paradas", e);
		}
    }
    
    public boolean doesMultiStopRideExist(Driver driver, String origin, String destination, Date date) {
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

//Crear reserva con estado "Pending" (NO reducir asientos todavía)
    		Reservation reservation = new Reservation();
    		reservation.setRide(ride);
    		reservation.setTraveler(traveler);
    		reservation.setSeats(seats);
    		reservation.setStartStopIndex(startIdx);
    		reservation.setEndStopIndex(endIdx);
    		reservation.setSegmentPrice(ride.calculateSegmentPrice(startIdx, endIdx));
    		reservation.setStatus("Pending"); // Estado pendiente

    		db.persist(reservation);

    		// Notificar al conductor (sin reducir asientos)
    		Notification notification = new Notification(
    				ride.getDriver(), 
    				"Solicitud de reserva de segmento: " + stops.get(startIdx) + " → " + stops.get(endIdx)
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

    private String saveImageToFileSystem(byte[] imageData, String fileName) {
        try {
            // Ruta donde se guardarán las imágenes (ajusta según tu estructura de proyecto)
            String imagesDir = System.getProperty("user.dir") + "/src/main/resources/images/";
            
            // Crear directorio si no existe
            new File(imagesDir).mkdirs();
            
            // Ruta completa del archivo
            String filePath = imagesDir + fileName;
            
            // Escribir los bytes en el archivo
            Files.write(Paths.get(filePath), imageData);
            
            // Retornar solo el nombre del archivo para almacenar en la base de datos
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return "default_profile.png"; // Retornar imagen por defecto en caso de error
        }
    }
    
    public List<Ride> findRidesBySegment(String from, String to, Date date) {
        try {
            // Primero obtenemos todos los viajes en la fecha especificada
            TypedQuery<Ride> query = db.createQuery(
                "SELECT r FROM Ride r WHERE r.date = :date", Ride.class);
            query.setParameter("date", date);
            List<Ride> rides = query.getResultList();
            
            // Filtramos los que contengan el segmento from→to
            List<Ride> result = new ArrayList<>();
            for (Ride ride : rides) {
                List<String> stops = ride.getAllStops();
                int fromIndex = stops.indexOf(from);
                int toIndex = stops.indexOf(to);
                
                // Verificamos que ambas paradas existan y estén en orden correcto
                if (fromIndex != -1 && toIndex != -1 && fromIndex < toIndex) {
                    // Verificamos disponibilidad de asientos para el segmento
                    if (ride.hasAvailableSeatsForSegment(fromIndex, toIndex)) {
                        result.add(ride);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public double calculateSegmentPrice(Integer rideId, int startIdx, int endIdx) {
        db.getTransaction().begin();
        try {
            Ride ride = db.find(Ride.class, rideId);
            if (ride == null) {
                throw new IllegalArgumentException("Viaje no encontrado");
            }
            
            List<String> stops = ride.getAllStops();
            if (startIdx < 0 || endIdx >= stops.size() || startIdx >= endIdx) {
                throw new IllegalArgumentException("Índices de parada inválidos");
            }
            
            double price = ride.calculateSegmentPrice(startIdx, endIdx);
            db.getTransaction().commit();
            return price;
        } catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw new RuntimeException("Error al calcular precio del segmento", e);
        }
    }
    

    public Reservation reserveRideSegment(Integer rideId, String travelerEmail, 
                                        int startIdx, int endIdx, int seats) {
        db.getTransaction().begin();
        try {
            // Validar existencia de entidades
            Ride ride = db.find(Ride.class, rideId);
            Traveler traveler = db.find(Traveler.class, travelerEmail);
            
            if (ride == null || traveler == null) {
                throw new IllegalArgumentException("Viaje o viajero no encontrado");
            }
            
            // Validar índices de paradas
            List<String> stops = ride.getAllStops();
            if (startIdx < 0 || endIdx >= stops.size() || startIdx >= endIdx) {
                throw new IllegalArgumentException("Índices de parada inválidos");
            }
            
            // Validar disponibilidad de asientos
            if (!ride.hasAvailableSeatsForSegment(startIdx, endIdx, seats)) {
                throw new IllegalStateException("No hay suficientes asientos disponibles para este segmento");
            }
            
            // Calcular precio del segmento
            double segmentPrice = ride.calculateSegmentPrice(startIdx, endIdx) * seats;
            
            // Verificar fondos del viajero
            if (traveler.getWallet().getBalance() < segmentPrice) {
                throw new IllegalStateException("Fondos insuficientes para realizar la reserva");
            }
            
            // Crear la reserva
            Reservation reservation = new Reservation();
            reservation.setRide(ride);
            reservation.setTraveler(traveler);
            reservation.setSeats(seats);
            reservation.setStartStopIndex(startIdx);
            reservation.setEndStopIndex(endIdx);
            reservation.setSegmentPrice(segmentPrice);
            reservation.setStatus("Pending"); // Estado inicial pendiente
            
            // Reducir asientos disponibles en el viaje
            ride.reduceSeatsForSegment(startIdx, endIdx, seats);
            
            // Persistir cambios
            db.persist(reservation);
            db.merge(ride);
            
            // Crear notificación para el conductor
            String message = String.format("Nueva reserva de segmento: %d asientos de %s a %s",
                seats, stops.get(startIdx), stops.get(endIdx));
            Notification notification = new Notification(ride.getDriver(), message);
            db.persist(notification);
            
            db.getTransaction().commit();
            return reservation;
        } catch (Exception e) {
            if (db.getTransaction().isActive()) {
                db.getTransaction().rollback();
            }
            throw new RuntimeException("Error al reservar segmento de viaje", e);
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
    try {
        TypedQuery<User> query = db.createQuery("SELECT u FROM User u", User.class);
        return query.getResultList();
    }catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
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
