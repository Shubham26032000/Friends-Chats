package com.example.friendschats.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.friendschats.Adapters.UsersAdapter;
import com.example.friendschats.Constant.Constants;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Fragments.ChatsFragment;
import com.example.friendschats.Fragments.ProfileFragment;
import com.example.friendschats.Fragments.UsersFragment;
import com.example.friendschats.Models.User;
import com.example.friendschats.R;
import com.example.friendschats.databinding.ActivityMainBinding;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ProgressDialog statusProgressDialog;

    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Setting progressbar
        statusProgressDialog = new ProgressDialog(this);
        statusProgressDialog.setMessage("Uploading Image");
        statusProgressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();



        //Open default fragment manager
        Fragment fragment1 = new UsersFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrame,fragment1)
                .addToBackStack(null)
                .commit();

        //Bottom navigation
          binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
              @Override
              public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                  Fragment fragment = null;
                  switch (item.getItemId())
                  {
                      //If chats fragment
                      case R.id.menu_chats:
                          fragment = new UsersFragment();
                          break;

                      //If user fragment
                      case R.id.menu_users:
                          fragment = new ChatsFragment();
                          break;

                      //If profile fragment
                      case R.id.menu_profile:
                          fragment = new ProfileFragment();
                          break;

                  }
                  //Load selected  fragment
                  getSupportFragmentManager().beginTransaction().replace(R.id.containerFrame,fragment)
                          .commit();
                  return true;
              }
          });
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            updateUserStatus("Offline");
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            updateUserStatus("Online");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            updateUserStatus("Online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            updateUserStatus("Offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            updateUserStatus("Online");
    }









    //Menu item initialisation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);

    }

    //Menu item action
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                updateUserStatus("Offline");

                logoutUser();
                break;


        }
        return super.onOptionsItemSelected(item);
    }


    /***
     * Log out user from app.
     */
    public void logoutUser()
    {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Log out")
                .setMessage("Are you sure do you want to log out?")

                .setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        auth.signOut();
                        Intent intent = new Intent(MainActivity.this, SIgnInActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "Log out", Toast.LENGTH_SHORT).show();

                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.friend_chat_icon)
                .show();
    }

    /***
     * This method show status of user whether he's online or offline
     * @param status
     */
    public static void updateUserStatus(String status)
    {
        String saveCurrentTime , saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String , Object> onlineUserStatus = new HashMap<>();
        onlineUserStatus.put(FirebaseConstants.STATE, status);
        onlineUserStatus.put(FirebaseConstants.DATE, saveCurrentDate);
        onlineUserStatus.put(FirebaseConstants.TIME, saveCurrentTime);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(FirebaseConstants.USER).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(FirebaseConstants.USER_STATUS)
                .updateChildren(onlineUserStatus);
    }




}