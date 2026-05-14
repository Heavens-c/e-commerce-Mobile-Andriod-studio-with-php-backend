package com.flashshop.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.flashshop.app.R;
import com.flashshop.app.adapters.CartAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.models.*;
import com.flashshop.app.utils.SessionManager;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartActionListener {
    private RecyclerView rvCart;
    private CartAdapter adapter;
    private TextView tvTotal;
    private Button btnCheckout;
    private ProgressBar progressBar;
    private SessionManager session;
    private List<CartItem> cartItemlist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        session = new SessionManager(this);

        rvCart = findViewById(R.id.rv_cart);
        tvTotal = findViewById(R.id.tv_total);
        btnCheckout = findViewById(R.id.btn_checkout);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        adapter = new CartAdapter(cartItemlist, this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        btnCheckout.setOnClickListener(v -> {
            if (!cartItemlist.isEmpty()) startActivity(new Intent(this, CheckoutActivity.class));
        });

        loadCart();
    }

    @Override
    protected void onResume() { super.onResume(); loadCart(); }

    private void loadCart() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService().getCart(session.getToken()).enqueue(new Callback<ApiResponse<CartData>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartData>> call, Response<ApiResponse<CartData>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CartData data = response.body().getData();
                    cartItemlist.clear();
                    cartItemlist.addAll(data.items);
                    adapter.notifyDataSetChanged();
                    tvTotal.setText(String.format("$%.2f", data.subtotal));
                    btnCheckout.setText(String.format("Checkout (%d)", data.itemCount));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<CartData>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onQuantityChanged(int cartId, String action) {
        Map<String, Object> body = new HashMap<>();
        body.put("cart_id", cartId);
        body.put("action", action);
        ApiClient.getService().updateCart(session.getToken(), body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override public void onResponse(Call<ApiResponse<Object>> c, Response<ApiResponse<Object>> r) { loadCart(); }
            @Override public void onFailure(Call<ApiResponse<Object>> c, Throwable t) {}
        });
    }
}
