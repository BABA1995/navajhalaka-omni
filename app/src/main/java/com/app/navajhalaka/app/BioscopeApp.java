package com.app.navajhalaka.app;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.app.navajhalaka.database.BusProvider;
import com.app.navajhalaka.receiver.AcknowledgementReceiver;
import com.app.navajhalaka.receiver.VideoCompleteReceiver;
import com.google.firebase.FirebaseApp;
import com.lib.firebase.FireBaseApplication;
import com.lib.location.LocationApplication;
import com.lib.location.databases.LocationProvider;
import com.lib.route.RouteApplication;
import com.lib.utility.util.CustomIntent;
import com.lib.videoplayer.VideoApplication;
import com.lib.videoplayer.database.VideoProvider;
import com.lib.videoplayer.receivers.VideoCommandReceiver;
import com.lib.videoplayer.util.AdsSlotConfigUtil;
import com.lib.videoplayer.util.StateMachine;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

//import static com.lib.videoplayer.util.AdsSlotConfigUtil.DEFAULT_ADS_PER_SLOT_COUNT;
//import static com.lib.videoplayer.util.AdsSlotConfigUtil.DEFAULT_SLOTS_PER_HOUR_COUNT;

public class BioscopeApp extends Application {

    private static final String TAG = BioscopeApp.class.getSimpleName();
    private static final String FOLDER_NAME = "movie_bioscope";
    private static final String FIRST_RUN_KEY = "first_run";

