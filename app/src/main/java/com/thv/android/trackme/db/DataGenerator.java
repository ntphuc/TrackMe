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

package com.thv.android.trackme.db;

import com.thv.android.trackme.db.dto.LocationDTO;
import com.thv.android.trackme.db.entity.CommentEntity;
import com.thv.android.trackme.db.entity.WorkoutEntity;
import com.thv.android.trackme.model.Workout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Generates data to pre-populate the database
 */
public class DataGenerator {

    private static final String[] FIRST = new String[]{
            "Special edition", "New", "Cheap", "Quality", "Used"};
    private static final String[] SECOND = new String[]{
            "Three-headed Monkey", "Rubber Chicken", "Pint of Grog", "Monocle"};
    private static final String[] DESCRIPTION = new String[]{
            "is finally here", "is recommended by Stan S. Stanman",
            "is the best sold workout on Mêlée Island", "is \uD83D\uDCAF", "is ❤️", "is fine"};
    private static final String[] COMMENTS = new String[]{
            "Comment 1", "Comment 2", "Comment 3", "Comment 4", "Comment 5", "Comment 6"};

    public static ArrayDeque<LocationDTO> generateListLocations() {
        ArrayDeque<LocationDTO> locations = new ArrayDeque<LocationDTO>();
        Random rnd = new Random();


//10.746708, 106.655183
        LocationDTO dto =new LocationDTO();
        dto.setLatitude(10.746708);
            dto.setLongitude(106.655183);
            locations.add(dto);

            //10.7491642,106.6555375
        LocationDTO dto2 =new LocationDTO();
        dto2.setLatitude(10.7491642);
        dto2.setLongitude(106.6555375);
        locations.add(dto2);

        return locations;
    }

    public static List<WorkoutEntity> generateWorkouts() {
        List<WorkoutEntity> workouts = new ArrayList<>(FIRST.length );
        Random rnd = new Random();
        for (int i = 0; i < FIRST.length; i++) {
                WorkoutEntity workout = new WorkoutEntity();
                workout.setLocations(generateListLocations());
                workout.setId(i);
                workouts.add(workout);

        }
        return workouts;
    }

    public static List<CommentEntity> generateCommentsForWorkouts(
            final List<WorkoutEntity> workouts) {
        List<CommentEntity> comments = new ArrayList<>();
        Random rnd = new Random();

        for (Workout workout : workouts) {
            int commentsNumber = rnd.nextInt(5) + 1;
            for (int i = 0; i < commentsNumber; i++) {
                CommentEntity comment = new CommentEntity();
                comment.setWorkoutId(workout.getId());
                comment.setText(COMMENTS[i] + " for " + workout.getId());
                comment.setPostedAt(new Date(System.currentTimeMillis()
                        - TimeUnit.DAYS.toMillis(commentsNumber - i) + TimeUnit.HOURS.toMillis(i)));
                comments.add(comment);
            }
        }

        return comments;
    }
}
