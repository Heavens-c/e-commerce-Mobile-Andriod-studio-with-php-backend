package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductListData {
    @SerializedName("products") public List<Product> products;
    @SerializedName("pagination") public Pagination pagination;

    public static class Pagination {
        @SerializedName("total") public int total;
        @SerializedName("page") public int page;
        @SerializedName("limit") public int limit;
        @SerializedName("total_pages") public int totalPages;
    }
}

