package com.example.smartcart.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String id;
    private String productID;
    private String shoppingCartID;
    private float totalPrice;
    private int amount;
    private String name;

    private String imageResId;


    public CartItem() {
    }

    public CartItem setId(String id) {
        this.id = id;
        return this;
    }

    public CartItem setProductID(String productID) {
        this.productID = productID;
        return this;
    }

    public CartItem setShoppingCartID(String shoppingCartID) {
        this.shoppingCartID = shoppingCartID;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getProductID() {
        return productID;
    }

    public String getShoppingCartID() {
        return shoppingCartID;
    }

    public String getName() {
        return name;
    }

    public CartItem setName(String name) {
        this.name = name;
        return this;
    }

    public CartItem setImageSrc(String imageResId) {
        this.imageResId = imageResId;
        return this;
    }

    public String getImageSrc() {
        return imageResId;
    }


    public float getTotalPrice() {
        return totalPrice;
    }

    public int getAmount() {
        return amount;
    }

    public CartItem setAmount(int amount) {
        this.amount = amount;
        return this;
    }


    public CartItem setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        CartItem cartItem = (CartItem) o;

        return this.id != null && this.id.equals(cartItem.id);
    }

    @Override
    public int hashCode() {
        return this.id != null ? this.id.hashCode() : 0;
    }


}
