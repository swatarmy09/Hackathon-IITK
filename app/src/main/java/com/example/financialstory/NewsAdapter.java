package com.example.financialstory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsItem> newsItems;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public NewsAdapter(Context context, List<NewsItem> newsItems) {
        this.context = context;
        this.newsItems = newsItems;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsItems.get(position);

        holder.titleTextView.setText(newsItem.getTitle());
        holder.descriptionTextView.setText(newsItem.getDescription());

        // Format and set date
        try {
            Date date = inputFormat.parse(newsItem.getPublishedAt());
            holder.dateTextView.setText(outputFormat.format(date));
        } catch (ParseException e) {
            holder.dateTextView.setText(newsItem.getPublishedAt());
        }

        holder.sourceTextView.setText(newsItem.getSourceName());

        // Load image with Glide
        if (newsItem.getImageUrl() != null && !newsItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(newsItem.getImageUrl())
                    .placeholder(R.drawable.bg)
                    .error(R.drawable.error_image)
                    .into(holder.newsImageView);
            holder.newsImageView.setVisibility(View.VISIBLE);
        } else {
            holder.newsImageView.setVisibility(View.GONE);
        }

        // Set click listener to open the news URL
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(newsItem.getUrl()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView newsImageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView dateTextView;
        TextView sourceTextView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            newsImageView = itemView.findViewById(R.id.newsImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
        }
    }
}