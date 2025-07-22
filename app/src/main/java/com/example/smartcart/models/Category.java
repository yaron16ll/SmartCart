package com.example.smartcart.models;

import com.google.firebase.database.PropertyName;

public class Category {
    private String name;
    private  String ID;


    public Category() {
    }


    public String getName() {
        return name;
    }

    public Category setName(String name) {
        this.name = name;
        return this;
    }

    @PropertyName("ID")

    public String getID() {
        return ID;
    }

    @PropertyName("ID")

    public Category setID(String ID) {
        this.ID = ID;
        return this;
    }

    public String toString() {

        return name;
    }
}
