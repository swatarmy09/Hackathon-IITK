package com.example.financialstory;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsItems;
    private ProgressBar progressBar;
    private TextView errorText;
    private Button refreshButton;
    private RequestQueue requestQueue;

    // Replace with your actual GNews API key
    private static final String API_KEY = "3fb0b2811d684e5aae7a084d189c00b3";
    private static final String NEWS_URL = "https://gnews.io/api/v4/search?q=finance&country=in&category=business&apikey=" + API_KEY + "&max=10&lang=en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        errorText = findViewById(R.id.errorText);
        refreshButton = findViewById(R.id.refreshButton);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsItems = new ArrayList<>();
        newsAdapter = new NewsAdapter(this, newsItems);
        recyclerView.setAdapter(newsAdapter);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Set refresh button click listener
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNews();
            }
        });

        // Fetch news when activity starts
        fetchNews();
    }

    private void fetchNews() {
        showLoading();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                NEWS_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseNewsResponse(response);
                            showContent();
                        } catch (JSONException e) {
                            showError("Failed to parse news data");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError("Failed to load news. Please check your internet connection.");
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void parseNewsResponse(JSONObject response) throws JSONException {
        newsItems.clear();

        JSONArray articles = response.getJSONArray("articles");
        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);

            String title = article.getString("title");
            String description = article.getString("description");
            String content = article.getString("content");
            String url = article.getString("url");
            String imageUrl = article.optString("image", "");
            String publishedAt = article.getString("publishedAt");

            JSONObject source = article.getJSONObject("source");
            String sourceName = source.getString("name");

            NewsItem newsItem = new NewsItem(
                    title,
                    description,
                    content,
                    url,
                    imageUrl,
                    publishedAt,
                    sourceName
            );

            newsItems.add(newsItem);
        }

        newsAdapter.notifyDataSetChanged();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}