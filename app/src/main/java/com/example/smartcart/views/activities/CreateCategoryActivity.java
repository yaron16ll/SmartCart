package com.example.smartcart.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartcart.R;
import com.example.smartcart.controllers.CreateCategoryController;
import com.example.smartcart.databinding.CreateCategoryActivityBinding;
import com.example.smartcart.models.Category;
import com.example.smartcart.utilities.viewmodels.CategoryViewModel;

public class CreateCategoryActivity extends AppCompatActivity {
    private Category category;
    private boolean isNameValid = false;
    private CreateCategoryActivityBinding binding;
    private CreateCategoryController createCategoryController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataBinding();
        createCategoryController = new CreateCategoryController();
        initViews();
        checkFields();

    }

    private void initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.create_category_activity);
        CategoryViewModel viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        category = new Category();
        binding.setCategory(category);
        binding.setLifecycleOwner(this);
    }

    private void initViews() {
        String nameRegex = "^([A-Z][a-z]+)( [A-Z][a-z]+)*$";
        isNameValid(binding.categoryNameErrorMsg, nameRegex);
        binding.submitBtn.setOnClickListener(v -> submit());
    }

    private void passToNextActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }
    private void isNameValid(TextView errorMsg, String regex) {
        binding.categoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    category.setName(newText);
                    isNameValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isNameValid = false;
                }
                checkFields();

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void checkFields() {

        binding.submitBtn.setEnabled(isNameValid);
    }

    private  void  submit (){
        binding.progressBar.setVisibility(View.VISIBLE);

        createCategoryController.submitCategory(category, aVoid -> {
            binding.progressBar.setVisibility(View.GONE);
            showToast("Category has been added");
            passToNextActivity();
        }, e -> {
            binding.progressBar.setVisibility(View.GONE);
            showToast("Category creation has failed");
        });
}
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}