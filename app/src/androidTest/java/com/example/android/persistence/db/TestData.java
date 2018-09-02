package com.thv.android.trackme.db;

import com.thv.android.trackme.db.entity.WorkoutEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Utility class that holds values to be used for testing.
 */
public class TestData {

    static final WorkoutEntity WORKOUT_ENTITY = new WorkoutEntity(1, "name", "desc",
            3);
    static final WorkoutEntity WORKOUT_ENTITY2 = new WorkoutEntity(2, "name2", "desc2",
            20);

    static final List<WorkoutEntity> WORKOUTS = Arrays.asList(WORKOUT_ENTITY, WORKOUT_ENTITY2);

    static final CommentEntity COMMENT_ENTITY = new CommentEntity(1, WORKOUT_ENTITY.getId(),
            "desc", new Date());
    static final CommentEntity COMMENT_ENTITY2 = new CommentEntity(2,
            WORKOUT_ENTITY2.getId(), "desc2", new Date());

    static final List<CommentEntity> COMMENTS = Arrays.asList(COMMENT_ENTITY, COMMENT_ENTITY2);


}
