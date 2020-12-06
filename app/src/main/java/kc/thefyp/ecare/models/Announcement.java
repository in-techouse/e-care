package kc.thefyp.ecare.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Announcement implements Serializable {
    private String id, userId, category, address, name, description, contact, donatedFrom;
    private double latitude, longitude;
    private List<String> images;
    private boolean isFilled;

    public Announcement() {
        images = new ArrayList<>();
        isFilled = false;
        donatedFrom = "";
    }

    public Announcement(String id, String userId, String category, String address, String name, String description, String contact, String donatedFrom, double latitude, double longitude, List<String> images, boolean isFilled) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.address = address;
        this.name = name;
        this.description = description;
        this.contact = contact;
        this.donatedFrom = donatedFrom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.images = images;
        this.isFilled = isFilled;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDonatedFrom() {
        return donatedFrom;
    }

    public void setDonatedFrom(String donatedFrom) {
        this.donatedFrom = donatedFrom;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public void setFilled(boolean filled) {
        isFilled = filled;
    }
}
