package com.flashshop.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.flashshop.app.R;
import com.flashshop.app.adapters.ProductAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.models.Product;
import com.flashshop.app.models.ProductListData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Opens filtered product grid for one category (from home categories). */
public class CategoryProductsActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_NAME = "category_name";

    private int categoryId;
    private ApiService api;
    private ProductAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        categoryId = getIntent().getIntExtra(EXTRA_CATEGORY_ID, 0);
        String title = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (title != null) {
            ((TextView) findViewById(R.id.tv_category_title)).setText(title);
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        RecyclerView rv = findViewById(R.id.rv_products);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        api = ApiClient.getService();
        adapter = new ProductAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> loadPage(1, true));
        loadPage(1, false);
    }

    private void loadPage(int page, boolean fromSwipe) {
        if (!fromSwipe) progressBar.setVisibility(View.VISIBLE);
        api.getProducts(categoryId, "", 0, 0, page, 30, "created_at", "desc")
                .enqueue(new Callback<ApiResponse<ProductListData>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ProductListData>> call,
                                           @NonNull Response<ApiResponse<ProductListData>> response) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                                && response.body().getData() != null) {
                            List<Product> list = response.body().getData().products;
                            adapter.updateData(list != null ? list : new ArrayList<>());
                            tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            Toast.makeText(CategoryProductsActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ProductListData>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(CategoryProductsActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
