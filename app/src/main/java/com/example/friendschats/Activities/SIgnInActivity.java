package com.example.friendschats.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.friendschats.Models.User;
import com.example.friendschats.R;
import com.example.friendschats.databinding.ActivitySignInBinding;
import com.example.friendschats.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class SIgnInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Log in");
        progressDialog.setMessage("Log in to your account...");
        progressDialog.setCancelable(false);

        ///Sign in user
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkAllNecessaryDetailsFilled();
            }
        });

        //If new user then send him to sign up page
        binding.tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SIgnInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });


    }


    /***
     * Sign in user in app.
     * @param email
     * @param password
     */
    public void signInWithEmailAndPassword(String email, String password)
    {

        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(SIgnInActivity.this,MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }else
                        {
                            Toast.makeText(SIgnInActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /****
     * Check whether all necessary information is filled by user
     */
    public void checkAllNecessaryDetailsFilled()
    {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        if (email.equals(""))
        {
            binding.etEmail.setError("Please enter email");
            return;
        }
        if (password.equals(""))
        {
            binding.etPassword.setError("Please enter password");
            return;
        }


        progressDialog.show();

        //send user for sign Up
        signInWithEmailAndPassword(email, password);


    }

}