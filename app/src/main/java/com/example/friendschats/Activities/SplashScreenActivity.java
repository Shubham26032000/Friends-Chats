package com.example.friendschats.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.friendschats.databinding.ActivitySplashScreenBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {

    ActivitySplashScreenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        Intent intent;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth == null)
        {
            intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        }else{
            intent = new Intent(SplashScreenActivity.this, SignUpActivity.class);
        }

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        },1300);



    }
}