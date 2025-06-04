package com.example.smartcart.utilities.interfaces;

import com.example.smartcart.models.User;

import java.util.ArrayList;

public interface GetUsersFromDB {

    void onSuccess(ArrayList<User> users);

    void onFailure(String errorMessage);


}
