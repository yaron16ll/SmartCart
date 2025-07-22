package com.example.smartcart.controllers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.smartcart.models.ShoppingCart;
import com.example.smartcart.models.TempMemoryCache;
import com.example.smartcart.models.User;
import com.example.smartcart.utilities.GoogleAuthHelper;
import com.example.smartcart.utilities.interfaces.GetShoppingCartsFromDB;
import com.example.smartcart.utilities.interfaces.GetUsersFromDB;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class LoginController {
    private DatabaseReference usersRef;
    private DatabaseReference shoppingCartsRef;

    private Context context;

    public LoginController(Context context) {
        this.context = context;
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        shoppingCartsRef = FirebaseDatabase.getInstance().getReference("shoppingCarts");

    }


    public void login(String username, String password, GetUsersFromDB callback) {
        getUsersFromDB(new GetUsersFromDB() {
            @Override
            public void onSuccess(ArrayList<User> users) {
                callback.onSuccess(users);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public User checkUserSignUp(ArrayList<User> users, String username, String password) {
        if (users == null || users.isEmpty()) {
            return null;  // Return null if the list is null or empty
        }

        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private void getUsersFromDB(final GetUsersFromDB callback) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> users = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                callback.onSuccess(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }

    private void getShoppingCartsFromDB(final GetShoppingCartsFromDB callback) {
        shoppingCartsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<ShoppingCart> carts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ShoppingCart cart = snapshot.getValue(ShoppingCart.class);
                    if (cart != null) {
                        carts.add(cart);
                    }
                }
                callback.onSuccess(carts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }

    public void cacheUser(User user) {
        String userString = new Gson().toJson(user);
        TempMemoryCache.getInstance().putString("user", userString);
        cacheToken();
    }

    private void cacheToken() {
        new Thread(() -> {
            try {
                String token = GoogleAuthHelper.getAccessToken(context);
                TempMemoryCache.getInstance().putString("token", token);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void HandleShoppingCart(User user, GetShoppingCartsFromDB callback) {
        getShoppingCartsFromDB(new GetShoppingCartsFromDB() {
            @Override
            public void onSuccess(ArrayList<ShoppingCart> carts) {
                if (isOpenCartExist(carts, user)) {
                    cacheCart(carts, user);
                } else {
                    createShoppingCartInDB(user,
                            savedCart -> {
                                saveCart(savedCart);
                            },
                            e -> {
                                callback.onFailure(String.valueOf(e));
                            }
                    );
                }
                callback.onSuccess(carts);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    private void cacheCart(ArrayList<ShoppingCart> carts, User user) {
        for (ShoppingCart cart : carts) {
            if (cart.getUserID().equals(user.getID()) && cart.getIsCheckedOut() == false) {
                saveCart(cart);
            }
        }
    }

    private void saveCart(ShoppingCart cart) {
        String stringCart = new Gson().toJson(cart);
        TempMemoryCache.getInstance().putString("ShoppingCart", stringCart);
    }

    private boolean isOpenCartExist(ArrayList<ShoppingCart> carts, User user) {
        for (ShoppingCart cart : carts) {
            if (cart.getUserID().equals(user.getID()) && cart.getIsCheckedOut() == false) {
                return true;
            }
        }
        return false;
    }

    private void createShoppingCartInDB(User user, OnSuccessListener<ShoppingCart> onSuccessListener, OnFailureListener onFailureListener) {
        String cartID = shoppingCartsRef.push().getKey();
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());


        ShoppingCart cart = new ShoppingCart().setId(cartID).setUserID(user.getID()).setTimeStamp(formattedDate).setTotalPrice(0).setIsCheckedOut(false);

        shoppingCartsRef.child(cartID).setValue(cart)
                .addOnSuccessListener(aVoid -> {
                    shoppingCartsRef.child(cartID).get().addOnSuccessListener(snapshot -> {
                        ShoppingCart savedCart = snapshot.getValue(ShoppingCart.class);
                        onSuccessListener.onSuccess(savedCart);
                    }).addOnFailureListener(onFailureListener);
                })
                .addOnFailureListener(onFailureListener);
    }
}
