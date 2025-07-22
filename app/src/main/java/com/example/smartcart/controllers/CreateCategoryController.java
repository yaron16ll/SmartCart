package com.example.smartcart.controllers;

import com.example.smartcart.models.Category;
import com.example.smartcart.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateCategoryController {
    private DatabaseReference categoriesRef;

    public CreateCategoryController() {
        this.categoriesRef = FirebaseDatabase.getInstance().getReference("categories");
    }


    public void submitCategory(Category category, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        category.setID(this.categoriesRef.push().getKey());

        categoriesRef.child(category.getID()).setValue(category)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

}
