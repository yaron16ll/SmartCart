package com.example.smartcart.controllers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.smartcart.models.User;
import com.example.smartcart.utilities.interfaces.GetUsersFromDB;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SignUpController {
    private DatabaseReference usersRef;
    private Context context;

    public SignUpController(Context context) {
        this.context = context;
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void getUsersFromDB(final GetUsersFromDB callback) {
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

    public boolean isUserNameExist(ArrayList<User> users, String username) {
        if (users != null || username != null) {
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    return true;
                }
            }
            return false;
        }
        return  false;
    }

    public boolean isIDExist(ArrayList<User> users, String ID) {
        if (users != null || ID != null) {
            for (User user : users) {
                if (user.getID().equals(ID)) {
                    return true;
                }
            }
            return false;
        }
        return  false;
    }

    public void addUserToDB(User user, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        usersRef.child(user.getUsername()).setValue(user)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }
}
