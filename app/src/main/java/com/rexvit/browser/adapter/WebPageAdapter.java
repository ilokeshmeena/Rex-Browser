package com.rexvit.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rexvit.browser.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.bumptech.glide.Glide;
import com.rexvit.browser.utils.WebPage;

import java.util.List;

public class WebPageAdapter extends RecyclerView.Adapter<WebPageAdapter.ViewHolder> {
    private final Context context;
    private List<WebPage> pages;
    private LayoutInflater inflater;
    private ItemClickListener clickListener;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onAddButtonClick();
    }

    
    public WebPageAdapter(Context context, List<WebPage> pages) {
        this.inflater = LayoutInflater.from(context);
        this.pages = pages;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD_BUTTON) {
            View view = inflater.inflate(R.layout.item_add_button, parent, false);
            return new ViewHolder(view, viewType);
        } else {
            View view = inflater.inflate(R.layout.item_page, parent, false);
            return new ViewHolder(view, viewType);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            WebPage page = pages.get(position);
            holder.pageName.setText(page.getName());
            holder.pageUrl.setText(page.getUrl());
           
            Glide.with(context)
                    .load(page.getIconUrl()) 
                    .error(R.drawable.ic_default_page) 
                    .into(holder.pageIcon); 


        }
        
    }

    @Override
    public int getItemCount() {
        return pages.size() + 1; 
    }

    @Override
    public int getItemViewType(int position) {
        if (position == pages.size()) {
            return TYPE_ADD_BUTTON;
        } else {
            return TYPE_ITEM;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView pageName;
        TextView pageUrl;
        ShapeableImageView pageIcon;
        ImageView addButton;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_ITEM) {
                pageName = itemView.findViewById(R.id.pageName);
                pageUrl = itemView.findViewById(R.id.pageUrl);
                pageIcon = itemView.findViewById(R.id.pageIcon);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            } else if (viewType == TYPE_ADD_BUTTON) {
                addButton = itemView.findViewById(R.id.addButton);
                /*
                addButton.setOnClickListener(v -> {
                    if (clickListener != null) clickListener.onItemClick(v, getAdapterPosition());
                });

                 */
                addButton.setOnClickListener(v -> {
                    if (clickListener != null) clickListener.onAddButtonClick();
                });

            }
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (clickListener != null) {
                clickListener.onItemLongClick(view, getAdapterPosition());
                return true;
            }
            return false;
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public List<WebPage> getPages() {
        return pages;
    }

}
