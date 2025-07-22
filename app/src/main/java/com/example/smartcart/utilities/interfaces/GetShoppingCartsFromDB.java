package com.example.smartcart.utilities.interfaces;

import com.example.smartcart.models.ShoppingCart;

import java.util.ArrayList;

public interface GetShoppingCartsFromDB {

    void onSuccess(ArrayList<ShoppingCart> shoppingCarts);

    void onFailure(String errorMessage);

}
