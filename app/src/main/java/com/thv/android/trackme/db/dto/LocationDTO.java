package com.thv.android.trackme.db.dto;


import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Contains a single location. This location is both parcelable as well as serializable.
 *
 * This object is very similar to Android's Location class. However Android's Location class
 * is not serializable.
 *
 * Additionally we can save some memory by storing only the interesting parts of a Location
 * object (but waste some more CPU cycles during conversion)
 */
public class LocationDTO implements Parcelable, Serializable {

    double latitude;
    double longitude;
    long time;
    boolean hasSpeed;
    float speed;
    boolean hasAccuracy;
    float accuracy;

    /** creates a new location object */
    public LocationDTO(final Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        time = location.getTime();
        hasSpeed = location.hasSpeed();
        speed = hasSpeed ? location.getSpeed() : 0.0f;
        hasAccuracy = location.hasAccuracy();
        accuracy = hasAccuracy ? location.getAccuracy() : 0.0f;
    }

    public LocationDTO() {

    }

    /** computes the distance in meters between two locations */
    public float distanceTo(final LocationDTO other) {
        float dist[] = new float[1];
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, dist);
        return dist[0];
    }

    /** creates a new location from a Parcel */
    private LocationDTO(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        time = in.readLong();
        speed = in.readFloat();
        accuracy = in.readFloat();
    }

    /** flattens this location list into a Parcel */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeLong(time);
        parcel.writeFloat(speed);
        parcel.writeFloat(accuracy);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationDTO> CREATOR = new Creator<LocationDTO>() {
        @Override
        public LocationDTO createFromParcel(Parcel in) {
            return new LocationDTO(in);
        }

        @Override
        public LocationDTO[] newArray(final int size) {
            return new LocationDTO[size];
        }
    };


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isHasSpeed() {
        return hasSpeed;
    }

    public void setHasSpeed(boolean hasSpeed) {
        this.hasSpeed = hasSpeed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isHasAccuracy() {
        return hasAccuracy;
    }

    public void setHasAccuracy(boolean hasAccuracy) {
        this.hasAccuracy = hasAccuracy;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }


}
