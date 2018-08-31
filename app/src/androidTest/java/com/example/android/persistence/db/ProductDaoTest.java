/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.thv.android.trackme.db;

import static com.thv.android.trackme.db.TestData.WORKOUTS;
import static com.thv.android.trackme.db.TestData.WORKOUT_ENTITY;

import static junit.framework.Assert.assertTrue;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.thv.android.trackme.LiveDataTestUtil;
import com.thv.android.trackme.db.dao.WorkoutDao;
import com.thv.android.trackme.db.entity.WorkoutEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Test the implementation of {@link WorkoutDao}
 */
@RunWith(AndroidJUnit4.class)
public class WorkoutDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase mDatabase;

    private WorkoutDao mWorkoutDao;

    @Before
    public void initDb() throws Exception {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                AppDatabase.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();

        mWorkoutDao = mDatabase.workoutDao();
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }

    @Test
    public void getWorkoutsWhenNoWorkoutInserted() throws InterruptedException {
        List<WorkoutEntity> workouts = LiveDataTestUtil.getValue(mWorkoutDao.loadAllWorkouts());

        assertTrue(workouts.isEmpty());
    }

    @Test
    public void getWorkoutsAfterInserted() throws InterruptedException {
        mWorkoutDao.insertAll(WORKOUTS);

        List<WorkoutEntity> workouts = LiveDataTestUtil.getValue(mWorkoutDao.loadAllWorkouts());

        assertThat(workouts.size(), is(WORKOUTS.size()));
    }

    @Test
    public void getWorkoutById() throws InterruptedException {
        mWorkoutDao.insertAll(WORKOUTS);

        WorkoutEntity workout = LiveDataTestUtil.getValue(mWorkoutDao.loadWorkout
                (WORKOUT_ENTITY.getId()));

        assertThat(workout.getId(), is(WORKOUT_ENTITY.getId()));
        assertThat(workout.getName(), is(WORKOUT_ENTITY.getName()));
        assertThat(workout.getDescription(), is(WORKOUT_ENTITY.getDescription()));
        assertThat(workout.getPrice(), is(WORKOUT_ENTITY.getPrice()));
    }

}
