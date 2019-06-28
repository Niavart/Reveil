package nya.niavart.reveil;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import static android.util.Log.d;

public class MainActivity extends AppCompatActivity {

    ArrayList<ClockObject> clockObjects;
    ListView listView;
    private static CustomAdapter adapter;
    Context context;

    public static final String PREFS1 = "RINGTONE";

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationManager.cancel(0);

        listView = (ListView) findViewById(R.id.list);

        clockObjects = new ArrayList<>();

        this.context = this;

        Intent intent = this.getIntent();
        int position = intent.getIntExtra("position", -1);
        Log.d("SNOOZE", "pos: " + position);

        Gson gson = new Gson();
        SharedPreferences mPrefs = getSharedPreferences("CLOCK", MODE_PRIVATE);
        String json = mPrefs.getString("MyObject", "");
        ArrayList<ClockObject> list = new ArrayList<ClockObject>();
        list  = gson.fromJson(json, new TypeToken<ArrayList<ClockObject>>(){}.getType());
        if (list == null)
            list = new ArrayList<ClockObject>();
        for (Integer m = 0; m < list.size(); m++) {
            if (m == position)
                list.get(m).inc += 1;
            clockObjects.add(new ClockObject(list.get(m)));
        }
        String json2 = gson.toJson(list);
        Log.d("SNOOZE", json2);
        adapter = new CustomAdapter(clockObjects, getApplicationContext(), getLayoutInflater(), MainActivity.this);
        listView.setAdapter(adapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_timepicker, null);
                // initialize our time picker
                final TimePicker alarm_timepicker = (TimePicker) mView.findViewById(R.id.alarm_timePicker);
                alarm_timepicker.setIs24HourView(true);
                Button ok = (Button) mView.findViewById(R.id.ok_timepicker);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String hour = String.valueOf(alarm_timepicker.getCurrentHour());
                        String minute = String.valueOf(alarm_timepicker.getCurrentMinute());
                        String ans = hour + ':' + minute;

                        Log.d("Tag :: ", ans);

                        SharedPreferences  mPrefs = getSharedPreferences("CLOCK", MODE_PRIVATE);
                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        Gson gson = new Gson();
                        String json = mPrefs.getString("MyObject", "");
                        ArrayList<ClockObject> list = new ArrayList<ClockObject>();
                        list  = gson.fromJson(json, new TypeToken<ArrayList<ClockObject>>(){}.getType());
                        if (list == null)
                            list = new ArrayList<ClockObject>();
                        clockObjects.add(new ClockObject(ans));

                        String json2 = gson.toJson(clockObjects);
                        Log.d("REVEILADD", json2);
                        prefsEditor.putString("MyObject", json2);
                        prefsEditor.commit();


                        adapter = new CustomAdapter(clockObjects, getApplicationContext(), getLayoutInflater(), MainActivity.this);
                        listView.setAdapter(adapter);
                        dialog.dismiss();
                        Intent intent100 = getIntent();
                        finish();
                        startActivity(intent100);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent12 = new Intent(this.context, SettingsActivity.class);
            this.context.startActivity(intent12);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d("EXTRAS", extras.toString());
    }

}
