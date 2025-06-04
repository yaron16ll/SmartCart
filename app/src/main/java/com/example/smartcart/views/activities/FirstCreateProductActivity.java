package com.example.smartcart.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartcart.databinding.CreateProductActivityBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartcart.R;
import com.example.smartcart.models.Product;
import com.example.smartcart.controllers.CreateProductController;
import com.example.smartcart.utilities.viewmodels.ProductViewModel;
import com.google.gson.Gson;

public class FirstCreateProductActivity extends AppCompatActivity {
    private Product product;
    private CreateProductActivityBinding binding;

    private boolean isProductImgPickerActive = false, isPriceValid = false, isNameValid = false, isDescriptionValid = false;

    private CreateProductController createProductController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataBinding();

        createProductController = new CreateProductController();
        initViews();
        checkFields();
    }


    private void isPriceValid(TextView errorMsg, String regex) {
        binding.productPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    product.setPrice(Float.parseFloat(newText));
                    isPriceValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isPriceValid = false;
                }
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void isNameValid(TextView errorMsg, String regex) {
        binding.productName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    product.setName(newText);
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

        boolean areFieldsFilled = isNameValid
                && isDescriptionValid
                && isPriceValid;

        binding.submitBtn.setEnabled(areFieldsFilled);
    }

    private void initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.create_product_activity);
        ProductViewModel viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        product = new Product();
        binding.setProduct(product);
        binding.setLifecycleOwner(this);
    }


    private void initViews() {

        String priceRegex = "^\\d+\\.\\d{2}$";
        String nameRegex = "^([A-Z][a-z]+)( [A-Z][a-z]+)*$";
        String descriptionRegex = "^[A-Za-z ]{2,300}$";

        isPriceValid(binding.productPriceErrorMsg, priceRegex);
        isNameValid(binding.productNameErrorMsg, nameRegex);
        isDescriptionValid(binding.productDescriptionErrorMsg, descriptionRegex);

        binding.submitBtn.setOnClickListener(v -> passToNextActivity());
    }

    private void passToNextActivity() {
        Intent intent = new Intent(FirstCreateProductActivity.this, SecondCreateProductActivity.class);
        String productString = new Gson().toJson(createProduct());
        intent.putExtra("product", productString);
        startActivity(intent);

    }

    private void isDescriptionValid(TextView errorMsg, String regex) {
        binding.productDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    product.setDescription(newText);
                    isDescriptionValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isDescriptionValid = false;
                }
                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }



    private Product createProduct() {
        return new Product()
                .setName(binding.productName.getText().toString())
                .setDescription(binding.productDescription.getText().toString())
                .setPrice(Float.parseFloat(binding.productPrice.getText().toString()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
