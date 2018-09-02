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

package com.thv.android.trackme.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.db.entity.WorkoutEntity;

import java.util.List;

public class WorkoutListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<WorkoutEntity>> mObservableWorkouts;

    public WorkoutListViewModel(Application application) {
        super(application);

        mObservableWorkouts = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableWorkouts.setValue(null);

        LiveData<List<WorkoutEntity>> workouts = ((BasicApp) application).getRepository()
                .getWorkouts();

        // observe the changes of the workouts from the database and forward them
        mObservableWorkouts.addSource(workouts, mObservableWorkouts::setValue);
    }

    public void updateListWorkouts(){
//        LiveData<List<WorkoutEntity>> workouts = ((BasicApp) getApplication()).getRepository()
//                .getWorkouts();
//        mObservableWorkouts.setValue(workouts);
    }
    /**
     * Expose the LiveData Workouts query so the UI can observe it.
     */
    public LiveData<List<WorkoutEntity>> getWorkouts() {

        return mObservableWorkouts;
    }
}
