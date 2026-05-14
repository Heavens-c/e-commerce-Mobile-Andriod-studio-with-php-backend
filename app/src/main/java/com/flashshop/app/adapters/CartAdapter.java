package com.flashshop.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.flashshop.app.R;
import com.flashshop.app.models.CartItem;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private final List<CartItem> items;
    private final CartActionListener listener;

    public interface CartActionListener {
        void onQuantityChanged(int cartId, String action);
    }

    public CartAdapter(List<CartItem> items, CartActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        CartItem item = items.get(pos);
        h.tvName.setText(item.name);
        h.tvPrice.setText(item.getFormattedPrice());
        h.tvQuantity.setText(String.valueOf(item.quantity));
        if (item.variation != null) { h.tvVariation.setText(item.variation); h.tvVariation.setVisibility(View.VISIBLE); }
        else { h.tvVariation.setVisibility(View.GONE); }
        Glide.with(h.itemView.getContext()).load(item.imageUrl).centerCrop().into(h.ivImage);
        h.btnMinus.setOnClickListener(v -> listener.onQuantityChanged(item.cartId, item.quantity > 1 ? "decrement" : "remove"));
        h.btnPlus.setOnClickListener(v -> listener.onQuantityChanged(item.cartId, "increment"));
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage; TextView tvName, tvPrice, tvQuantity, tvVariation; ImageButton btnMinus, btnPlus;
        ViewHolder(@NonNull View v) {
            super(v);
            ivImage = v.findViewById(R.id.iv_cart_item); tvName = v.findViewById(R.id.tv_cart_name);
            tvPrice = v.findViewById(R.id.tv_cart_price); tvQuantity = v.findViewById(R.id.tv_cart_qty);
            tvVariation = v.findViewById(R.id.tv_cart_variation);
            btnMinus = v.findViewById(R.id.btn_minus); btnPlus = v.findViewById(R.id.btn_plus);
        }
    }
}
