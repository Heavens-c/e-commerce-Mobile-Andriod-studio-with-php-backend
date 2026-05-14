package com.flashshop.app.api;

import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.models.BannerListData;
import com.flashshop.app.models.CartData;
import com.flashshop.app.models.CategoryListData;
import com.flashshop.app.models.AdminAnalyticsData;
import com.flashshop.app.models.AdminOrdersData;
import com.flashshop.app.models.AdminStatsData;
import com.flashshop.app.models.LoginData;
import com.flashshop.app.models.LoginResponse;
import com.flashshop.app.models.NotificationListData;
import com.flashshop.app.models.OrderData;
import com.flashshop.app.models.OrderListData;
import com.flashshop.app.models.ProductListData;
import com.flashshop.app.models.ProfileData;
import com.flashshop.app.models.ReviewListData;
import com.flashshop.app.models.SearchSuggestionsData;
import com.flashshop.app.models.UploadImageData;
import com.flashshop.app.models.WishlistData;

import java.util.Map;

import okhttp3.MultipartBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    // Auth — form-urlencoded for compatibility with Remote PHP ($_POST)
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(@Field("email") String email, @Field("password") String password);

    @POST("register.php")
    Call<ApiResponse<LoginData>> register(@Body Map<String, String> body);

    @POST("forgot_password.php")
    Call<ApiResponse<Object>> forgotPassword(@Body Map<String, String> body);

    @POST("reset_password_api.php")
    Call<ApiResponse<Object>> resetPasswordApi(@Body Map<String, String> body);

    @GET("search_suggestions.php")
    Call<ApiResponse<SearchSuggestionsData>> searchSuggestions(@Query("q") String query, @Query("limit") int limit);

    @POST("logout.php")
    Call<ApiResponse<Object>> logout(@Header("Authorization") String token);

    // Products
    @GET("get_products.php")
    Call<ApiResponse<ProductListData>> getProducts(
            @Query("category_id") int categoryId,
            @Query("search") String search,
            @Query("featured") int featured,
            @Query("flash_sale") int flashSale,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("sort") String sort,
            @Query("order") String order
    );

    // Categories
    @GET("get_categories.php")
    Call<ApiResponse<CategoryListData>> getCategories();

    // Banners
    @GET("get_banners.php")
    Call<ApiResponse<BannerListData>> getBanners();

    // Cart
    @POST("add_to_cart.php")
    Call<ApiResponse<Object>> addToCart(@Header("Authorization") String token, @Body Map<String, Object> body);

    @GET("get_cart.php")
    Call<ApiResponse<CartData>> getCart(@Header("Authorization") String token);

    @POST("update_cart.php")
    Call<ApiResponse<Object>> updateCart(@Header("Authorization") String token, @Body Map<String, Object> body);

    // Wishlist
    @POST("wishlist.php")
    Call<ApiResponse<WishlistData>> toggleWishlist(@Header("Authorization") String token, @Body Map<String, Object> body);

    @GET("wishlist.php")
    Call<ApiResponse<WishlistData>> getWishlist(@Header("Authorization") String token);

    // Orders
    @POST("place_order.php")
    Call<ApiResponse<OrderData>> placeOrder(@Header("Authorization") String token, @Body Map<String, Object> body);

    @GET("get_orders.php")
    Call<ApiResponse<OrderListData>> getOrders(@Header("Authorization") String token, @Query("status") String status);

    // Admin — requires admin Bearer token from login.php
    @GET("admin_stats.php")
    Call<ApiResponse<AdminStatsData>> getAdminStats(@Header("Authorization") String token);

    @GET("admin_orders.php")
    Call<ApiResponse<AdminOrdersData>> getAdminOrders(@Header("Authorization") String token);

    @POST("update_order_status.php")
    Call<ApiResponse<Object>> updateOrderStatus(@Header("Authorization") String token, @Body Map<String, Object> body);

    @POST("add_product.php")
    Call<ApiResponse<Object>> addProduct(@Header("Authorization") String token, @Body Map<String, Object> body);

    @POST("update_product.php")
    Call<ApiResponse<Object>> updateProduct(@Header("Authorization") String token, @Body Map<String, Object> body);

    @POST("delete_product.php")
    Call<ApiResponse<Object>> deleteProduct(@Header("Authorization") String token, @Body Map<String, Object> body);

    @GET("admin_list_products.php")
    Call<ApiResponse<ProductListData>> adminListProducts(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("limit") int limit
    );

    @Multipart
    @POST("admin_upload_image.php")
    Call<ApiResponse<UploadImageData>> adminUploadImage(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image
    );

    @GET("admin_analytics.php")
    Call<ApiResponse<AdminAnalyticsData>> getAdminAnalytics(@Header("Authorization") String token);

    // Reviews
    @GET("reviews.php")
    Call<ApiResponse<ReviewListData>> getReviews(@Query("product_id") int productId);

    @POST("reviews.php")
    Call<ApiResponse<Object>> addReview(@Header("Authorization") String token, @Query("product_id") int productId, @Body Map<String, Object> body);

    // Profile
    @GET("profile.php")
    Call<ApiResponse<ProfileData>> getProfile(@Header("Authorization") String token);

    @PUT("profile.php")
    Call<ApiResponse<ProfileData>> updateProfile(@Header("Authorization") String token, @Body Map<String, String> body);

    // Notifications
    @GET("get_notifications.php")
    Call<ApiResponse<NotificationListData>> getNotifications(@Header("Authorization") String token);
}
