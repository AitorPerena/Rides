package domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "reviewer_email")
    private User reviewer;

    @ManyToOne
    private User reviewedUser; 

    private int rating; 
    private String comment; 
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();  // Asignar fecha actual por defecto


    public Review() {
        super();
    }

    public Review(User reviewer, User reviewedUser, int rating, String comment) {
        this.reviewer = reviewer;
        this.reviewedUser = reviewedUser;
        this.rating = rating;
        this.comment = comment;
        this.date = new Date();
    }

    // Getters y Setters
    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public User getReviewedUser() {
        return reviewedUser;
    }

    public void setReviewedUser(User reviewedUser) {
        this.reviewedUser = reviewedUser;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return reviewId + "; " + reviewer.getEmail() + "; " + reviewedUser.getEmail() + "; " + rating + "; " + comment;
    }
}
