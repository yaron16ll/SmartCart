package com.example.smartcart.controllers;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcart.models.CartItem;
import com.example.smartcart.models.TempMemoryCache;
import com.example.smartcart.utilities.adapters.CartItemAdapter;
import com.example.smartcart.utilities.interfaces.DeleteCartItemFromDB;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ShoppingCartController {

    private Context context;
    private CartItemAdapter cartItemAdapter;
    private DatabaseReference cartItemsRef;


    public ShoppingCartController(Context context) {
        cartItemsRef = FirebaseDatabase.getInstance().getReference("cartItems");
        this.context = context;
    }


    public void setupCartItemsRecyclerView(CartItemAdapter cartItemAdapter, RecyclerView recyclerView) {
        this.cartItemAdapter = cartItemAdapter;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(cartItemAdapter);
    }

    public void deleteCartItemFromCache(CartItem cartItem) {
        String cartItemsString = TempMemoryCache.getInstance().getString("cartItems", "");
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
        ArrayList<CartItem> cartItems = new Gson().fromJson(cartItemsString, type);
        cartItems.remove(cartItem);
        TempMemoryCache.getInstance().putString("cartItems", new Gson().toJson(cartItems));
        cartItemAdapter.updateShoppingCart(cartItems);
    }

    public void deleteCartItem(CartItem cartItem, final DeleteCartItemFromDB callback) {
        DatabaseReference itemRef = cartItemsRef.child(cartItem.getId());
        deleteCartItemFromDB(cartItem, aVoid -> {
            deleteCartItemFromCache(cartItem);
            callback.onSuccess(cartItem);
        }, e -> {
            callback.onFailure(String.valueOf(e));

        });
    }

    public void deleteCartItemFromDB(CartItem cartItem, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        cartItemsRef.child(cartItem.getId()).removeValue()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

    }
}
