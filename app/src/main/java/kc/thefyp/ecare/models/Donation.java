package kc.thefyp.ecare.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Donation implements Serializable {
    private String id, userId, category, address, name, description, contact, donatedTo;
    private double latitude, longitude;
    private int quantity;
    private List<String> images;
    private boolean isDonated;
    private long timestamps;

    public Donation() {
        images = new ArrayList<>();
        isDonated = false;
        donatedTo = "";
        timestamps = 0;
    }

    public Donation(String id, String userId, String category, String address, String name, String description, String contact, String donatedTo, double latitude, double longitude, int quantity, List<String> images, boolean isDonated, long timestamps) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.address = address;
        this.name = name;
        this.description = description;
        this.contact = contact;
        this.donatedTo = donatedTo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.quantity = quantity;
        this.images = images;
        this.isDonated = isDonated;
        this.timestamps = timestamps;
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

    public String getDonatedTo() {
        return donatedTo;
    }

    public void setDonatedTo(String donatedTo) {
        this.donatedTo = donatedTo;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public boolean isDonated() {
        return isDonated;
    }

    public void setDonated(boolean donated) {
        isDonated = donated;
    }

    public long getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(long timestamps) {
        this.timestamps = timestamps;
    }
}
