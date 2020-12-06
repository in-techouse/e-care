package kc.thefyp.ecare.models;

import java.io.Serializable;

public class Request implements Serializable {
    private String id, donationId, toUser, fromUser, userName, description;
    private long timestamps;

    public Request() {
    }

    public Request(String id, String donationId, String toUser, String fromUser, String userName, String description, long timestamps) {
        this.id = id;
        this.donationId = donationId;
        this.toUser = toUser;
        this.fromUser = fromUser;
        this.userName = userName;
        this.description = description;
        this.timestamps = timestamps;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(long timestamps) {
        this.timestamps = timestamps;
    }
}
