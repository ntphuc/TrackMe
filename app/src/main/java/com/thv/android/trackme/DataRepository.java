package com.thv.android.trackme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.os.AsyncTask;

import com.thv.android.trackme.db.AppDatabase;
import com.thv.android.trackme.db.dao.WorkoutDao;
import com.thv.android.trackme.db.entity.WorkoutEntity;
import com.thv.android.trackme.listener.InsertCallbackListener;

import java.util.List;

/**
 * Repository handling the work with workouts and comments.
 */
public class DataRepository {

    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private MediatorLiveData<List<WorkoutEntity>> mObservableWorkouts;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
        mObservableWorkouts = new MediatorLiveData<>();

        mObservableWorkouts.addSource(mDatabase.workoutDao().loadAllWorkouts(),
                workoutEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableWorkouts.postValue(workoutEntities);
                    }
                });
    }

    public static DataRepository getInstance(final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of workouts from the database and get notified when the data changes.
     */
    public LiveData<List<WorkoutEntity>> getWorkouts() {
        return mObservableWorkouts;
    }

    public void insertWorkout(WorkoutEntity workout, InsertCallbackListener listener){
        InsertAsyncTask iat= new InsertAsyncTask(mDatabase.workoutDao(),listener);
        iat.execute(workout);

       // return mDatabase.workoutDao().insertWorkout(workout);
    }

    public LiveData<WorkoutEntity> loadWorkout(final int workoutId) {
        return mDatabase.workoutDao().loadWorkout(workoutId);
    }



    public void updateWordout(WorkoutEntity workout) {

        UpdateAsyncTask iat= new UpdateAsyncTask(mDatabase.workoutDao());
        iat.execute(workout);
    }

    private static class InsertAsyncTask extends AsyncTask<WorkoutEntity, Void, Void> {

        private final InsertCallbackListener listener;
        private WorkoutDao mAsyncTaskDao;

        public long getWorkoutId() {
            return workoutId;
        }

        private long workoutId=-1;
        InsertAsyncTask(WorkoutDao dao, InsertCallbackListener listener) {

            mAsyncTaskDao = dao;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(final WorkoutEntity... params) {
            workoutId=mAsyncTaskDao.insertWorkout(params[0]);
            listener.onInsertSuccess(workoutId);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<WorkoutEntity, Void, Void> {

      //  private final InsertCallbackListener listener;
        private WorkoutDao mAsyncTaskDao;

        public long getWorkoutId() {
            return workoutId;
        }

        private long workoutId=-1;

        UpdateAsyncTask(WorkoutDao dao) {

            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WorkoutEntity... params) {
            mAsyncTaskDao.updateWorkout(params[0]);
           // listener.onInsertSuccess(workoutId);
            return null;
        }
    }
}
