package kc.fyp.ecare.models;

import java.io.Serializable;

public class Announcement implements Serializable {
    private String id, userId;

    public Announcement() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
