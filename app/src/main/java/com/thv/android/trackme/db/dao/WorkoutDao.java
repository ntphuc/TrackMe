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

package com.thv.android.trackme.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import com.thv.android.trackme.db.converter.GsonObjectConverter;
import com.thv.android.trackme.db.dto.LocationDTO;
import com.thv.android.trackme.db.entity.WorkoutEntity;

import java.util.ArrayDeque;
import java.util.List;

@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY id DESC")
    LiveData<List<WorkoutEntity>> loadAllWorkouts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WorkoutEntity> workouts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWorkout(WorkoutEntity workout);

    @Query("select * from workouts where id = :workoutId")
    LiveData<WorkoutEntity> loadWorkout(int workoutId);

    @Query("select * from workouts where id = :workoutId")
    WorkoutEntity loadWorkoutSync(int workoutId);

    @TypeConverters(GsonObjectConverter.class)
   // @Query("update workouts set locations=:locations, status=:status  where id = :workoutId")
    @Update
    void updateWorkout(WorkoutEntity workoutEntity);

}
