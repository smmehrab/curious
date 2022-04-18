package com.example.curious.ViewModels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curious.Models.Comment;
import com.example.curious.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    Context context;
    ArrayList <Comment> comments;
    private OnCommentClickListener mOnCommentClickListener;

    public ArrayList<Comment> getComments(){
        return comments;
    }
    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public CommentAdapter(Context context, ArrayList<Comment> comments, OnCommentClickListener onCommentClickListener){
        this.context = context;
        this.comments = comments;
        this.mOnCommentClickListener = onCommentClickListener;
    }

    public void updateCommentsAdapter(ArrayList<Comment> newComments) {
        this.comments.clear();
        this.comments.addAll(newComments);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentViewHolder(LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false), mOnCommentClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.userTextView.setText(comments.get(position).getUname());
        holder.commentTextView.setText(comments.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView userTextView;
        TextView commentTextView;
        OnCommentClickListener onCommentClickListener;
        public CommentViewHolder(@NonNull View itemView, OnCommentClickListener onCommentClickListener) {
            super(itemView);
            userTextView = (TextView) itemView.findViewById(R.id.comment_item_user);
            commentTextView = (TextView) itemView.findViewById(R.id.comment_item_comment);
            this.onCommentClickListener = onCommentClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onCommentClickListener.onCommentClick(view, getAdapterPosition());
        }
    }

    public interface OnCommentClickListener {
        public void onCommentClick(View view, int position);
    }
}
