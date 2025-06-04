package com.example.smartcart.views.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartcart.controllers.OrderController;
import com.example.smartcart.databinding.ActivityOrderBinding;

import com.example.smartcart.R;
import com.example.smartcart.models.CartItem;
import com.example.smartcart.models.Order;
import com.example.smartcart.models.ShoppingCart;
import com.example.smartcart.models.TempMemoryCache;
import com.example.smartcart.utilities.viewmodels.OrderViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;


public class OrderActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private ActivityOrderBinding binding;
    private Order order;
    private boolean isShipStreetValid, isCreditNumValid, isCountryValid;
    private OrderController orderController;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDataBinding();
        orderController = new OrderController(this);
        initViews();

    }


    private void initDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order);
        OrderViewModel viewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        order = new Order();
        binding.setOrder(order);
        binding.setLifecycleOwner(this);
    }

    private void checkFieldsForValidity() {
        binding.orderButton.setEnabled(isShipStreetValid && isCreditNumValid && isCountryValid);
    }


    public void createOrderPdf(Order order) {
        int y = 30;
        int x = 60;
        float totalPrice = 0;
        int productCount = 0;

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        String cartItemsString = TempMemoryCache.getInstance().getString("cartItems", null);
        Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
        ArrayList<CartItem> cartItems = new Gson().fromJson(cartItemsString, type);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(20);
        canvas.drawText("My Receipt", 100, 30, paint);

        paint.setTextSize(10);
        for (CartItem cartItem : cartItems) {
            y += 25;
            productCount++;
            totalPrice += cartItem.getTotalPrice();
            canvas.drawText(productCount + ") Name: " + cartItem.getName() + ", Amount: " + cartItem.getAmount() + ", Price: " + cartItem.getTotalPrice() + ".", x, y, paint);
        }

        y += 30;
        paint.setUnderlineText(true);
        canvas.drawText("Total Price: " + totalPrice + ".", 118, y, paint);
        paint.setUnderlineText(false);
        y += 20;
        canvas.drawText("THANK YOU!", 125, y, paint);

        pdfDocument.finishPage(page);

        // שמירה לשם קבוע
        file = new File(getExternalFilesDir(null), "Receipt.pdf");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Failed to create PDF");
        }

        pdfDocument.close();
    }



    private void isStreetNameValid(TextView errorMsg, String regex) {
        binding.street.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    order.setShipStreet(newText);
                    isShipStreetValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isShipStreetValid = false;
                }
                checkFieldsForValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void isCreditNumValid(TextView errorMsg, String regex) {
        binding.cardCredit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                if (newText.matches(regex)) {
                    order.setCreditNum(newText);
                    isCreditNumValid = true;
                    errorMsg.setVisibility(View.INVISIBLE);
                } else {
                    errorMsg.setVisibility(View.VISIBLE);
                    isCreditNumValid = false;
                }
                checkFieldsForValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void pickDate() {
        binding.dateEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        order.setShipDate(date);
                        binding.dateEditText.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void initViews() {
        String streetRegex = "^[A-Za-z]+ [0-9]+$";
        String cardCreditRegex = "^5[1-5][0-9]{2}( [0-9]{4}){3}$";

        isStreetNameValid(binding.streetErrorMsg, streetRegex);
        isCreditNumValid(binding.cardCreditErrorMsg, cardCreditRegex);

        chooseCity();
        pickDate();

        binding.orderButton.setOnClickListener(v -> order());
    }

    private void chooseCity() {
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this,
                R.array.citylist, android.R.layout.simple_spinner_item);

        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.citylist.setAdapter(cityAdapter);
        binding.citylist.setOnItemSelectedListener(this);
    }


    private void order() {
        binding.progressBar.setVisibility(View.VISIBLE);

        orderController.addOrderToDB(order, aVoid -> {
            binding.progressBar.setVisibility(View.GONE);
            orderController.deleteCartItems(order.getShoppingCartID());
            updateShoppingCart();
            showToast("Order has been received");
        }, e -> {
            binding.progressBar.setVisibility(View.GONE);
            showToast("Failed to create order: " + e.getMessage());
        });
    }

    private void updateShoppingCart() {
        String shoppingCartString = TempMemoryCache.getInstance().getString("ShoppingCart", "");
        ShoppingCart shoppingCart = new Gson().fromJson(shoppingCartString, ShoppingCart.class);

        orderController.updateShoppingCart(shoppingCart, aVoid -> {
            showShareOrderDialog();
            updateOnCache(shoppingCart);
        }, e -> {
            showToast("Failed to set cart to be checked out: " + e.getMessage());
        });
    }



    private void showShareOrderDialog() {
        createOrderPdf(order);

        new AlertDialog.Builder(this)
                .setTitle("Order Sharing")
                .setMessage("Do you want to share order?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    share();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    TempMemoryCache.getInstance().putString("cartItems", null);
                    passToHomepage();
                })
                .show();
    }

    private void passToHomepage() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void share() {
        if (file == null || !file.exists()) {
            showToast("PDF not found. Please create it first.");
            return;
        }

        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, "Share PDF via");
        shareLauncher.launch(chooser); //pass to Homepage
    }


    private final ActivityResultLauncher<Intent> shareLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //When user is back from share
                TempMemoryCache.getInstance().putString("cartItems", null);
                passToHomepage();
            }
    );
    private void updateOnCache(ShoppingCart shoppingCart) {
        shoppingCart.setIsCheckedOut(true);
        String shoppingCartStrg = new Gson().toJson(shoppingCart);
        TempMemoryCache.getInstance().putString("ShoppingCart", shoppingCartStrg);

    }

    private void showToast(String message) {
        Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            isCountryValid = false;
        } else {
            order.setShipCity(parent.getItemAtPosition(position).toString());
            isCountryValid = true;
        }
        checkFieldsForValidity();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

