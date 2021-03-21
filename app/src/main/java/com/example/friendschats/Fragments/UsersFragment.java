package com.example.friendschats.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.friendschats.Activities.MainActivity;
import com.example.friendschats.Adapters.UsersAdapter;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Models.User;
import com.example.friendschats.R;
import com.example.friendschats.databinding.FragmentUsersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class UsersFragment extends Fragment {

    FragmentUsersBinding binding;

    ProgressDialog statusProgressDialog;


    UsersAdapter usersAdapter;
    List<User> userList;


    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;

    //This is for getting current log in user
    User user;

    public UsersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        //Setting progressbar
        statusProgressDialog = new ProgressDialog(getActivity());
        statusProgressDialog.setMessage("Uploading Image");
        statusProgressDialog.setCancelable(false);
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        //Get current log in user details
        getCurrentUser();

        //Setting UserList adapter for our userListRecyclerView
        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userList, getContext());
        binding.chatsRecyclerView.setHasFixedSize(true);
        binding.chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.chatsRecyclerView.setAdapter(usersAdapter);

        //Add user to list.
        addUserIntoList();

        return binding.getRoot();
    }



    /**
     * This method set value to current log in user to User object.
     */
    private void getCurrentUser() {


        firebaseDatabase.getReference()
                .child(FirebaseConstants.USER)
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( @NotNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled( @NotNull DatabaseError error) {

                    }
                });
    }

    /***
     * This method fetch present user in database and add them in our userList.
     */
    public void addUserIntoList()
    {
        binding.progressBar.setVisibility(View.VISIBLE);
        firebaseDatabase.getReference()
                .child(FirebaseConstants.USER)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        userList.clear(); // This is because if don't do this then it will add duplicate user in list
                        for (DataSnapshot userSnapShot : snapshot.getChildren())
                        {
                            User mUser = userSnapShot.getValue(User.class);
                            if(userSnapShot.hasChild(FirebaseConstants.USER_STATUS)){
                                String time = userSnapShot.child(FirebaseConstants.USER_STATUS).child(FirebaseConstants.TIME).getValue(String.class);
                                String date = userSnapShot.child(FirebaseConstants.USER_STATUS).child(FirebaseConstants.DATE).getValue(String.class);
                                String status = userSnapShot.child(FirebaseConstants.USER_STATUS).child(FirebaseConstants.STATE).getValue(String.class);
                                mUser.setTime(time);
                                mUser.setDate(date);
                                mUser.setStatus(status);
                            }
                            mUser.setUid(userSnapShot.getKey());
                            if (!mUser.getUid().equals(user.getUid()))
                                userList.add(mUser);
                        }
                        binding.progressBar.setVisibility(View.GONE);

                        usersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "No user", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}