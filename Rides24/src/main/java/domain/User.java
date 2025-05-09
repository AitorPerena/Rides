package domain;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.*;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)  // Estrategia de herencia
@Entity
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String email;  // El correo electrónico será la clave primaria
    public String name;  // Nombre del usuario
    private String password;  // Contraseña del usuario
    private String profileImagePath = "default_profile.png";
    private boolean banned;
    private Date banEndDate;
    private double averageRating = 0.0;
    
    @OneToOne(cascade = CascadeType.ALL)
    private Wallet wallet;

   
    public User() {
        super();
    }

    // Constructor con parámetros
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.banned= false;
    }

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isBanned() {
    	if (!banned) return false;
        if (banEndDate == null) return true; // Baneo permanente
        return new Date().before(banEndDate);
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public Date getBanEndDate() {
        return banEndDate;
    }

    public void setBanEndDate(Date banEndDate) {
        this.banEndDate = banEndDate;
        this.banned = banEndDate != null && banEndDate.after(new Date());
    }

    public void banUser(int days) {
        this.banned = true;
        if (days > 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, days);
            this.banEndDate = cal.getTime();
        } else {
            this.banEndDate = null; // Baneo permanente
        }
    }

    public void unbanUser() {
        this.banned = false;
        this.banEndDate = null;
    }
    
    public Wallet getWallet() {
    	return wallet;	
    }
    
    public void setWallet(Wallet wallet) {
    	this.wallet = wallet;
    }
    
    public String getProfileImagePath() { 
    	return profileImagePath; 
    }
    
    public void setProfileImagePath(String path) { 
    	this.profileImagePath = path; 
    }
    
    public double getAverageRating() {
    	return averageRating; 
    }
    
    public void setAverageRating(double rating) { 
    	this.averageRating = rating; 
    }
    
    public void updateAverageRating() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return email.equals(user.email);
    }


    public boolean isCurrentlyBanned() {
        if (!banned) return false;
        if (banEndDate == null) return true;
        return banEndDate.after(new Date());
    }

    public String getBanRemainingTime() {
        if (!isCurrentlyBanned()) return "No está baneado";
        
        if (banEndDate == null) return "Baneo permanente";
        
        long diff = banEndDate.getTime() - new Date().getTime();
        long days = diff / (1000 * 60 * 60 * 24);
        long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        
        return days + " días y " + hours + " horas restantes";
    }

    @Override
    public String toString() {
        return email + "; " + name;
    }
}