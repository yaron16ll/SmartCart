package com.example.smartcart.models;

public class Product {
    private  String id;
    private String name;
    private String imageResId;
    private String description;
    private String categoryID;
    private float price;
    private  int amount;
    private  boolean isHealthy;
    private  boolean isHighSugar;
    private  boolean isHighFat;


    public Product() {
    }


    public String getId() {
        return id;
    }

    public Product setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Product setName(String name) {
        this.name = name;
        return this;
    }

    public String getImageResId() {
        return imageResId;
    }

    public Product setImageResId(String imageResId) {
        this.imageResId = imageResId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Product setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public Product setCategoryID(String categoryID) {
        this.categoryID = categoryID;
        return this;
    }

    public float getPrice() {
        return price;
    }

    public Product setPrice(float price) {
        this.price = price;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public Product setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public boolean getIsHealthy() {
        return isHealthy;
    }

    public Product setIsHealthy(boolean healthy) {
        isHealthy = healthy;
        return this;
    }

    public boolean getIsHighSugar() {
        return isHighSugar;
    }

    public Product setIsHighSugar(boolean highSugar) {
        isHighSugar = highSugar;
        return this;
    }

    public boolean getIsHighFat() {
        return isHighFat;
    }

    public Product setIsHighFat(boolean highFat) {
        isHighFat = highFat;
        return this;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", amount=" + amount +
                ", categoryID='" + categoryID + '\'' +
                ", isHealthy=" + isHealthy +
                ", isHighSugar=" + isHighSugar +
                ", isHighFat=" + isHighFat +
                "}\n";
    }
}
