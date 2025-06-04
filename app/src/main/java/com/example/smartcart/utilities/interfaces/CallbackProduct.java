package com.example.smartcart.utilities.interfaces;

import com.example.smartcart.models.Product;

public interface CallbackProduct {
    void onProductClick(Product product);

    void onCartItemClick(Product product);
}
