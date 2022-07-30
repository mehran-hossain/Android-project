package com.example.demo3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String ALARMS = "ALARMS";
    public static final String C_ID = "ID";
    public static final String C_TITLE = "TITLE";
    public static final String C_DATE = "DATE";
    public static final String C_TIME = "TIME";



    public DataBaseHelper(@Nullable Context context) {
        super(context, "alarms.db", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + ALARMS + "(" + C_ID + " INTEGER, " + C_TITLE + " TEXT, " + C_DATE + " TEXT, " + C_TIME + " TEXT)";
        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addAlarm(AlarmModel alarmModel, boolean repeating){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();


        cv.put(C_ID, alarmModel.id);
        cv.put(C_TITLE, alarmModel.title);
        if (repeating) {
            String day = "";
            int iend = alarmModel.date.indexOf(",");
            if (iend != -1)
            {
                day= alarmModel.date.substring(0 , iend);
            }
            cv.put(C_DATE, day + ", Repeating");
        }
        else
            cv.put(C_DATE, alarmModel.date);
        cv.put(C_TIME, alarmModel.time);


        long insert = db.insert(ALARMS, null, cv);
        return insert != -1;
    }



    public void deleteAlarm(AlarmModel alarmModel){
        SQLiteDatabase db = this.getWritableDatabase();
        String qs = "DELETE FROM " + ALARMS + " WHERE " + C_ID + " = " + alarmModel.id;

        Cursor cursor = db.rawQuery(qs,null);
        cursor.moveToFirst();
    }



    public List<AlarmModel> display(){
        List<AlarmModel> returnList = new ArrayList<>();
        String qs = "SELECT * FROM " + ALARMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(qs, null);
        if(cursor.moveToFirst()){
            do{
                int alarmID = cursor.getInt(0);
                String title = cursor.getString(1);
                String date = cursor.getString(2);
                String time = cursor.getString(3);

                AlarmModel newAlarm = new AlarmModel(alarmID, title, date, time);
                returnList.add(newAlarm);
            }while(cursor.moveToNext());
        }
        return returnList;
    }

    public List<AlarmModel> filter(String day){
        List<AlarmModel> returnList = new ArrayList<>();
        String qs = "SELECT * FROM " + ALARMS + " WHERE DATE LIKE '%" + day + "%'";
        if (day.equals("All"))
            qs = "SELECT * FROM " + ALARMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(qs, null);
        if(cursor.moveToFirst()){
            do{
                int alarmID = cursor.getInt(0);
                String title = cursor.getString(1);
                String date = cursor.getString(2);
                String time = cursor.getString(3);

                AlarmModel newAlarm = new AlarmModel(alarmID, title, date, time);
                returnList.add(newAlarm);
            }while(cursor.moveToNext());
        }
        return returnList;
    }

    public int uniqueId(int id){
        String qs = "SELECT * FROM " + ALARMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(qs, null);
        if(cursor.moveToFirst()){
            do{
                int alarmID = cursor.getInt(0);
                if (id==alarmID) {
                    id++;
                    cursor.moveToFirst();
                }
            }while(cursor.moveToNext());
        }
        return id;
    }




}

