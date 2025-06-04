package com.example.smartcart.utilities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartcart.R;
import com.example.smartcart.models.Category;
import com.example.smartcart.utilities.interfaces.CallbackCategory;
import java.util.ArrayList;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private ArrayList<Category> categories;

    private Context context;
    private CallbackCategory listener;


    public CategoryAdapter(ArrayList<Category> categories, CallbackCategory listener, Context context) {
        this.context = context;
        this.listener = listener;
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_layout, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.category.setText(category.getName());

        holder.category.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(ArrayList<Category> newCategories) {
        if (newCategories != null) {
            this.categories = newCategories;
            notifyDataSetChanged();
        }
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView category;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.category_button);

        }
    }
}
