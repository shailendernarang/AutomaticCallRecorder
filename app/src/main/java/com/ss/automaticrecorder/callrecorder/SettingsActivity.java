package com.ss.automaticrecorder.callrecorder;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.ss.automaticrecorder.R;
import com.ss.automaticrecorder.database.CallLog;
import com.ss.automaticrecorder.database.Database;
import com.ss.automaticrecorder.receivers.MyAlarmReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;
        private static final int PERMISSION_REQUEST_CODE = 200;
        class MyArrayAdapter<T> extends ArrayAdapter<T> {

            ArrayList<Integer> icons;

            MyArrayAdapter(Context context, List objects, ArrayList<Integer> icons) {
                super(context, android.R.layout.simple_spinner_item, objects);
                this.icons = icons;
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(icons.get(position), 0, 0, 0);
                return view;
            }
        }

        @Override
        public void onStop () {
        final AppPreferences.OlderThan olderThan = AppPreferences.getInstance(this).getOlderThan();
        if (olderThan != AppPreferences.OlderThan.NEVER) {
            MyAlarmReceiver.setAlarm(SettingsActivity.this);
        } else {
            MyAlarmReceiver.cancleAlarm(SettingsActivity.this);
        }
        super.onStop();
    }

        AppPreferences preferences;

        @Override
        protected void onCreate (Bundle savedInstanceState) {
            try {
                super.onCreate(savedInstanceState);

                setContentView(R.layout.activity_settings);
                MobileAds.initialize(this,
                        "ca-app-pub-9506813711758016/6627439641");

                mInterstitialAd = new InterstitialAd(this);
                mInterstitialAd.setAdUnitId("ca-app-pub-9506813711758016/6627439641");
                mInterstitialAd.loadAd(new AdRequest.Builder()
                        .addTestDevice("83B7256E27A52E0FB0DD1CD849AFA3F2")
                        .build());

                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        // Load the next interstitial.
                        mInterstitialAd.loadAd(new AdRequest.Builder()
                                .addTestDevice("83B7256E27A52E0FB0DD1CD849AFA3F2")
                                .build());
                    }

                });
                if (!checkPermissionStorage()) {

                    requestPermission();

                }

                preferences = AppPreferences.getInstance(this);

                CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
                checkBox.setChecked(preferences.isRecordingIncomingEnabled());
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        preferences.setRecordingIncomingEnabled(isChecked);
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }
                    }
                });
                checkBox = (CheckBox) findViewById(R.id.checkBox2);
                checkBox.setChecked(preferences.isRecordingOutgoingEnabled());
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        preferences.setRecordingOutgoingEnabled(isChecked);
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }
                    }
                });

                File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(this, null);
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                List<String> list = new ArrayList<String>();
                ArrayList<Integer> icons = new ArrayList<>();

                File filesDir = getFilesDir();
                list.add(filesDir.getAbsolutePath());
                icons.add(R.drawable.ic_folder_black_24dp);

                for (File file : externalFilesDirs) {
                    try {
                        list.add(file.getAbsolutePath());

                        icons.add(R.drawable.ic_cards_black_24);
                    } catch (Exception e) {
                    }
                }
                final MyArrayAdapter<String> dataAdapter = new MyArrayAdapter<String>(this, list, icons);
                spinner.setAdapter(dataAdapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String path = dataAdapter.getItem(position);
                        calcFreeSpace(path);
                        AppPreferences.getInstance(getApplicationContext()).setFilesDirectory(path);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                String path = AppPreferences.getInstance(getApplicationContext()).getFilesDirectory().getAbsolutePath();
                spinner.setSelection(dataAdapter.getPosition(path.replace("/calls/", "")));
                calcFreeSpace(path);


                // Now, count the recordings
                ArrayList<CallLog> allCalls = Database.getInstance(getApplicationContext()).getAllCalls();
                TextView textView = (TextView) findViewById(R.id.textView4);
                String str = textView.getText().toString();
                str = String.format(str, allCalls.size());
                textView.setText(Html.fromHtml(str));

                // Get the length of each file...
                long length = 0;
                for (CallLog call : allCalls) {
                    File file = new File(call.getPathToRecording());
                    length += file.length();
                }
                textView = (TextView) findViewById(R.id.textView5);
                str = textView.getText().toString();
                str = String.format(str, length / 1024);
                textView.setText(Html.fromHtml(str));

                spinner = (Spinner) findViewById(R.id.spinner2);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Obviously <string-array name="pref_frequencies"> MUST be in the same order as AppPreferences.OlderThan enum
                        final AppPreferences.OlderThan olderThan = AppPreferences.OlderThan.values()[position];
                        AppPreferences.getInstance(getApplicationContext()).setOlderThan(olderThan);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                spinner.setSelection(AppPreferences.getInstance(getApplicationContext()).getOlderThan().ordinal());


            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Give Permissions To Storage!",Toast.LENGTH_LONG).show();
            }

        }


    private void calcFreeSpace(String path) {
        try {
            StatFs stat = new StatFs(path);
            long bytesTotal = 0;
            long bytesAvailable = 0;
            float megAvailable = 0;
            long megTotalAvailable = 0;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bytesTotal = (long) stat.getBlockSizeLong() * (long) stat.getBlockCountLong();
                bytesAvailable = (long) stat.getBlockSizeLong() * (long) stat.getAvailableBlocksLong();
            } else {
                bytesTotal = (long) stat.getBlockSize() * (long) stat.getBlockCount();
                bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
            }
            megAvailable = bytesAvailable / 1048576;
            megTotalAvailable = bytesTotal / 1048576;

            // Free Space
            TextView textView = (TextView) findViewById(R.id.textView6);
            String str = getString(R.string.pref_folder_total_folder_size);
            str = String.format(str, megAvailable);
            textView.setText(Html.fromHtml(str));
        }catch(Exception e){
            requestPermission();
        }
    }
    public boolean checkPermissionStorage() {

        int result = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);


        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {


                    boolean readStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;


                    if (readStorage) {


                    } else {


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to this permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SettingsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


}
