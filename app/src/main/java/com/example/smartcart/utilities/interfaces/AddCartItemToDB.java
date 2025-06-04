package com.example.smartcart.utilities.interfaces;

import com.example.smartcart.models.CartItem;


public interface AddCartItemToDB {
    void onSuccess(CartItem cartItem);

    void onFailure(String errorMessage);

}
