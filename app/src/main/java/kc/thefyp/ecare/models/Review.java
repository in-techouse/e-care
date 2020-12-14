package kc.thefyp.ecare.models;

import java.io.Serializable;

public class Review implements Serializable {
    private String id, fromUser, toUser, donationId, comment;
    private float rating;

    public Review() {
    }

    public Review(String id, String fromUser, String toUser, String donationId, String comment, float rating) {
        this.id = id;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.donationId = donationId;
        this.comment = comment;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
