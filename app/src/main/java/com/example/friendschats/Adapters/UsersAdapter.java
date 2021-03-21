package com.example.friendschats.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendschats.Activities.ChatActivity;
import com.example.friendschats.Activities.ViewImageActivity;
import com.example.friendschats.Constant.Constants;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Models.User;
import com.example.friendschats.R;
import com.example.friendschats.databinding.UserElementLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserAdapterViewHolder> {

    List<User> userList ;
    Context context;

    public UsersAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NotNull
    @Override
    public UserAdapterViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_element_layout,parent,false);
        return new UserAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapterViewHolder holder, int position) {
        User user = userList.get(position);

        //Here we need user id to create senderRoom child in our database ,So it will uniquely identify chats

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child(FirebaseConstants.CHATS)
                .child(FirebaseAuth.getInstance().getUid()+user.getUid())
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()){
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                String lastMessage = snapshot1.child(FirebaseConstants.MESSAGE).getValue(String.class);
                                   long time = snapshot1.child(FirebaseConstants.TIMESTAMP).getValue(Long.class);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                    holder.binding.tvMessageTime.setText(dateFormat.format(new Date(time)));


                                    holder.binding.tvLastMessage.setText(lastMessage);



                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });



        holder.binding.tvUserName.setText(user.getName());
        Glide.with(context).load(user.getProfilePic())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.ivProfile);

        holder.binding.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewImageActivity.class);
                intent.putExtra(Constants.PROFILE_PICTURE_URL, user.getProfilePic());
                intent.putExtra(Constants.ACTIVITY_NAME,user.getName());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Sending all values to chats activity to set toolbar
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Constants.USER_NAME,user.getName());
                intent.putExtra(Constants.UID,user.getUid());
                intent.putExtra(Constants.PROFILE_IMAGE,user.getProfilePic());
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() { return userList.size(); }

     class UserAdapterViewHolder extends  RecyclerView.ViewHolder {
        UserElementLayoutBinding binding;
        public UserAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = UserElementLayoutBinding.bind(itemView);
        }
    }
}
