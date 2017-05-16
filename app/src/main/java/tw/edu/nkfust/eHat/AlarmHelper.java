package tw.edu.nkfust.eHat;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


public class AlarmHelper extends Activity {
    private Context context;
    private AlarmManager mAlarmManager;

    private int hour, minute, second, sum;
    private String note;
    private int sumOfTimerOne, sumOfTimerTwo, sumOfTimerThree;
    private String noteOfTimerOne, noteOfTimerTwo, noteOfTimerThree;

    public AlarmHelper(Context context) {
        this.context = context;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }// End of structure

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setAlarm(final View view, final TextView name, final Button status) {
        final EditText editTextOfName = (EditText) view.findViewById(R.id.editTextOfName);
        final EditText editTextOfHour = (EditText) view.findViewById(R.id.editTextOfHour);
        final EditText editTextOfMinute = (EditText) view.findViewById(R.id.editTextOfMinute);
        final EditText editTextOfSecond = (EditText) view.findViewById(R.id.editTextOfSecond);
        final EditText editTextOfNote = (EditText) view.findViewById(R.id.editTextOfNote);
        //建立文字監聽
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //如果字數達到4，取消自己焦點，下一個EditText取得焦點
                if (editTextOfHour.getText().toString().length() == 2) {
                    editTextOfHour.clearFocus();
                    editTextOfMinute.requestFocus();
                }

                if (editTextOfMinute.getText().toString().length() == 2) {
                    editTextOfMinute.clearFocus();
                    editTextOfSecond.requestFocus();
                }

                if (editTextOfSecond.getText().toString().length() == 2) {
                    editTextOfSecond.clearFocus();
                }
            }
        };
        editTextOfHour.addTextChangedListener(mTextWatcher);
        editTextOfMinute.addTextChangedListener(mTextWatcher);
        editTextOfSecond.addTextChangedListener(mTextWatcher);

