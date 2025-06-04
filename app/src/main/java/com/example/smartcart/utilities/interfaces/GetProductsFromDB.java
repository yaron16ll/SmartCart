package com.example.smartcart.utilities.interfaces;

import com.example.smartcart.models.Product;
import com.example.smartcart.models.User;

import java.util.ArrayList;

public interface GetProductsFromDB {

    void onSuccess(ArrayList<Product> products);

    void onFailure(String errorMessage);
}
