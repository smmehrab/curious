package com.example.curious.ViewModels;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curious.Models.Article;
import com.example.curious.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {
    Context context;
    ArrayList <Article> articles;
    private OnArticleClickListener mOnArticleClickListener;

    public ArrayList<Article> getArticles(){
        return articles;
    }
    public void setArticles(ArrayList<Article> articles) {
        this.articles = articles;
    }

    public ArticleAdapter(Context context, ArrayList<Article> articles, OnArticleClickListener onArticleClickListener){
        this.context = context;
        this.articles = articles;
        this.mOnArticleClickListener = onArticleClickListener;
    }

    public void updateArticlesAdapter(ArrayList <Article> newArticles) {
        this.articles.clear();
        this.articles.addAll(newArticles);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArticleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_article, parent, false), mOnArticleClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Picasso.get().load(articles.get(position).getCoverUrl()).into(holder.cover);
        holder.date.setText(articles.get(position).getTimestamp().toString());
        holder.title.setText(articles.get(position).getTitle());
        holder.author.setText(articles.get(position).getUid());
        holder.views.setText(articles.get(position).getViewCount().toString() + " Views");
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView cover;
        TextView date;
        TextView title;
        TextView author;
        TextView views;
        OnArticleClickListener onArticleClickListener;
        public ArticleViewHolder(@NonNull View itemView, OnArticleClickListener onArticleClickListener) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.article_item_cover);
            date = (TextView) itemView.findViewById(R.id.article_item_date);
            title = (TextView) itemView.findViewById(R.id.article_item_title);
            author = (TextView) itemView.findViewById(R.id.article_item_author);
            views = (TextView) itemView.findViewById(R.id.article_item_views);

            this.onArticleClickListener = onArticleClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onArticleClickListener.onArticleClick(view, getAdapterPosition());
        }
    }

    public interface OnArticleClickListener {
        public void onArticleClick(View view, int position);
    }
}
