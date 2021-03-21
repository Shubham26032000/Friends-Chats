package com.example.friendschats.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendschats.Activities.ViewImageActivity;
import com.example.friendschats.Constant.Constants;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Models.MessageModel;
import com.example.friendschats.R;
import com.example.friendschats.databinding.DeleteDialogLayoutBinding;
import com.example.friendschats.databinding.ReceiveMessageBinding;
import com.example.friendschats.databinding.SendMessageBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVED =2;

    public Context context;
    private List<MessageModel> messagesList;

    //This will let know us which messages are send by sender and unique receiver for receiving that message
    String senderRoom;
    String receiverRoom;
    String receiverProfile;


    public MessageAdapter(Context context, List<MessageModel> messagesList, String senderRoom, String receiverRoom, String receiverProfile) {
        this.context = context;
        this.messagesList = messagesList;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
        this.receiverProfile = receiverProfile;
    }

    /***
     * This method will tell us the current message are of which type
     * i.e. sender type or receiver type
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        MessageModel message = messagesList.get(position);

        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId()))
        {
            return ITEM_SENT;
        }else {
            return ITEM_RECEIVED;
        }

    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.send_message, parent, false);
            return new SentViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.receive_message, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messagesList.get(position);


        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder) holder;

            //To check whether the message is text or image
                if(message.getType().equals("text") || message.getMessage().equals("This message is removed") )
                {
                    viewHolder.bindingSender.tvMessage.setText(message.getMessage());
                }else{
                    //hiding message and showing media
                    viewHolder.bindingSender.cardView.setVisibility(View.VISIBLE);
                    viewHolder.bindingSender.tvMessage.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(message.getMessage())
                            .placeholder(R.drawable.avatar)
                            .into(viewHolder.bindingSender.ivMedia);

                    //View media file
                    viewHolder.bindingSender.ivMedia.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ViewImageActivity.class);
                            intent.putExtra(Constants.PROFILE_PICTURE_URL, message.getMessage());
                            intent.putExtra(Constants.USER_NAME, "Image media");
                            context.startActivity(intent);
                        }
                    });
                }

                long time = message.getTimeStamp();
                 SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                 viewHolder.bindingSender.tvMessageTime.setText(dateFormat.format(new Date(time)));

            //Deleting message dialog
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteMessageAlert(message, ITEM_SENT);
                    return false;
                }
            });
        }else{


            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            //Set receiver profile photo along with it's message
                if (receiverProfile.equals("No Image"))
                {
                    viewHolder.bindingReceiver.ivReceiver.setImageResource(R.drawable.avatar);
                }else{
                    Glide.with(context)
                            .load(receiverProfile)
                            .placeholder(R.drawable.avatar)
                            .into(viewHolder.bindingReceiver.ivReceiver);
                }
            long time = message.getTimeStamp();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            viewHolder.bindingReceiver.tvMessageTime.setText(dateFormat.format(new Date(time)));

            //To check whether the message is text or image
                if(message.getType().equals("text") || message.getMessage().equals("This message is removed"))
                {
                    viewHolder.bindingReceiver.tvMessage.setText(message.getMessage());
                }else {
                    viewHolder.bindingReceiver.cardView.setVisibility(View.VISIBLE);
                    viewHolder.bindingReceiver.tvMessage.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(message.getMessage())
                            .placeholder(R.drawable.avatar)
                            .into(viewHolder.bindingReceiver.ivMedia);

                    //View media file
                    viewHolder.bindingReceiver.ivMedia.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ViewImageActivity.class);
                            intent.putExtra(Constants.PROFILE_PICTURE_URL, message.getMessage());
                            intent.putExtra(Constants.USER_NAME, "Image media");
                            context.startActivity(intent);
                        }
                    });
                }





            //Delete Message dialog
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                     deleteMessageAlert(message, ITEM_RECEIVED);

                    return false;
                }
            });

        }




    }



    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    /***
     * As we are taking messages and show them in different layout model so ,
     * we need separate layouts
     * One is for sender and second is for receiver message
     */

    public class SentViewHolder extends RecyclerView.ViewHolder {
        SendMessageBinding bindingSender;
        public SentViewHolder(@NonNull  View itemView) {
            super(itemView);
            bindingSender = SendMessageBinding.bind(itemView);
        }
    }


    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ReceiveMessageBinding bindingReceiver;
        public ReceiverViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            bindingReceiver = ReceiveMessageBinding.bind(itemView);
        }
    }


    /***
     * Delete message dialog
     * @param message  this is message object.
     * @param className this is for sake of identifying sender and
     *                  receiver to update ui accordingly
     */
    private void deleteMessageAlert(MessageModel message, int className)
    {

        //Deleting message alertdialog
        View deleteView = LayoutInflater.from(context).inflate(R.layout.delete_dialog_layout,null);
        DeleteDialogLayoutBinding bindingDelete = DeleteDialogLayoutBinding.bind(deleteView);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Delete message ?")
                .setView(bindingDelete.getRoot())
                .create();

        if (className == ITEM_SENT)
        {
            bindingDelete.tvDeleteEveryone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    message.setMessage("This message is removed");

                    //If image then delete or else set message text as above.
                    if (message.getType().equals("image"))
                        FirebaseDatabase.getInstance().getReference()
                                .child(FirebaseConstants.CHATS)
                                .child(senderRoom)
                                .child(message.getMessageId()).removeValue();
                    else
                        FirebaseDatabase.getInstance().getReference()
                                .child(FirebaseConstants.CHATS)
                                .child(senderRoom)
                                .child(message.getMessageId()).setValue(message);


                    /**
                     * This is for avoiding setting message again to already deleted message by user.
                     */
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child(FirebaseConstants.CHATS)
                            .child(receiverRoom);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(message.getMessageId()))
                            {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(FirebaseConstants.CHATS)
                                            .child(receiverRoom)
                                            .child(message.getMessageId()).setValue(message);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

                    alertDialog.dismiss();
                }
            });
        }else {

            //this is for avoiding receiver to delete message at sender too.
            bindingDelete.tvDeleteEveryone.setVisibility(View.GONE);
        }


        bindingDelete.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseDatabase.getInstance().getReference()
                        .child(FirebaseConstants.CHATS)
                        .child(senderRoom)
                        .child(message.getMessageId()).removeValue();

                alertDialog.dismiss();

            }
        });



        bindingDelete.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }








}
