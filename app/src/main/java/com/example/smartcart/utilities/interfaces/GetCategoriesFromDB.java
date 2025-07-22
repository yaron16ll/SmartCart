package com.example.smartcart.utilities.interfaces;

import com.example.smartcart.models.Category;
import com.example.smartcart.models.Product;

import java.util.ArrayList;

public interface GetCategoriesFromDB {
    void onSuccess(ArrayList<Category> categories);

    void onFailure(String errorMessage);
}
