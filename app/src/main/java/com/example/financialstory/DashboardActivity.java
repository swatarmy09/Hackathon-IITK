package com.example.financialstory;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialstory.adapters.StoryAdapter;
import com.example.financialstory.models.Story;
import com.example.financialstory.models.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView storyRecyclerView;
    private ProgressBar loadingProgressBar;
    private PieChart categoryPieChart;
    private BarChart monthlyBarChart;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    private DatabaseReference dbRef;
    private String userId = "default_user"; // Static user ID (Change if needed)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI components
        storyRecyclerView = findViewById(R.id.story_recycler_view);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        categoryPieChart = findViewById(R.id.category_pie_chart);
        monthlyBarChart = findViewById(R.id.monthly_bar_chart);

        storyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(storyList);
        storyRecyclerView.setAdapter(storyAdapter);

        // Initialize Firebase Realtime Database reference
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Load all data
        loadStories();
        loadTransactions();
    }

    private void loadStories() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        dbRef.child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    Story story = storySnapshot.getValue(Story.class);
                    if (story != null) {
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(DashboardActivity.this, "Failed to load stories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTransactions() {
        dbRef.child("statements").orderByChild("timestamp").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot statementSnapshot : snapshot.getChildren()) {
                                List<Transaction> transactions = new ArrayList<>();
                                for (DataSnapshot transactionSnapshot : statementSnapshot.child("transactions").getChildren()) {
                                    Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                                    if (transaction != null) {
                                        transactions.add(transaction);
                                    }
                                }
                                generateCategoryPieChart(transactions);
                                generateMonthlyBarChart(transactions);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DashboardActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generateCategoryPieChart(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                String category = transaction.getCategory();
                double amount = Math.abs(transaction.getAmount());
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }

        if (categoryTotals.isEmpty()) {
            categoryPieChart.setVisibility(View.GONE);
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending by Category");
        dataSet.setColors(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        categoryPieChart.setData(data);
        categoryPieChart.setDescription(null);
        categoryPieChart.setCenterText("Spending\nby Category");
        categoryPieChart.setHoleColor(Color.TRANSPARENT);
        categoryPieChart.invalidate();
    }

    private void generateMonthlyBarChart(List<Transaction> transactions) {
        Map<String, Double> monthlyTotals = new HashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);

        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                String month = monthFormat.format(transaction.getDate());
                double amount = Math.abs(transaction.getAmount());
                monthlyTotals.put(month, monthlyTotals.getOrDefault(month, 0.0) + amount);
            }
        }

        if (monthlyTotals.isEmpty()) {
            monthlyBarChart.setVisibility(View.GONE);
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Spending");
        dataSet.setColor(Color.MAGENTA);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        monthlyBarChart.setData(data);
        monthlyBarChart.invalidate();
    }
}
