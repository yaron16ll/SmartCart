package com.example.smartcart.utilities.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.smartcart.models.Product;

public class ProductViewModel extends ViewModel {
    private final Product product = new Product();

    public Product getProduct() {
        return product;
    }
}

