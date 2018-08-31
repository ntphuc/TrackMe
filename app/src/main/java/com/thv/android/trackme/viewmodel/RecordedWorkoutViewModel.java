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
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.util.Log;

import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.DataRepository;
import com.thv.android.trackme.common.Constanst;
import com.thv.android.trackme.db.entity.CommentEntity;
import com.thv.android.trackme.db.entity.WorkoutEntity;
import com.thv.android.trackme.listener.InsertCallbackListener;

import java.util.List;

public class RecordedWorkoutViewModel extends AndroidViewModel implements InsertCallbackListener {

   private final MutableLiveData<WorkoutEntity> mObservableWorkout ;


    public ObservableField<WorkoutEntity> workout = new ObservableField<WorkoutEntity>();

   private long mWorkoutId;


    public RecordedWorkoutViewModel(@NonNull Application application, DataRepository repository
                                     ) {
        super(application);
      //  mWorkoutId = workoutId;

        //repository.insertWorkout(workout);
        //mObservableWorkout = repository.loadWorkout((int)mWorkoutId);


        workout.set(new WorkoutEntity());
        mObservableWorkout = new MutableLiveData<WorkoutEntity>();
        mObservableWorkout.setValue(workout.get());
        //repository.insertWorkout(workout.get(),this);



    }

    /**
     * Expose the LiveData Comments query so the UI can observe it.
     */

    public MutableLiveData<WorkoutEntity> getObservableWorkout() {
        return mObservableWorkout;
    }

    public void setWorkout(WorkoutEntity workout) {
        this.workout.set(workout);
    }

    @Override
    public void onInsertSuccess(long workoutId) {
        Log.e(Constanst.LOG_TAG , " on Insert Success "+workoutId);
       // workout.get().setId((int)workoutId);
        mObservableWorkout.getValue().setId((int)workoutId);
    }

    public void updateWorkout() {
        DataRepository repository = ((BasicApp)this.getApplication()).getRepository() ;
        repository.updateWordout(mObservableWorkout.getValue());
    }

    // public void insertWorkout(WorkoutEntity workout) {
     //   repository.insertWorkout(workout);
    //}

    /**
     * A creator is used to inject the workout ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the workout ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private WorkoutEntity mWorkout;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application) {
            mApplication = application;
            this.mWorkout = mWorkout;
            mRepository = ((BasicApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new RecordedWorkoutViewModel(mApplication, mRepository);
        }
    }
}
