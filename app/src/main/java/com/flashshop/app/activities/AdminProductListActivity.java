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

import com.flashshop.app.R;
import com.flashshop.app.adapters.ProductAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.models.Product;
import com.flashshop.app.models.ProductListData;
import com.flashshop.app.utils.AuthUiHelper;
import com.flashshop.app.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductListActivity extends AppCompatActivity {

    private SessionManager session;
    private ApiService api;
    private ProductAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_list);

        session = new SessionManager(this);
        api = ApiClient.getService();
        if (!session.isLoggedIn() || !session.isAdminAccount()) {
            AuthUiHelper.clearSessionAndOpenLogin(this);
            return;
        }

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tv_empty);
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(new ArrayList<>(), this::openEditor, this::onProductLongPress);
        rv.setAdapter(adapter);

        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(this::loadProducts);
        swipe.setColorSchemeResources(R.color.primary);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AdminProductEditorActivity.class)));

        loadProducts();
    }

    private boolean onProductLongPress(Product p) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.admin_deactivate_title)
                .setMessage(getString(R.string.admin_deactivate_message, p.name))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.admin_deactivate_confirm, (d, w) -> deactivateProduct(p))
                .show();
        return true;
    }

    private void deactivateProduct(Product p) {
        progress.setVisibility(View.VISIBLE);
        Map<String, Object> body = new HashMap<>();
        body.put("id", p.id);
        api.deleteProduct(session.getToken(), body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                progress.setVisibility(View.GONE);
                if (response.code() == 401) {
                    AuthUiHelper.clearSessionAndOpenLogin(AdminProductListActivity.this);
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminProductListActivity.this, R.string.admin_deactivate_ok, Toast.LENGTH_SHORT).show();
                    loadProducts();
                } else {
                    Toast.makeText(AdminProductListActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(AdminProductListActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openEditor(Product p) {
        Intent i = new Intent(this, AdminProductEditorActivity.class);
        i.putExtra(AdminProductEditorActivity.EXTRA_PRODUCT, p);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        progress.setVisibility(View.VISIBLE);
        api.adminListProducts(session.getToken(), 1, 100).enqueue(new Callback<ApiResponse<ProductListData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ProductListData>> call,
                                   @NonNull Response<ApiResponse<ProductListData>> response) {
                progress.setVisibility(View.GONE);
                androidx.swiperefreshlayout.widget.SwipeRefreshLayout s = findViewById(R.id.swipe);
                s.setRefreshing(false);
                if (response.code() == 401) {
                    AuthUiHelper.clearSessionAndOpenLogin(AdminProductListActivity.this);
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    List<Product> list = response.body().getData().products;
                    adapter.updateData(list != null ? list : new ArrayList<>());
                    tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(AdminProductListActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ProductListData>> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                findViewById(R.id.swipe).post(() -> ((androidx.swiperefreshlayout.widget.SwipeRefreshLayout) findViewById(R.id.swipe)).setRefreshing(false));
                Toast.makeText(AdminProductListActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
