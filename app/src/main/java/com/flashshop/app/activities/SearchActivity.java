package com.flashshop.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flashshop.app.R;
import com.flashshop.app.adapters.ProductAdapter;
import com.flashshop.app.adapters.SearchHistoryAdapter;
import com.flashshop.app.adapters.SearchSuggestionAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.models.Product;
import com.flashshop.app.models.ProductListData;
import com.flashshop.app.models.SearchSuggestion;
import com.flashshop.app.models.SearchSuggestionsData;
import com.flashshop.app.utils.SearchHistoryHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private static final int DEBOUNCE_MS = 350;
    private static final int PAGE_SIZE = 20;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Runnable suggestRunnable;

    private ApiService api;
    private RecyclerView rvResults;
    private RecyclerView rvSuggestions;
    private RecyclerView rvHistory;
    private LinearLayout rowHistory;
    private MaterialButton btnClearHistory;
    private ProductAdapter productAdapter;
    private SearchSuggestionAdapter suggestionAdapter;
    private SearchHistoryAdapter historyAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EditText etSearch;

    private String currentQuery = "";
    private int currentPage = 1;
    private boolean loadingMore;
    private boolean lastPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        api = ApiClient.getService();
        etSearch = findViewById(R.id.et_search);
        rvResults = findViewById(R.id.rv_search_results);
        rvSuggestions = findViewById(R.id.rv_suggestions);
        rvHistory = findViewById(R.id.rv_search_history);
        rowHistory = findViewById(R.id.row_search_history);
        btnClearHistory = findViewById(R.id.btn_clear_history);
        progressBar = findViewById(R.id.progress_search);
        tvEmpty = findViewById(R.id.tv_search_empty);

        productAdapter = new ProductAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvResults.setAdapter(productAdapter);
        rvResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0 || currentQuery.isEmpty() || loadingMore || lastPage) return;
                GridLayoutManager lm = (GridLayoutManager) rv.getLayoutManager();
                if (lm == null) return;
                int last = lm.findLastVisibleItemPosition();
                if (last >= productAdapter.getItemCount() - 4) {
                    loadSearchPage(currentPage + 1, true);
                }
            }
        });

        suggestionAdapter = new SearchSuggestionAdapter(s -> {
            etSearch.setText(s.name);
            etSearch.setSelection(s.name.length());
            rvSuggestions.setVisibility(View.GONE);
            runSearchNow(s.name.trim());
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSuggestions.setAdapter(suggestionAdapter);

        historyAdapter = new SearchHistoryAdapter(new SearchHistoryAdapter.Listener() {
            @Override
            public void onPickQuery(String query) {
                etSearch.setText(query);
                etSearch.setSelection(query.length());
                rvSuggestions.setVisibility(View.GONE);
                runSearchNow(query);
            }

            @Override
            public void onRemoveQuery(String query) {
                SearchHistoryHelper.remove(SearchActivity.this, query);
                refreshSearchHistoryUi();
                Toast.makeText(SearchActivity.this, R.string.search_history_removed, Toast.LENGTH_SHORT).show();
            }
        });
        rvHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHistory.setAdapter(historyAdapter);

        btnClearHistory.setOnClickListener(v -> {
            SearchHistoryHelper.clear(this);
            refreshSearchHistoryUi();
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                updateHistoryStripVisibility(q);

                if (suggestRunnable != null) handler.removeCallbacks(suggestRunnable);
                suggestRunnable = () -> loadSuggestions(q);
                handler.postDelayed(suggestRunnable, DEBOUNCE_MS);

                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    if (q.isEmpty()) {
                        loadingMore = false;
                        productAdapter.updateData(new ArrayList<>());
                        tvEmpty.setVisibility(View.GONE);
                        lastPage = true;
                        return;
                    }
                    runSearchNow(q);
                };
                handler.postDelayed(searchRunnable, DEBOUNCE_MS);
            }
        });

        refreshSearchHistoryUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSearchHistoryUi();
        updateHistoryStripVisibility(etSearch.getText() != null ? etSearch.getText().toString().trim() : "");
    }

    private void updateHistoryStripVisibility(String q) {
        boolean show = q.isEmpty() && !SearchHistoryHelper.get(this).isEmpty();
        rowHistory.setVisibility(show ? View.VISIBLE : View.GONE);
        rvHistory.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void refreshSearchHistoryUi() {
        List<String> list = SearchHistoryHelper.get(this);
        historyAdapter.setItems(list);
        String q = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        updateHistoryStripVisibility(q);
    }

    private void loadSuggestions(String q) {
        if (q.length() < 2) {
            rvSuggestions.setVisibility(View.GONE);
            suggestionAdapter.setItems(new ArrayList<>());
            return;
        }
        api.searchSuggestions(q, 10).enqueue(new Callback<ApiResponse<SearchSuggestionsData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<SearchSuggestionsData>> call,
                                   @NonNull Response<ApiResponse<SearchSuggestionsData>> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()
                        || response.body().getData() == null) return;
                List<SearchSuggestion> list = response.body().getData().suggestions;
                if (list == null || list.isEmpty()) {
                    rvSuggestions.setVisibility(View.GONE);
                } else {
                    rvSuggestions.setVisibility(View.VISIBLE);
                    suggestionAdapter.setItems(list);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<SearchSuggestionsData>> call, @NonNull Throwable t) {}
        });
    }

    private void runSearchNow(String q) {
        currentQuery = q;
        currentPage = 1;
        lastPage = false;
        rvSuggestions.setVisibility(View.GONE);
        SearchHistoryHelper.add(this, q);
        refreshSearchHistoryUi();
        loadSearchPage(1, false);
    }

    private void loadSearchPage(int page, boolean append) {
        if (currentQuery.isEmpty()) return;
        if (loadingMore) return;
        loadingMore = true;
        if (!append) progressBar.setVisibility(View.VISIBLE);

        api.getProducts(0, currentQuery, 0, 0, page, PAGE_SIZE, "sold_count", "desc")
                .enqueue(new Callback<ApiResponse<ProductListData>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ProductListData>> call,
                                           @NonNull Response<ApiResponse<ProductListData>> response) {
                        loadingMore = false;
                        progressBar.setVisibility(View.GONE);
                        if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()
                                || response.body().getData() == null) {
                            Toast.makeText(SearchActivity.this, R.string.error_invalid_response, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ProductListData data = response.body().getData();
                        List<Product> chunk = data.products != null ? data.products : new ArrayList<>();
                        if (append && page > 1) {
                            List<Product> merged = productAdapter.snapshotProducts();
                            merged.addAll(chunk);
                            productAdapter.updateData(merged);
                        } else {
                            productAdapter.updateData(chunk);
                        }
                        currentPage = page;
                        int totalPages = data.pagination != null ? data.pagination.totalPages : 1;
                        lastPage = page >= totalPages || chunk.isEmpty();
                        tvEmpty.setVisibility(productAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ProductListData>> call, @NonNull Throwable t) {
                        loadingMore = false;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
