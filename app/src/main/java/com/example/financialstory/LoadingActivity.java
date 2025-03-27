package com.example.financialstory;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Automatically close after 5 seconds (adjust as needed)
        new Handler().postDelayed(this::finish, 5000);
    }
}
