package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id") public int id;
    @SerializedName("full_name") public String fullName;
    @SerializedName("email") public String email;
    @SerializedName("phone") public String phone;
    @SerializedName("avatar_url") public String avatarUrl;
    @SerializedName("address") public String address;
    @SerializedName("city") public String city;
    @SerializedName("province") public String province;
    @SerializedName("zip_code") public String zipCode;
    @SerializedName("country") public String country;
    @SerializedName("membership") public String membership;
    @SerializedName("coins") public int coins;
    @SerializedName("points") public int points;
}
