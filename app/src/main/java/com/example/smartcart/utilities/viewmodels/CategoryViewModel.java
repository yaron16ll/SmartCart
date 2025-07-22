package com.example.smartcart.utilities.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.smartcart.models.Category;

public class CategoryViewModel extends ViewModel {
    private final Category category = new Category();

    public Category getCategory() {
        return category;
    }
}