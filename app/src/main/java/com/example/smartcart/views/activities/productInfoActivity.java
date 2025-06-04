package com.example.smartcart.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcart.models.Product;
import com.example.smartcart.R;
import com.google.gson.Gson;

public class productInfoActivity extends AppCompatActivity {

    private TextView productInfo;
    private TextView productName;
    private ImageView productImg;

    private ImageView isHealthyImg;

    private ImageView isHighFatImg;

    private ImageView isHighSugarImg;

    private Product product;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.productinfo_activity);

        product = getProduct();
        findViews();
        initView();
    }

    private Product getProduct() {
        Intent intent = getIntent();
        String productString = intent.getStringExtra("product");
        Product product = new Gson().fromJson(productString, Product.class);
        return product;
    }


    private void initView() {

        productName.setText(product.getName());

        productInfo.setText(product.getDescription());

        // Load image using Glide
        Glide.with(this).load(product.getImageResId()).into(productImg);

        initChecks();
    }

    private void initChecks() {
        boolean isHealthy = product.getIsHealthy();
        initCheck(isHealthy, isHealthyImg);
        boolean isHighFat = product.getIsHighFat();
        initCheck(isHighFat, isHighFatImg);
        boolean isHighSugar = product.getIsHighSugar();
        initCheck(isHighSugar, isHighSugarImg);
    }

    private void initCheck(boolean isChecked, ImageView imageView) {
        if (isChecked) {
            int checkGreenRes = getResources().getIdentifier("check_green", "drawable", getPackageName());
            Glide.with(this).load(checkGreenRes).into(imageView);
        } else {
            int redXRes = getResources().getIdentifier("red_x", "drawable", getPackageName());
            Glide.with(this).load(redXRes).into(imageView);
        }
    }


    private void findViews() {
        productName = findViewById(R.id.product_name);
        productInfo = findViewById(R.id.product_description);
        productImg = findViewById(R.id.product_img);
        isHighSugarImg = findViewById(R.id.first_check);
        isHighFatImg = findViewById(R.id.second_check);
        isHealthyImg = findViewById(R.id.third_check);


    }
}
