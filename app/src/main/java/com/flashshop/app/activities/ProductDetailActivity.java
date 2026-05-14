package com.flashshop.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.flashshop.app.R;
import com.flashshop.app.adapters.BannerAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.models.*;
import com.flashshop.app.utils.SessionManager;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private Product product;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        session = new SessionManager(this);
        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) { finish(); return; }

        // Image
        ImageView ivMain = findViewById(R.id.iv_product_main);
        Glide.with(this).load(product.imageUrl).centerCrop().into(ivMain);

        // Info
        ((TextView) findViewById(R.id.tv_product_name)).setText(product.name);
        ((TextView) findViewById(R.id.tv_product_price)).setText(product.getFormattedPrice());
        ((TextView) findViewById(R.id.tv_product_desc)).setText(product.description);
        ((TextView) findViewById(R.id.tv_rating)).setText(String.valueOf(product.rating));
        ((TextView) findViewById(R.id.tv_reviews_count)).setText(product.reviewCount + " Reviews");
        ((TextView) findViewById(R.id.tv_sold_count)).setText(product.getFormattedSoldCount());
        ((TextView) findViewById(R.id.tv_stock)).setText("In Stock: " + product.stock);

        if (product.originalPrice != null) {
            TextView tvOriginal = findViewById(R.id.tv_original_price);
            tvOriginal.setText(product.getFormattedOriginalPrice());
            tvOriginal.setVisibility(android.view.View.VISIBLE);
            ((TextView) findViewById(R.id.tv_discount)).setText("-" + product.discountPercent + "%");
            findViewById(R.id.tv_discount).setVisibility(android.view.View.VISIBLE);
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cart_top).setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        findViewById(R.id.btn_add_to_cart).setOnClickListener(v -> addToCart());
        findViewById(R.id.btn_buy_now).setOnClickListener(v -> {
            addToCart();
            startActivity(new Intent(this, CartActivity.class));
        });

        findViewById(R.id.btn_favorite).setOnClickListener(v -> toggleWishlist());
    }

    private void addToCart() {
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", product.id);
        body.put("quantity", 1);
        ApiClient.getService().addToCart(session.getToken(), body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> c, Response<ApiResponse<Object>> r) {
                if (r.isSuccessful() && r.body() != null && r.body().isSuccess()) {
                    Toast.makeText(ProductDetailActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to add", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ApiResponse<Object>> c, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleWishlist() {
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", product.id);
        ApiClient.getService().toggleWishlist(session.getToken(), body).enqueue(new Callback<ApiResponse<WishlistData>>() {
            @Override
            public void onResponse(Call<ApiResponse<WishlistData>> c, Response<ApiResponse<WishlistData>> r) {
                Toast.makeText(ProductDetailActivity.this, r.body() != null ? r.body().getMessage() : "Done", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<ApiResponse<WishlistData>> c, Throwable t) {}
        });
    }
}
