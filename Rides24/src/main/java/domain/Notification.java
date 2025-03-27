package domain;

import java.io.Serializable;
import javax.persistence.*;

@Entity
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Integer notificationId;

    @ManyToOne
    private User user;

    private String message;
    private boolean read; 
    public Notification() {
        super();
    }

    public Notification(User user, String message) {
        this.user = user;
        this.message = message;
        this.read = false;
    }

    // Getters y Setters
    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return notificationId + "; " + user.getEmail() + "; " + message + "; " + read;
    }
}