//        editTextOfHour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (editTextOfHour.hasFocus()) {
//                    Log.d("editText", "OfHour取得焦點");
//                    BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
//                }
//            }
//        });
//        editTextOfMinute.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (editTextOfMinute.hasFocus()) Log.d("editText", "OfMinute取得焦點");
//            }
//        });
//        editTextOfSecond.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (editTextOfSecond.hasFocus()) Log.d("editText", "OfSecond取得焦點");
//            }
//        });

        editTextOfName.setText(name.getText());
        editTextOfNote.setText(R.string.message_TimeIsUpOfTimer);
        note = editTextOfNote.getText().toString();

        new AlertDialog.Builder(context)
                .setTitle(R.string.title_EditOfTimerDialog)
                .setView(view)
                .setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        name.setText(editTextOfName.getText());

                        if (editTextOfHour.getText().toString().equals("")) {
                            hour = 0;
                        } else {
                            hour = Integer.valueOf(editTextOfHour.getText().toString());
                        }// End of if-condition

                        if (editTextOfMinute.getText().toString().equals("")) {
                            minute = 0;
                        } else {
                            minute = Integer.valueOf(editTextOfMinute.getText().toString());
                        }// End of if-condition

                        if (editTextOfSecond.getText().toString().equals("")) {
                            second = 0;
                        } else {
                            second = Integer.valueOf(editTextOfSecond.getText().toString());
                        }// End of if-condition

                        if (!editTextOfNote.getText().toString().equals("")) {
                            note = editTextOfNote.getText().toString();
                        }// End of if-condition

                        while (second >= 60) {
                            second = second - 60;
                            minute = minute + 1;

                            while (minute >= 60) {
                                minute = minute - 60;
                                hour = hour + 1;
                            }// End of while-loop
                        }// End of while-loop

                        status.setText(String.format("%02d : %02d : %02d", hour, minute, second));
                        sum = hour * 3600 * 1000 + minute * 60 * 1000 + second * 1000;

                        switch (status.getId()) {
                            case R.id.buttonOfTheTimerOneStatus:
                                noteOfTimerOne = note;
                                sumOfTimerOne = sum;
                                break;
                            case R.id.buttonOfTheTimerTwoStatus:
                                noteOfTimerTwo = note;
                                sumOfTimerTwo = sum;
                                break;
                            case R.id.buttonOfTheTimerThreeStatus:
                                noteOfTimerThree = note;
                                sumOfTimerThree = sum;
                                break;
                        }// End of switch-condition

                        if (sum != 0) status.setEnabled(true);
                    }// End of onClick
                })
                .setNegativeButton(R.string.textOfButton_DialogNo, null)
                .show();
    }// End of setAlarm

    public CountDownTimer getTimer(final Button status) {
        switch (status.getId()) {
            case R.id.buttonOfTheTimerOneStatus:
                note = noteOfTimerOne;
                sum = sumOfTimerOne;
                MainActivity.isTimerOneCounting = true;
                break;
            case R.id.buttonOfTheTimerTwoStatus:
                note = noteOfTimerTwo;
                sum = sumOfTimerTwo;
                MainActivity.isTimerTwoCounting = true;
                break;
            case R.id.buttonOfTheTimerThreeStatus:
                note = noteOfTimerThree;
                sum = sumOfTimerThree;
                MainActivity.isTimerThreeCounting = true;
                break;
        }// End of switch-condition

        int interval = 1000;// 間隔時間
        CountDownTimer timer;

        timer = new CountDownTimer(sum, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                int hour = (int) (millisUntilFinished / 3600 / 1000);
                int minute = (int) (millisUntilFinished / 60 / 1000) - hour * 60;
                int second = (int) (millisUntilFinished / 1000) - minute * 60 - hour * 3600;
                status.setText(String.format("%02d : %02d : %02d", hour, minute, second));
            }// End of onTick

            @Override
            public void onFinish() {
                status.setText(R.string.textOfInitTimer);
                status.setEnabled(false);

                if (!MainActivity.mRFduinoManager.isOutOfRange()) {
                    switch (status.getId()) {
                        case R.id.buttonOfTheTimerOneStatus:
                            MainActivity.mRFduinoManager.onStateChanged(2);
                            break;
                        case R.id.buttonOfTheTimerTwoStatus:
                            MainActivity.mRFduinoManager.onStateChanged(4);
                            break;
                        case R.id.buttonOfTheTimerThreeStatus:
                            MainActivity.mRFduinoManager.onStateChanged(6);
                            break;
                    }// End of switch-condition
                }// End of if-condition

                new AlertDialog.Builder(context)
                        .setMessage(note)
                        .setPositiveButton(R.string.textOfButton_DialogYes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (status.getId()) {
                                    case R.id.buttonOfTheTimerOneStatus:
                                        if (!MainActivity.mRFduinoManager.isOutOfRange())
                                            MainActivity.mRFduinoManager.onStateChanged(3);
                                        MainActivity.isTimerOneCounting = false;
                                        break;
                                    case R.id.buttonOfTheTimerTwoStatus:
                                        if (!MainActivity.mRFduinoManager.isOutOfRange())
                                            MainActivity.mRFduinoManager.onStateChanged(5);
                                        MainActivity.isTimerTwoCounting = false;
                                        break;
                                    case R.id.buttonOfTheTimerThreeStatus:
                                        if (!MainActivity.mRFduinoManager.isOutOfRange())
                                            MainActivity.mRFduinoManager.onStateChanged(7);
                                        MainActivity.isTimerThreeCounting = false;
                                        break;
                                }// End of switch-condition
                            }// End of onClick
                        })
                        .show();
            }// End of onFinish
        };

        return timer;
    }// End of getTimer

    public void setTimePicker(int hourOfDay, int minute) {
        MainActivity.calendar.setTimeInMillis(System.currentTimeMillis());
        MainActivity.calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        MainActivity.calendar.set(Calendar.MINUTE, minute);
        MainActivity.calendar.set(Calendar.SECOND, 0);
        MainActivity.calendar.set(Calendar.MILLISECOND, 0);
    }// End of setTimePicker

    public void startTimePicker() {
        Intent intent = new Intent(context, TimePickerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, MainActivity.calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(context, R.string.toast_TimePickerStart, Toast.LENGTH_SHORT).show();
    }// End of startTimePicker

    public void stopTimePicker() {
        Intent intent = new Intent(context, TimePickerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        mAlarmManager.cancel(pendingIntent);
        Toast.makeText(context, R.string.toast_TimePickerStop, Toast.LENGTH_SHORT).show();
    }// End of stopTimePicker
}// End of AlarmHelper
