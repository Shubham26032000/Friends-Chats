package com.example.friendschats.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.friendschats.Constant.Constants;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Models.User;
import com.example.friendschats.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        progressDialog  = new ProgressDialog(this);
        progressDialog.setTitle("Sign up");
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        /**
         * Check if user is already log in.
         */
        if (auth.getCurrentUser() != null)
        {
            Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }


        /***
         * Sign up user
         */
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkAllNecessaryDetailsFilled();

            }
        });

        /***
         * If user already register then send to log in activity
         */
        binding.tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, SIgnInActivity.class);
                startActivity(intent);
            }
        });

    }



    /***
     * Sign up user with email and password to our app
     * @param email
     * @param password
     * @param userName
     */
    public void signUpWithEmailAndPassword(String email, String password, String userName)
    {
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful())
                        {
                            User user = new User(userName, email, password);
                            String id =  task.getResult().getUser().getUid();
                            firebaseDatabase.getReference()
                                    .child(FirebaseConstants.USER)
                                    .child(id).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                Intent intent = new Intent(SignUpActivity.this,SetUpProfileActivity.class);
                                                intent.putExtra(Constants.USER_NAME, userName);
                                                startActivity(intent);
                                                finish();
                                            }else
                                            {
                                                Toast.makeText(SignUpActivity.this, task.getException().toString().trim(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
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
        String userName = binding.etUserName.getText().toString().trim();
        if (email.equals(""))
        {
            binding.etEmail.setError("Please enter email address");
            return;
        }

        if (password.equals(""))
        {
            binding.etPassword.setError("Please enter password");
            return;
        }else if (password.length() < 6) //password must contain 6 letter.
        {
            binding.etPassword.setError("Password must contain at least 6 character");
            return;
        }
        if (userName.equals(""))
        {
            binding.etUserName.setError("Please enter name");
            return;
        }
        progressDialog.show();

        //send user for sign Up
        signUpWithEmailAndPassword(email, password, userName);


    }

}