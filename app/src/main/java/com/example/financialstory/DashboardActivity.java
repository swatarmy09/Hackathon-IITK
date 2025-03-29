package com.example.financialstory;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private TextView storyTextView;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_OPEN_COUNT = "openCount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        storyTextView = findViewById(R.id.story_text);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Get current open count
        int openCount = sharedPreferences.getInt(KEY_OPEN_COUNT, 0);

        // Alternate between the two IDs
        String statementId = (openCount % 2 == 0) ? "1743213029896" : "1743210743384";

        // Increment and save the open count
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_OPEN_COUNT, openCount + 1);
        editor.apply();

        // Fetch and display financial story based on statementId
        fetchFinancialStory(statementId);
    }

    private void fetchFinancialStory(String statementId) {
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("users/default_user/statements/" + statementId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String financialStory = dataSnapshot.child("financialStory").getValue(String.class);
                if (financialStory != null) {
                    storyTextView.setText(financialStory);
                } else {
                    storyTextView.setText("No financial story available.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to fetch data", databaseError.toException());
                storyTextView.setText("Failed to load data.");
            }
        });
    }
}
