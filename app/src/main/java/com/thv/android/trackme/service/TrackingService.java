package com.thv.android.trackme.service;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.DataRepository;
import com.thv.android.trackme.R;
import com.thv.android.trackme.common.Constanst;
import com.thv.android.trackme.db.dto.LocationDTO;
import com.thv.android.trackme.db.entity.WorkoutEntity;
import com.thv.android.trackme.listener.InsertCallbackListener;
import com.thv.android.trackme.utils.LogUtils;
import com.thv.android.trackme.utils.PreferenceUtils;
import com.thv.android.trackme.viewmodel.RecordedWorkoutViewModel;

import java.util.List;

//TODO
// Register location update service

public class TrackingService extends Service implements LocationListener, InsertCallbackListener {
    private static final String TAG = TrackingService.class.getSimpleName();
    // actions
    // start receiving continuous location updates
    public static final String ACTION_NEW_SESSION_RECORDING = "action.new_session_recording";
    // pause receiving continuous location updates
    public static final String ACTION_RESUME_RECORDING = "action.resume_recording";
    // pause receiving continuous location updates
    public static final String ACTION_PAUSE_RECORDING = "action.pause_recording";
    // stop receiving continuous location updates
    public static final String ACTION_STOP_RECORDING = "action.stop_recording";
    // request broadcast message about current locations
    public static final String ACTION_BROADCAST_LOCATIONS = "action.broadcast_locations";
;


    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; //50m
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 2; //2m

//    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
//    private static final long MIN_TIME_BW_UPDATES = 1000 ;

    private LocationManager locationManager;


    private boolean isRecording = true;
    // backlog of locations (first entry = oldest, last entry = newest)
    protected WorkoutEntity mWordkout = new WorkoutEntity();
    // parameters
    // workout entity
    public static final String EXTRA_PARAM_WORKOUT_ENTITY = "extra.workout_entity";
    // a list of locations
    public static final String EXTRA_PARAM_LOCATION_LIST = "extra.location_list";
    // boolean about the current GPS provider state
    public static final String EXTRA_PARAM_GPS_PROVIDER = "extra.gps_provider";

    @Override
    public void onCreate() {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService onStartCommand");
        // as a service we must call this ourselves
        if (intent != null && intent.getAction() != null) {
            onHandleIntent(intent);
        }

        return Service.START_STICKY;
    }

    /**
     * Handle intents
     * Note: for services this is no override, instead we have to explicitly call it during onStartCommand()
     */
    protected void onHandleIntent(final Intent intent) {
        Log.v(TAG, "onHandleIntent()");
        if (intent == null) {
            Log.w(TAG, "onHandleIntent(): could not handle intent: null");
            return;
        }

        final String action = intent.getAction();
        if (action == null) {
            Log.w(TAG, "onHandleIntent(): could not handle intent action: null");
            return;
        }

        Log.v(TAG, "onHandleIntent(): action " + action);
        switch (action) {
            case ACTION_NEW_SESSION_RECORDING:
                newSessionRecording(intent);
                break;
            case ACTION_RESUME_RECORDING:
                resumeRecording();
                break;
            case ACTION_PAUSE_RECORDING:
                pauseRecording();
                break;
            case ACTION_STOP_RECORDING:
                stopRecording();
                break;
            case ACTION_BROADCAST_LOCATIONS:
                sendLocationBroadcast();
                break;
            default:
                Log.e(TAG, "onHandleIntent(): invalid action: " + action);
                break;
        }
    }

    private void newSessionRecording(Intent intent) {

        // update old workout
//        LiveData<List<WorkoutEntity>> listWorkout= BasicApp.getInstance().getRepository().getWorkouts();
//        for (WorkoutEntity workout:listWorkout.getValue()
//                ) {
//            if (workout.getStatus()==WorkoutEntity.RECORDING){
//                workout.setStatus(WorkoutEntity.FINISHED);
//                BasicApp.getInstance().getRepository().updateWordout(workout);
//                break;
//            }
//        }


        mWordkout = intent.getParcelableExtra(TrackingService.EXTRA_PARAM_WORKOUT_ENTITY);
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService newSessionRecording id =" +mWordkout.getId());
        if (mWordkout.getId()==0) {
            // create new session
            LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService create new session id =" +mWordkout.getId());

            // update old workout
            LiveData<List<WorkoutEntity>> listWorkout= BasicApp.getInstance().getRepository().getWorkouts();
            for (WorkoutEntity workout:listWorkout.getValue()) {
                if (workout.getStatus()==WorkoutEntity.RECORDING){
                    LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService update workout id =" +workout.getId());

                    workout.setStatus(WorkoutEntity.FINISHED);
                    BasicApp.getInstance().getRepository().updateWordout(workout);
                    break;
                }
            }
            // insert new workout
            BasicApp.getInstance().getRepository().insertWorkout(mWordkout, this);

        }
        resumeRecording();


    }

