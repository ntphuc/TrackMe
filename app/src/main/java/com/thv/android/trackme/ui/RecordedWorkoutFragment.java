/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thv.android.trackme.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.liefery.android.icon_badge.IconBadge;
import com.liefery.android.waypoint_map_view.FreeWaypointMap;
import com.liefery.android.waypoint_map_view.WaypointMap;
import com.thv.android.trackme.R;
import com.thv.android.trackme.common.Constanst;
import com.thv.android.trackme.databinding.RecordedWorkoutFragmentBinding;
import com.thv.android.trackme.db.dto.LocationDTO;
import com.thv.android.trackme.db.entity.WorkoutEntity;
import com.thv.android.trackme.listener.WorkoutActionListener;
import com.thv.android.trackme.listener.WorkoutControlListener;
import com.thv.android.trackme.service.TrackingService;
import com.thv.android.trackme.utils.LogUtils;
import com.thv.android.trackme.utils.PreferenceUtils;
import com.thv.android.trackme.viewmodel.RecordedWorkoutViewModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

//TODO
// Update map with list locations
// Update satistics info such as current speed, current distance, duration
// Action pause, stop, resume.
public class RecordedWorkoutFragment extends Fragment implements OnMapReadyCallback {

    private static final String KEY_WORKOUT_ID = "workout_id";
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    private RecordedWorkoutViewModel model;
    private RecordedWorkoutFragmentBinding mBinding;
    //private WorkoutEntity mWorkout = new WorkoutEntity();
    private boolean isRecording = true;
    private MapView mMapView;
    private GoogleMap gmap;
    private FreeWaypointMap fwMap;
    /**
     * Handle broadcast messages from GPSReceiver
     */
    private BroadcastReceiver mGPSReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(Constanst.LOG_TAG, " on Received ");
            if (isRecording != PreferenceUtils.isRecordEnable(context)) {
                // state of GPSReceiver doesn't match ours, this can happen if we are getting killed
                // and the GPSReceiver service has been respawned automatically by Android
                isRecording = PreferenceUtils.isRecordEnable(context);
                mBinding.setIsRecording(isRecording);
            }

            if (intent.hasExtra(TrackingService.EXTRA_PARAM_WORKOUT_ENTITY)) {
                final WorkoutEntity workoutEntity = intent.getParcelableExtra(TrackingService.EXTRA_PARAM_WORKOUT_ENTITY);
                // workoutEntity.setId(model.workout.get().getId());
                handleLocationChanged(workoutEntity);
            } else if (intent.hasExtra(TrackingService.EXTRA_PARAM_GPS_PROVIDER)) {
                showGPSProviderHint(intent.getExtras().getBoolean(TrackingService.EXTRA_PARAM_GPS_PROVIDER));
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.recorded_workout_fragment, container, false);


