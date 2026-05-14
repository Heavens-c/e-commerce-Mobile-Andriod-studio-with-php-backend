package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Data wrapper classes for API responses
public class LoginData {
    @SerializedName("token") public String token;
    @SerializedName("user") public User user;
}

class Pagination {
    @SerializedName("total") public int total;
    @SerializedName("page") public int page;
    @SerializedName("limit") public int limit;
    @SerializedName("total_pages") public int totalPages;
}
