package com.example.smartcart.views.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartcart.models.ShoppingCart;
import com.example.smartcart.utilities.interfaces.GetShoppingCartsFromDB;
import com.example.smartcart.utilities.interfaces.GetUsersFromDB;
import com.example.smartcart.R;
import com.example.smartcart.controllers.LoginController;
import com.example.smartcart.databinding.LoginActivityBinding;
import com.example.smartcart.models.User;
import com.example.smartcart.utilities.viewmodels.UserViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private LoginActivityBinding binding;
    private User user;
    private boolean isPasswordValid, isUsernameValid;
    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Data Binding
        initDataBinding();
         // Initialize Controller
        loginController = new LoginController(this);
        // Initialize Views and set up listeners
        initViews();
    }

    private void initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.login_activity);
        UserViewModel viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        user = new User();
        binding.setUser(user);
        binding.setLifecycleOwner(this);
    }

    private void initViews() {
        String passwordRegex = "^(?=(?:.*[A-Z]){1})(?=(?:.*[a-z]){5})(?=(?:.*\\d){3}).{9}$";
        String usernameRegex = "^[a-z0-9]{5}$";

        binding.signupTextview.setOnClickListener(v -> passToSignUpActivity());
        binding.loginButton.setOnClickListener(v -> passToHomePageActivity());

        isPasswordValid(binding.passwordError, passwordRegex);
        isUsernameValid(binding.usernameError, usernameRegex);
    }

    private void passToSignUpActivity() {
        Intent intent = new Intent(this, FirstSignUpActivity.class);
        startActivity(intent);
    }

    private void passToHomePageActivity() {
        // Start auth timer for login
        SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);
        prefs.edit().putLong("open_time", System.currentTimeMillis()).apply();

        binding.progressBar.setVisibility(View.VISIBLE);
        user.setUsername(binding.username.getText().toString());
        user.setPassword(binding.password.getText().toString());
        login();
    }

    private void login() {
        loginController.login(user.getUsername(), user.getPassword(), new GetUsersFromDB() {
            @Override
            public void onSuccess(ArrayList<User> users) {
                User user = loginController.checkUserSignUp(users, LoginActivity.this.user.getUsername(), LoginActivity.this.user.getPassword());
                binding.progressBar.setVisibility(View.GONE);

                if (user != null) {
                    showToast("You're logged in!");

                    checkLogin();
                    loginController.cacheUser(user);
                    checkIsCustomer(user);

                } else {
                    showToast("Invalid username or password!");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast("Login failed: " + errorMessage);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    void checkLogin(){
        // Measure login duration
        SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);
        long startTime = prefs.getLong("auth_start", 0);
        long durationSec = (System.currentTimeMillis() - startTime) / 1000;

        Bundle bundle = new Bundle();
        bundle.putLong("login_duration", durationSec); // Key for login timing
        bundle.putString(FirebaseAnalytics.Param.METHOD, "manual"); // Or "email", etc.

        FirebaseAnalytics.getInstance(LoginActivity.this).setUserId(user.getUsername());
        FirebaseAnalytics.getInstance(LoginActivity.this).logEvent("my_login", bundle);
    }



    private void checkIsCustomer(User user) {
        if (user.getIsCustomer()){
            handleShoppingCart(user);
        }
        else {
            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
            startActivity(intent);
        }
    }

    private void handleShoppingCart(User user) {
        loginController.HandleShoppingCart(user, new GetShoppingCartsFromDB() {
            @Override
            public void onSuccess(ArrayList<ShoppingCart> carts) {
                Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast("Shopping cart request failed: " + errorMessage);
            }
        });
    }

    private void isPasswordValid(TextView errorMsg, String regex) {
        binding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    user.setPassword(newText);
                    isPasswordValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isPasswordValid = false;
                }
                checkFieldsForValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void isUsernameValid(TextView errorMsg, String regex) {
        binding.username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    user.setUsername(newText);
                    isUsernameValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isUsernameValid = false;
                }
                checkFieldsForValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void checkFieldsForValidity() {
        binding.loginButton.setEnabled(isPasswordValid && isUsernameValid);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
