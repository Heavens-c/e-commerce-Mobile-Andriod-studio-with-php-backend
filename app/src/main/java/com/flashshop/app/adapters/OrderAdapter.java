package com.flashshop.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.flashshop.app.R;
import com.flashshop.app.models.Order;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private final List<Order> orders;

    public OrderAdapter(List<Order> orders) { this.orders = orders; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Order o = orders.get(pos);
        h.tvOrderNum.setText(o.orderNumber);
        h.tvTotal.setText(String.format("$%.2f", o.total));
        h.tvStatus.setText(o.orderStatus.toUpperCase());
        h.tvDate.setText(o.createdAt);
        h.tvItems.setText(o.items != null ? o.items.size() + " item(s)" : "");

        // Color status
        if ("delivered".equals(o.orderStatus)) h.tvStatus.setTextColor(0xFF4CAF50);
        else if ("cancelled".equals(o.orderStatus)) h.tvStatus.setTextColor(0xFFBA1A1A);
        else h.tvStatus.setTextColor(0xFF765B00);

        if (o.items != null && !o.items.isEmpty()) {
            Glide.with(h.itemView.getContext()).load(o.items.get(0).productImage)
                    .centerCrop().into(h.ivThumb);
        }
    }

    @Override public int getItemCount() { return orders.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNum, tvTotal, tvStatus, tvDate, tvItems;
        ImageView ivThumb;
        ViewHolder(@NonNull View v) {
            super(v);
            tvOrderNum = v.findViewById(R.id.tv_order_num);
            tvTotal = v.findViewById(R.id.tv_order_total);
            tvStatus = v.findViewById(R.id.tv_order_status);
            tvDate = v.findViewById(R.id.tv_order_date);
            tvItems = v.findViewById(R.id.tv_order_items);
            ivThumb = v.findViewById(R.id.iv_order_thumb);
        }
    }
}
