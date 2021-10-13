package com.example.friendschats.Models;

public class MessageModel {
    private String messageId;
    private String message;
    private String senderId;
    private long timeStamp;
    private String type ;

    public MessageModel() {
    }

    public MessageModel(String type,String message, String senderId)
    {
        this.type = type;
        this.senderId = senderId;
        this.message = message;
    }




    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

}
