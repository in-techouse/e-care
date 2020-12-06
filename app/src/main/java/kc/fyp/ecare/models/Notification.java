package kc.fyp.ecare.models;

import java.io.Serializable;

public class Notification implements Serializable {
    private String notificationId, message, userId, type, id;

    public Notification() {
    }

    public Notification(String notificationId, String message, String userId, String type, String id) {
        this.notificationId = notificationId;
        this.message = message;
        this.userId = userId;
        this.type = type;
        this.id = id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
