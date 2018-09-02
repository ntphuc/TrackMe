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

package com.thv.android.trackme.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;

import com.mypopsy.maps.StaticMap;
import com.mypopsy.maps.StaticMap.Path;
import com.mypopsy.maps.StaticMap.Path.Style;
import com.mypopsy.maps.StaticMap.GeoPoint;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.thv.android.trackme.BasicApp;
import com.thv.android.trackme.R;
import com.thv.android.trackme.common.Constanst;
import com.thv.android.trackme.db.converter.DateConverter;
import com.thv.android.trackme.db.converter.GsonObjectConverter;
import com.thv.android.trackme.db.dto.LocationDTO;
import com.thv.android.trackme.model.Workout;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.Iterator;

@Entity(tableName = "workouts")
public class WorkoutEntity implements Workout, Parcelable {

    public final static int RECORDING = 0;
    public final static int PAUSE = 1;
    public final static int STOP = 2;
    public final static int FINISHED = 3;


    @PrimaryKey(autoGenerate = true)
    private int id;


    private int status;

    // list of locations
    @TypeConverters(GsonObjectConverter.class)
    private ArrayDeque<LocationDTO> locations;

    // location statistics, doesn't need to be serializable
    private transient Statistics statistics;

    // map
    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    @Override
    public String getDisplayStatus() {
        String result="";
        switch (status){
            case RECORDING :
                result = "RECORDING";
                break;
            case PAUSE :
                result = "PAUSE";
                break;
            case STOP :
                result = "STOP";
                break;
            case FINISHED :
                result = "FINISHED";
                break;
        }
        return result;
    }

    public ArrayDeque<LocationDTO> getLocations() {
        return locations;
    }

    public void setLocations(ArrayDeque<LocationDTO> locations) {
        this.locations = locations;
    }

    @Override
    public LocationDTO getCurrentLocation() {
        return locations.getLast();
    }

    @Override
    public double getTotalDistance() {
        return getStatistics().distanceTotal;
    }

    @Override
    public double getAverageSpeed() {
        Log.e(Constanst.LOG_TAG, "Avarage speed " + getStatistics().speedAvg);
        return getStatistics().speedAvg;
    }

    @Override
    public double getCurrentSpeed() {
        return getStatistics().speedLast;
    }

    @Override
    public long getDuration() {
        return getStatistics().duration;
    }

    @Override
    public String getTimeDisplay() {
        return DateConverter.humanReadableDuration(getStatistics().duration);
    }

