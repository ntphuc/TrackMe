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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.thv.android.trackme.R;
import com.thv.android.trackme.databinding.WorkoutFragmentBinding;
import com.thv.android.trackme.db.entity.CommentEntity;
import com.thv.android.trackme.db.entity.WorkoutEntity;
import com.thv.android.trackme.model.Comment;
import com.thv.android.trackme.viewmodel.WorkoutViewModel;

import java.util.List;

public class WorkoutFragment extends Fragment {

    private static final String KEY_WORKOUT_ID = "workout_id";

    private WorkoutFragmentBinding mBinding;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.workout_fragment, container, false);
      //  WorkoutEntity workout = getArguments().getInt(KEY_WORKOUT_ID);
     return mBinding.getRoot();
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WorkoutViewModel.Factory factory = new WorkoutViewModel.Factory(
                getActivity().getApplication(), getArguments().getInt(KEY_WORKOUT_ID));

        final WorkoutViewModel model = ViewModelProviders.of(this, factory)
                .get(WorkoutViewModel.class);

        mBinding.setWorkoutViewModel(model);

        subscribeToModel(model);


    }

    private void subscribeToModel(final WorkoutViewModel model) {

        // Observe workout data
        model.getObservableWorkout().observe(this, new Observer<WorkoutEntity>() {
            @Override
            public void onChanged(@Nullable WorkoutEntity workoutEntity) {
                model.setWorkout(workoutEntity);
                ImageView ivMap  =  mBinding.getRoot().findViewById(R.id.ivMap);
                ImageLoader.getInstance().displayImage(workoutEntity.getImageUrl(),ivMap);

            }
        });


    }

    /** Creates workout fragment for specific workout ID */
    public static WorkoutFragment forWorkout(int workoutId) {
        WorkoutFragment fragment = new WorkoutFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_WORKOUT_ID, workoutId);
        fragment.setArguments(args);
        return fragment;
    }
}
