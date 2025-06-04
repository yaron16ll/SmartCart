package com.example.smartcart.utilities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcart.R;
import com.example.smartcart.models.CartItem;
import com.example.smartcart.utilities.interfaces.CallbackCartItem;

import java.util.ArrayList;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {

    private ArrayList<CartItem> cartItems;
    private CallbackCartItem listener;
    private Context context;

    public CartItemAdapter(ArrayList<CartItem> cartItems, CallbackCartItem listener, Context context) {
        this.cartItems = cartItems;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cartitem_layout, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.cartItemName.setText(cartItem.getName());
        holder.cartItemAmount.setText(String.valueOf(cartItem.getAmount()));
        holder.cartItemTotalPrice.setText(String.format("₪ %.2f", cartItem.getTotalPrice()));

        // Load image using Glide
        Glide.with(context).load(cartItem.getImageSrc()).into(holder.cartItemImg);

        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteItemClick(cartItem);
            }
        });
    }

    public void updateShoppingCart(ArrayList<CartItem> newCartItems) {
        if (newCartItems != null) {
            this.cartItems = newCartItems;
            notifyDataSetChanged();
        }
    }

    public boolean isShoppingCartEmpty() {
        if (this.cartItems.isEmpty())
            return true;
        return false;
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartItemViewHolder extends RecyclerView.ViewHolder {
        public TextView cartItemName;
        public TextView cartItemAmount;
        public TextView cartItemTotalPrice;
        public ImageView cartItemImg;
        public ImageView deleteBtn;

        public CartItemViewHolder(View view) {
            super(view);
            cartItemName = view.findViewById(R.id.cartItem_name);
            cartItemAmount = view.findViewById(R.id.cartItem_amount);
            cartItemTotalPrice = view.findViewById(R.id.cartItem_totalPrice);
            cartItemImg = view.findViewById(R.id.cartItem_img);
            deleteBtn = view.findViewById(R.id.delete_btn);

        }
    }
}
