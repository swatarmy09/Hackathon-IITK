package com.example.financialstory;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialstory.adapters.ChatAdapter;
import com.example.financialstory.models.ChatMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private Handler handler = new Handler();
    private boolean isFirstMessage = true; // Track first message

    private static final String API_KEY = "YOUR_COHERE_API_KEY"; // Replace with your Cohere API key
    private static final String API_URL = "https://api.cohere.com/v1/generate";

    // Firebase Database Reference
    private DatabaseReference chatDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Financial Assistant");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        // Set up RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Initialize Firebase Database
        chatDatabase = FirebaseDatabase.getInstance().getReference("chatHistory");

        // Load previous chats from Firebase
        loadChatHistory();

        // Add welcome message only if there are no previous messages
        if (chatMessages.isEmpty()) {
            addMessageToChat("Hello! I'm your financial assistant. Ask me anything about your finances.", false);
        }

        // Set click listener for send button
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
            }
        });
    }

    private void sendMessage(String message) {
        // Show user message in chat
        addMessageToChat(message, true);

        // Clear input field
        messageInput.setText("");

        // Show typing animation
        ChatMessage typingMessage = new ChatMessage("Typing...", false);
        chatMessages.add(typingMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

        // Modify first message to be finance-related
        String finalMessage = isFirstMessage ? "Give financial advice about: " + message : message;
        isFirstMessage = false; // Update flag after first message

        // Send request to Cohere API
        new Thread(() -> {
            String response = getCohereResponse(finalMessage);
            if (response == null || response.trim().isEmpty()) {
                response = "I'm sorry, but I couldn't generate a response. Please try again.";
            }

            String finalResponse = response;
            runOnUiThread(() -> {
                // Remove typing animation
                chatMessages.remove(typingMessage);
                chatAdapter.notifyDataSetChanged();

                // Add AI response
                addMessageToChat(finalResponse, false);
            });
        }).start();
    }

    private String getCohereResponse(String message) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("model", "command");
            json.put("prompt", "User: " + message + "\nAI:");
            json.put("max_tokens", 150);
            json.put("temperature", 0.7);
            json.put("k", 0);
            json.put("p", 0.75);
            json.put("frequency_penalty", 0);
            json.put("presence_penalty", 0);
            json.put("stop_sequences", new JSONArray().put("\n"));
            json.put("return_likelihoods", "NONE");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;

            JSONObject responseBody = new JSONObject(response.body().string());
            JSONArray generations = responseBody.getJSONArray("generations");

            if (generations.length() > 0) {
                return generations.getJSONObject(0).getString("text").trim();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addMessageToChat(String text, boolean isUser) {
        ChatMessage chatMessage = new ChatMessage(text, isUser);
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

        // Save message to Firebase
        chatDatabase.push().setValue(chatMessage);
    }

    private void loadChatHistory() {
        chatDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear(); // Clear previous messages
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        chatMessages.add(chatMessage);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
            }
        });
    }
}