        return mBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "RecordedWorkoutFragment onActivityCreated");

        RecordedWorkoutViewModel.Factory factory = new RecordedWorkoutViewModel.Factory(
                getActivity().getApplication());

        model = ViewModelProviders.of(this, factory)
                .get(RecordedWorkoutViewModel.class);

        mBinding.setRecordedWorkoutViewModel(model);
        mBinding.setStatusControl(mStatusControlListener);

        subscribeToModel(model);


        startServiceRecord();

        initMap(savedInstanceState);
    }

    private void initMap(Bundle savedInstanceState) {
        mMapView = (MapView) mBinding.getRoot().findViewById(R.id.map);

        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(this);


    }

    private void startServiceRecord() {
        // receive broadcast messages from GPSReceiver service
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "RecordedWorkoutFragment onActivityCreated");

        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
                mGPSReceiver, new IntentFilter(TrackingService.class.getSimpleName()));

        // check if GPSReceiver is recording (i.e. we got killed but the GPS service not)
        isRecording = PreferenceUtils.isRecordEnable(this.getActivity());
        mBinding.setIsRecording(isRecording);
        // get previously recorded locations

        Bundle bundle = getArguments();
        WorkoutEntity workout = new WorkoutEntity();
        if (bundle.getParcelable(TrackingService.EXTRA_PARAM_WORKOUT_ENTITY)!=null){
            workout = (WorkoutEntity) bundle.getParcelable(TrackingService.EXTRA_PARAM_WORKOUT_ENTITY);
            model.getObservableWorkout().setValue(workout);
        }

        newSessionRecording(this.getActivity(),workout);
    }

    private void subscribeToModel(final RecordedWorkoutViewModel model) {

        // Observe workout data
        model.getObservableWorkout().observe(this, new Observer<WorkoutEntity>() {
            @Override
            public void onChanged(@Nullable WorkoutEntity workoutEntity) {
                Log.e(Constanst.LOG_TAG, " on change work entity " + workoutEntity.getId());
                model.setWorkout(workoutEntity);
                mBinding.setRecordedWorkoutViewModel(model);
            }
        });


    }

    /**
     * Handle location updates
     */
    private void handleLocationChanged(final WorkoutEntity workoutEntity) {
        model.getObservableWorkout().setValue(workoutEntity);
        updateMap(workoutEntity.getLocations());

    }

    private void updateMap(ArrayDeque<LocationDTO> locations) {
        //  map1ReadyCallback.onMapReady(gmap);
        configureMap(this.getContext(), fwMap, locations);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                CameraUpdate update = fwMap.adjustZoomToMarkers();
                fwMap.getGoogleMap().moveCamera(update);
            }
        });

    }