    private static Context sContext;
    public int Adsltsphr, sltsphr;


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        //FirebaseApp.initializeApp(this);
        VideoApplication.setVideoContext(this);
        LocationApplication.setLocationContext(this);
        RouteApplication.setRouteContext(this);
        FireBaseApplication.setFirebaseContext(this);
        registerVideoCommand();
        registerVideoComplete();
        registerAcknowledgementCommand();
        createBackupDb();
        //putAdsConfigData();
        if (isFirstRun()) {
            initSequence();
            initIntroVideo();
            putAdsConfigData();
        } else {
            setFirstRun(false);
            movieSequence();
            putAdsConfigData();
        }
        setFirstRun(false);
    }

    private boolean isFirstRun() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(FIRST_RUN_KEY, true);
    }

    private void initIntroVideo() {
        ContentValues lValues1 = new ContentValues();
        lValues1.put(VideoProvider.VIDEO_COLUMNS.VIDEO_ID, "VID3203");
        lValues1.put(VideoProvider.VIDEO_COLUMNS.NAME, "Navajhalaka");
        lValues1.put(VideoProvider.VIDEO_COLUMNS.TYPE, VideoProvider.VIDEO_TYPE.INTRO_VIDEO);
        lValues1.put(VideoProvider.VIDEO_COLUMNS.PATH, "android.resource://" + getContext().getPackageName() + "/" + com.lib.videoplayer.R.raw.navajhalaka_intro);
        lValues1.put(VideoProvider.VIDEO_COLUMNS.LAST_PLAYED_TIME, System.currentTimeMillis());
        lValues1.put(VideoProvider.VIDEO_COLUMNS.DOWNLOAD_STATUS, VideoProvider.DOWNLOAD_STATUS.DOWNLOADED);
        getContext().getContentResolver().insert(VideoProvider.CONTENT_URI_VIDEO_TABLE, lValues1);
    }

    private void initSequence() {
        String[] landingSequence = getContext().getResources().getStringArray(com.lib.videoplayer.R.array.landing_sequence);
        for (int i = 0; i < landingSequence.length; i++) {
            ContentValues values = new ContentValues();
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_TYPE, StateMachine.SEQUENCE_TYPE.LANDING_TYPE);
            values.put(VideoProvider.SEQUENCE_COLUMNS.VIDEO_TYPE, landingSequence[i]);
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_ORDER, i);
            values.put(VideoProvider.SEQUENCE_COLUMNS.UPDATED_TIME, System.currentTimeMillis());
            getContext().getContentResolver().insert(VideoProvider.CONTENT_URI_SEQUENCE_TABLE, values);
        }

        String[] movieInitSequence = getContext().getResources().getStringArray(com.lib.videoplayer.R.array.movie_init_sequence);
        for (int i = 0; i < movieInitSequence.length; i++) {
            ContentValues values = new ContentValues();
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_TYPE, StateMachine.SEQUENCE_TYPE.MOVIE_INIT_TYPE);
            values.put(VideoProvider.SEQUENCE_COLUMNS.VIDEO_TYPE, movieInitSequence[i]);
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_ORDER, i);
            values.put(VideoProvider.SEQUENCE_COLUMNS.UPDATED_TIME, System.currentTimeMillis());
            getContext().getContentResolver().insert(VideoProvider.CONTENT_URI_SEQUENCE_TABLE, values);
        }
        String[] extenalSequence=getContext().getResources().getStringArray(com.lib.videoplayer.R.array.external_sequence);
        for (int i=0;i<extenalSequence.length;i++){
            ContentValues values=new ContentValues();
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_TYPE, StateMachine.SEQUENCE_TYPE.EXERNAL_HARD_DISK);
            values.put(VideoProvider.SEQUENCE_COLUMNS.VIDEO_TYPE, extenalSequence[i]);
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_ORDER, i);
            values.put(VideoProvider.SEQUENCE_COLUMNS.UPDATED_TIME, System.currentTimeMillis());
            getContext().getContentResolver().insert(VideoProvider.CONTENT_URI_SEQUENCE_TABLE, values);

        }
    }





    private void movieSequence() {
        String[] landingSequence = getContext().getResources().getStringArray(com.lib.videoplayer.R.array.landing_sequence);
        for (int i = 0; i < landingSequence.length; i++) {
            ContentValues values = new ContentValues();
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_TYPE, StateMachine.SEQUENCE_TYPE.LANDING_TYPE);
            values.put(VideoProvider.SEQUENCE_COLUMNS.VIDEO_TYPE, landingSequence[i]);
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_ORDER, i);
            values.put(VideoProvider.SEQUENCE_COLUMNS.UPDATED_TIME, System.currentTimeMillis());
            getContext().getContentResolver().insert(VideoProvider.CONTENT_URI_SEQUENCE_TABLE, values);
        }

        String[] movieInitSequence = getContext().getResources().getStringArray(com.lib.videoplayer.R.array.movie_sequence);
        for (int i = 0; i < movieInitSequence.length; i++) {
            ContentValues values = new ContentValues();
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_TYPE, StateMachine.SEQUENCE_TYPE.MOVIE_INIT_TYPE);
            values.put(VideoProvider.SEQUENCE_COLUMNS.VIDEO_TYPE, movieInitSequence[i]);
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_ORDER, i);
            values.put(VideoProvider.SEQUENCE_COLUMNS.UPDATED_TIME, System.currentTimeMillis());
            getContext().getContentResolver().insert(VideoProvider.CONTENT_URI_SEQUENCE_TABLE, values);
        }
        String[] extenalSequence=getContext().getResources().getStringArray(com.lib.videoplayer.R.array.external_sequence);
        for (int i=0;i<extenalSequence.length;i++){
            ContentValues values=new ContentValues();
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_TYPE, StateMachine.SEQUENCE_TYPE.EXERNAL_HARD_DISK);
            values.put(VideoProvider.SEQUENCE_COLUMNS.VIDEO_TYPE, extenalSequence[i]);
            values.put(VideoProvider.SEQUENCE_COLUMNS.SEQUENCE_ORDER, i);
            values.put(VideoProvider.SEQUENCE_COLUMNS.UPDATED_TIME, System.currentTimeMillis());
            getContext().getContentResolver().insert(VideoProvider.CONTENT_URI_SEQUENCE_TABLE, values);

        }
    }

    public static Context getContext() {
        return sContext;
    }
    public void createBackupDb(){
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "/data/data/com.app.navajhalaka/databases/bioscope.db";
                String backupDBPath = "backupname.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            Log.e("Bioscope","backup.db",e);
        }

    }


    private void putAdsConfigData() {

        checkAdsConfigData();
        if (Adsltsphr == 0 && sltsphr == 0) {
        } else {
            AdsSlotConfigUtil.insertAdsSlotsConfiguration("landing", Adsltsphr, sltsphr);
        }
    }

    private void checkAdsConfigData() {
        Adsltsphr = AdsSlotConfigUtil.getSlotsPerHourCount(AdsSlotConfigUtil.SLOT_TYPE.LANDING_SLOT_TYPE);
        sltsphr = AdsSlotConfigUtil.getAdsPerSlotCount(AdsSlotConfigUtil.SLOT_TYPE.LANDING_SLOT_TYPE);
    }

    private void registerVideoCommand() {
        IntentFilter lIntentFilter = new IntentFilter();
        lIntentFilter.addAction(CustomIntent.ACTION_VIDEO_DATA_RECEIVED);
        lIntentFilter.addAction(CustomIntent.ACTION_MOVIE_LIST);
        lIntentFilter.addAction(CustomIntent.ACTION_ROUTE_CHANGED);
        lIntentFilter.addAction(CustomIntent.ACTION_MOVIE_SELECTION_CHANGED);
        lIntentFilter.addAction(CustomIntent.ACTION_ADS_SLOTS_CONFIG_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(new VideoCommandReceiver(), lIntentFilter);
    }

    private void registerVideoComplete() {
        IntentFilter lIntentFilter = new IntentFilter();
        lIntentFilter.addAction(CustomIntent.ACTION_MOVIE_COMPLETED);
        lIntentFilter.addAction(CustomIntent.ACTION_ADV_COMPLETED);
        LocalBroadcastManager.getInstance(this).registerReceiver(new VideoCompleteReceiver(), lIntentFilter);
    }

    private void registerAcknowledgementCommand() {
        IntentFilter lIntentFilter = new IntentFilter();
        lIntentFilter.addAction(CustomIntent.ACTION_ACK_DATA_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(new AcknowledgementReceiver(), lIntentFilter);
    }

    public void setFirstRun(boolean firstRun) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(FIRST_RUN_KEY, firstRun);
        editor.commit();
    }
}
