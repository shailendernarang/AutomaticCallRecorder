package com.ss.automaticrecorder.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;



import com.ss.automaticrecorder.R;
import com.ss.automaticrecorder.callrecorder.AppPreferences;
import com.ss.automaticrecorder.callrecorder.LocalBroadcastActions;
import com.ss.automaticrecorder.callrecorder.MainActivity;
import com.ss.automaticrecorder.database.CallLog;

import java.io.File;
import java.util.Calendar;

/**
 * The nitty gritty Service that handles actually recording the conversations
 */

public class RecordCallService extends Service {

    public final static String ACTION_START_RECORDING = "com.ss.ACTION_CLEAN_UP";
    public final static String ACTION_STOP_RECORDING = "com.ss.ACTION_STOP_RECORDING";
    public final static String EXTRA_PHONE_CALL = "com.ss.EXTRA_PHONE_CALL";

    public RecordCallService(){
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ContentValues parcelableExtra = intent.getParcelableExtra(EXTRA_PHONE_CALL);

        startRecording(new CallLog(parcelableExtra));
        return START_NOT_STICKY ;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        stopRecording();

    }

    private CallLog phoneCall;

    boolean isRecording =false ;

    private void stopRecording() {

        if (isRecording) {
            try {
                phoneCall.setEndTime(Calendar.getInstance());
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                phoneCall.save(getBaseContext());
                displayNotification(phoneCall);

                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(LocalBroadcastActions.NEW_RECORDING_BROADCAST));




            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        phoneCall = null;
    }


    MediaRecorder mediaRecorder =new MediaRecorder();


    private void startRecording(CallLog phoneCall) {
        if (!isRecording) {
            isRecording = true;
            this.phoneCall = phoneCall;
            File file = null;
            try {
                this.phoneCall.setSartTime(Calendar.getInstance());
                File dir = AppPreferences.getInstance(getApplicationContext()).getFilesDirectory();



                String manufacturer = Build.MANUFACTURER;
                if(manufacturer.toLowerCase().contains("asus")){
                    file = File.createTempFile("record", ".wav", dir);
                    this.phoneCall.setPathToRecording(file.getAbsolutePath());

                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    mediaRecorder.setAudioSamplingRate(8000);
                    mediaRecorder.setAudioEncodingBitRate(12200);
                    mediaRecorder.setOutputFile(phoneCall.getPathToRecording());
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                }
                else
                {
                    file = File.createTempFile("record", ".wav", dir);
                    this.phoneCall.setPathToRecording(file.getAbsolutePath());
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mediaRecorder.setAudioChannels(2);
                    mediaRecorder.setAudioSamplingRate(44100);
                    mediaRecorder.setAudioEncodingBitRate(128000);
                    mediaRecorder.setOutputFile(phoneCall.getPathToRecording());
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                }


            } catch (Exception e) {
                e.printStackTrace();
                isRecording = false;
                if (file != null) file.delete();
                this.phoneCall = null;
                isRecording = false;
            }
        }
    }

    public void displayNotification(CallLog phoneCall) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_recording_conversation_white_24);
        builder.setContentTitle(getApplicationContext().getString(R.string.notification_title));
        builder.setContentText(getApplicationContext().getString(R.string.notification_text));
        builder.setContentInfo(getApplicationContext().getString(R.string.notification_more_text));
        builder.setAutoCancel(true);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction(Long.toString(System.currentTimeMillis())); // fake action to force PendingIntent.FLAG_UPDATE_CURRENT
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtra("RecordingId", phoneCall.getId());

        builder.setContentIntent(PendingIntent.getActivity(this, 0xFeed, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notificationManager.notify(0xfeed, builder.build());
    }


    public static void startRecording(Context context, CallLog phoneCall) {
        Intent intent = new Intent(context, RecordCallService.class);
        intent.setAction(ACTION_START_RECORDING);
        intent.putExtra(EXTRA_PHONE_CALL, phoneCall.getContent());
        context.startService(intent);
    }


    public static void stopRecording(Context context) {
        Intent intent = new Intent(context, RecordCallService.class);
        intent.setAction(ACTION_STOP_RECORDING);

        context.stopService(intent);
    }


}
