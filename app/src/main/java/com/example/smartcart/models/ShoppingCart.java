package com.example.smartcart.models;

public class ShoppingCart {
    private String id;
    private String userID;
    private  String timeStamp;
    private  float totalPrice;
    private  boolean isCheckedOut;


    public ShoppingCart( ) {
     }

    public String getId() {
        return id;
    }

    public ShoppingCart setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public ShoppingCart setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public ShoppingCart setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public ShoppingCart setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    public boolean getIsCheckedOut() {
        return isCheckedOut;
    }

    public ShoppingCart setIsCheckedOut(boolean isCheckedOut) {
        this.isCheckedOut = isCheckedOut;
        return this;
    }
}
