package domain;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class Admin extends User {
    
    public Admin() {
        super();
    }

    public Admin(String email, String password) {
        super(email, password);
    }

 
    public void banUser(User user, int days) {
        if (user == null) return;
        
        user.banUser(days);
    }
    
 
    public void unbanUser(User user) {
        if (user == null) return;
        
        user.unbanUser();
    }
    

    public boolean deleteUser(User user) {
        
        return true;
    }
    

    public void respondToReport(Report report, String response) {
        if (report == null || response == null || response.trim().isEmpty()) return;
        
        report.setAdminResponse(response);
        report.setResolved(true);
    }
}