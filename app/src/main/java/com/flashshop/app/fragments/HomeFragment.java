package com.flashshop.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.flashshop.app.R;
import com.flashshop.app.activities.CartActivity;
import com.flashshop.app.activities.CategoryProductsActivity;
import com.flashshop.app.activities.ProductDetailActivity;
import com.flashshop.app.activities.SearchActivity;
import com.flashshop.app.adapters.BannerAdapter;
import com.flashshop.app.adapters.CategoryAdapter;
import com.flashshop.app.adapters.FlashSaleAdapter;
import com.flashshop.app.adapters.ProductAdapter;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.*;
import com.flashshop.app.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private ApiService apiService;
    private ViewPager2 bannerPager;
    private RecyclerView rvCategories, rvFlashSale, rvRecommended;
    private SwipeRefreshLayout swipeRefresh;
    private ProductAdapter productAdapter;
    private FlashSaleAdapter flashSaleAdapter;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiClient.getService();

        bannerPager = view.findViewById(R.id.banner_pager);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvFlashSale = view.findViewById(R.id.rv_flash_sale);
        rvRecommended = view.findViewById(R.id.rv_recommended);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        view.findViewById(R.id.btn_cart).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CartActivity.class)));

        // Setup adapters
        bannerAdapter = new BannerAdapter(new ArrayList<>());
        bannerPager.setAdapter(bannerAdapter);

        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        categoryAdapter.setOnCategoryClickListener(category -> {
            Intent i = new Intent(getActivity(), CategoryProductsActivity.class);
            i.putExtra(CategoryProductsActivity.EXTRA_CATEGORY_ID, category.id);
            i.putExtra(CategoryProductsActivity.EXTRA_CATEGORY_NAME, category.name);
            startActivity(i);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setClipToPadding(false);
        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        rvCategories.setPadding(pad, 0, pad, 0);
        rvCategories.setAdapter(categoryAdapter);

        flashSaleAdapter = new FlashSaleAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
        rvFlashSale.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFlashSale.setAdapter(flashSaleAdapter);

        productAdapter = new ProductAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
        rvRecommended.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRecommended.setAdapter(productAdapter);
        rvRecommended.setNestedScrollingEnabled(false);

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadData);

        EditText etSearch = view.findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.setFocusable(false);
            etSearch.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), SearchActivity.class)));
        }

        loadData();
    }

    private void loadData() {
        swipeRefresh.setRefreshing(true);
        loadBanners();
        loadCategories();
        loadFlashSale();
        loadRecommended();
    }

    private void loadBanners() {
        apiService.getBanners().enqueue(new Callback<ApiResponse<BannerListData>>() {
            @Override
            public void onResponse(Call<ApiResponse<BannerListData>> call, Response<ApiResponse<BannerListData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    bannerAdapter.updateData(response.body().getData().banners);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<BannerListData>> call, Throwable t) {}
        });
    }

    private void loadCategories() {
        apiService.getCategories().enqueue(new Callback<ApiResponse<CategoryListData>>() {
            @Override
            public void onResponse(Call<ApiResponse<CategoryListData>> call, Response<ApiResponse<CategoryListData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    categoryAdapter.updateData(response.body().getData().categories);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<CategoryListData>> call, Throwable t) {}
        });
    }

    private void loadFlashSale() {
        apiService.getProducts(0, "", 0, 1, 1, 10, "sold_count", "desc")
                .enqueue(new Callback<ApiResponse<ProductListData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ProductListData>> call, Response<ApiResponse<ProductListData>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            flashSaleAdapter.updateData(response.body().getData().products);
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<ProductListData>> call, Throwable t) {}
                });
    }

    private void loadRecommended() {
        apiService.getProducts(0, "", 1, 0, 1, 20, "rating", "desc")
                .enqueue(new Callback<ApiResponse<ProductListData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ProductListData>> call, Response<ApiResponse<ProductListData>> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            productAdapter.updateData(response.body().getData().products);
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<ProductListData>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }
}
