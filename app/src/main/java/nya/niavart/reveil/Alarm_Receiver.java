package nya.niavart.reveil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Alarm_Receiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        // fetch the extra string from intent
        String get_your_string = intent.getExtras().getString("extra");
        int position = intent.getIntExtra("position", 0);

        String asd = intent.getExtras().getString("extra1");
        Log.d("Alarm_Receiver", asd);

        // create an intent to the ringtone service
        Intent service_intent = new Intent(context, RingTonePlayingService.class);

        // pass the extra string from Main Activity to the Ring Tone Playing Service
        service_intent.putExtra("extra", get_your_string);
        service_intent.putExtra("position", position);

        // start the ringtone service
        context.startService(service_intent);

                    /*
            final Calendar calendar = Calendar.getInstance();
            // setting calendar instance with the hour and minute we picked on time picker
            calendar.set(Calendar.HOUR_OF_DAY, hour + 24);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            final Intent my_intent = new Intent(context, Alarm_Receiver.class);

            // put in extra string in my intent to tell clock you pressed on button
            my_intent.putExtra("extra", "alarm on");
            my_intent.putExtra("extra1", "alarm on at time :: " + hjk);
            PendingIntent pending_intent;
            // create a pending intent that delays the intent until the specified calendar time
            pending_intent = PendingIntent.getBroadcast(context, 42, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // set alarm manager
            alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending_intent);

*/

    }
}
