package nya.niavart.reveil;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.view.GestureDetector.SimpleOnGestureListener;

import static nya.niavart.reveil.MainActivity.PREFS1;

public class RingTonePlayingService extends Service implements View.OnTouchListener {

    MediaPlayer media_song;
    boolean isRunning;
    int startId;
    private Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // fetch the extra string values
        String state = intent.getExtras().getString("extra");
        int position = intent.getExtras().getInt("position", -1);
        Log.d("TAG::","inside service" + state);
        assert state != null;
        switch (state) {
            case "alarm on":
                startId = 1;
                break;
            case "alarm off":
                startId = 0;
                break;
            default:
                startId = 0;
                break;
        }

        final BroadcastReceiver myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("REVEIL", "get notif");
            }
        };

        registerReceiver(myReceiver, new IntentFilter("nya.niavart.reveil"));


        // if else conditions
        // if music is not playing and the user says alarm on then music should start playing
        if (!this.isRunning && startId == 1) {

            SharedPreferences prefs = getSharedPreferences(PREFS1, 0);
            String rt = prefs.getString("ringtone", "not found");

            SharedPreferences pref2 = getSharedPreferences("DELAY", 0);
            int delay = pref2.getInt("DELAY", 1200000);
            if(rt.equals("not found")){
            media_song = MediaPlayer.create(this, R.raw.analog_watch_alarm);
            }
            else {
                Uri uri = Uri.parse(rt);
                media_song = MediaPlayer.create(this, uri);
            }


            media_song.setLooping(true);

            // start the ringtone
            media_song.start();

            final Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    media_song.stop();
                }
            };

//Task for timer to execute when time expires
            class SleepTask extends TimerTask {
                @Override
                public void run(){
                    handler.sendEmptyMessage(0);
                }
            }

//then in some other function...
            Timer timer = new Timer("timer",true);
            timer.schedule(new SleepTask(),delay);

            this.isRunning = true;
            this.startId = 0;

            // Notification
            // set up the Notification Service
            NotificationManager notify_manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            // set up an intent that goes to the main activity
            Intent intent_main_activity = new Intent(this.getApplicationContext(), MainActivity.class);
            //Intent i = new Intent("stop_alarm");

            intent_main_activity.putExtra("position", position);
            // set up a pending intent
            PendingIntent pending_intent_main_activity = PendingIntent.getActivity(this, 0, intent_main_activity, PendingIntent.FLAG_UPDATE_CURRENT);
            // make the notification parameters

            Log.d("TAG::","startid="+startId);
            this.context=this;
            createNotificationChannel();
            Notification notification_popup = new NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
                    .setContentTitle("Wake up")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText("Tap to close the alarm.")
                    .setContentIntent(pending_intent_main_activity)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze",
                            pending_intent_main_activity)
                    .build();

            notification_popup.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
            notification_popup.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
            notification_popup.ledOnMS = 800;
            notification_popup.ledOffMS = 1000;
            Intent i = new Intent("stop_alarm");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
            notification_popup.contentIntent = pending_intent_main_activity;

            // set up a notification call command
            notify_manager.notify(0, notification_popup);
        }

        // if there is music playing and the user says alarm off then music should stop playing
        if (this.isRunning && startId == 0) {

            Log.d("TAG::","inside if ");
            //int snooze = intent.getExtras().getInt("snooze", 20);

            // stop the ringtone
            media_song.stop();
            media_song.reset();

            this.isRunning = false;
            this.startId = 0;
        }

        // if the user presses random button just to remove any sort of bugs
        // if music is not playing and the user says alarm off then do nothing
        if (!this.isRunning && startId == 0) {

            this.isRunning = false;
            this.startId = 0;
        }

        // if there is music playing and the user says alarm on then do nothing
        if (this.isRunning && startId == 1) {

            this.isRunning = true;
            this.startId = 1;
        }

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "my_Own_Reveil",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.

        super.onDestroy();
        this.isRunning = false;
    }
    private GestureDetector gestureDetector;

    public void OnSwipeTouchListener(Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        return gestureDetector.onTouchEvent(motionEvent);
    }
    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
        Intent intentXXX = new Intent(context,MainActivity.class);
        context.startActivity(intentXXX);
    }

    public void onSwipeLeft() {
        Intent intentXXY = new Intent(context,MainActivity.class);
        context.startActivity(intentXXY);
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }
}
