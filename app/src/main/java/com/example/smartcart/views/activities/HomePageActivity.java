package com.example.smartcart.views.activities;

import static android.os.Debug.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcart.controllers.HomePageController;
import com.example.smartcart.models.CartItem;
import com.example.smartcart.models.Category;
import com.example.smartcart.models.Product;
import com.example.smartcart.models.TempMemoryCache;
import com.example.smartcart.models.User;
import com.example.smartcart.utilities.adapters.CategoryAdapter;
import com.example.smartcart.utilities.adapters.ProductAdapter;
import com.example.smartcart.utilities.interfaces.AddCartItemToDB;
import com.example.smartcart.utilities.interfaces.CallbackCategory;
import com.example.smartcart.utilities.interfaces.CallbackProduct;
import com.example.smartcart.R;
import com.example.smartcart.utilities.interfaces.GetCategoriesFromDB;
import com.example.smartcart.utilities.interfaces.GetProductsFromDB;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomePageActivity extends AppCompatActivity implements CallbackProduct, CallbackCategory {

    private ProgressBar productsProgressBar, categoriesProgressBar, recommendationsProgressBar;
    private ImageView viewCartBtn, homeBtn;
    private RecyclerView productRecyclerView, categoryRecyclerView;
    private ProductAdapter productAdapter;

    private CategoryAdapter categoryAdapter;

    private TextInputEditText categorySearch;

    private ArrayList<Product> products;
    private ArrayList<Category> categories;

    private HomePageController homePageController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_activity);

        //init Controller
        homePageController = new HomePageController(this);
        findViews();
        initViews();
        getProducts();
        getCategories();
        setUpSearchFilter();

    }

    private void getProducts() {
        ArrayList<Product> products;
        products = getCachedProducts();

        if (products == null || products.size() == 0) {
            productsProgressBar.setVisibility(View.VISIBLE);
            getProductsFromDB();

        } else {
            this.products = products;
            initProductRecyclerView();

        }
    }

    private void getCategories() {
        ArrayList<Category> categories;
        categories = getCachedCategories();

        if (categories == null || categories.size() == 0) {
            categoriesProgressBar.setVisibility(View.VISIBLE);
            getCategoriesFromDB();

        } else {
            this.categories = categories;
            initCategoryRecyclerView();

        }
    }

    private ArrayList<Category> getCachedCategories() {
        String categoriesString = TempMemoryCache.getInstance().getString("categories", "");
        Type type = new TypeToken<ArrayList<Category>>() {
        }.getType();
        ArrayList<Category> categoriesList = new Gson().fromJson(categoriesString, type);
        return categoriesList;
    }


    private ArrayList<Product> getCachedProducts() {
        String productsString = TempMemoryCache.getInstance().getString("products", "");
        Type type = new TypeToken<ArrayList<Product>>() {
        }.getType();
        ArrayList<Product> productList = new Gson().fromJson(productsString, type);
        return productList;
    }


    private void getCategoriesFromDB() {
        homePageController.getCategories(new GetCategoriesFromDB() {
            @Override
            public void onSuccess(ArrayList<Category> categoriesFromDB) {
                categoriesProgressBar.setVisibility(View.GONE);
                categories = categoriesFromDB;
                initCategoryRecyclerView();

            }

            @Override
            public void onFailure(String errorMessage) {
                categoriesProgressBar.setVisibility(View.GONE);
                showToast("Get Products failed: " + errorMessage);
            }
        });
    }


    private void getProductsFromDB() {
        homePageController.getProducts(new GetProductsFromDB() {
            @Override
            public void onSuccess(ArrayList<Product> productsFromDB) {
                productsProgressBar.setVisibility(View.GONE);
                products = productsFromDB;

                homePageController.cacheProducts(products);
                initProductRecyclerView();
            }

            @Override
            public void onFailure(String errorMessage) {
                productsProgressBar.setVisibility(View.GONE);
                showToast("Get Products failed: " + errorMessage);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initProductRecyclerView() {
        // Set up RecyclerView
        productAdapter = new ProductAdapter(products, this, this);
        homePageController.setupProductsRecyclerView(productAdapter, productRecyclerView);
    }

    private void initCategoryRecyclerView() {
        // Set up RecyclerView
        categoryAdapter = new CategoryAdapter(categories, this, this);
        homePageController.setupCategoriesRecyclerView(categoryAdapter, categoryRecyclerView);
    }

    private void setUpSearchFilter() {
        // Set up search filter
        categorySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                homePageController.filterCategoriesByName(s.toString(), products);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }


    private void initViews() {
        viewCartBtn.setOnClickListener(v -> {
            // Pass to Cart activity
            Intent intent = new Intent(HomePageActivity.this, ShoppingCartActivity.class);
            startActivity(intent);
        });

        homeBtn.setOnClickListener((v -> {
            // Pass to Login activity
            Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
            startActivity(intent);
        }));
    }

    private void findViews() {
        recommendationsProgressBar = findViewById(R.id.recommendationsProgressBar);
        productsProgressBar = findViewById(R.id.product_progressBar);
        viewCartBtn = findViewById(R.id.view_cart_btn);
        productRecyclerView = findViewById(R.id.product_layout);
        categoryRecyclerView = findViewById(R.id.category_layout);
        categoriesProgressBar = findViewById(R.id.category_progressBar);

        categorySearch = findViewById(R.id.category_search);
        homeBtn = findViewById(R.id.back_login_btn);
    }

    @Override
    public void onProductClick(Product product) {
        homePageController.onProductClick(product);
        sendEvent(product);

        SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);
        String categoryType = prefs.getString("last_category_type", "unknown");
        String categoryName = prefs.getString("last_category_name", "unknown");

        // Build event for Firebase Analytics
        Bundle bundle = new Bundle();
        bundle.putString("product_name", product.getName());
        bundle.putString("product_id", product.getId());
        bundle.putString("category_name", categoryName);
        bundle.putString("category_type", categoryType);

        FirebaseAnalytics.getInstance(this).logEvent("product_selected_custom", bundle);
    }


    private void sendEvent(Product product) {
        String userString = TempMemoryCache.getInstance().getString("user", "");
        User user = new Gson().fromJson(userString, User.class);
        String eventTime = Instant.now().toString();
        OkHttpClient client = new OkHttpClient();

        String url = "https://retail.googleapis.com/v2/projects/105020571010/locations/global/catalogs/default_catalog/userEvents:write";

        String json = "{\n" +
                "  \"eventType\": \"detail-page-view\",\n" +
                "  \"eventTime\": \"" + eventTime + "\",\n" +
                "  \"visitorId\": \"" + user.getID() + "\",\n" +
                "  \"userInfo\": {\n" +
                "    \"userId\": \"" + user.getID() + "\"\n" +
                "  },\n" +
                "  \"productDetails\": [\n" +
                "    {\n" +
                "      \"product\": {\n" +
                "        \"id\": \"" + product.getId() + "\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Request request = createRequest(url, json);
        sendEventToAPI(client, request);
    }


    private void sendEventToAPI(OkHttpClient client, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e("RETAIL_API", "Network Error", e);
                    showToast("Network error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                int code = response.code();

                runOnUiThread(() -> {
                    Log.d("RETAIL_API", "HTTP Code: " + code);
                    Log.d("RETAIL_API", "Response Body:\n" + responseBody);

                    if (response.isSuccessful()) {
                        showToast("SUCCESS");
                    } else {
                        showToast("FAILED: " + code + "\n" + responseBody);
                    }
                });
            }
        });
    }


    private void selectCategory(Category category, ArrayList<Product> products) {
        if (category.getID().equals("1")) {
            productAdapter.updateProducts(products);
        } else if (category.getID().equals("2")) {
            showRecommendations(products);
        } else {
            ArrayList<Product> filteredProducts = getFilteredProductsByCategory(category, products);
            productAdapter.updateProducts(filteredProducts);
        }
    }

    private ArrayList<Product> getFilteredProductsByCategory(Category category, ArrayList<Product> products) {
        ArrayList<Product> filteredProducts = new ArrayList<>();

        for (Product product : products) {

            if (product.getCategoryID().equals(category.getID())) {
                filteredProducts.add(product);
            }
        }
        return filteredProducts;
    }

    private void showRecommendations(ArrayList<Product> products) {
        String userString = TempMemoryCache.getInstance().getString("user", "");
        User user = new Gson().fromJson(userString, User.class);
        String eventTime = Instant.now().toString();
        OkHttpClient client = new OkHttpClient();
        String url = "https://retail.googleapis.com/v2/projects/105020571010/locations/global/catalogs/default_catalog/placements/homepage-recommendations:predict";
        String json = "{\n" +
                "  \"userEvent\": {\n" +
                "    \"eventType\": \"home-page-view\",\n" +
                "    \"visitorId\": \"" + user.getID() + "\",\n" +
                "    \"eventTime\": \"" + eventTime + "\"\n" +
                "  },\n" +
                "  \"pageSize\": 10\n" +
                "}";

        Request request = createRequest(url, json);
        getRecommendationsFromAPI(client, request);

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

    private void getRecommendationsFromAPI(OkHttpClient client, Request request) {
        runOnUiThread(() -> recommendationsProgressBar.setVisibility(View.VISIBLE));
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showToast("HTTP_ERROR Request failed");
                    recommendationsProgressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                ArrayList<Product> recommendations = getProducts(responseBody, products);

                runOnUiThread(() -> {
                    productAdapter.updateProducts(recommendations);
                    recommendationsProgressBar.setVisibility(View.GONE);
                });
            }
        });
    }


    private ArrayList<Product> toProductArray(ArrayList<String> productIds, ArrayList<Product> products) {
        ArrayList<Product> filteredProducts = new ArrayList<>();

        for (String id : productIds) {
            for (Product product : products) {
                if (id.equals(product.getId())) {
                    filteredProducts.add(product);
                }
            }
        }
        if (!filteredProducts.isEmpty())
            return filteredProducts;
        return null;
    }


    private ArrayList<Product> getProducts(String jsonResponse, ArrayList<Product> products) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray results = jsonObject.getJSONArray("results");

            ArrayList<String> productIds = new ArrayList<>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject productObj = results.getJSONObject(i);
                String productId = productObj.getString("id");
                productIds.add(productId);
            }
            ArrayList<Product> finalProducts = toProductArray(productIds, products);
            return finalProducts;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void onCategoryClick(Category category) {
        selectCategory(category, products);

        SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("last_category_name", category.getName());

        if (category.getName().equalsIgnoreCase("Recommended")) {
            editor.putString("last_category_type", "ai_recommendations");
        } else {
            editor.putString("last_category_type", "regular");
        }

        editor.apply();
    }


    @Override
    public void onCartItemClick(Product product) {
        if (product.getAmount() > 0) {
            createCartItem(product);
        } else {
            showToast("Can not add empty items");
        }
    }


    private void createCartItem(Product product) {
        homePageController.addCartItemToDB(product, new AddCartItemToDB() {
            @Override
            public void onSuccess(CartItem cartItem) {
                showToast("Cart item has been added");
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast("Cart item addition has been failed: " + errorMessage);
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        homePageController.deleteCartItems();
    }
}
