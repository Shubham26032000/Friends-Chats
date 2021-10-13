package com.example.friendschats.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.friendschats.Adapters.UsersAdapter;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Models.User;
import com.example.friendschats.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class ChatsFragment extends Fragment {
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;

    UsersAdapter usersAdapter;
    ArrayList<User> userList;

    //This is for getting all chatsRoom
    ArrayList<String> chatRoomList;
    User user;

    FragmentChatsBinding binding;
    public ChatsFragment() { 
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //Setting adapter
        userList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.userWithChatRecyclerView.setLayoutManager(layoutManager);
        binding.userWithChatRecyclerView.setHasFixedSize(true);
        usersAdapter = new UsersAdapter(userList, getContext());
        binding.userWithChatRecyclerView.setAdapter(usersAdapter);

        //Initialising chatRoomList
        chatRoomList = new ArrayList<>();
        checkAlreadyTalk();
        getCurrentUser();



        return binding.getRoot();
    }




    /***
     * Get current user
     */
    public void getCurrentUser()
    {
        firebaseDatabase.getReference()
                .child(FirebaseConstants.USER)
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    /***
     * This method will add those user who already talk or sent message
     */
    private void addAlreadyTalkUser() {

        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseDatabase.getReference()
                .child(FirebaseConstants.USER)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            User user = dataSnapshot.getValue(User.class);

                            if (!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                            {
                                //Ti check whether they talk or not using their chat node
                                String senderRoom = user.getUid() + FirebaseAuth.getInstance().getUid();
                                String receiverRoom = FirebaseAuth.getInstance().getUid() + user.getUid();

                                if (chatRoomList.contains(senderRoom) || chatRoomList.contains(receiverRoom))
                                {
                                    userList.add(user);
                                }
                            }
                        }
                        binding.progressBar.setVisibility(View.GONE);
                        usersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }


    /***
     * This method will add chatsRoom in chatRoomList
     * And we call  # addAlreadyTalkUser() within this activity
     * as both method do their task on different thread so if we don't do then we will not have
     * desired result
     */
    private void checkAlreadyTalk()
    {
        //Add chats room in chatRoomList;
        firebaseDatabase.getReference()
                .child(FirebaseConstants.CHATS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        chatRoomList.clear();

                        for (DataSnapshot chatRoomSnapshot: snapshot.getChildren())
                        {
                            chatRoomList.add(chatRoomSnapshot.getKey());
                        }
                        //Checking if user already talk
                        addAlreadyTalkUser();

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }


}