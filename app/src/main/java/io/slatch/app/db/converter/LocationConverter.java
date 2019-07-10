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

package io.slatch.app.db.converter;

import android.arch.persistence.room.TypeConverter;


import com.google.gson.Gson;

import io.slatch.app.db.entity.Location;

public class LocationConverter {

    @TypeConverter
    public static Location toLocation(String locationString) {
        if (locationString == null)
            return null;
        else {
			try {
				Gson gson = new Gson();
				return gson.fromJson(locationString, Location.class);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
    }

    @TypeConverter
    public static String toLocationString(Location location) {
        if (location == null)
            return null;
        else {
			try {
				Gson gson = new Gson();
				return gson.toJson(location);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
    }

}
