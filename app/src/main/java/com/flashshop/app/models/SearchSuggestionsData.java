package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchSuggestionsData {
    @SerializedName("suggestions") public List<SearchSuggestion> suggestions;
}
