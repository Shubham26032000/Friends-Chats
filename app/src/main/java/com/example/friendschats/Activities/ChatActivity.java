package com.example.friendschats.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DirectAction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendschats.Adapters.MessageAdapter;
import com.example.friendschats.Constant.Constants;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Models.MessageModel;
import com.example.friendschats.Models.UserStateClass;
import com.example.friendschats.R;
import com.example.friendschats.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Consumer;

public class ChatActivity extends AppCompatActivity {

    MessageAdapter messageAdapter;
    List<MessageModel> messageList;

    ActivityChatBinding binding;
    FirebaseDatabase firebaseDatabase;
    final  int MEDIA_SELECT_CODE = 212;
     String senderRoom;
     String receiverRoom;

     ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait Sending image");
        progressDialog.setCancelable(false);


        messageList = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //Getting user name and profile image
        String userName = getIntent().getStringExtra(Constants.USER_NAME);
        String profileImage = getIntent().getStringExtra(Constants.PROFILE_IMAGE);


        //Fetching sender and receiver uid.

        String receiverUid = getIntent().getStringExtra(Constants.UID);
        String profilePicOfReceiver = getIntent().getStringExtra(Constants.PROFILE_IMAGE);
        String senderUid = FirebaseAuth.getInstance().getUid();

        //Making sender and receiver room for uniqueness between their chats
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        //Setting message adapter and recyclerview.
        messageAdapter = new MessageAdapter(this,messageList,senderRoom,receiverRoom,profilePicOfReceiver);
         binding.chatsRecyclerView.setHasFixedSize(true);
        binding.chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatsRecyclerView.setAdapter(messageAdapter);

        //Setting toolbar
        binding.tvUserName.setText(userName);

        //Gets user current activity status i.e. Online or Offline
        updateUserStatus("Online");
        getUserStatus();
        //Setting profile of receiver
        Glide.with(this).load(profileImage)
                .placeholder(R.drawable.avatar)
                .into(binding.ivProfileImage);


        //Opening Image
        binding.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,ViewImageActivity.class);
                intent.putExtra(Constants.PROFILE_PICTURE_URL, profileImage);
                intent.putExtra(Constants.ACTIVITY_NAME, userName);
                startActivity(intent);
            }
        });

        binding.ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });


        /**
         * Adding message to message list
         */
        addMessageToMessageList();


        /***
         * Selecting medida file to send
         */
        binding.btnAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, MEDIA_SELECT_CODE);;
            }
        });



        binding.ivSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sending message to user.
                uploadMessageInFirebaseDatabase(senderUid);
            }
        });

    }


    //Getting Receivers current status.
    private void getUserStatus()
    {

        //Update sender status
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(FirebaseConstants.USER)
                .child(getIntent().getStringExtra(Constants.UID))
                .child(FirebaseConstants.USER_STATUS);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    UserStateClass state = snapshot.getValue(UserStateClass.class);

                    String status = state.getState();
                    String time = state.getTime();
                    String date = state.getDate();

                    if(status.equals("Online"))
                    {
                        binding.tvStatus.setText(status);
                    }else
                    {
                        binding.tvStatus.setText("Last seeen "+date+" "+time);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    /***
     * This following lifecycle activities
     * are for sender/current user status
      */

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("Online");
        getUserStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();
       updateUserStatus("Online");
        getUserStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("Offline");
        getUserStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("Offline");
        getUserStatus();
    }

    //get selected media file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEDIA_SELECT_CODE && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();

            //This method upload selected media to database
            uploadMediaFileToFirebase(imageUri);
        }
    }



    /**
     * Adding message to message list.
     */
    public void addMessageToMessageList()
    {
        firebaseDatabase.getReference().child(FirebaseConstants.CHATS)
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot snapshot1: snapshot.getChildren())
                        {
                            MessageModel message = snapshot1.getValue(MessageModel.class);
                            message.setMessageId(snapshot1.getKey());
                            messageList.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        binding.chatsRecyclerView.smoothScrollToPosition(binding.chatsRecyclerView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }



    /***
     * add send messages to firebaseDatabase
     * @param senderUid
     */
    private void uploadMessageInFirebaseDatabase(String senderUid)
      {
        if(binding.etMessageBox.getText().toString().trim().equals(""))
        {
            return;
        }

        String messageText = binding.etMessageBox.getText().toString().trim();

        Date date = new Date();
        final MessageModel message = new MessageModel("text",messageText,senderUid);
        message.setTimeStamp(date.getTime());
        binding.etMessageBox.setText("");


        String messageKey = firebaseDatabase.getReference()
                .child(FirebaseConstants.CHATS)
                .child(senderRoom)
                .push().getKey();
        firebaseDatabase.getReference()
                .child(FirebaseConstants.CHATS)
                .child(senderRoom)
                .child(messageKey)
                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                firebaseDatabase.getReference()
                        .child(FirebaseConstants.CHATS)
                        .child(receiverRoom)
                        .child(messageKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
            }
        });


    }



    /***
     * This takes Uri of media and then store in firebaseStorage
     * @param mediaFile
     */
    public void uploadMediaFileToFirebase(Uri mediaFile)
    {
        progressDialog.show();
        Date date = new Date();
        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child(FirebaseConstants.MEDIA).child(date.getTime()+"");

        reference.putFile(mediaFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String mediaUrl = uri.toString();

                            MessageModel message = new MessageModel("image",mediaUrl,FirebaseAuth.getInstance().getUid());
                            message.setTimeStamp(date.getTime());

                            String messageKey = firebaseDatabase.getReference()
                                    .child(FirebaseConstants.CHATS)
                                    .child(senderRoom)
                                    .push().getKey();

                            firebaseDatabase.getReference()
                                    .child(FirebaseConstants.CHATS)
                                    .child(senderRoom)
                                    .child(messageKey)
                                    .setValue(message)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            firebaseDatabase.getReference()
                                                    .child(FirebaseConstants.CHATS)
                                                    .child(receiverRoom)
                                                    .child(messageKey)
                                                    .setValue(message)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            progressDialog.dismiss();
                                                        }
                                                    });
                                        }
                                    });


                        }
                    });
                }
            }
        });
    }



    /***
     * This method show status of user whether he's online or offline
     * @param status
     */
    public  void updateUserStatus(String status)
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