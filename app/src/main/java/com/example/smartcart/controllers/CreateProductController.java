package com.example.smartcart.controllers;

import android.net.Uri;

import com.example.smartcart.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class CreateProductController {
    private DatabaseReference productsRef;

    public CreateProductController() {
         this.productsRef = FirebaseDatabase.getInstance().getReference("products");
    }

    public String getProductID(){
        return  this.productsRef.push().getKey();
    }

    public void submitProduct(Product product, OnCompleteListener<Void> onCompleteListener) {
        productsRef.child(product.getId()).setValue(product)
                .addOnCompleteListener(onCompleteListener);
    }

    public void uploadImageToStorage(Uri imageUri, OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener) {
        if (imageUri == null) {
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> onSuccessListener.onSuccess(uri.toString()))
                        .addOnFailureListener(onFailureListener))
                .addOnFailureListener(onFailureListener);
    }
}


