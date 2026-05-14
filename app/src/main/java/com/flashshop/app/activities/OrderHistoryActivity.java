package com.flashshop.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.flashshop.app.R;
import com.flashshop.app.adapters.OrderAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.models.*;
import com.flashshop.app.utils.SessionManager;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        SessionManager session = new SessionManager(this);
        RecyclerView rv = findViewById(R.id.rv_orders);
        ProgressBar progress = findViewById(R.id.progress_bar);

        rv.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        progress.setVisibility(View.VISIBLE);
        ApiClient.getService().getOrders(session.getToken(), "all").enqueue(new Callback<ApiResponse<OrderListData>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderListData>> call, Response<ApiResponse<OrderListData>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    rv.setAdapter(new OrderAdapter(response.body().getData().orders));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<OrderListData>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(OrderHistoryActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
