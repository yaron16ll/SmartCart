package com.example.smartcart.utilities.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.smartcart.models.Order;
import com.example.smartcart.models.Product;

public class OrderViewModel extends ViewModel {
    private final Order order = new Order();

    public Order getOrder() {
        return order;
    }
}

