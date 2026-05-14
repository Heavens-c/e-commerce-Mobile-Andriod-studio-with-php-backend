package com.flashshop.app.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flashshop.app.R;
import com.flashshop.app.adapters.AdminDashboardRecentOrdersAdapter;
import com.flashshop.app.adapters.AdminDashboardStatAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.AdminOrderRow;
import com.flashshop.app.models.AdminOrdersData;
import com.flashshop.app.models.AdminStatsData;
import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.utils.AuthUiHelper;
import com.flashshop.app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Modern admin dashboard: analytics tiles, quick actions, recent orders, bottom navigation.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private static final int RECENT_LIMIT = 5;

    private SessionManager sessionManager;
    private ApiService api;
    private BottomNavigationView bottomNav;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;

    private AdminDashboardStatAdapter statAdapter;
    private AdminDashboardRecentOrdersAdapter recentAdapter;

    private TextView tvWelcomeTitle;
    private TextView tvRecentEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);
        api = ApiClient.getService();

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdminAccount()) {
            AuthUiHelper.clearSessionAndOpenLogin(this);
            return;
        }

        swipeRefresh = findViewById(R.id.swipe_refresh_admin);
        bottomNav = findViewById(R.id.bottom_nav_admin);
        tvWelcomeTitle = findViewById(R.id.tv_admin_welcome_title);
        tvRecentEmpty = findViewById(R.id.tv_recent_orders_empty);

        bindWelcomeHeader();

        RecyclerView rvStats = findViewById(R.id.rv_admin_stats);
        rvStats.setLayoutManager(new GridLayoutManager(this, 2));
        rvStats.setHasFixedSize(true);
        statAdapter = new AdminDashboardStatAdapter();
        rvStats.setAdapter(statAdapter);

        RecyclerView rvRecent = findViewById(R.id.rv_admin_recent_orders);
        rvRecent.setLayoutManager(new LinearLayoutManager(this));
        rvRecent.setHasFixedSize(false);
        recentAdapter = new AdminDashboardRecentOrdersAdapter();
        rvRecent.setAdapter(recentAdapter);

        findViewById(R.id.btn_view_all_orders).setOnClickListener(v -> openOrders());

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);
        findViewById(R.id.card_logout).setOnClickListener(v -> doLogout());

        bindQuickTiles();
        bindBottomNavigation();

        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null && sessionManager.isAdminAccount()) {
            bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
        }
    }

    private void bindWelcomeHeader() {
        String name = "Admin";
        if (sessionManager.getAdmin() != null && sessionManager.getAdmin().name != null
                && !sessionManager.getAdmin().name.isEmpty()) {
            name = sessionManager.getAdmin().name;
        }
        tvWelcomeTitle.setText(getString(R.string.admin_welcome_named, name));
    }

    private void configureQuickTile(
            MaterialCardView card,
            int innerBgRes,
            int iconDrawable,
            int iconTintColor,
            @NonNull CharSequence title,
            @NonNull View.OnClickListener click
    ) {
        MaterialCardView c = card;
        View inner = c.findViewById(R.id.quick_inner);
        inner.setBackgroundResource(innerBgRes);
        android.widget.ImageView icon = c.findViewById(R.id.iv_quick_icon);
        icon.setImageResource(iconDrawable);
        icon.setColorFilter(iconTintColor);
        TextView label = c.findViewById(R.id.tv_quick_label);
        label.setText(title);
        c.setOnClickListener(click);
    }

    private void bindQuickTiles() {
        MaterialCardView tOrders = findViewById(R.id.tile_quick_orders);
        MaterialCardView tProducts = findViewById(R.id.tile_quick_products);
        MaterialCardView tCustomers = findViewById(R.id.tile_quick_customers);
        MaterialCardView tReports = findViewById(R.id.tile_quick_reports);

        configureQuickTile(
                tOrders,
                R.drawable.bg_quick_action_orders,
                R.drawable.ic_bn_orders,
                getColor(R.color.stat_accent_orders),
                getString(R.string.admin_nav_orders),
                v -> openOrders()
        );
        configureQuickTile(
                tProducts,
                R.drawable.bg_quick_action_products,
                R.drawable.ic_bn_products,
                getColor(R.color.stat_accent_products),
                getString(R.string.admin_nav_products),
                v -> startActivity(new Intent(this, AdminProductListActivity.class))
        );
        configureQuickTile(
                tCustomers,
                R.drawable.bg_quick_action_customers,
                R.drawable.ic_bn_customers,
                getColor(R.color.stat_accent_customers),
                getString(R.string.admin_nav_customers),
                v -> showSoon(R.string.admin_feature_customers)
        );
        configureQuickTile(
                tReports,
                R.drawable.bg_quick_action_reports,
                R.drawable.ic_bn_reports,
                getColor(R.color.primary),
                getString(R.string.admin_nav_reports),
                v -> startActivity(new Intent(this, AdminReportsActivity.class))
        );
    }

    private void bindBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dashboard) {
                return true;
            }
            if (id == R.id.nav_admin_orders_bn) {
                openOrders();
                return false;
            }
            if (id == R.id.nav_admin_products_bn) {
                startActivity(new Intent(this, AdminProductListActivity.class));
                return false;
            }
            if (id == R.id.nav_admin_customers_bn) {
                showSoon(R.string.admin_feature_customers);
                return false;
            }
            if (id == R.id.nav_admin_reports_bn) {
                startActivity(new Intent(this, AdminReportsActivity.class));
                return false;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
    }

    private void loadDashboardData() {
        swipeRefresh.setRefreshing(true);
        AtomicInteger pending = new AtomicInteger(2);

        api.getAdminStats(sessionManager.getToken()).enqueue(new Callback<ApiResponse<AdminStatsData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AdminStatsData>> call,
                                   @NonNull Response<ApiResponse<AdminStatsData>> response) {
                if (!isSessionValid(response.code())) finishRefresh(pending);
                else if (!response.isSuccessful() || response.body() == null) finishRefresh(pending);
                else {
                    ApiResponse<AdminStatsData> body = response.body();
                    if (body.isSuccess() && body.getData() != null) {
                        applyStats(body.getData());
                    }
                    finishRefresh(pending);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AdminStatsData>> call, @NonNull Throwable t) {
                finishRefresh(pending);
            }
        });

        api.getAdminOrders(sessionManager.getToken()).enqueue(new Callback<ApiResponse<AdminOrdersData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AdminOrdersData>> call,
                                   @NonNull Response<ApiResponse<AdminOrdersData>> response) {
                if (!isSessionValid(response.code())) finishRefresh(pending);
                else if (!response.isSuccessful() || response.body() == null) finishRefresh(pending);
                else {
                    ApiResponse<AdminOrdersData> body = response.body();
                    if (body.isSuccess() && body.getData() != null && body.getData().orders != null) {
                        applyRecentOrders(body.getData().orders);
                    }
                    finishRefresh(pending);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AdminOrdersData>> call, @NonNull Throwable t) {
                finishRefresh(pending);
            }
        });
    }

    private void finishRefresh(AtomicInteger pending) {
        if (pending.decrementAndGet() <= 0) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private boolean isSessionValid(int httpCode) {
        if (httpCode == 401) {
            AuthUiHelper.clearSessionAndOpenLogin(this);
            return false;
        }
        return true;
    }

    private void applyStats(AdminStatsData d) {
        int cOrders = getColor(R.color.stat_accent_orders);
        int cSales = getColor(R.color.stat_accent_sales);
        int cProd = getColor(R.color.stat_accent_products);
        int cCust = getColor(R.color.stat_accent_customers);

        String salesFormatted = "$" + String.format(Locale.US, "%,.2f", d.totalSales);

        List<AdminDashboardStatAdapter.Stat> list = new ArrayList<>();
        list.add(new AdminDashboardStatAdapter.Stat(R.drawable.ic_admin_receipt, cOrders,
                String.format(Locale.US, "%,d", d.totalOrders), getString(R.string.stat_total_orders)));
        list.add(new AdminDashboardStatAdapter.Stat(R.drawable.ic_admin_analytics, cSales,
                salesFormatted, getString(R.string.stat_total_sales)));
        list.add(new AdminDashboardStatAdapter.Stat(R.drawable.ic_admin_inventory, cProd,
                String.format(Locale.US, "%,d", d.totalProducts), getString(R.string.stat_total_products)));
        list.add(new AdminDashboardStatAdapter.Stat(R.drawable.ic_admin_users, cCust,
                String.format(Locale.US, "%,d", d.totalCustomers), getString(R.string.stat_total_customers)));

        statAdapter.setStats(list);
    }

    private void applyRecentOrders(List<AdminOrderRow> all) {
        if (all == null || all.isEmpty()) {
            recentAdapter.setRows(Collections.emptyList());
            tvRecentEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvRecentEmpty.setVisibility(View.GONE);
        int n = Math.min(RECENT_LIMIT, all.size());
        recentAdapter.setRows(new ArrayList<>(all.subList(0, n)));
    }

    private void openOrders() {
        startActivity(new Intent(this, OrderListActivity.class));
    }

    private void doLogout() {
        sessionManager.logout();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void showSoon(int messageRes) {
        Snackbar bar = Snackbar.make(swipeRefresh, getString(messageRes), Snackbar.LENGTH_SHORT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bar.setAnchorView(bottomNav);
        }
        bar.show();
    }

    /** Local helper to avoid static import churn for getColor compatibility. */
}
