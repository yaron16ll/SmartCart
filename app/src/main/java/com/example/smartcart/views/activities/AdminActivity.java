package com.example.smartcart.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcart.R;

public class AdminActivity extends AppCompatActivity {
    ImageView addProductBtn, addCategoryBtn,backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        findViews();
        initViews();
    }

    private void initViews() {

        addProductBtn.setOnClickListener(v -> passToAddProductActivity());
        addCategoryBtn.setOnClickListener(v -> passToAddCategoryActivity());
        backBtn.setOnClickListener(v -> passToLoginActivity());

    }

    private void passToAddCategoryActivity() {
        Intent intent = new Intent(AdminActivity.this, CreateCategoryActivity.class);
        startActivity(intent);
    }
    private void passToLoginActivity() {
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void passToAddProductActivity() {
        Intent intent = new Intent(AdminActivity.this, FirstCreateProductActivity.class);
        startActivity(intent);
    }

    private void findViews() {
        addProductBtn = findViewById(R.id.addProductBtn);
        addCategoryBtn = findViewById(R.id.addCategoryBtn);
        backBtn = findViewById(R.id.back_login_btn);

    }
}