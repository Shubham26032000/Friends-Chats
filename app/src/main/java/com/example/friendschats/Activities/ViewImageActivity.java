package com.example.friendschats.Activities;

import androidx.appcompat.app.AppCompatActivity;

import com.example.friendschats.Constant.Constants;
import com.example.friendschats.R;
import com.example.friendschats.databinding.ActivityViewImageBinding;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * In this activity we show selected image.
 */
public class ViewImageActivity extends AppCompatActivity {

    ActivityViewImageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String profilePic = getIntent().getStringExtra(Constants.PROFILE_PICTURE_URL);
        String ActivityName = getIntent().getStringExtra(Constants.ACTIVITY_NAME);

        binding.tvUiName.setText(ActivityName);
        Picasso.get().load(profilePic)
                .placeholder(R.drawable.avatar)
                .into(binding.ivProfilepic);

        binding.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getSupportActionBar().hide();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}