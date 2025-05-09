package domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Integer walletId;
    
    private float balance;
    
    private String cardNumber;
    private Date expirationDate;
    private String cvv;
    
    @OneToOne
    private User user;

    public Wallet() {
        this.balance = 0.0f;
    }

    public Wallet(User user) {
        this();
        this.user = user;
    }

    // Getters y Setters
    public float getBalance() { return balance; }
    public void setBalance(float balance) { this.balance = balance; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    // Métodos para operaciones
    public void addFunds(float amount) { this.balance += amount; }
    public boolean deductFunds(float amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}
