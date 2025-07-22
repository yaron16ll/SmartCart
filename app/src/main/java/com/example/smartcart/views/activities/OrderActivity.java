package com.example.smartcart.views.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.smartcart.models.User;
import com.example.smartcart.utilities.viewmodels.OrderViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class OrderActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final Log log = LogFactory.getLog(OrderActivity.class);
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
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
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
            orderController.deleteCartItems(order.getShoppingCartID());


            SharedPreferences prefs = getSharedPreferences("smartcart", MODE_PRIVATE);

            // Retrieve the time when checkout started
            long startTime = prefs.getLong("checkout_start_time", 0);

            if (startTime > 0) {
                // Calculate the duration from checkout start to completion (in seconds)
                long durationSec = (System.currentTimeMillis() - startTime) / 1000;

                // Log the checkout completion duration to Firebase Analytics
                Bundle bundle = new Bundle();
                bundle.putLong("checkout_duration", durationSec); // Time from entering checkout to finishing it
                bundle.putString("platform", "android"); // platform identifier
                FirebaseAnalytics.getInstance(this).logEvent("custom_purchase_completed", bundle);
            }
            updateShoppingCart();
        }, e -> {
            binding.progressBar.setVisibility(View.GONE);
            showToast("Failed to create order: " + e.getMessage());
        });
    }

    private void updateShoppingCart() {
        String shoppingCartString = TempMemoryCache.getInstance().getString("ShoppingCart", "");
        ShoppingCart shoppingCart = new Gson().fromJson(shoppingCartString, ShoppingCart.class);

        orderController.updateShoppingCart(shoppingCart, aVoid -> {
            sendUserEvent(shoppingCart);
        }, e -> {
            showToast("Failed to set cart to be checked out: " + e.getMessage());
        });
    }

    private void sendUserEvent(ShoppingCart shoppingCart) {
        String userString = TempMemoryCache.getInstance().getString("user", "");
        User user = new Gson().fromJson(userString, User.class);
        String eventTime = Instant.now().toString();
        OkHttpClient client = new OkHttpClient();
        String url = "https://retail.googleapis.com/v2/projects/105020571010/locations/global/catalogs/default_catalog/userEvents:write";
        String productString = getStringifyProducts();
        String transactionId = "txn-" + System.currentTimeMillis();

        String json =
                "{\n" +
                        "  \"eventType\": \"purchase-complete\",\n" +
                        "  \"eventTime\": \"" + eventTime + "\",\n" +
                        "  \"visitorId\": \"" + user.getID() + "\",\n" +
                        "  \"userInfo\": {\n" +
                        "    \"userId\": \"" + user.getID() + "\"\n" +
                        "  },\n" +
                        "  \"purchaseTransaction\": {\n" +
                        "    \"id\": \"" + transactionId + "\",\n" +
                        "    \"revenue\": " + shoppingCart.getTotalPrice() + ",\n" +
                        "    \"currencyCode\": \"ILS\"\n" +
                        "  },\n" +
                        "  \"productDetails\": [\n" +
                        productString +
                        "  ]\n" +
                        "}";


        Request request = createRequest(url, json);
        sendEventToAPI(client, request, shoppingCart);
    }

    private String getStringifyProducts() {
        String cartItemsString = TempMemoryCache.getInstance().getString("cartItems", null);
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
        ArrayList<CartItem> cartItems = new Gson().fromJson(cartItemsString, type);
        StringBuilder productDetailsBuilder = new StringBuilder();

        for (int i = 0; i < cartItems.size(); i++) {
            String productID = cartItems.get(i).getProductID();
            int quantity = cartItems.get(i).getAmount();

            productDetailsBuilder.append("    {\n")
                    .append("      \"product\": {\n")
                    .append("        \"id\": \"").append(productID).append("\"\n")
                    .append("      },\n")
                    .append("      \"quantity\": ").append(quantity).append("\n")
                    .append("    }");

            if (i < cartItems.size() - 1) {
                productDetailsBuilder.append(",\n");
            } else {
                productDetailsBuilder.append("\n");
            }
        }

        return productDetailsBuilder.toString();
    }

    private Request createRequest(String url, String json) {
        String token = TempMemoryCache.getInstance().getString("token", null);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();

        return request;
    }

    private void sendEventToAPI(OkHttpClient client, Request request, ShoppingCart shoppingCart) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    android.util.Log.e("RETAIL_API", "Network Error", e);
                    showToast("Network error: " + e.getMessage());
                    binding.progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                int code = response.code();

                runOnUiThread(() -> {
                    android.util.Log.d("RETAIL_API", "HTTP Code: " + code);
                    android.util.Log.d("RETAIL_API", "Response Body:\n" + responseBody);
                    binding.progressBar.setVisibility(View.GONE);

                    if (response.isSuccessful()) {
                        showToast("The Order Has Been Added");
                        updateCache(shoppingCart);
                        showShareOrderDialog();
                     } else {
                        showToast("FAILED: " + code + "\n" + responseBody);
                    }
                });
            }
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
        // Check if the PDF file exists before attempting to share
        if (file == null || !file.exists()) {
            showToast("PDF not found. Please create it first.");
            return;
        }

        // Get a content URI for the file using FileProvider
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

        // Create an intent to send the PDF file
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        // Set the type of content to be shared
        shareIntent.setType("application/pdf");

        // Attach the PDF file URI to the intent
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        // Grant temporary read permission for the receiving app
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Create a chooser dialog so the user can select the app to share with
        Intent chooser = Intent.createChooser(shareIntent, "Share PDF via");

        // Launch the sharing activity; actual result will be handled in the shareLauncher callback
        shareLauncher.launch(chooser);
    }


    private ActivityResultLauncher<Intent> shareLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                FirebaseAnalytics.getInstance(this).logEvent("receipt_shared", null);
                //When user is back from share
                TempMemoryCache.getInstance().putString("cartItems", null);
                passToHomepage();
            }
    );

    private void updateCache(ShoppingCart shoppingCart) {
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

