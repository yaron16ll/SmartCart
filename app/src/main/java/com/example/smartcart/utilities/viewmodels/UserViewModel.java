package com.example.smartcart.utilities.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.smartcart.models.User;

public class UserViewModel extends ViewModel {
    private final User user = new User();

    public User getUser() {
        return user;
    }
}