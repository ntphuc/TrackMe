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
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.DataRepository;
import com.thv.android.trackme.db.entity.CommentEntity;
import com.thv.android.trackme.db.entity.WorkoutEntity;

import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {

    private final LiveData<WorkoutEntity> mObservableWorkout;

    public ObservableField<WorkoutEntity> workout = new ObservableField<>();

    private final int mWorkoutId;


    public WorkoutViewModel(@NonNull Application application, DataRepository repository,
                            final int workoutId) {
        super(application);
        mWorkoutId = workoutId;

        mObservableWorkout = repository.loadWorkout(mWorkoutId);
    }



    public LiveData<WorkoutEntity> getObservableWorkout() {
        return mObservableWorkout;
    }

    public void setWorkout(WorkoutEntity workout) {
        this.workout.set(workout);
    }

    /**
     * A creator is used to inject the workout ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the workout ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mWorkoutId;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application, int workoutId) {
            mApplication = application;
            mWorkoutId = workoutId;
            mRepository = ((BasicApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new WorkoutViewModel(mApplication, mRepository, mWorkoutId);
        }
    }
}
