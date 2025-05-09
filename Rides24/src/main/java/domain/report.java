package domain;

import javax.persistence.*;

import java.util.Date;

@Entity
public class Report {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User reporter;
    
    @ManyToOne
    private User reportedUser;
    
    private String description;
    private Date reportDate;
    private boolean resolved;
    private String adminResponse;
    private Date resolutionDate;

    public Report() {
        this.reportDate = new Date();
        this.resolved = false;
    }

    public Report(User reporter, User reportedUser, String description) {
        this();
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.description = description;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public User getReportedUser() {
        return reportedUser;
    }

    public void setReportedUser(User reportedUser) {
        this.reportedUser = reportedUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
        if (resolved	) {
            this.resolutionDate = new Date();
        }
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public Date getResolutionDate() {
        return resolutionDate;
    }
}