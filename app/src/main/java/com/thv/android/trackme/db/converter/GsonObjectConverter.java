package com.thv.android.trackme.db.converter;

import android.arch.persistence.room.TypeConverter;

import com.thv.android.trackme.db.dto.LocationDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Type;

public class GsonObjectConverter {



    @TypeConverter
    public static ArrayDeque<LocationDTO> stringToSomeObjectList(String data) {
        if (data == null) {
            return new ArrayDeque<LocationDTO>();
        }

        Type listType = new TypeToken<ArrayDeque<LocationDTO>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(ArrayDeque<LocationDTO> someObjects) {
        Gson gson = new Gson();
        return gson.toJson(someObjects);
    }
}