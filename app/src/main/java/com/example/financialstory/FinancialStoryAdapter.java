package com.example.financialstory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinancialStoryAdapter extends RecyclerView.Adapter<FinancialStoryAdapter.ViewHolder> {

    private List<FinancialStory> financialStories;
    private SimpleDateFormat dateFormat;

    public FinancialStoryAdapter(List<FinancialStory> financialStories) {
        this.financialStories = financialStories;
        this.dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_financial_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FinancialStory story = financialStories.get(position);
        holder.tvFinancialStory.setText(story.getFinancialStory());

        // Format the timestamp to a readable date
        String formattedDate = dateFormat.format(new Date(story.getTimestamp()));
        holder.tvTimestamp.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return financialStories.size();
    }

    public void updateData(List<FinancialStory> newStories) {
        this.financialStories = newStories;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFinancialStory;
        TextView tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFinancialStory = itemView.findViewById(R.id.tvFinancialStory);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

