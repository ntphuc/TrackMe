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
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.R;
import com.thv.android.trackme.databinding.ListFragmentBinding;
import com.thv.android.trackme.db.entity.WorkoutEntity;
import com.thv.android.trackme.listener.WorkoutActionListener;
import com.thv.android.trackme.listener.WorkoutClickCallback;
import com.thv.android.trackme.model.Workout;
import com.thv.android.trackme.service.TrackingService;
import com.thv.android.trackme.utils.LogUtils;
import com.thv.android.trackme.viewmodel.WorkoutListViewModel;
import com.thv.android.trackme.listener.WorkoutClickCallback;

import java.util.List;

public class WorkoutListFragment extends Fragment {

    public static final String TAG = "WorkoutListViewModel";

    private WorkoutAdapter mWorkoutAdapter;

    private ListFragmentBinding mBinding;
    private WorkoutListViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "WorkoutListFragment onCreateView");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.list_fragment, container, false);
        mBinding.setRecord(mWorkoutActionListener);
        mWorkoutAdapter = new WorkoutAdapter(mWorkoutClickCallback);
        mBinding.workoutsList.setAdapter(mWorkoutAdapter);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtils.i(LogUtils.TAG_ROUTE_CREATE_SERVICE, "WorkoutListFragment onActivityCreated");
        viewModel =
                ViewModelProviders.of(this).get(WorkoutListViewModel.class);

        subscribeUi(viewModel);


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void subscribeUi(WorkoutListViewModel viewModel) {
        // Update the list when the data changes
        viewModel.getWorkouts().observe(this, new Observer<List<WorkoutEntity>>() {
            @Override
            public void onChanged(@Nullable List<WorkoutEntity> myWorkouts) {
                if (myWorkouts != null) {
                    mBinding.setIsLoading(false);
                    mWorkoutAdapter.setWorkoutList(myWorkouts);
                } else {
                    mBinding.setIsLoading(true);
                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
//                if (myWorkouts!=null) {
//                    for (WorkoutEntity workout : myWorkouts) {
//                        if (workout.getStatus() == WorkoutEntity.RECORDING) {
//                            Bundle bundle = new Bundle();
//                            bundle.putParcelable(TrackingService.EXTRA_PARAM_WORKOUT_ENTITY, (Parcelable) workout);
//                            ((MainActivity) getActivity()).showRecordedWorkout(bundle);
//                            break;
//                        }
//                    }
//                }
            }
        });
    }

    private final WorkoutClickCallback mWorkoutClickCallback = new WorkoutClickCallback() {
        @Override
        public void onClick(Workout workout) {

            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {

                if (workout.getStatus() != WorkoutEntity.FINISHED) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(TrackingService.EXTRA_PARAM_WORKOUT_ENTITY, (Parcelable) workout);
                    ((MainActivity) getActivity()).showRecordedWorkout(bundle);
                } else {
                    ((MainActivity) getActivity()).show(workout);
                }
            }
        }
    };

    private final WorkoutActionListener mWorkoutActionListener = new WorkoutActionListener() {

        @Override
        public void onRecord() {
            Toast.makeText(WorkoutListFragment.this.getContext(), "onRecord", Toast.LENGTH_LONG).show();
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                ((MainActivity) getActivity()).showRecordedWorkout(new Bundle());
            }
        }
    };

    public void updateListWorkout(WorkoutEntity workout) {
        for (WorkoutEntity w:viewModel.getWorkouts().getValue()
             ) {
            if (w.getId()==workout.getId()){
                w.updateWorkout(workout);
                break;
            }
        }
        mWorkoutAdapter.notifyDataSetChanged();
    }
}