    //    public String getImageUrl() {
//        // The URL will usually come from a model (i.e Profile)
////        String latEiffelTower = "48.858235";
////        String lngEiffelTower = "2.294571";
//        //  String url = "http://maps.google.com/maps/api/staticmap?center=Brooklyn+Bridge,New+York,NY&zoom=14&size=512x512&maptype=roadmap%20&markers=color:blue|label:S|40.702147,-74.015794&markers=color:green|label:G|40.711614,-74.012318%20&markers=color:red|color:red|label:C|40.718217,-73.998284&sensor=false&key=AIzaSyCtaT3lMfUibYQKQPUhxkRKpqDm_hKrF6Y";
//        //  String url = "http://maps.google.com/maps/api/staticmap?center=Brooklyn+Bridge,New+York,NY&zoom=14&size=512x512&maptype=roadmap%20&markers=color:blue|label:S|40.702147,-74.015794&markers=color:green|label:G|40.711614,-74.012318%20&markers=color:red|color:red|label:C|40.718217,-73.998284&sensor=false&key=AIzaSyCtaT3lMfUibYQKQPUhxkRKpqDm_hKrF6Y";
//        // https://maps.googleapis.com/maps/api/staticmap?path=40.737102,-73.990318|40.749825,-73.987963|40.737102,-73.990318|40.735781,-74.003571|40.737102,-73.990318|40.731690,-73.977849&size=512x512
//        String url = "https://maps.googleapis.com/maps/api/staticmap?key=AIzaSyCtaT3lMfUibYQKQPUhxkRKpqDm_hKrF6Y&zoom=14&size=512x512&maptype=roadmap&center=";
//        StringBuilder s = new StringBuilder(url);
//        int i = 0;
//        for (LocationDTO location : locations) {
//          //  s.append(location.getLatitude() + "," + location.getLongitude() + "|");
//            if (i == 0) {
//                s.append(location.getLatitude() + "," + location.getLongitude());
//
//            } else {
//                s.append("&markers=" + location.getLatitude() + "," + location.getLongitude());
//            }
//             i++;
//        }
//        //s= s.substring(0,s.length()-2);
//        //  s.append("&key=" + BasicApp.getInstance().getAppContext().getResources().getString(R.string.google_maps_api_key));
//        Log.e(Constanst.LOG_TAG, " url " + s.substring(0, s.length() - 1));
//        return s.toString();
//    }
    public String getImageUrl() {
        if (locations.size()==0) return "";
        StaticMap map = new StaticMap().zoom(20)
                .size(1280, 960).type(StaticMap.Type.ROADMAP);
        map.key(BasicApp.getInstance().getAppContext().getResources().getString(R.string.google_maps_api_key));
               // .path(Path.Style.builder().color(Color.BLUE).build());
//        GeoPoint[] gp = new GeoPoint[locations.size()];
//        int i =0;
//        for (LocationDTO location : locations) {
//            gp[i]=new GeoPoint(location.getLatitude(), location.getLongitude());
//            i++;
//        }


        GeoPoint[] gp = new GeoPoint[2];
        gp[0] = new GeoPoint(locations.getFirst().getLatitude(), locations.getFirst().getLongitude());
        gp[1] = new GeoPoint(locations.getLast().getLatitude(), locations.getLast().getLongitude());

        map.marker(gp);

        Style style = Path.Style.builder().color(Color.RED).build();
        if (gp!=null && gp.length!=0)

             map.path(style,gp);
        String url = "";
        try {
            url = map.toURL().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.e(Constanst.LOG_TAG ,  " image url "+ url);
        return url;
    }

    @Override
    public int getLocationSize() {
        return locations.size();
    }


    public WorkoutEntity() {
        locations = new ArrayDeque<LocationDTO>();
    }

    @Ignore
    public WorkoutEntity(int id, ArrayDeque<LocationDTO> locations) {
        this.id = id;
        this.locations = locations;
    }


    /**
     * Return the number of stored locations
     */
    int size() {
        return locations.size();
    }

    /**
     * Return the first (=oldest) location or null if empty
     */
    LocationDTO getFist() {
        return locations.isEmpty() ? null : locations.getFirst();
    }

    /**
     * Return the last (=newest) location or null if empty
     */
    LocationDTO getLast() {
        return locations.isEmpty() ? null : locations.getLast();
    }

    /**
     * Return various statistics about the stored locations
     */
    Statistics getStatistics() {
        // cache result
        if (statistics == null) {
            statistics = new Statistics(locations);
        }
        return statistics;
    }

    /* Add a new location */
    void addLast(final LocationDTO location) {
        locations.addLast(location);
        statistics = null;
    }

    /**
     * Remove the oldest location
     */
    void removeFirst() {
        locations.removeFirst();
        statistics = null;
    }

    /**
     * Clear all locations
     */
    void clear() {
        locations.clear();
        statistics = null;
    }

    /**
     * Create a new location list from a Parcel
     */
    private WorkoutEntity(Parcel in) {
        id = in.readInt();
        status = in.readInt();
        int size = in.readInt();
        locations = new ArrayDeque<>(size);
        for (int i = 0; i < size; ++i) {
            final LocationDTO location = in.readParcelable(LocationDTO.class.getClassLoader());
            locations.addLast(location);
        }
    }

    /**
     * Flatten this location list into a Parcel
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(status);
        out.writeInt(locations.size());
        for (final LocationDTO location : locations) {
            out.writeParcelable(location, flags);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WorkoutEntity> CREATOR = new Creator<WorkoutEntity>() {
        @Override
        public WorkoutEntity createFromParcel(Parcel in) {
            return new WorkoutEntity(in);
        }

        @Override
        public WorkoutEntity[] newArray(final int size) {
            return new WorkoutEntity[size];
        }
    };

    public void updateWorkout(WorkoutEntity workout) {
        this.id = workout.getId();
        this.status =workout.getStatus();
        this.locations=workout.getLocations();
    }

    class Statistics implements Serializable {
        // distance in total (in meters)
        double distanceTotal = 0;
        // distance from the last location measurement (in meters)
        double distanceLast = 0;
        // average speed
        double speedAvg = 0.0;
        // speed since the last location measurement (in km/h)
        double speedLast = 0.0;
        // max speed (in km/h)
        double speedMax = 0.0;
        // duration (in milliseconds)
        long duration = 0;

        Statistics(final ArrayDeque<LocationDTO> locations) {
            if (locations.size() > 0) {
                final LocationDTO lastLocation = locations.getLast();
                if (lastLocation.isHasSpeed()) {
                    speedLast = lastLocation.getSpeed() * 3.6;
                }
            }

            if (locations.size() > 1) {
                final Iterator<LocationDTO> it = locations.iterator();
                LocationDTO prevLocation = it.next();
                speedAvg = prevLocation.getSpeed();
                speedMax = prevLocation.getSpeed();
                while (true) {
                    final LocationDTO curLocation = it.next();
                    distanceTotal += prevLocation.distanceTo(curLocation);
                    // we could replace the current speedAvg calculation by distance / time.
                    // however distance is inaccurate, especially with low recording intervals.
                    speedAvg += curLocation.getSpeed();
                    speedMax = Math.max(speedMax, curLocation.getSpeed());

                    if (!it.hasNext()) {
                        distanceLast = curLocation.distanceTo(prevLocation);
                        break;
                    }

                    prevLocation = curLocation;
                }
                speedAvg /= locations.size();

                // convert from m/s to km/h
                speedAvg *= 3.6;
                speedMax *= 3.6;

                duration = locations.getLast().getTime() - locations.getFirst().getTime();
            }
        }
    }
}
