package com.flashshop.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.flashshop.app.R;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.models.*;
import com.flashshop.app.utils.SessionManager;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    private EditText etName, etPhone, etAddress, etCity, etProvince, etZip, etVoucher, etNotes;
    private RadioGroup rgPayment;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        session = new SessionManager(this);

        etName = findViewById(R.id.et_shipping_name);
        etPhone = findViewById(R.id.et_shipping_phone);
        etAddress = findViewById(R.id.et_shipping_address);
        etCity = findViewById(R.id.et_shipping_city);
        etProvince = findViewById(R.id.et_shipping_province);
        etZip = findViewById(R.id.et_shipping_zip);
        etVoucher = findViewById(R.id.et_voucher);
        etNotes = findViewById(R.id.et_notes);
        rgPayment = findViewById(R.id.rg_payment);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Pre-fill from user profile
        User user = session.getUser();
        if (user != null) {
            etName.setText(user.fullName);
            etPhone.setText(user.phone != null ? user.phone : "");
            etAddress.setText(user.address != null ? user.address : "");
            etCity.setText(user.city != null ? user.city : "");
            etProvince.setText(user.province != null ? user.province : "");
            etZip.setText(user.zipCode != null ? user.zipCode : "");
        }

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        if (name.isEmpty()) { etName.setError("Required"); return; }
        if (phone.isEmpty()) { etPhone.setError("Required"); return; }
        if (address.isEmpty()) { etAddress.setError("Required"); return; }

        String paymentMethod = "cod";
        int checkedId = rgPayment.getCheckedRadioButtonId();
       // if (checkedId == R.id.rb_gcash) paymentMethod = "gcash";
      //  else if (checkedId == R.id.rb_card) paymentMethod = "credit_card";//

        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("shipping_name", name);
        body.put("shipping_phone", phone);
        body.put("shipping_address", address);
        body.put("shipping_city", etCity.getText().toString().trim());
        body.put("shipping_province", etProvince.getText().toString().trim());
        body.put("shipping_zip", etZip.getText().toString().trim());
        body.put("payment_method", paymentMethod);
        body.put("voucher_code", etVoucher.getText().toString().trim());
        body.put("notes", etNotes.getText().toString().trim());

        ApiClient.getService().placeOrder(session.getToken(), body).enqueue(new Callback<ApiResponse<OrderData>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderData>> call, Response<ApiResponse<OrderData>> response) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CheckoutActivity.this, getString(R.string.order_success), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Order failed";
                    Toast.makeText(CheckoutActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<OrderData>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