//    /** Creates workout fragment for specific workout ID */
//    public static RecordedWorkoutFragment forWorkout(int workoutId) {
//        RecordedWorkoutFragment fragment = new RecordedWorkoutFragment();
//        Bundle args = new Bundle();
//        args.putInt(KEY_WORKOUT_ID, workoutId);
//        fragment.setArguments(args);
//        return fragment;
//    }

    public static RecordedWorkoutFragment getInstance(Bundle bundle) {
        RecordedWorkoutFragment fragment = new RecordedWorkoutFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        fwMap = new FreeWaypointMap(this.getActivity(), gmap);

    }


    private void configureMap(Context context, WaypointMap map, ArrayDeque<LocationDTO> listLocations) {
        if (context == null) return;
        if (map!=null)
            map.clear();
        if (listLocations.isEmpty()) {

        } else {
            LatLng position1 = new LatLng(listLocations.getFirst().getLatitude(), listLocations.getFirst().getLongitude());
            LatLng position2 = new LatLng(listLocations.getLast().getLatitude(), listLocations.getLast().getLongitude());

            IconBadge badge1 = new IconBadge(context);
            badge1.setBackgroundShapeCircle();
            badge1.setBackgroundShapeColor(Color.BLUE);
            badge1.setElevation(context.getResources().getDimension(
                    R.dimen.marker_shadow));
            badge1.setForegroundShapeColor(Color.WHITE);
            badge1.setNumber(1);


            IconBadge badge2 = new IconBadge(context);
            badge2.setBackgroundShapePin();
            badge2.setBackgroundShapeColor(Color.RED);
            badge2.setElevation(context.getResources().getDimension(
                    R.dimen.marker_shadow));
            badge2.setForegroundShapeColor(Color.WHITE);
            badge2.setNumber(2);


            map.addWaypoint(badge1, position1);
            map.addWaypoint(badge2, position2);

//        ArrayList<LatLng> planned = new ArrayList<>( 3 );
//        planned.add( new LatLng( 52.49625750906928, 13.348448602482677 ) );
//        planned.add( new LatLng( 52.49643060295327, 13.349650232121348 ) );
//        planned.add( new LatLng( 52.495267921517275, 13.349698511883616 ) );
//        map.addRoutePlanned( PolyUtil.encode( planned ) );

            ArrayList<LatLng> actual = new ArrayList<LatLng>(listLocations.size());
            int size = listLocations.size();
            for (final LocationDTO location : listLocations) {
                actual.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }
//            actual.add(new LatLng(52.49528098552441, 13.349714605137706));
//            actual.add(new LatLng(52.49463431251219, 13.349741427227855));
//            actual.add(new LatLng(52.49448080792577, 13.347928253933787));
//            actual.add(new LatLng(52.494516734579136, 13.346082894131541));
//            actual.add(new LatLng(52.49506542891036, 13.346243826672435));
//            actual.add(new LatLng(52.495940712837644, 13.346254555508494));
//            actual.add(new LatLng(52.49627057278243, 13.348545162007213));
            map.addRouteActual(PolyUtil.encode(actual));
        }
    }

    /**
     * Show information about disabled/enabled GPS provider
     */
    private void showGPSProviderHint(boolean gpsEnabled) {
        if (!gpsEnabled) {
            // warn about disabled GPS provider
            AlertDialog dialog = new AlertDialog.Builder(this.getActivity())
                    .setTitle(R.string.dialog_title_gps_provider_disabled)
                    .setMessage(R.string.dialog_body_gps_provider_disabled)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            dialog.show();
        } else {
            // notify about GPS provider being enabled now
            Toast toast = Toast.makeText(this.getActivity().getApplicationContext(), R.string.toast_gps_provider_enabled, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mMapviewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mMapviewBundle == null) {
            mMapviewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mMapviewBundle);
        }

        mMapView.onSaveInstanceState(mMapviewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        fwMap.clear();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private final WorkoutControlListener mStatusControlListener = new WorkoutControlListener() {

        @Override
        public void onPause() {
            Toast.makeText(RecordedWorkoutFragment.this.getContext(), "onPause", Toast.LENGTH_LONG).show();
            mBinding.setIsRecording(false);
            pauseTracking(RecordedWorkoutFragment.this.getContext());

        }

        @Override
        public void onResume() {
            Toast.makeText(RecordedWorkoutFragment.this.getContext(), "onResume", Toast.LENGTH_LONG).show();
            resumeTracking(RecordedWorkoutFragment.this.getContext());
        }

        @Override
        public void onStop() {
            Toast.makeText(RecordedWorkoutFragment.this.getContext(), "onStop", Toast.LENGTH_LONG).show();
            stopTracking(RecordedWorkoutFragment.this.getContext());
            returnListWorkouts();
        }
    };
    /**
     * Stop receiving continuous location updates
     */
    public void newSessionRecording(final Context context, WorkoutEntity workout) {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "RecordedWorkoutFragment newSessionRecording");

        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(TrackingService.ACTION_NEW_SESSION_RECORDING);
        intent.putExtra(TrackingService.EXTRA_PARAM_WORKOUT_ENTITY, (Parcelable)workout);
        context.startService(intent);
    }
    /**
     * Stop receiving continuous location updates
     */
    public void resumeTracking(final Context context) {
        Log.v(Constanst.LOG_TAG, "resume tracking");

        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(TrackingService.ACTION_RESUME_RECORDING);
        context.startService(intent);
    }

    /**
     * Stop receiving continuous location updates
     */
    public  void pauseTracking(final Context context) {
        Log.v(Constanst.LOG_TAG, "pause tracking ()");

        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(TrackingService.ACTION_PAUSE_RECORDING);
        context.startService(intent);
    }

    /**
     * Stop receiving continuous location updates
     */
    public  void stopTracking(final Context context) {
        Log.v(Constanst.LOG_TAG, "stop tracking ()");

        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(TrackingService.ACTION_STOP_RECORDING);
        context.startService(intent);
    }
    /**
     * Shows the workout detail fragment
     */
    public void returnListWorkouts() {

        getActivity().getSupportFragmentManager().popBackStack();
    }

    public WorkoutEntity getWorkout() {
        return model.getObservableWorkout().getValue();
    }
}
