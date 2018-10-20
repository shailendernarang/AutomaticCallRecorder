package  com.ss.automaticrecorder.callrecorder;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.ss.automaticrecorder.AboutDialog;
import com.ss.automaticrecorder.R;
import com.ss.automaticrecorder.RateMeNowDialog;
import com.ss.automaticrecorder.database.CallLog;
import com.ss.automaticrecorder.database.Database;



public class MainActivity extends AppCompatActivity implements RecordingFragment.OnListFragmentInteractionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaController.MediaPlayerControl {

     AdView mAdView;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private static final int PERMISSION_REQUEST_CODE = 200;
    PhoneCallRecord selectedItems[];

     ViewPager mViewPager;
    private InterstitialAd mInterstitialAd;

    private Handler handler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {



            handler.postDelayed(this, 60*1000);


            showInterstitial();

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-9506813711758016~1461908725");
        mAdView = (AdView) findViewById(R.id.adView);
        mInterstitialAd = new InterstitialAd(MainActivity.this);
        loadInterstitial();


        if (!checkPermissionStorage()) {

            requestPermission();

        }



        // Prepare the Interstitial Ad

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("B6B98FD4578E72D3AEA02322D6F3B1BF")
                .build();


        mAdView.loadAd(adRequest);



       mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //set up MediaPlayer
        mediaController = new MediaController(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);

        RateMeNowDialog.showRateDialog(this, 10);



        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        handler = new Handler();
        handler.postDelayed(mRunnable,20*1000);
    }



    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("83B7256E27A52E0FB0DD1CD849AFA3F2")

                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.setAdUnitId("ca-app-pub-9506813711758016/6627439641");

