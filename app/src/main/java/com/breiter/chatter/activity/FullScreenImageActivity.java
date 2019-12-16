package com.breiter.chatter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.breiter.chatter.R;
import com.breiter.chatter.tool.OnSwipeTouchListener;
import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity {
    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        Intent intent = getIntent();
        Glide.with(FullScreenImageActivity.this).load(intent.getStringExtra("imageUrl")).into(fullScreenImageView);

        //Return to previous activity once swiped up or down
        fullScreenImageView.setOnTouchListener(new OnSwipeTouchListener(FullScreenImageActivity.this) {
            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                finish();
            }

            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                finish();
            }


        });


    }


}
