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

    /**
     * Método para banear a un usuario por un número de días
     */
    public void banUser(User user, int days) {
        if (user == null) return;
        
        user.banUser(days);
    }
    
    /**
     * Método para quitar el baneo a un usuario
     */
    public void unbanUser(User user) {
        if (user == null) return;
        
        user.unbanUser();
    }
    
    /**
     * Método para eliminar permanentemente un usuario
     */
    public boolean deleteUser(User user) {
        return true;
    }
    
    /**
     * Método para responder a un reporte
     */
    public void respondToReport(Report report, String response) {
        if (report == null || response == null || response.trim().isEmpty()) return;
        
        report.setAdminResponse(response);
        report.setResolved(true);
    }
}