package com.thv.android.trackme.service;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import com.thv.android.trackme.utils.PreferenceUtils;
import com.thv.android.trackme.viewmodel.RecordedWorkoutViewModel;

//TODO
// Register location update service

public class TrackingService extends Service implements LocationListener, InsertCallbackListener {
    private static final String TAG = TrackingService.class.getSimpleName();
    // actions
    // start receiving continuous location updates
    private static final String ACTION_START_RECORDING = "action.start_recording";
    // pause receiving continuous location updates
    private static final String ACTION_PAUSE_RECORDING = "action.pause_recording";
    // stop receiving continuous location updates
    private static final String ACTION_STOP_RECORDING = "action.stop_recording";
    // request broadcast message about current locations
    private static final String ACTION_BROADCAST_LOCATIONS = "action.broadcast_locations";
    // add a single location
    private static final String ACTION_ADD_SINGLE_LOCATION = "action.add_single_location";
    // delete all locations
    private static final String ACTION_DELETE_ALL_LOCATIONS = "action.delete_all_locations";
    // re-read settings
    private static final String ACTION_REREAD_SETTINGS = "action.reread_settings";


    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

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
        Log.v(TAG, "onCreate()");
        BasicApp.getInstance().getRepository().insertWorkout(mWordkout, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand()");
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
            case ACTION_START_RECORDING:
                startRecording();
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

    /**
     * Start receiving continuous location updates
     */
    protected void startRecording() {
        Log.v(TAG, "startRecording()");

        isRecording = true;
        PreferenceUtils.putValueRecording(this, true);

        // startForeground(NOTIFICATION_ID, buildNotification());
        requestLocationUpdates(true /* continuous */);
    }

    /**
     * Stop receiving continuous location updates
     */
    protected void pauseRecording() {
        Log.v(TAG, "pauseRecording()");

        isRecording = false;
        PreferenceUtils.putValueRecording(this, isRecording);

        if (locationManager != null) {
            locationManager.removeUpdates(this);

        }

        storeState();


    }

    /**
     * Stop receiving continuous location updates
     */
    protected void stopRecording() {
        Log.v(TAG, "stopRecording()");
        pauseRecording();
        storeState();
        stopSelf();
    }

    /**
     * Request a broadcast message about current locations.
     */
    public static void startActionBroadcastLocations(final Context context) {
        Log.v(TAG, "startActionBroadcastLocations()");
        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(ACTION_BROADCAST_LOCATIONS);
        context.startService(intent);
    }

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
        Log.v(TAG, "requestLocationUpdates(): continuous " + continuous);

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
        Log.v(Constanst.LOG_TAG, "onLocationChanged() " + location.getLatitude());
        if (location != null) {
            mWordkout.getLocations().addLast(new LocationDTO(location));
        } // else continue, we have been called from deleteLocations()

        // we just wanted to obtain a single location
        if (!PreferenceUtils.isRecordEnable(getApplicationContext()) && locationManager != null) {
            locationManager.removeUpdates(this);
        }
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
        Log.v(TAG, "sendLocationBroadcast()");
        Intent intent = new Intent(TrackingService.class.getSimpleName());
        intent.putExtra(EXTRA_PARAM_WORKOUT_ENTITY, (Parcelable) mWordkout);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    /**
     * Stop receiving continuous location updates
     */
    public static void startTracking(final Context context) {
        Log.v(TAG, "start tracking");

        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(ACTION_START_RECORDING);
        context.startService(intent);
    }

    /**
     * Stop receiving continuous location updates
     */
    public static void pauseTracking(final Context context) {
        Log.v(TAG, "pause tracking ()");

        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(ACTION_PAUSE_RECORDING);
        context.startService(intent);
    }

    /**
     * Stop receiving continuous location updates
     */
    public static void stopTracking(final Context context) {
        Log.v(TAG, "stop tracking ()");

        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(ACTION_STOP_RECORDING);
        context.startService(intent);
    }

    @Override
    public void onLowMemory() {
        stopRecording();
        super.onLowMemory();
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        stopRecording();


    }

    private void storeState() {
        DataRepository db = BasicApp.getInstance().getRepository();
        if (db != null)
            db.updateWordout(mWordkout);
    }

    @Override
    public void onInsertSuccess(long workoutId) {
        mWordkout.setId((int) workoutId);
    }

}
