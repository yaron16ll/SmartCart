package com.example.smartcart.utilities.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcart.models.Product;
import com.example.smartcart.utilities.interfaces.CallbackProduct;
import com.example.smartcart.R;
import com.example.smartcart.views.activities.ShoppingCartActivity;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private ArrayList<Product> products;
    private Context context;
    private CallbackProduct listener;


    public ProductAdapter(ArrayList<Product> products, CallbackProduct listener, Context context) {
        this.context = context;
        this.listener = listener;
        this.products = products != null ? products : new ArrayList<>();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_layout, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        Product product = products.get(position);
        Glide.with(context).load(product.getImageResId()).into(holder.image);
        holder.name.setText(product.getName());
        holder.price.setText("₪ " + String.valueOf(product.getPrice()));
        handleEvents(holder, product);

    }

    private void handleEvents(@NonNull ProductViewHolder holder, Product product) {
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
        holder.plusBtn.setOnClickListener(v -> {
            int currentAmount = Integer.parseInt(holder.productAmount.getText().toString());
            currentAmount++;
             Product p  =product.setAmount(currentAmount);
            holder.productAmount.setText(String.valueOf(currentAmount));
        });

        holder.minusBtn.setOnClickListener(v -> {
            int currentAmount = Integer.parseInt(holder.productAmount.getText().toString());
            if (currentAmount > 0) {
                currentAmount--;
                product.setAmount(currentAmount);
                holder.productAmount.setText(String.valueOf(currentAmount));
            }
        });
        holder.addToCartBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCartItemClick(product);
            }
        });


    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(ArrayList<Product> newProducts) {
        if (newProducts != null) {
            this.products = newProducts;
            notifyDataSetChanged();
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView price;

        TextView productAmount;
        ImageButton plusBtn;

        ImageButton minusBtn;

        CardView cardView;
        ImageButton addToCartBtn;


        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            productAmount = itemView.findViewById(R.id.product_amount);
            plusBtn = itemView.findViewById(R.id.plus_btn);
            minusBtn = itemView.findViewById(R.id.minus_btn);
            cardView = itemView.findViewById(R.id.product_card);
            addToCartBtn = itemView.findViewById(R.id.addtocart_button);


        }
    }
}
