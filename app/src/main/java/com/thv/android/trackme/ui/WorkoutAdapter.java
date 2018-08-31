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

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.thv.android.trackme.common.Constanst;
import com.thv.android.trackme.databinding.WorkoutItemBinding;
import com.thv.android.trackme.model.Workout;
import com.thv.android.trackme.R;
import com.thv.android.trackme.listener.WorkoutClickCallback;
import java.util.List;
import java.util.Objects;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    List<? extends Workout> mWorkoutList;

    @Nullable
    private final WorkoutClickCallback mWorkoutClickCallback;

    public WorkoutAdapter(@Nullable WorkoutClickCallback clickCallback) {
        mWorkoutClickCallback = clickCallback;
    }

    public void setWorkoutList(final List<? extends Workout> workoutList) {
        if (mWorkoutList == null) {
            mWorkoutList = workoutList;
            notifyItemRangeInserted(0, workoutList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mWorkoutList.size();
                }

                @Override
                public int getNewListSize() {
                    return workoutList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mWorkoutList.get(oldItemPosition).getId() ==
                            workoutList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Workout newWorkout = workoutList.get(newItemPosition);
                    Workout oldWorkout = mWorkoutList.get(oldItemPosition);
                    return newWorkout.getId() == oldWorkout.getId();
//                            && Objects.equals(newWorkout.getDescription(), oldWorkout.getDescription())
//                            && Objects.equals(newWorkout.getName(), oldWorkout.getName())
//                            && newWorkout.getPrice() == oldWorkout.getPrice();
                }
            });
            mWorkoutList = workoutList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        WorkoutItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.workout_item,
                        parent, false);
        binding.setCallback(mWorkoutClickCallback);
        return new WorkoutViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int position) {
        Log.e(Constanst.LOG_TAG, "Number locations "+mWorkoutList.get(position).getLocationSize());
        holder.binding.setWorkout(mWorkoutList.get(position));
        ImageLoader.getInstance().displayImage(mWorkoutList.get(position).getImageUrl(),holder.binding.ivMap);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mWorkoutList == null ? 0 : mWorkoutList.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {

        final WorkoutItemBinding binding;

        public WorkoutViewHolder(WorkoutItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
