package com.example.demo3;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;
public class Fragment2 extends Fragment implements AdapterView.OnItemSelectedListener{
    private EditText mEditTextInput2;
    private EditText mEditTextInput;
    private TextView mTextViewCountDown;
    private Button mButtonSet;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private Button mButtonMark;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private static int count = 0;
    private TextView next_state;
    long millisInput = 0;
    long millisInput2 = 0;
    ListView lv_alarms;
    DataBaseHelper dataBaseHelper;
    ArrayAdapter alarmArrayAdapter;
    TextView selected;
    AlarmModel clickedAlarm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment2_layout, container, false);
        super.onCreate(savedInstanceState);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.days_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        mEditTextInput2 = view.findViewById(R.id.edit_text_input2);
        mEditTextInput = view.findViewById(R.id.edit_text_input);
        mTextViewCountDown = view.findViewById(R.id.text_view_countdown);
        mButtonSet = view.findViewById(R.id.button_set);
        mButtonStartPause = view.findViewById(R.id.button_start_pause);
        mButtonReset = view.findViewById(R.id.button_reset);
        next_state = view.findViewById(R.id.next_state);
        selected = view.findViewById(R.id.selected);
        mButtonMark = view.findViewById(R.id.button_mark_done);
        dataBaseHelper = new DataBaseHelper(getActivity());
        lv_alarms = (ListView) (view.findViewById(R.id.lv_alarms));
        updateListView();
        setupListViewlistener();

        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String input = mEditTextInput.getText().toString();
                String input2 = mEditTextInput2.getText().toString();
                if (input.length() == 0 || input2.length() == 0) {
                    Toast.makeText(getActivity(), "Fields can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                millisInput = Long.parseLong(input) * 60000;
                millisInput2 = Long.parseLong(input2) * 60000;
                if (millisInput == 0 || millisInput2 == 0) {
                    Toast.makeText(getActivity(), "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                mEditTextInput.setText("");
                mEditTextInput2.setText("");
                next_state.setText("Break in:");
            }
        });
        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        mButtonMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm(clickedAlarm.id);
                dataBaseHelper.deleteAlarm(clickedAlarm);
                updateListView();
                selected.setText("None");
                Toast.makeText(getActivity(), "Removed from list", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    public void setupListViewlistener() {
        lv_alarms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                clickedAlarm = (AlarmModel) parent.getItemAtPosition(position);
                selected.setText(clickedAlarm.title);
//                cancelAlarm(clickedAlarm.id);
//                dataBaseHelper.deleteAlarm(clickedAlarm);
//                updateListView();
                Toast.makeText(getActivity(), "Selected", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }





    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();

    }
    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onFinish() {
                Calendar c = Calendar.getInstance();
                mTimerRunning = false;
                updateWatchInterface();
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(getActivity(), AlertReceiver.class);

                if (count%2==0){
                    setTime(millisInput2);
                    startTimer();
                    intent.putExtra("message", "Time for a break!");
                    next_state.setText("Back to work in:");
                    next_state.setTextColor(Color.parseColor("#fc031c"));
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), -1, intent, 0);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

                }
                else{
                    setTime(millisInput2);
                    startTimer();
                    intent.putExtra("message", "Time for work!");
                    next_state.setText("Break in");
                    next_state.setTextColor(Color.parseColor("#673AB7"));
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), -2, intent, 0);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

                }
                count = count+1;
            }
        }.start();
        mTimerRunning = true;
        updateWatchInterface();
    }
    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateWatchInterface();
    }
    private void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
    }
    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }
        mTextViewCountDown.setText(timeLeftFormatted);

    }
    public void updateListView(){
        alarmArrayAdapter = new ArrayAdapter<AlarmModel>(getActivity(), android.R.layout.simple_list_item_1, dataBaseHelper.display());
        lv_alarms.setAdapter(alarmArrayAdapter);
    }

    private void updateWatchInterface() {
        if (mTimerRunning) {
            mEditTextInput.setVisibility(View.INVISIBLE);
            mEditTextInput2.setVisibility((View.INVISIBLE));
            mButtonSet.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
            next_state.setVisibility(View.VISIBLE);
        } else {
            mEditTextInput2.setVisibility((View.VISIBLE));
            mEditTextInput.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
            mButtonStartPause.setText("Start");
            next_state.setVisibility(View.INVISIBLE);

            if (mTimeLeftInMillis < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }
            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("startTimeInMillis", mStartTimeInMillis);
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);
        editor.apply();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = this.getActivity().getSharedPreferences("prefs", getActivity().MODE_PRIVATE);
        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        updateCountDownText();
        updateWatchInterface();
        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        alarmArrayAdapter = new ArrayAdapter<AlarmModel>(getActivity(), android.R.layout.simple_list_item_1, dataBaseHelper.filter(parent.getItemAtPosition(position).toString()));
        lv_alarms.setAdapter(alarmArrayAdapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}


