package com.example.friendschats.Models;

public class UserStateClass {
    private String time;
    private String date;
    private String state;


    //An emptyConstructor for firebase database
    public UserStateClass(){}


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
