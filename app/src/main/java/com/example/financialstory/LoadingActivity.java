package com.example.financialstory;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView percentageText;
    private ImageView logoImage;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Initialize views
        progressBar = findViewById(R.id.loadingProgressBar);
        percentageText = findViewById(R.id.percentageText);
        logoImage = findViewById(R.id.logoImage);

        // Set up logo animation
        animateLogo();

        // Set up progress animation
        animateProgress();

        // Automatically close after 5 seconds (adjust as needed)
        new Handler().postDelayed(this::finish, 5000);
    }

    private void animateLogo() {
        // Scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoImage, "scaleX", 0.8f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoImage, "scaleY", 0.8f, 1.2f, 1.0f);

        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();

        // Fade in animation
        logoImage.setAlpha(0f);
        logoImage.setVisibility(View.VISIBLE);
        logoImage.animate()
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateProgress() {
        // Update the progress bar in a separate thread
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;

                // Update the progress bar and percentage text on the UI thread
                handler.post(() -> {
                    progressBar.setProgress(progressStatus);
                    percentageText.setText(progressStatus + "%");
                });

                try {
                    // Sleep for 50 milliseconds to show progress slowly
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

