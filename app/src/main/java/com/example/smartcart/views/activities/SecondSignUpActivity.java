package com.example.smartcart.views.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartcart.controllers.SignUpController;
import com.example.smartcart.models.User;
import com.example.smartcart.utilities.viewmodels.UserViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartcart.R;
import com.example.smartcart.databinding.SecondSignUpActivityBinding;

public class SecondSignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private SecondSignUpActivityBinding binding;
    private User user;
    private boolean isFirstNameValid, isStreetValid, isLastNameValid, isCountryValid;

    private SignUpController signUpController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUser();
        initDataBinding();
        signUpController = new SignUpController(this);
        initViews();
    }

    private void initUser() {
        String json = getIntent().getStringExtra("user");
        user = new Gson().fromJson(json, User.class);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            isCountryValid = false;
        } else {
            user.setCity(parent.getItemAtPosition(position).toString());
            isCountryValid = true;
        }
        checkFieldsForValidity();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No action needed
    }


    private void checkFieldsForValidity() {
        binding.signUpBtn.setEnabled(isFirstNameValid && isStreetValid && isLastNameValid && isCountryValid);
    }

    private void isFirstNameValid(TextView errorMsg, String regex) {
        binding.userFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    user.setFirstName(newText);
                    isFirstNameValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isFirstNameValid = false;
                }
                checkFieldsForValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void isLastNameValid(TextView errorMsg, String regex) {
        binding.userLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    user.setLastName(newText);
                    isLastNameValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isLastNameValid = false;
                }
                checkFieldsForValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void isStreetValid(TextView errorMsg, String regex) {
        binding.street.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    user.setStreet(newText);
                    isStreetValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isStreetValid = false;
                }
                checkFieldsForValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void passToLoginActivity() {
        binding.progressBar.setVisibility(View.VISIBLE);
        user.setIsCustomer(true);
        signUpController.addUserToDB(user, aVoid -> {
            binding.progressBar.setVisibility(View.GONE);
            showToast("You are signed up");

            checkSignUp();

            moveToLoginActivity();
        }, e -> {
            binding.progressBar.setVisibility(View.GONE);
            showToast("Failed to add user: " + e.getMessage());
        });

    }

    void checkSignUp(){
        SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);
        long startTime = prefs.getLong("auth_start", 0); // Retrieve start time
        long durationSec = (System.currentTimeMillis() - startTime) / 1000; // Calculate duration in second
        Bundle bundle = new Bundle();
        bundle.putLong("sign_up_duration", durationSec); // Add duration as parameter
        bundle.putString(FirebaseAnalytics.Param.METHOD, "manual"); // Sign-up method: manual/emai
        FirebaseAnalytics.getInstance(this).setUserId(user.getUsername());
        FirebaseAnalytics.getInstance(this).logEvent("my_sign_up", bundle);
    }

    private void initViews() {
        String firstNameRegex = "^[A-Za-z]+$";
        String lastNameRegex = "^[A-Za-z]+$";
        String streetRegex = "^[A-Za-z]+ [0-9]+$";

        isFirstNameValid(binding.userFirstNameErrorMsg, firstNameRegex);
        isLastNameValid(binding.userLastNameErrorMsg, lastNameRegex);
        isStreetValid(binding.streetErrorMsg, streetRegex);

        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this,
                R.array.citylist, android.R.layout.simple_spinner_item);

        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.citylist.setAdapter(cityAdapter);
        binding.citylist.setOnItemSelectedListener(this);

        binding.signUpBtn.setOnClickListener(v -> passToLoginActivity());
    }

    private void moveToLoginActivity() {

        Intent intent = new Intent(SecondSignUpActivity.this, LoginActivity.class);

        startActivity(intent);
        finish();
    }

    private void initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.second_sign_up_activity);
        UserViewModel viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        binding.setUser(user);
        binding.setLifecycleOwner(this);
    }

    private void showToast(String message) {
        Toast.makeText(SecondSignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}