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

package com.thv.android.trackme.model;

import android.databinding.BindingAdapter;
import android.location.Location;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.thv.android.trackme.db.dto.LocationDTO;

public interface Workout {
    int getId();
    int getStatus();
    String getDisplayStatus();
    LocationDTO getCurrentLocation();
    double getTotalDistance();
    double getAverageSpeed();
    double getCurrentSpeed();
    long getDuration();
    String getTimeDisplay();
    public String getImageUrl() ;
    int getLocationSize();


//    @BindingAdapter({"bind:imageUrl"})
//    public  void loadImage(ImageView view, String imageUrl) ;

}
