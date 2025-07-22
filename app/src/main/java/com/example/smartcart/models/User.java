package com.example.smartcart.models;

import androidx.databinding.BaseObservable;

import java.io.Serializable;
import java.util.ArrayList;

public class User extends BaseObservable implements Serializable {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private String city;
    private String street;

    public boolean getIsCustomer() {
        return isCustomer;
    }

    public User setIsCustomer(boolean isCustomer) {
        this.isCustomer = isCustomer;
        return this;
    }

    private  boolean isCustomer;
    private ArrayList<Product> products;



    public User() {
        // Default constructor
    }



    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }


    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getID() {
        return id;
    }

    public User setID(String id) {
        this.id = id;
        return this;
    }


    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }


    public String getCity() {
        return city;
    }

    public User setCity(String city) {
        this.city = city;
        return this;
    }


    public String getStreet() {
        return street;
    }

    public User setStreet(String street) {
        this.street = street;
        return this;
    }


    public ArrayList<Product> getProducts() {
        if (products == null) {
            products = new ArrayList<>();
        }
        return products;
    }

    public User setProducts(ArrayList<Product> products) {
        this.products = products;
        return this;
    }

    public void addProduct(Product product) {
        if (products == null) {
            products = new ArrayList<>();
        }
        products.add(product);

    }
}
