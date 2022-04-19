package com.example.curious.ViewModels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curious.Models.Article;
import com.example.curious.Models.ArticleItem;
import com.example.curious.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ArticleItemAdapter extends RecyclerView.Adapter<ArticleItemAdapter.ArticleItemViewHolder> {
    Context context;
    ArrayList<ArticleItem> articleItems;
    private OnArticleItemClickListener mOnArticleItemClickListener;

    public ArrayList<ArticleItem> getArticleItems(){
        return articleItems;
    }
    public void setArticleItems(ArrayList<ArticleItem> articleItems) {
        this.articleItems = articleItems;
    }

    public ArticleItemAdapter(Context context, ArrayList<ArticleItem> articleItems, OnArticleItemClickListener onArticleItemClickListener){
        this.context = context;
        this.articleItems = articleItems;
        this.mOnArticleItemClickListener = onArticleItemClickListener;
    }

    public void updateArticlesAdapter(ArrayList <ArticleItem> newArticleItems) {
        this.articleItems.clear();
        this.articleItems.addAll(newArticleItems);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArticleItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_article_item, parent, false), mOnArticleItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleItemViewHolder holder, int position) {
        Picasso.get().load(articleItems.get(position).getCoverUrl()).into(holder.cover);
        holder.date.setText(articleItems.get(position).getDate());
        holder.title.setText(articleItems.get(position).getTitle());
        holder.author.setText(articleItems.get(position).getUname());
        holder.status.setText(articleItems.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return articleItems.size();
    }

    public class ArticleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView cover;
        TextView date;
        TextView title;
        TextView author;
        TextView status;
        OnArticleItemClickListener onArticleItemClickListener;
        public ArticleItemViewHolder(@NonNull View itemView, OnArticleItemClickListener onArticleItemClickListener) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.article_item_item_cover);
            date = (TextView) itemView.findViewById(R.id.article_item_item_date);
            title = (TextView) itemView.findViewById(R.id.article_item_item_title);
            author = (TextView) itemView.findViewById(R.id.article_item_item_author);
            status = (TextView) itemView.findViewById(R.id.article_item_item_status);

            this.onArticleItemClickListener = onArticleItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onArticleItemClickListener.onArticleItemClick(view, getAdapterPosition());
        }
    }

    public interface OnArticleItemClickListener {
        public void onArticleItemClick(View view, int position);
    }
}
