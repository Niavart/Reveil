package nya.niavart.reveil;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import static nya.niavart.reveil.MainActivity.PREFS1;

public class SettingsActivity extends AppCompatActivity {

    Button ringtone, syncCode, createCode, DelayButton;
    EditText CodeInput, DelayInput;
    String chosenRingtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences pref2 = getSharedPreferences("DELAY", 0);
        int delay = pref2.getInt("DELAY", 1200000);
        DelayInput = (EditText) findViewById(R.id.delayInput);
        DelayInput.setText(String.valueOf(delay));

        ringtone = (Button) findViewById(R.id.ringtonepicker);
        ringtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                startActivityForResult(intent, 5);
            }
        });

        createCode = (Button) findViewById(R.id.CreateCode);
        createCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final android.support.v7.app.AlertDialog.Builder mBuilder = new android.support.v7.app.AlertDialog.Builder(SettingsActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.dialog_timepicker, null);
                // initialize our time picker
                final TimePicker alarm_timepicker = (TimePicker) mView.findViewById(R.id.alarm_timePicker);
                alarm_timepicker.setIs24HourView(true);
                Button ok = (Button) mView.findViewById(R.id.ok_timepicker);

                mBuilder.setView(mView);
                final android.support.v7.app.AlertDialog dialog = mBuilder.create();
                dialog.show();

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        String hour = String.valueOf(alarm_timepicker.getCurrentHour());
                        String minute = String.valueOf(alarm_timepicker.getCurrentMinute());
                        String ans = hour + ':' + minute + ":ONN";
                        Gson gson = new Gson();
                        String json2 = gson.toJson(new ClockObject(ans));
                        Log.d("REVEILADD", json2);


                        Random random = new Random();

                        final String id = String.format("%04d", random.nextInt(10000));
                        final String mRequestBody = "{ \"code\":" + id + ", \"clock\":" + json2 + "}";

                        Log.d("VOLLEYURL", mRequestBody);
                        RequestQueue queue = Volley.newRequestQueue(SettingsActivity.this);
                        String url = "https://reveil.herokuapp.com/code/setCode";

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                dialog.dismiss();
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("reveil", id);
                                clipboard.setPrimaryClip(clip);
                                Snackbar.make(findViewById(R.id.activity_settings), "Code " + id + " copied to clipboard.", Snackbar.LENGTH_LONG)
                                        .setAction("No action", null).show();
                                Log.i("LOG_VOLLEY", response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("LOG_VOLLEY", error.toString());
                            }
                        }) {
                            @Override
                            public String getBodyContentType() {
                                return "application/json; charset=utf-8";
                            }

                            @Override
                            public byte[] getBody() throws AuthFailureError {
                                try {
                                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                                } catch (UnsupportedEncodingException uee) {
                                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                                    return null;
                                }
                            }

                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                String responseString = "";
                                if (response != null) {

                                    responseString = String.valueOf(response.statusCode);

                                }
                                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                            }
                        };
// Add the request to the RequestQueue.
                        queue.add(stringRequest);
                    }
                });
            }
        });

        CodeInput = (EditText) findViewById(R.id.CodeInput);

        syncCode = (Button) findViewById(R.id.CodeButton);
        syncCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CodeInput.getText().toString();
                RequestQueue queue = Volley.newRequestQueue(SettingsActivity.this);
                String url = "https://reveil.herokuapp.com/code/getCode?code=" + CodeInput.getText().toString();
                Log.d("VOLLEYGETURL SYNC", url);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("LOG_VOLLEY SYNC", response);
                                SharedPreferences mPrefs = SettingsActivity.this.getSharedPreferences("CLOCK", MODE_PRIVATE);

                                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                                Gson gson = new Gson();
                                String json = mPrefs.getString("MyObject", "");

                                ArrayList<ClockObject> list = new ArrayList<ClockObject>();
                                list = gson.fromJson(json, new TypeToken<ArrayList<ClockObject>>() {
                                }.getType());
                                if (list == null)
                                    list = new ArrayList<ClockObject>();
                                ClockObject rep = gson.fromJson(response, ClockObject.class);
                                list.add(rep);

                                String json2 = gson.toJson(list);
                                Log.d("LOG_VOLLEY_ADD", json2);
                                prefsEditor.putString("MyObject", json2);
                                prefsEditor.commit();


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LOG_VOLLEY", error.toString());
                    }
                });
                queue.add(stringRequest);
            }
        });

        DelayInput = (EditText) findViewById(R.id.delayInput);

        DelayButton = (Button) findViewById(R.id.DelayButton);
        DelayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value= DelayInput.getText().toString();
                int finalValue=Integer.parseInt(value);
                SharedPreferences x1 = getSharedPreferences("DELAY", 0);
                SharedPreferences.Editor editor = x1.edit();
                editor.putInt("DELAY", finalValue);
                editor.commit();
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        String title = "";
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                this.chosenRingtone = uri.toString();
                SharedPreferences x1 = getSharedPreferences(PREFS1, 0);
                SharedPreferences.Editor editor = x1.edit();
                editor.putString("ringtone", this.chosenRingtone);
                editor.commit();
                Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(this.chosenRingtone));
                title = ringtone.getTitle(this);
            } else {
                this.chosenRingtone = null;
            }
        }
        Toast.makeText(SettingsActivity.this, "Ringtone Selected: " + title, Toast.LENGTH_SHORT).show();
    }


}
