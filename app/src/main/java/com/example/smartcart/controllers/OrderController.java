package com.example.smartcart.controllers;

import android.content.Context;
import android.icu.text.SimpleDateFormat;

import androidx.annotation.NonNull;

import com.example.smartcart.models.Order;
import com.example.smartcart.models.ShoppingCart;
import com.example.smartcart.models.TempMemoryCache;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrderController {
    private DatabaseReference ordersRef, cartsRef, cartItemsRef;
    private Context context;

    public OrderController(Context context) {
        this.context = context;
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        cartsRef = FirebaseDatabase.getInstance().getReference("shoppingCarts");
        cartItemsRef = FirebaseDatabase.getInstance().getReference("cartItems");

    }

    private void updateOrder(Order order) {
        String id = ordersRef.push().getKey();

        String shoppingCartString = TempMemoryCache.getInstance().getString("ShoppingCart", "");
        ShoppingCart shoppingCart = new Gson().fromJson(shoppingCartString, ShoppingCart.class);

        order.setShoppingCartID(shoppingCart.getId());
        order.setTotalPrice(shoppingCart.getTotalPrice());
        order.setUserID(shoppingCart.getUserID());

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date());
        order.setOrderDate(currentDate);

        order.setId(id);
    }
    public void deleteCartItems(String shoppingCartID) {
        Query query = cartItemsRef.orderByChild("shoppingCartID").equalTo(shoppingCartID);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    itemSnapshot.getRef().onDisconnect().cancel();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public void addOrderToDB(Order order, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        updateOrder(order);
        ordersRef.child(order.getId()).setValue(order)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public void updateShoppingCart(ShoppingCart cart, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isCheckedOut", true);
        updates.put("totalPrice", cart.getTotalPrice());

        cartsRef.child(cart.getId())
                .updateChildren(updates)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }
}
