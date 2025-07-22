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

import com.example.smartcart.utilities.interfaces.GetUsersFromDB;
import com.example.smartcart.R;
import com.example.smartcart.controllers.SignUpController;
import com.example.smartcart.databinding.FirstSignUpActivityBinding;
import com.example.smartcart.models.User;
import com.example.smartcart.utilities.viewmodels.UserViewModel;
import com.google.gson.Gson;

import java.util.ArrayList;

public class FirstSignUpActivity extends AppCompatActivity {
    private FirstSignUpActivityBinding binding;
    private User user;
    private boolean isPasswordValid, isUsernameValid, isIDValid;
    private SignUpController signUpController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDataBinding();

        SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);
        prefs.edit().putLong("open_time", System.currentTimeMillis()).apply(); // Save current time as start

        signUpController = new SignUpController(this);
        initViews();
    }

    private void initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.first_sign_up_activity);
        UserViewModel viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);
        prefs.edit().putLong("auth_start", System.currentTimeMillis()).apply(); // Save current time as start

        user = new User();
        binding.setUser(user);
        binding.setLifecycleOwner(this);
    }


    private void checkFieldsForValidity() {
        binding.moveBtn.setEnabled(isPasswordValid && isUsernameValid && isIDValid);
    }


    private void passToSecondSignUp() {

        signUpController.getUsersFromDB(new GetUsersFromDB() {
            @Override
            public void onSuccess(ArrayList<User> users) {
                boolean isUserNameNotExist = false;
                boolean isIDNotExist = false;

                if (!signUpController.isUserNameExist(users, user.getUsername())) {
                    isUserNameNotExist = true;
                } else {
                    showToast("Username already exists!");
                }

                if (!signUpController.isIDExist(users, user.getID())) {
                    isIDNotExist = true;

                } else {
                    showToast("ID already exists!");
                }
                if (isUserNameNotExist && isIDNotExist) {
                    moveToSecondSignUp();
                }
            }


            @Override
            public void onFailure(String errorMessage) {
                showToast("Sign up failed: " + errorMessage);
            }
        });
    }


    private void moveToSecondSignUp() {
        Intent intent = new Intent(this, SecondSignUpActivity.class);
        String json = new Gson().toJson(user);

        intent.putExtra("user", json);
        startActivity(intent);
        finish();
    }

    private void isPasswordValid(TextView errorMsg, String regex) {
        binding.passwordTextView.addTextChangedListener(new TextWatcher() {
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

    private void isIDValid(TextView errorMsg, String regex) {
        binding.userID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    user.setID(newText);
                    isIDValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isIDValid = false;
                }
                checkFieldsForValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void initViews() {
        String passwordRegex = "^(?=(?:.*[A-Z]){1})(?=(?:.*[a-z]){5})(?=(?:.*\\d){3}).{9}$";
        String usernameRegex = "^[a-z0-9]{5}$";
        String IDRegex = "^[0-9]{9}$";

        isPasswordValid(binding.passwordErrorMsg, passwordRegex);
        isUsernameValid(binding.usernameErrorMsg, usernameRegex);
        isIDValid(binding.userIDErrorMsg, IDRegex);


        binding.moveBtn.setOnClickListener(v -> passToSecondSignUp());
    }

    // Reusable showToast method
    private void showToast(String message) {
        Toast.makeText(FirstSignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
