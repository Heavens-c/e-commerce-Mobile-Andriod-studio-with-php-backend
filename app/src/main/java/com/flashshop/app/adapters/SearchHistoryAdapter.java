package com.flashshop.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flashshop.app.R;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.VH> {

    public interface Listener {
        void onPickQuery(String query);

        void onRemoveQuery(String query);
    }

    private final List<String> items = new ArrayList<>();
    private final Listener listener;

    public SearchHistoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<String> queries) {
        items.clear();
        if (queries != null) items.addAll(queries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String q = items.get(position);
        h.tv.setText(q);
        h.itemView.setOnClickListener(v -> listener.onPickQuery(q));
        h.itemView.setOnLongClickListener(v -> {
            listener.onRemoveQuery(q);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tv;

        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_query);
        }
    }
}
