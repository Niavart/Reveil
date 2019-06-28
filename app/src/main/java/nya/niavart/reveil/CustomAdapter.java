package nya.niavart.reveil;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class CustomAdapter extends ArrayAdapter<ClockObject> implements View.OnClickListener {

    private ArrayList<ClockObject> dataSet;
    Context mContext;

    AlarmManager alarm_manager;
    PendingIntent pending_intent;
    LayoutInflater mInflator;
    Activity mActivity;

    // View lookup cache
    private static class ViewHolder {
        TextView time;
        Button settings_button, delete_row;
        ToggleButton toggle;
        // initialize alarm manager
    }

    public CustomAdapter(ArrayList<ClockObject> data, Context context, LayoutInflater mInflator, Activity mActivity) {
        super(context, R.layout.row_item, data);
        this.mContext = context;
        this.dataSet = data;
        this.mInflator = mInflator;
        this.mActivity = mActivity;

    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        ClockObject clockObject = (ClockObject) object;
        Log.d("REVEIL", "Clicked ??");
    }

    private int lastPosition = -1;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View getView(final int position, View convertView, ViewGroup parent) {

        alarm_manager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);

        // Get the data item for this position
        final ClockObject clockObject = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.time = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.delete_row = (Button) convertView.findViewById(R.id.delete);
            viewHolder.settings_button = (Button) convertView.findViewById(R.id.chooseRing);
            viewHolder.toggle = (ToggleButton) convertView.findViewById(R.id.togglebutton);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        // create an instance of an calendar
        final Calendar calendar = Calendar.getInstance();

        //create an intent to Alarm receiver class
        final Intent my_intent = new Intent(getContext(), Alarm_Receiver.class);

        String abcd = clockObject.getTime();

        Log.d("SNOOZE", "Inc: " + clockObject.getInc());
        if (clockObject.getInc() >= 1)
            removeOneCalendar(position, my_intent);
        if (clockObject.getInc() >= 2)
            removeOneCalendar(100 + position, my_intent);
        if (clockObject.getInc() >= 3)
            removeOneCalendar(200 + position, my_intent);
        if (clockObject.getInc() >= 4)
            removeOneCalendar(300 + position, my_intent);

        int l = abcd.indexOf(':');
        int ll = abcd.lastIndexOf(':');
        int yui;

        for (yui = l + 1; yui < abcd.length(); yui++)
            if (abcd.charAt(yui) == ':')
                break;

        String opqw, button_on_or_off;

        final int hour = Integer.parseInt(abcd.substring(0, l));
        if (ll == l) {
            opqw = abcd.substring(l + 1);
            button_on_or_off = "";
        } else {
            opqw = abcd.substring(l + 1, yui);
            button_on_or_off = abcd.substring(yui + 1);
        }

        Log.d("Tag :: ", opqw);
        final int minute = Integer.parseInt(opqw.trim());

        // convert int to string
        String hour_string = String.valueOf(hour);
        String minute_string = String.valueOf(minute);

        Log.d("Tag button :: ", button_on_or_off);

        if (hour == 0)
            hour_string = "00";
        if (minute < 10)
            minute_string = "0" + String.valueOf(minute);

        final String hjk = hour_string + ':' + minute_string;

        viewHolder.time.setText(hour_string + ':' + minute_string);

        CompoundButton.OnCheckedChangeListener lol = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!buttonView.isPressed()) {
                    return;
                }
                if (isChecked) {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat df_check = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate1 = df_check.format(cal.getTime());
                    String xy[] = formattedDate1.split(" ");
                    String xy1[] = xy[1].split(":");
                    final int current_hour_2 = Integer.parseInt(xy1[0]);
                    final int current_minutes_2 = Integer.parseInt(xy1[1]);

                    Log.d("Tag :: ", current_hour_2 + " " + hour + " :: " + current_minutes_2 + " " + minute);

                    if (current_hour_2 < hour || (current_hour_2 == hour && current_minutes_2 <= minute)) {

                        Calendar c1 = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedDate = df.format(c1.getTime());
                        String y[] = formattedDate.split(" ");
                        String y1[] = y[1].split(":");
                        int current_hour = Integer.parseInt(y1[0]);
                        int current_minutes = Integer.parseInt(y1[1]);


                        Log.d("REVEIL", "pressed alarm on button successfully !");
                        int p = hour * 60 + minute;
                        int m = current_hour * 60 + current_minutes;

                        int diff = p - m;
                        Log.d("DIFF", "p: " + p + " m: " + m + " diff: " + diff);

                        int di = 0;
                        if (diff < 0)
                            di = 24;

                        setCalendar(hour + di, minute, clockObject);

                        setPref(clockObject, true);


                        //this.findViewById(android.R.id.content);
                        if (diff < 60)
                            Snackbar.make(buttonView, "Alarm set for " + diff + " minutes from now.", Snackbar.LENGTH_LONG)
                                    .setAction("No action", null).show();
                        else
                            Snackbar.make(buttonView, "Alarm set for " + (diff / 60) + " hours and " + (diff % 60) + " minutes from now.", Snackbar.LENGTH_LONG)
                                    .setAction("No action", null).show();

                    } else {
                        Log.d("Tag :: ", "pressed alarm on button successfully !");

                        setCalendar(hour + 24, minute, clockObject);

                        Calendar c1 = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedDate = df.format(c1.getTime());
                        String y[] = formattedDate.split(" ");
                        String y1[] = y[1].split(":");
                        int current_hour = Integer.parseInt(y1[0]);
                        int current_minutes = Integer.parseInt(y1[1]);

                        setPref(clockObject, true);

                        int p = hour * 60 + minute;
                        int m = current_hour * 60 + current_minutes;

                        Log.d("TAG :: ", "p: " + p + " m: " + m);
                        int diff = p - m + (24 * 60);
                        Log.d("DIFF", "p: " + p + " m: " + m + " diff: " + diff);

                        if (diff < 60)
                            Snackbar.make(buttonView, "Alarm set for " + diff + " minutes from now.", Snackbar.LENGTH_LONG)
                                    .setAction("No action", null).show();
                        else
                            Snackbar.make(buttonView, "Alarm set for " + (diff / 60) + " hours and " + (diff % 60) + " minutes from now.", Snackbar.LENGTH_LONG)
                                    .setAction("No action", null).show();
                    }
                    // The toggle is enabled
                } else {
                    // put in extra string in my intent to tell clock you pressed off button
                    // to have current time
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat df_check = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate1 = df_check.format(cal.getTime());
                    Log.i("REVEIL", cal.getTime().toString());
                    Log.d("REVEIL", "Off Button Pressed");
                    String xy[] = formattedDate1.split(" ");
                    String xy1[] = xy[1].split(":");
                    final int current_hour_2 = Integer.parseInt(xy1[0]);
                    final int current_minutes_2 = Integer.parseInt(xy1[1]);

                    Log.d("REVEIL", current_hour_2 + " " + hour + " :: " + current_minutes_2 + " " + minute);

                    if (current_hour_2 == hour && (current_minutes_2 == minute || current_minutes_2 == ((minute + 1) % 60))) {

                    }

                    removeCalendar();

                    setPref(clockObject, false);

                    Intent intent101 = mActivity.getIntent();
                    mActivity.finish();
                    mActivity.startActivity(intent101);

                }
            }

            public void setCalendar(int hhour, int mminute, ClockObject data) {
                // put in extra string in my intent to tell clock you pressed on button
                my_intent.putExtra("extra", "alarm on");
                my_intent.putExtra("extra1", "alarm on at time :: " + hjk);
                my_intent.putExtra("position", position);
                int mmp = mminute;
                setOneCalendar((hhour + (mmp) / 60) / 24, (hhour + (mmp) / 60) % 24, (mmp) % 60, position);
                mmp += data.snooze1;
                Log.d("SNOOZE1", "dd: " + (hhour + (mmp) / 60) / 24 + " hh: " + (hhour + (mmp) / 60) % 24 + " mm: " + (mmp) % 60);
                if (data.snooze1 > 0)
                    setOneCalendar((hhour + (mmp) / 60) / 24, (hhour + (mmp) / 60) % 24, (mmp) % 60, position + 100);
                mmp += data.snooze2;
                Log.d("SNOOZE2", "dd: " + (hhour + (mmp) / 60) / 24 + " hh: " + (hhour + (mmp) / 60) % 24 + " mm: " + (mmp) % 60);
                if (data.snooze2 > 0)
                    setOneCalendar((hhour + (mmp) / 60) / 24, (hhour + (mmp) / 60) % 24, (mmp) % 60, position + 200);
                mmp += data.snooze3;
                Log.d("SNOOZE3", "dd: " + (hhour + (mmp) / 60) / 24 + " hh: " + (hhour + (mmp) / 60) % 24 + " mm: " + (mmp) % 60);
                if (data.snooze3 > 0)
                    setOneCalendar((hhour + (mmp) / 60) / 24, (hhour + (mmp) / 60) % 24, (mmp) % 60, position + 300);

            }


            public void removeCalendar() {
                // put in extra string in my intent to tell clock you pressed off button
                my_intent.putExtra("extra", "alarm off");
                my_intent.putExtra("extra1", "alarm off at time :: " + hjk);

                PendingIntent ppending_intent = PendingIntent.getBroadcast(getContext(), position, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent ppending_intent1 = PendingIntent.getBroadcast(getContext(), position + 100, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent ppending_intent2 = PendingIntent.getBroadcast(getContext(), position + 200, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent ppending_intent3 = PendingIntent.getBroadcast(getContext(), position + 300, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);


                // cancel the pending intent
                alarm_manager.cancel(ppending_intent);
                alarm_manager.cancel(ppending_intent1);
                alarm_manager.cancel(ppending_intent2);
                alarm_manager.cancel(ppending_intent3);

                // stop the ringtone
                getContext().sendBroadcast(my_intent);
            }

            private void setOneCalendar(int day, int hhour, int mminute, int pos) {
                Calendar calendar_d = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_YEAR, calendar_d.get(Calendar.DAY_OF_YEAR) + day);
                calendar.set(Calendar.HOUR_OF_DAY, hhour);
                calendar.set(Calendar.MINUTE, mminute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);


                // create a pending intent that delays the intent until the specified calendar time
                PendingIntent ppending_intent = PendingIntent.getBroadcast(getContext(), pos, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                // set alarm manager
                alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), ppending_intent);
            }

            public void setPref(ClockObject data, Boolean on) {
                SharedPreferences mPrefs = mActivity.getSharedPreferences("CLOCK", MODE_PRIVATE);


                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = mPrefs.getString("MyObject", "");
                ArrayList<ClockObject> list = new ArrayList<ClockObject>();
                list = gson.fromJson(json, new TypeToken<ArrayList<ClockObject>>() {
                }.getType());
                if (list == null)
                    list = new ArrayList<ClockObject>();
                for (Integer m = 0; m < list.size(); m++) {
                    if (Objects.equals(list.get(m).time, data.getTime()))
                        list.set(m, data);
                }
                data.time = hour + ":" + minute + (on ? ":ONN" : " ");
                String json2 = gson.toJson(list);
                Log.d("REVEIL SetObject", json2);
                prefsEditor.putString("MyObject", json2);
                prefsEditor.commit();
            }
        };
        viewHolder.toggle.setOnCheckedChangeListener(lol);

        if (button_on_or_off.equals("ONN")) {
            viewHolder.toggle.setChecked(true);
        }

        viewHolder.settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(mActivity);
                dialog.setContentView(R.layout.settings_dialog);
                dialog.setTitle("Settings");
                Button dialogButton = (Button) dialog.findViewById(R.id.dismiss);
                final TextView snooze1 = (TextView) dialog.findViewById(R.id.snooze1);
                final TextView snooze2 = (TextView) dialog.findViewById(R.id.snooze2);
                final TextView snooze3 = (TextView) dialog.findViewById(R.id.snooze3);
                Button ringtone = (Button) dialog.findViewById(R.id.snooze1_ring);
                snooze1.setText(clockObject.getSnooze1().toString());
                snooze2.setText(clockObject.getSnooze2().toString());
                snooze3.setText(clockObject.getSnooze3().toString());
                // if button is clicked, close the custom dialog
                ringtone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                        mActivity.startActivityForResult(intent, 5);
                    }
                });
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clockObject.setSnooze(1, Integer.valueOf(snooze1.getText().toString()));
                        clockObject.setSnooze(2, Integer.valueOf(snooze2.getText().toString()));
                        clockObject.setSnooze(3, Integer.valueOf(snooze3.getText().toString()));
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        viewHolder.delete_row.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {

                Gson gson = new Gson();
                SharedPreferences mPrefs = mActivity.getSharedPreferences("CLOCK", MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();

                String json = mPrefs.getString("MyObject", "");
                ArrayList<ClockObject> list = new ArrayList<ClockObject>();
                list = gson.fromJson(json, new TypeToken<ArrayList<ClockObject>>() {
                }.getType());
                int toremove = -1;
                if (list == null)
                    list = new ArrayList<ClockObject>();
                for (int m = 0; m < list.size(); m++) {
                    if (Objects.equals(list.get(m).time, clockObject.getTime())) {
                        toremove = m;
                    }
                }
                if (toremove >= 0) {
                    list.remove(toremove);
                    Log.d("REVEIL", "REMOVED");
                }
                String json2 = gson.toJson(list);
                Log.d("REVEIL", json2);
                prefsEditor.putString("MyObject", json2);
                prefsEditor.commit();

                // put in extra string in my intent to tell clock you pressed off button
                my_intent.putExtra("extra", "alarm off");
                my_intent.putExtra("extra1", "alarm off at time :: " + hjk);

                PendingIntent ppending_intent = PendingIntent.getBroadcast(getContext(), position, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent ppending_intent1 = PendingIntent.getBroadcast(getContext(), position + 100, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent ppending_intent2 = PendingIntent.getBroadcast(getContext(), position + 200, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent ppending_intent3 = PendingIntent.getBroadcast(getContext(), position + 300, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // cancel the pending intent
                alarm_manager.cancel(ppending_intent);
                alarm_manager.cancel(ppending_intent1);
                alarm_manager.cancel(ppending_intent2);
                alarm_manager.cancel(ppending_intent3);

                // stop the ringtone
                getContext().sendBroadcast(my_intent);

                dataSet.remove(position);
                notifyDataSetChanged();
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    public void removeOneCalendar(int pos, Intent my_intent) {
        AlarmManager alarm_manager2 = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);

        my_intent.putExtra("extra", "alarm off");
        my_intent.putExtra("extra1", "alarm off at time :: " + "snooze");
        PendingIntent ppending_intent = PendingIntent.getBroadcast(getContext(), pos, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm_manager2.cancel(ppending_intent);
        getContext().sendBroadcast(my_intent);
    }

}