    /**
     * Start receiving continuous location updates
     */
    protected void resumeRecording() {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService resumeRecording id =" +mWordkout.getId());


        isRecording = true;
        PreferenceUtils.putValueRecording(this, true);

        // startForeground(NOTIFICATION_ID, buildNotification());
        requestLocationUpdates(true /* continuous */);
        mWordkout.setStatus(WorkoutEntity.RECORDING);
        storeState();
        sendLocationBroadcast();
    }

    /**
     * Stop receiving continuous location updates
     */
    protected void pauseRecording() {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService pauseRecording id =" +mWordkout.getId());


        isRecording = false;
        PreferenceUtils.putValueRecording(this, isRecording);

        if (locationManager != null) {
            locationManager.removeUpdates(this);

        }
        mWordkout.setStatus(WorkoutEntity.PAUSE);
        storeState();
        sendLocationBroadcast();

    }

    /**
     * Stop receiving continuous location updates
     */
    protected void stopRecording() {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService stopRecording id =" +mWordkout.getId());

        pauseRecording();
        mWordkout.setStatus(WorkoutEntity.FINISHED);
        storeState();
        stopSelf();
    }

//    /**
//     * Request a broadcast message about current locations.
//     */
//    public static void startActionBroadcastLocations(final Context context) {
//        Log.v(TAG, "startActionBroadcastLocations()");
//        Intent intent = new Intent(context, TrackingService.class);
//        intent.setAction(ACTION_BROADCAST_LOCATIONS);
//        context.startService(intent);
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Start receiving location updates (continuous or a single one) or change update interval.
     * We don't ask for permissions here. This must be handled by the main activity!
     */
    @SuppressWarnings("MissingPermission")
    protected void requestLocationUpdates(boolean continuous) {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService requestLocationUpdates id =" +mWordkout.getId());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_location_permissions_missing, Toast.LENGTH_LONG);
            toast.show();

            return;

        }

        Location location = null;
        try {
            if (locationManager == null)
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    }
                }
            }
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        if (location != null) {
            mWordkout.getLocations().addLast(new LocationDTO(location));
        }
        sendLocationBroadcast();
    }

    /**
     * Handle location updates
     */
    @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
    @Override
    public void onLocationChanged(Location location) {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService onLocationChanged locations size =" +mWordkout.getLocations().size());
        if (location != null) {
                mWordkout.getLocations().addLast(new LocationDTO(location));
        } // else continue, we have been called from deleteLocations()

//        // we just wanted to obtain a single location
//        if (!PreferenceUtils.isRecordEnable(getApplicationContext()) && locationManager != null) {
//            locationManager.removeUpdates(this);
//        }
// LogUtils.showToast(getApplicationContext(), "onLocationChanged locations size="+mWordkout.getLocations().size());
        sendLocationBroadcast();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * Send broadcast message with location backlog
     */
    protected void sendLocationBroadcast() {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService sendLocationBroadcast id =" +mWordkout.getId());
        Intent intent = new Intent(TrackingService.class.getSimpleName());
        intent.putExtra(EXTRA_PARAM_WORKOUT_ENTITY, (Parcelable) mWordkout);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }




    @Override
    public void onLowMemory() {
        storeState();
        super.onLowMemory();
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onDestroy() {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService onDestroy id =" +mWordkout.getId());



    }

    private void storeState() {
        DataRepository db = BasicApp.getInstance().getRepository();
        if (db != null)
            db.updateWordout(mWordkout);
    }

    @Override
    public void onInsertSuccess(long workoutId) {

        mWordkout.setId((int) workoutId);
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "TrackingService onInsertSuccess id =" +mWordkout.getId());

    }

}
