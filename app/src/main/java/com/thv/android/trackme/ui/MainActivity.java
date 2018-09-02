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

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.content.ComponentCallbacks2;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.R;
import com.thv.android.trackme.common.Constanst;
import com.thv.android.trackme.db.entity.WorkoutEntity;
import com.thv.android.trackme.model.Workout;
import com.thv.android.trackme.service.TrackingService;
import com.thv.android.trackme.utils.CommonUtils;
import com.thv.android.trackme.utils.LogUtils;
import com.thv.android.trackme.utils.NotificationHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final static int PERMISSION_ALL = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        BasicApp.getInstance().setAppContext(this);
        requestAppPermission();
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "MainActivity OnCreate");
        // Add workout list fragment if this is first creation
        if (savedInstanceState == null) {
            WorkoutListFragment fragment = new WorkoutListFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, WorkoutListFragment.class.getSimpleName()).commit();



        }
    }

    private void requestAppPermission() {


        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if(!CommonUtils.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }

    /** Shows the workout detail fragment */
    public void show(Workout workout) {

        WorkoutFragment workoutFragment = WorkoutFragment.forWorkout(workout.getId());

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(WorkoutFragment.class.getSimpleName())
                .replace(R.id.fragment_container,
                        workoutFragment, WorkoutFragment.class.getSimpleName()).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // create new notification if app not visible
        FragmentManager fm = getSupportFragmentManager();
        Fragment listFragment = fm.findFragmentByTag(WorkoutListFragment.class.getSimpleName());
        if (listFragment!=null && listFragment instanceof WorkoutListFragment ){
            ((WorkoutListFragment)listFragment).showNotification();
        }
    }

    @Override
    protected void onUserLeaveHint() {

        super.onUserLeaveHint();
    }

    /** Shows the workout detail fragment */
    public void showRecordedWorkout(Bundle bundle) {

        RecordedWorkoutFragment workoutFragment = RecordedWorkoutFragment.getInstance(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(RecordedWorkoutFragment.class.getSimpleName())
                .replace(R.id.fragment_container,
                        workoutFragment, RecordedWorkoutFragment.class.getSimpleName()).commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode){
//            case PERMISSION_ALL:
//                Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_location_permissions_missing, Toast.LENGTH_LONG);
//                toast.show();
//        }
    }



    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment instanceof RecordedWorkoutFragment){
           WorkoutEntity workout = ((RecordedWorkoutFragment) fragment).getWorkout();
           Fragment listFragment = fm.findFragmentByTag(WorkoutListFragment.class.getSimpleName());
           if (listFragment!=null && listFragment instanceof WorkoutListFragment ){
               ((WorkoutListFragment)listFragment).updateListWorkout(workout);
           }
        }
        super.onBackPressed();
    }


}