        mInterstitialAd.loadAd(adRequest);
    }


    private InterstitialAd newInterstitialAd() {

        final InterstitialAd interstitialAd = new InterstitialAd(this);

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

            }

            @Override
            public void onAdClosed() {

                // Reload ad so it can be ready to be show to the user next time
                mInterstitialAd = new InterstitialAd(MainActivity.this);


            }
        });
        return interstitialAd;

    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {

            mInterstitialAd.show();
        } else {

            mInterstitialAd = newInterstitialAd();
            loadInterstitial();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(mRunnable);
        mediaController.hide();

        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //the MediaController will hide after 3 seconds - tap the screen to make it appear again
        mediaController.show();
        return false;
    }

    Menu optionsMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SwitchCompat switchCompat = (SwitchCompat) menu.findItem(R.id.onswitch).getActionView().findViewById(R.id.switch1);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppPreferences.getInstance(MainActivity.this).setRecordingEnabled(isChecked);
            }
        });
        switchCompat.setChecked(AppPreferences.getInstance(MainActivity.this).isRecordingEnabled());
        return true;
    }

    boolean doubleBackToExitPressedOnce;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        // Does the user really want to exit?
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_back_again), Toast.LENGTH_LONG).show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {



                } else {



                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);

                }
            }

            Intent intent = new Intent(this, SettingsActivity.class);

            startActivity(intent);
            return true;
        }


        if (id == R.id.action_save) {
            if (null != selectedItems && selectedItems.length > 0) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        for (PhoneCallRecord record : selectedItems) {
                            record.getPhoneCall().setKept(true);
                            record.getPhoneCall().save(MainActivity.this);
                        }
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(LocalBroadcastActions.NEW_RECORDING_BROADCAST)); // Causes refresh

                    }
                };
                handler.post(runnable);
            }
            return true;
        }



        if (id == R.id.action_delete) {
            if (null != selectedItems && selectedItems.length > 0) {
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        this);
                alert.setTitle(R.string.delete_recording_title);
                alert.setMessage(R.string.delete_recording_subject);
                alert.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Database callLog = Database.getInstance(MainActivity.this);
                                for (PhoneCallRecord record : selectedItems) {
                                    int id = record.getPhoneCall().getId();
                                    callLog.removeCall(id);
                                }

                                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(LocalBroadcastActions.RECORDING_DELETED_BROADCAST));
                            }
                        };
                        handler.post(runnable);

                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();
            }
            return true;
        }

        if (id == R.id.action_delete_all) {

            AlertDialog.Builder alert = new AlertDialog.Builder(
                    this);
            alert.setTitle(R.string.delete_recording_title);
            alert.setMessage(R.string.delete_all_recording_subject);
            alert.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Database.getInstance(MainActivity.this).removeAllCalls(false);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(LocalBroadcastActions.RECORDING_DELETED_BROADCAST));
                        }
                    };
                    handler.post(runnable);

                    dialog.dismiss();

                }
            });
            alert.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            alert.show();

            return true;
        }

        if (R.id.action_whitelist == id) {
          try {
              Intent intent = new Intent(this, WhitelistActivity.class);
              startActivity(intent);
          }catch (Exception e){}
            return true;
        }
        if (R.id.action_about == id) {

            AboutDialog.show(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (null != intent) {

            long id = intent.getIntExtra("RecordingId", -1);
            if (-1 != id) {
                CallLog call = Database.getInstance(this).getCall((int) id);
                if (null != call) {
                    audioPlayer(call.getPathToRecording());
                }
                intent.putExtra("RecordingId", -1); // run only once...
            }
        }
    }

    @Override
    public void onListFragmentInteraction(PhoneCallRecord items[]) {
        try {
            optionsMenu.findItem(R.id.action_delete).setVisible(items.length > 0);

            selectedItems = items;

            if (mediaController.isEnabled() && !mediaController.isShowing()) mediaController.show();
        }catch (Exception e)
        {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getApplicationContext());
            builder1.setMessage("Something Wrong . Try again Later :)");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }




    @Override
    public void onItemPlay(PhoneCallRecord item) {
        audioPlayer(item.getPhoneCall().getPathToRecording());
    }

    @Override
    public boolean onListItemLongClick(View v, final PhoneCallRecord record, final PhoneCallRecord items[]) {
        selectedItems = items;
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        popupMenu.show();
        return false;
    }

    MediaPlayer mediaPlayer;
    MediaController mediaController;

    public void audioPlayer(String path) {

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Media Player stuff
     **/

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(this.findViewById(R.id.list));

        handler.post(new Runnable() {
            public void run() {
                mediaController.setEnabled(true);
                mediaController.show(5000);
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        mediaController.hide();
        mediaController.setEnabled(false);
    }





    public boolean checkPermissionStorage(){

        int result = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result1 = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS);
        int result2 = ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.RECORD_AUDIO);
        int result3 = ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CALL_PHONE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2==PackageManager.PERMISSION_GRANTED && result3==PackageManager.PERMISSION_GRANTED;


    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CODE);


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull int[] grantResults) {
        try {
        switch (requestCode) {

                case PERMISSION_REQUEST_CODE:
                    if (grantResults.length > 0) {

                        boolean readContacts = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        boolean record = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                        boolean call = grantResults[3] == PackageManager.PERMISSION_GRANTED;


                        if (readContacts && readStorage && record && call) {


                        } else {


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                                    showMessageOKCancel("You need to allow access to all the permissions",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                                        , Manifest.permission.CALL_PHONE, Manifest.permission.RECORD_AUDIO},
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
        }catch (Exception e){}
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 1)
                return RecordingFragment.newInstance(1, RecordingFragment.SORT_TYPE.INCOMING);
            else if (position == 2)
                return RecordingFragment.newInstance(1, RecordingFragment.SORT_TYPE.OUTGOING);
            else
                return RecordingFragment.newInstance(1, RecordingFragment.SORT_TYPE.ALL);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.all);
                case 1:
                    return getString(R.string.incoming);
                case 2:
                    return getString(R.string.outgoing);
            }
            return null;
        }


    }

    }



