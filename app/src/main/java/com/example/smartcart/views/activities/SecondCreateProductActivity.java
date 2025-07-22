package com.example.smartcart.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.smartcart.R;
import com.example.smartcart.controllers.CreateProductController;
import com.example.smartcart.controllers.HomePageController;
import com.example.smartcart.databinding.CreateProductActivityBinding;
import com.example.smartcart.models.Category;
import com.example.smartcart.models.Product;
import com.example.smartcart.models.ShoppingCart;
import com.example.smartcart.models.TempMemoryCache;
import com.example.smartcart.models.User;
import com.example.smartcart.utilities.interfaces.GetCategoriesFromDB;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SecondCreateProductActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int IMAGE_PICK_REQUEST_CODE = 1;
    private static final int DEFAULT_IMAGE_RES_ID = R.drawable.upload_image;

    private ArrayList<Category> categories;
    private boolean isProductImgPickerActive = false;
    private Uri ProductImageUri;
    private Spinner categorySpinner;
    private Button submitBtn;
    private CheckBox isHighFat, isHighSugar, isHealthy;
    private ProgressBar progressBar;
    private ImageView productImgBtn, productImage;

    private Category selectedCategory;
    private CreateProductController createProductController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_create_product);

        createProductController = new CreateProductController();

        findViews();
        getCategoriesFromDB();
        checkFields();

    }

    private void findViews() {
        categorySpinner = findViewById(R.id.spinnerCategory);
        submitBtn = findViewById(R.id.submitBtn);
        isHighFat = findViewById(R.id.isHighFat);
        isHighSugar = findViewById(R.id.isHighSugar);
        isHealthy = findViewById(R.id.isHealthy);
        progressBar = findViewById(R.id.progressBar);
        productImgBtn = findViewById(R.id.productImgBtn);
        productImage = findViewById(R.id.productImage);
    }

    private void getCategoriesFromDB() {
        new HomePageController(this).getCategories(new GetCategoriesFromDB() {
            @Override
            public void onSuccess(ArrayList<Category> categoriesFromDB) {
                categories = categoriesFromDB;
                categories.remove(0); // remove All
                categories.remove(0); // remove Recommended

                Category placeholder = new Category();
                placeholder.setName("Select a category:");
                categories.add(0, placeholder);
                initViews();

            }

            @Override
            public void onFailure(String errorMessage) {
                showToast("Get Categories failed: " + errorMessage);
            }
        });
    }

    private void checkFields() {
        boolean areFieldsFilled = ProductImageUri != null && categorySpinner.getSelectedItemPosition() != 0;
        submitBtn.setEnabled(areFieldsFilled);
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                Log.d("ImagePicker", "Selected URI: " + imageUri);

                if (isProductImgPickerActive) {
                    ProductImageUri = imageUri;
                    Glide.with(this).load(ProductImageUri).apply(new RequestOptions().override(800, 800)).into(productImage);
                }
                checkFields();
            }
        }
    }

    private void submit() {
        showLoading(true);

        if (ProductImageUri != null) {
            createProductController.uploadImageToStorage(
                    ProductImageUri,
                    downloadUrl -> {
                        Product product = createProduct(downloadUrl);
                        createProductController.submitProduct(product, task -> {
                            if (task.isSuccessful()) {
                                saveProductToCatalog(product);
                            } else {
                                showToast("Failed to add product");
                                showLoading(false);

                            }
                        });
                    },
                    e -> {
                        showToast("Failed to upload product image");
                        showLoading(false);
                    }
            );
        } else {
            showToast("Please pick product");
            showLoading(false);
        }
    }

    private void saveProductToCatalog(Product product) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://retail.googleapis.com/v2/projects/105020571010/locations/global/catalogs/default_catalog/branches/0/products?productId=" + product.getId();

        String json = "{\n" +
                "  \"id\": \"" + product.getId() + "\",\n" +
                "  \"title\": \"" + product.getName() + "\",\n" +
                "  \"type\": \"PRIMARY\",\n" +
                "  \"categories\": [\"" + selectedCategory.getName() + "\"],\n" +
                "  \"brands\": [\"FreshFarm\"],\n" +
                "  \"priceInfo\": {\n" +
                "    \"price\": " + product.getPrice() + ",\n" +
                "    \"originalPrice\": " + product.getPrice() + ",\n" +
                "    \"currencyCode\": \"ILS\"\n" +
                "  },\n" +
                "  \"availability\": \"IN_STOCK\"\n" +
                "}";

        Request request = createRequest(url, json);
        sendEventToAPI(client, request);
    }


    private Request createRequest(String url, String json) {
        String token = TempMemoryCache.getInstance().getString("token", null);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")

                .build();

        return request;
    }

    private void sendEventToAPI(OkHttpClient client, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    android.util.Log.e("RETAIL_API", "Network Error", e);
                    showToast("Network error: " + e.getMessage());
                    showLoading(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                int code = response.code();

                runOnUiThread(() -> {
                    showLoading(false);
                    Log.d("RETAIL_API", "HTTP Code: " + code);
                    Log.d("RETAIL_API", "Response Body:\n" + responseBody);

                    if (response.isSuccessful()) {
                        showToast("The product has been added");
                        passToNextActivity();
                    } else {
                        showToast("FAILED: " + code + "\n" + responseBody);
                    }


                });
            }
        });
    }

    private void passToNextActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
        finish();
    }

    private Product createProduct(String productImageUrl) {
        String value = getIntent().getStringExtra("product");
        Product product = new Gson().fromJson(value, Product.class);
        selectedCategory = (Category) categorySpinner.getSelectedItem();

        return product
                .setIsHealthy(isHealthy.isChecked())
                .setIsHighFat(isHighFat.isChecked())
                .setIsHighSugar(isHighSugar.isChecked())
                .setImageResId(productImageUrl)
                .setCategoryID(getCategoryID(categorySpinner.getSelectedItem().toString()))
                .setId(createProductController.getProductID());
    }

    private String getCategoryID(String categoryName) {
        for (Category c : categories) {
            if (c.getName().equals(categoryName)) {
                return c.getID();
            }
        }

        return "";
    }


    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        submitBtn.setEnabled(!isLoading);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        checkFields();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void initViews() {
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(this);

        productImgBtn.setOnClickListener(v -> {
            isProductImgPickerActive = true;
            openImagePicker();
        });
        submitBtn.setOnClickListener(v -> submit());

    }

}