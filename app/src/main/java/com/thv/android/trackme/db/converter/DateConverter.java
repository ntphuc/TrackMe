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

package com.thv.android.trackme.db.converter;

import android.annotation.SuppressLint;
import android.arch.persistence.room.TypeConverter;
import android.content.Context;

import com.thv.android.trackme.R;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }


    /**
     * Converts a distance from meters to an human readable String
     *
     * @param precisionHint  Number of desired digits to show.
     *                       Note that for larger numbers the precision will be reduced by 1 to keep the string short.
     */
    @SuppressLint("DefaultLocale")
    public static String humanReadableDistance(final Context context, final double value, final int precisionHint) {
        final int reducedPrecision = precisionHint >= 1 ? precisionHint - 1 : 0;
        if (value >= 1000 * 100) { // >= 100 km
            final String formatNumber = "%." + reducedPrecision + "f";
            return String.format(formatNumber + " %s",
                    value / 1000, context.getString(R.string.distance_unit_kilometers));
        } else if (value >= 1000) { // >= 1 km
            final String formatNumber = "%." + precisionHint + "f";
            return String.format(formatNumber + " %s",
                    value / 1000f, context.getString(R.string.distance_unit_kilometers));
        } else if (value >= 100) { // >= 10 m
            final String formatNumber = "%." + reducedPrecision + "f";
            return String.format(formatNumber + " %s",
                    value, context.getString(R.string.distance_unit_meters));
        } else { // < 1 km
            final String formatNumber = "%." + precisionHint + "f";
            return String.format(formatNumber + " %s",
                    value, context.getString(R.string.distance_unit_meters));
        }
    }

    /** Converts a duration from milliseconds to an human readable String */
    @SuppressLint("DefaultLocale")
    public static String humanReadableDuration( final long milliSeconds) {
        long tmp = milliSeconds / 1000;
        long seconds = tmp % 60;
        tmp /= 60;
        long minutes = tmp % 60;
        tmp /= 60;
        long hours = tmp % 24;
        tmp /= 24;
        long days = tmp;
        if (days == 0) {
            return String.format("%02d:%02d:%02d",
                    hours, minutes, seconds);
        } else {
            return String.format("%d %s, %02d:%02d:%02d",
                    days, "d", hours, minutes, seconds);
        }
    }
}
