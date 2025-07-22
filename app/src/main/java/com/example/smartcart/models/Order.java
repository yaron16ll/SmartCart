package com.example.smartcart.models;

public class Order {

    private String id;
    private String shoppingCartID;

    private String userID;
    private String orderDate;

    private String shipDate;

    private String shipStreet;

    private String shipCity;

    private float totalPrice;

    private String creditNum;

    public Order() {
    }

    public String getId() {
        return id;
    }

    public Order setId(String id) {
        this.id = id;
        return this;
    }

    public String getShoppingCartID() {
        return shoppingCartID;
    }

    public Order setShoppingCartID(String shoppingCartID) {
        this.shoppingCartID = shoppingCartID;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public Order setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public Order setOrderDate(String orderDate) {
        this.orderDate = orderDate;
        return this;
    }

    public String getShipDate() {
        return shipDate;
    }

    public Order setShipDate(String shipDate) {
        this.shipDate = shipDate;
        return this;
    }

    public String getShipStreet() {
        return shipStreet;
    }

    public Order setShipStreet(String shipStreet) {
        this.shipStreet = shipStreet;
        return this;
    }

    public String getShipCity() {
        return shipCity;
    }

    public Order setShipCity(String shipCity) {
        this.shipCity = shipCity;
        return this;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public Order setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    public String getCreditNum() {
        return creditNum;
    }

    public Order setCreditNum(String creditNum) {
        this.creditNum = creditNum;
        return this;
    }
}