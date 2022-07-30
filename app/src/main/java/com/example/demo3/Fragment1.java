package com.example.demo3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Fragment1 extends Fragment {
    private static final String TAG = "MainActivity";

    EditText et_title;
    TextView dateTextView, timeTextView;
    Button addbtn, date_btn, time_btn;
    ListView lv_alarms;
    ArrayAdapter alarmArrayAdapter;
    DataBaseHelper dataBaseHelper;
    Switch repeatSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1_layout, container, false);

        et_title = view.findViewById(R.id.et_title);
        dateTextView = view.findViewById(R.id.dateTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        date_btn = view.findViewById(R.id.date);
        time_btn = view.findViewById(R.id.time);
        repeatSwitch = view.findViewById(R.id.switch1);
        addbtn = view.findViewById(R.id.button);
        lv_alarms = (ListView) (view.findViewById(R.id.lv_alarms));
        dataBaseHelper = new DataBaseHelper(getActivity());


        updateListView();
        setupListViewlistener();
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra("reboot", false)) {
            List<AlarmModel> alarmList = dataBaseHelper.display();
            for (int i = 0; i < alarmList.size(); i++) {
                AlarmModel alarm = alarmList.get(i);
                Calendar cal = Calendar.getInstance();
                String dt = alarm.date + " " + alarm.time;
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy h:mm a", Locale.ENGLISH);
                try {
                    cal.setTime(sdf.parse(dt));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startAlarm(cal, alarm.id, alarm.title);
                Toast.makeText(getActivity(), "added", Toast.LENGTH_SHORT).show();
            }
        }

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmModel alarmModel = null;
                dataBaseHelper = new DataBaseHelper(getActivity());

                if (et_title.getText().toString().equals("") || dateTextView.getText().toString().equals("") || timeTextView.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Enter value", Toast.LENGTH_SHORT).show();
                } else {
                    alarmModel = new AlarmModel(dataBaseHelper.uniqueId(lv_alarms.getAdapter().getCount()), et_title.getText().toString(), dateTextView.getText().toString(), timeTextView.getText().toString());
                    Calendar cal = Calendar.getInstance();
                    String dt = dateTextView.getText().toString() + " " + timeTextView.getText().toString();

                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy h:mm a", Locale.ENGLISH);

                    try {
                        cal.setTime(sdf.parse(dt));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (startAlarm(cal, alarmModel.id, alarmModel.title)) {
                        dataBaseHelper.addAlarm(alarmModel, repeatSwitch.isChecked());
                    }

                }

                updateListView();
            }
        });


        date_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDateButton();
            }
        });
        time_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTimeButton();
            }
        });


        return view;
    }


    public void updateListView() {
        if (dataBaseHelper!=null) {
            alarmArrayAdapter = new ArrayAdapter<AlarmModel>(getActivity(), android.R.layout.simple_list_item_1, dataBaseHelper.display());
            lv_alarms.setAdapter(alarmArrayAdapter);
        }
    }

    public void setupListViewlistener() {
        lv_alarms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlarmModel clickedAlarm = (AlarmModel) parent.getItemAtPosition(position);
                cancelAlarm(clickedAlarm.id);
                dataBaseHelper.deleteAlarm(clickedAlarm);
                updateListView();
                Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();

                return false;
            }
        });
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            updateListView();
//        }
//    }


    private void handleDateButton() {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.YEAR, year);
                calendar1.set(Calendar.MONTH, month);
                calendar1.set(Calendar.DATE, date);
                String dateText = DateFormat.format("EEEE, MMM d, yyyy", calendar1).toString();

                dateTextView.setText(dateText);
            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();
    }

    private void handleTimeButton() {
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);
        boolean is24HourFormat = DateFormat.is24HourFormat(getActivity());

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Log.i(TAG, "onTimeSet: " + hour + minute);
                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.HOUR_OF_DAY, hour);
                calendar1.set(Calendar.MINUTE, minute);
                String dateText = DateFormat.format("h:mm a", calendar1).toString();
                timeTextView.setText(dateText);
            }
        }, HOUR, MINUTE, is24HourFormat);

        timePickerDialog.show();

    }

    public boolean startAlarm(Calendar c, int id, String title) {
        if (c.before(Calendar.getInstance())) {
            Toast.makeText(getActivity(), "Cannot add reminder in past", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(getActivity(), AlertReceiver.class);
            intent.putExtra("message", title);
            intent.putExtra("id", id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), id, intent, 0);
            if (repeatSwitch.isChecked())
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            Toast.makeText(getActivity(), "added", Toast.LENGTH_SHORT).show();
            return  true;
        }
    }

    public void cancelAlarm(int id) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), id, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void setMenuVisibility(boolean isvisible) {
        super.setMenuVisibility(isvisible);
        if (isvisible){
            Log.d("Viewpager", "fragment is visible ");
            updateListView();
        }else {
            Log.d("Viewpager", "fragment is not visible ");
        }
    }
}






