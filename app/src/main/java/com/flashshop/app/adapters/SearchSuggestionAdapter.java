package com.flashshop.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flashshop.app.R;
import com.flashshop.app.models.SearchSuggestion;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.VH> {

    public interface Listener {
        void onPick(SearchSuggestion s);
    }

    private final List<SearchSuggestion> items = new ArrayList<>();
    private final Listener listener;

    public SearchSuggestionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<SearchSuggestion> next) {
        items.clear();
        if (next != null) items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_suggestion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SearchSuggestion s = items.get(position);
        h.tv.setText(s.name);
        if (s.imageUrl != null && !s.imageUrl.isEmpty()) {
            Glide.with(h.itemView).load(s.imageUrl).centerCrop().into(h.iv);
        }
        h.itemView.setOnClickListener(v -> listener.onPick(s));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tv;
        final ImageView iv;

        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_suggest_name);
            iv = itemView.findViewById(R.id.iv_suggest_thumb);
        }
    }
}
