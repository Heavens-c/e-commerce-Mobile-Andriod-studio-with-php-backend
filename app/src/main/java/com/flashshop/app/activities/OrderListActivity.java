package com.flashshop.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.flashshop.app.R;
import com.flashshop.app.adapters.AdminOrderAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.AdminOrderRow;
import com.flashshop.app.models.AdminOrdersData;
import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.utils.AuthUiHelper;
import com.flashshop.app.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Admin panel: all orders with status updates.
 * (Customer history remains {@link OrderHistoryActivity}.)
 */
public class OrderListActivity extends AppCompatActivity implements AdminOrderAdapter.Listener {

    private SessionManager session;
    private ApiService api;
    private AdminOrderAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        session = new SessionManager(this);
        api = ApiClient.getService();

        if (!session.isLoggedIn() || !session.isAdminAccount()) {
            AuthUiHelper.clearSessionAndOpenLogin(this);
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.admin_orders_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progress_bar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        RecyclerView rv = findViewById(R.id.rv_orders);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(this, this);
        rv.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadOrders);
        loadOrders();
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        api.getAdminOrders(session.getToken()).enqueue(new Callback<ApiResponse<AdminOrdersData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AdminOrdersData>> call,
                                   @NonNull Response<ApiResponse<AdminOrdersData>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.code() == 401) {
                    Toast.makeText(OrderListActivity.this, R.string.auth_session_failed, Toast.LENGTH_SHORT).show();
                    AuthUiHelper.clearSessionAndOpenLogin(OrderListActivity.this);
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(OrderListActivity.this, R.string.error_invalid_response, Toast.LENGTH_SHORT).show();
                    return;
                }
                ApiResponse<AdminOrdersData> body = response.body();
                if (!body.isSuccess() || body.getData() == null || body.getData().orders == null) {
                    String msg = body.getMessage() != null ? body.getMessage() : getString(R.string.error_generic);
                    Toast.makeText(OrderListActivity.this, msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                List<AdminOrderRow> list = body.getData().orders;
                adapter.setOrders(list);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AdminOrdersData>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(OrderListActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAdminOrderStatusSelected(AdminOrderRow row, String newStatus) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", row.id);
        payload.put("status", newStatus);

        api.updateOrderStatus(session.getToken(), payload).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.code() == 401) {
                    Toast.makeText(OrderListActivity.this, R.string.auth_session_failed, Toast.LENGTH_SHORT).show();
                    AuthUiHelper.clearSessionAndOpenLogin(OrderListActivity.this);
                    return;
                }
                ApiResponse<Object> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    row.status = newStatus;
                    adapter.notifyDataSetChanged();
                    Toast.makeText(OrderListActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
                    loadOrders();
                } else {
                    String msg = body != null && body.getMessage() != null ? body.getMessage()
                            : getString(R.string.error_generic);
                    Toast.makeText(OrderListActivity.this, msg, Toast.LENGTH_SHORT).show();
                    loadOrders();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                Toast.makeText(OrderListActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                loadOrders();
            }
        });
    }
}
