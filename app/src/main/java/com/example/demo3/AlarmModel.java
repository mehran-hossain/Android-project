package com.example.demo3;

public class AlarmModel {
    int id;
    String title;
    String date;
    String time;

    public AlarmModel(int id, String title, String date, String time){
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;

    }

    public String toString(){
        return title + "\n" + date + "\n" + time;
    }

}