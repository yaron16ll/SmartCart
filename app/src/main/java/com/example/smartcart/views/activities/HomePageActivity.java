package com.example.smartcart.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.smartcart.utilities.adapters.CategoryAdapter;
import com.example.smartcart.utilities.adapters.ProductAdapter;
import com.example.smartcart.utilities.interfaces.AddCartItemToDB;
import com.example.smartcart.utilities.interfaces.CallbackCategory;
import com.example.smartcart.utilities.interfaces.CallbackProduct;
import com.example.smartcart.R;
import com.example.smartcart.utilities.interfaces.GetCategoriesFromDB;
import com.example.smartcart.utilities.interfaces.GetProductsFromDB;
import com.example.smartcart.utilities.interfaces.UpdateCartItemToDB;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity implements CallbackProduct, CallbackCategory {

    private ProgressBar productsProgressBar, categoriesProgressBar;
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
            finish();
        }));
    }

    private void findViews() {
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
    }


    @Override
    public void onCategoryClick(Category category) {
        ArrayList<Product> filteredProducts = homePageController.onCategoryClick(category, products);
        productAdapter.updateProducts(filteredProducts);

    }

    @Override
    public void onCartItemClick(Product product) {
        if (product.getAmount() > 0) {
                  createCartItem(product);
            }
           else {
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
