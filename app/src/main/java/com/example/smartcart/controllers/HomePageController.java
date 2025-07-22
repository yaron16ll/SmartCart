package com.example.smartcart.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.util.Log;
import android.view.View;

import com.example.smartcart.models.CartItem;
import com.example.smartcart.models.ShoppingCart;
import com.example.smartcart.models.TempMemoryCache;
import com.example.smartcart.models.User;
import com.example.smartcart.utilities.interfaces.AddCartItemToDB;
import com.example.smartcart.views.activities.HomePageActivity;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.JustifyContent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcart.models.Category;
import com.example.smartcart.models.Product;
import com.example.smartcart.utilities.adapters.CategoryAdapter;
import com.example.smartcart.utilities.adapters.ProductAdapter;
import com.example.smartcart.utilities.interfaces.GetCategoriesFromDB;
import com.example.smartcart.utilities.interfaces.GetProductsFromDB;
import com.example.smartcart.views.activities.productInfoActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HomePageController {

    private Context context;
    private DatabaseReference productsRef;
    private DatabaseReference categoriesRef;

    private DatabaseReference cartItemsRef;
    private ProductAdapter productAdapter;

    private ArrayList<Product> recommendations;

    public HomePageController(Context context) {
        this.context = context;
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        categoriesRef = FirebaseDatabase.getInstance().getReference("categories");
        cartItemsRef = FirebaseDatabase.getInstance().getReference("cartItems");

    }


    public void setupCategoriesRecyclerView(CategoryAdapter categoryAdapter, RecyclerView recyclerView) {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setAlignItems(AlignItems.FLEX_START);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(categoryAdapter);
    }

    public void setupProductsRecyclerView(ProductAdapter productAdapter, RecyclerView recyclerView) {
        this.productAdapter = productAdapter;
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3)); // 3 columns
        recyclerView.setAdapter(productAdapter);
    }

    public void filterCategoriesByName(String query, ArrayList<Product> products) {
        if (query == null || products == null) return;

        ArrayList<Product> filteredProducts = new ArrayList<>();
        String queryLower = query.toLowerCase().trim();

        if (queryLower.isEmpty()) {
            // Show all categories if query is empty
            filteredProducts.addAll(products);
        } else {
            for (Product product : products) {
                if (product.getName().toLowerCase().contains(queryLower)) {
                    filteredProducts.add(product);
                }
            }
        }

        updateDisplayedCategories(filteredProducts);
    }

    private void updateDisplayedCategories(ArrayList<Product> filteredCategories) {
        if (filteredCategories == null) {
            filteredCategories = new ArrayList<>();
        }
        productAdapter.updateProducts(filteredCategories);
    }

    public void onProductClick(Product product) {
        Intent intent = new Intent(context, productInfoActivity.class);

        String productString = new Gson().toJson(product);
        intent.putExtra("product", productString);

        context.startActivity(intent);
    }


    private void getRecommendations(OkHttpClient client, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //   Debug.log("error event");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
            }
        });
    }


    public void getProducts(final GetProductsFromDB callback) {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Product> products = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        products.add(product);
                    }
                }
                callback.onSuccess(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }

    public void getCategories(final GetCategoriesFromDB callback) {
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                callback.onSuccess(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }

    public void cacheProducts(ArrayList<Product> products) {
        String productsString = new Gson().toJson(products);
        TempMemoryCache.getInstance().putString("products", productsString);

    }

    public void addCartItemToDB(Product product, final AddCartItemToDB callback) {
        if (isProductExistOnCache(product)) {
            updateCartItem(product, callback);
        } else {
            addCartItem(product, callback);
        }
    }

    private void updateCartItem(Product product, AddCartItemToDB callback) {
        CartItem cartItem = getCombinedCartItems(product);


        updateCartItemToDB(cartItem, aVoid -> {
            cacheCartItem(cartItem);
            callback.onSuccess(cartItem);
        }, e -> {
            callback.onFailure(String.valueOf(e));

        });

    }

    private CartItem getCombinedCartItems(Product product) {
        ArrayList<CartItem> cartItems;

        String cartItemID = cartItemsRef.push().getKey();
        String shoppingCartID = getShoppingCartID();
        CartItem newCartItem = new CartItem().setId(cartItemID).setProductID(product.getId()).setShoppingCartID(shoppingCartID).setTotalPrice(product.getAmount() * product.getPrice()).setAmount(product.getAmount()).setName(product.getName()).setImageSrc(product.getImageResId());

        String cartItemsString = TempMemoryCache.getInstance().getString("cartItems", null);
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
        cartItems = new Gson().fromJson(cartItemsString, type);

        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(product.getName())) {
                CartItem c = new CartItem().setTotalPrice(getCombinedTotalPrice(cartItem, newCartItem)).setAmount(getCombinedAmount(cartItem, newCartItem)).setName(cartItem.getName())
                        .setImageSrc(cartItem.getImageSrc()).setShoppingCartID(getShoppingCartID()).setProductID(cartItem.getProductID()).setId(cartItem.getId());
                return c;
            }
        }
        return null;
    }

    private void addCartItem(Product product, final AddCartItemToDB callback) {
        String cartItemID = cartItemsRef.push().getKey();
        String shoppingCartID = getShoppingCartID();
        CartItem cartItem = new CartItem().setId(cartItemID).setProductID(product.getId()).setShoppingCartID(shoppingCartID).setTotalPrice(product.getAmount() * product.getPrice()).setAmount(product.getAmount()).setName(product.getName()).setImageSrc(product.getImageResId());

        addCartItemToDB(cartItem, aVoid -> {
            cacheCartItem(cartItem);
            callback.onSuccess(cartItem);
        }, e -> {
            callback.onFailure(String.valueOf(e));

        });
    }

    private boolean isProductExistOnCache(Product product) {
        ArrayList<CartItem> cartItems;

        String cartItemsString = TempMemoryCache.getInstance().getString("cartItems", null);
        if (cartItemsString == null) {
            return false;
        }

        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
        cartItems = new Gson().fromJson(cartItemsString, type);

        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(product.getName())) {
                return true;
            }
        }
        return false;
    }

    public void deleteCartItems() {
        Query query = cartItemsRef.orderByChild("shoppingCartID").equalTo(getShoppingCartID());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    itemSnapshot.getRef().onDisconnect().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private String getShoppingCartID() {
        ShoppingCart shoppingCart;
        String shoppingCartString = TempMemoryCache.getInstance().getString("ShoppingCart", "");
        shoppingCart = new Gson().fromJson(shoppingCartString, ShoppingCart.class);
        return shoppingCart.getId();
    }

    public void addCartItemToDB(CartItem cartItem, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        cartItemsRef.child(cartItem.getId())
                .setValue(cartItem)
                .addOnSuccessListener(aVoid -> {
                    cartItemsRef.child(cartItem.getId()).onDisconnect().removeValue();
                    onSuccessListener.onSuccess(aVoid);
                })
                .addOnFailureListener(onFailureListener);
    }

    public void updateCartItemToDB(CartItem cartItem, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("totalPrice", cartItem.getTotalPrice());
        updates.put("amount", cartItem.getAmount());

        cartItemsRef.child(cartItem.getId())
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    onSuccessListener.onSuccess(aVoid);
                })
                .addOnFailureListener(onFailureListener);
    }

    private void cacheCartItem(CartItem cartItem) {

        if (AreCartItemsCached() == true) {

            String cartItemsString = TempMemoryCache.getInstance().getString("cartItems", null);
            Type type = new TypeToken<ArrayList<CartItem>>() {
            }.getType();

            ArrayList<CartItem> cartItems = new Gson().fromJson(cartItemsString, type);
            ArrayList<CartItem> newCartItems = updateCartItems(cartItems, cartItem);

            cartItemsString = new Gson().toJson(newCartItems);
            TempMemoryCache.getInstance().putString("cartItems", cartItemsString);

        } else {
            ArrayList<CartItem> cartItems = new ArrayList<>();
            cartItems.add(cartItem);
            String cartItemsString = new Gson().toJson(cartItems);
            TempMemoryCache.getInstance().putString("cartItems", cartItemsString);
        }
    }

    private ArrayList<CartItem> updateCartItems(ArrayList<CartItem> cartItems, CartItem newCartItem) {
        ArrayList<CartItem> newCartItems;

        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(newCartItem.getName())) {
                newCartItems = deleteExistItem(cartItems, cartItem);
                newCartItems.add(newCartItem);
                return newCartItems;
            }
        }
        cartItems.add(newCartItem);
        return cartItems;
    }

    private ArrayList<CartItem> deleteExistItem(ArrayList<CartItem> cartItems, CartItem c) {
        ArrayList<CartItem> newCartItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(c.getName())) {
                continue;
            }
            newCartItems.add(cartItem);
        }
        return newCartItems;
    }


    private float getCombinedTotalPrice(CartItem c1, CartItem c2) {
        return c1.getTotalPrice() + c2.getTotalPrice();
    }

    private int getCombinedAmount(CartItem c1, CartItem c2) {
        return c1.getAmount() + c2.getAmount();
    }

    private boolean AreCartItemsCached() {
        String cartItemsString = TempMemoryCache.getInstance().getString("cartItems", null);
        if (cartItemsString != null)
            return true;
        return false;
    }
}
