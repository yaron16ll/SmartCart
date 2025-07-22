package com.example.smartcart.views.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcart.controllers.ShoppingCartController;
import com.example.smartcart.models.CartItem;
import com.example.smartcart.models.ShoppingCart;
import com.example.smartcart.models.TempMemoryCache;
import com.example.smartcart.utilities.adapters.CartItemAdapter;
import com.example.smartcart.R;
import com.example.smartcart.utilities.interfaces.CallbackCartItem;
import com.example.smartcart.utilities.interfaces.DeleteCartItemFromDB;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

public class ShoppingCartActivity extends AppCompatActivity implements CallbackCartItem {
    private RecyclerView cartItemsRecyclerView;
    private ArrayList<CartItem> cartItems;
    private TextInputEditText cartItemSearch;
    private CartItemAdapter cartItemAdapter;

    private ImageView emptyCartItemsImg, backBtn, orderbtn;
    private ShoppingCartController shoppingCartController;
    private TextView totalPriceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoppingcart_activity);

        shoppingCartController = new ShoppingCartController(this);

        findViews();
        initViews();
        initItems();
        setTotalPrice();
        setUpSearchFilter();
    }

    private void setTotalPrice() {
        float totalPrice = 0;

        if (cartItems == null || cartItems.isEmpty()) {
            this.totalPriceText.setText("Total Price:" + String.valueOf(totalPrice));
        } else {
            for (CartItem cartItem : cartItems) {
                totalPrice = cartItem.getTotalPrice() + totalPrice;
            }
            this.totalPriceText.setText("Total Price: " + String.format(Locale.getDefault(), "%.2f", totalPrice));

        }
    }

    private void initViews() {
        backBtn.setOnClickListener(v -> passToHomePage());
        orderbtn.setOnClickListener(v -> passToOrderActivity());

    }

    private void passToHomePage() {
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

    private void passToOrderActivity() {
        Intent intent = new Intent(this, OrderActivity.class);
        float totalPrice = 0;
        boolean isCartEmpty = true;
        String priceText = totalPriceText.getText().toString();
        String cleaned = priceText.replace("Total Price:", "").trim();

        String shoppingCartString = TempMemoryCache.getInstance().getString("ShoppingCart", "");
        ShoppingCart shoppingCart = new Gson().fromJson(shoppingCartString, ShoppingCart.class);
        totalPrice = Float.parseFloat(cleaned);
         if (totalPrice > 0){
             isCartEmpty = false;
         }

        shoppingCart.setTotalPrice(totalPrice);
        String shoppingCartStrg = new Gson().toJson(shoppingCart);

        TempMemoryCache.getInstance().putString("ShoppingCart", shoppingCartStrg);

        if (isCartEmpty) {
            showToast("Your cart is empty!");
        } else {
            startActivity(intent);
            SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);
            prefs.edit().putLong("checkout_start_time", System.currentTimeMillis()).apply();

        }
    }

    private void initItemsRecyclerView() {
        // Set up RecyclerView
        cartItemAdapter = new CartItemAdapter(cartItems, this, this);
        shoppingCartController.setupCartItemsRecyclerView(cartItemAdapter, cartItemsRecyclerView);
    }

    private ArrayList<CartItem> getCachedItems() {
        String cartItemsString = TempMemoryCache.getInstance().getString("cartItems", null);
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
        return new Gson().fromJson(cartItemsString, type);
    }

    private void initItems() {
        cartItems = getCachedItems();
        if (cartItems == null || cartItems.isEmpty()) {
            emptyCartItemsImg.setVisibility(View.VISIBLE);
        } else {
            emptyCartItemsImg.setVisibility(View.GONE);
            initItemsRecyclerView();

        }
    }


    private void setUpSearchFilter() {
        cartItemSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emptyCartItemsImg.getVisibility() == View.GONE) {
                    filterItemsByName(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }

    public void filterItemsByName(String query) {
        ArrayList<CartItem> filteredCartItems = new ArrayList<>();
        String queryLower = query.toLowerCase().trim();

        if (queryLower.isEmpty()) {
            filteredCartItems.addAll(cartItems);
        } else {
            for (CartItem cartItem : cartItems) {
                if (cartItem.getName().toLowerCase().contains(queryLower)) {
                    filteredCartItems.add(cartItem);
                }
            }
        }

        updateDisplayedItems(filteredCartItems);
    }

    public void updateDisplayedItems(ArrayList<CartItem> cartItems) {
        cartItemAdapter.updateShoppingCart(cartItems);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteItemClick(CartItem cartItem) {
        shoppingCartController.deleteCartItem(cartItem, new DeleteCartItemFromDB() {
            @Override
            public void onSuccess(CartItem cartItem) {
                cartItems = getCachedItems();
                updateDisplayedItems(cartItems);
                showEmptyCartIcon();
                showToast("Cart item has been deleted");
                setTotalPrice();
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast("Cart item deletion has been failed: " + errorMessage);
            }
        });
    }

    private void showEmptyCartIcon() {
        if (cartItemAdapter.isShoppingCartEmpty()) {
            emptyCartItemsImg.setVisibility(View.VISIBLE);
        } else {
            emptyCartItemsImg.setVisibility(View.GONE);
        }
    }


    private void findViews() {
        cartItemsRecyclerView = findViewById(R.id.cartItemsList);
        emptyCartItemsImg = findViewById(R.id.empty_items_img);
        cartItemSearch = findViewById(R.id.cartItem_search);
        backBtn = findViewById(R.id.back_btn);
        totalPriceText = findViewById(R.id.total_price);
        orderbtn = findViewById(R.id.orderbtn);
    }
}
