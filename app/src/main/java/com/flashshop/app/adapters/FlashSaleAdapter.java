package com.flashshop.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.flashshop.app.R;
import com.flashshop.app.models.Product;
import java.util.List;

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.ViewHolder> {
    private List<Product> products;
    private final ProductAdapter.OnProductClickListener listener;

    public FlashSaleAdapter(List<Product> products, ProductAdapter.OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flash_sale, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = products.get(position);
        holder.tvPrice.setText(p.getFormattedPrice());
        holder.tvDiscount.setText("-" + p.discountPercent + "%");
        holder.tvSold.setText(p.soldCount > 100 ? ((p.soldCount * 75 / 100) + "% SOLD") : "NEW");

        Glide.with(holder.itemView.getContext()).load(p.imageUrl).centerCrop()
                .transform(new RoundedCorners(16)).into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> listener.onProductClick(p));
    }

    @Override public int getItemCount() { return products.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvPrice, tvDiscount, tvSold;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImage = v.findViewById(R.id.iv_flash);
            tvPrice = v.findViewById(R.id.tv_flash_price);
            tvDiscount = v.findViewById(R.id.tv_flash_discount);
            tvSold = v.findViewById(R.id.tv_flash_sold);
        }
    }
}
