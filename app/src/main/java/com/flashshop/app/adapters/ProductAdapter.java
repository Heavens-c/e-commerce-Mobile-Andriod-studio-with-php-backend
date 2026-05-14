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
import com.flashshop.app.models.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> products;
    private final OnProductClickListener listener;
    /** When non-null, long-press opens admin actions (e.g. deactivate). */
    private final OnProductLongClickListener longClickListener;

    public interface OnProductClickListener { void onProductClick(Product product); }

    public interface OnProductLongClickListener { boolean onProductLongClick(Product product); }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this(products, listener, null);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener, OnProductLongClickListener longClickListener) {
        this.products = products;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public List<Product> snapshotProducts() {
        return new java.util.ArrayList<>(products);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.name);
        holder.tvPrice.setText(product.getFormattedPrice());
        holder.tvRating.setText(String.valueOf(product.rating));
        holder.tvSold.setText(product.getFormattedSoldCount());

        Glide.with(holder.itemView.getContext())
                .load(product.imageUrl)
                .centerCrop()
                .placeholder(R.color.surface_container)
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> longClickListener.onProductLongClick(product));
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override public int getItemCount() { return products.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvRating, tvSold;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvSold = itemView.findViewById(R.id.tv_sold);
        }
    }
}
