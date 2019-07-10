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

public class StringArrayConverter {

    @TypeConverter
    public static String[] toStringArray(String arrayString) {
        if (arrayString == null)
            return null;
        else {
			try {
				Gson gson = new Gson();
				return gson.fromJson(arrayString, String[].class);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
    }

    @TypeConverter
    public static String fromStringArray(String[] stringArray) {
        if (stringArray == null)
            return null;
        else {
			try {
				Gson gson = new Gson();
				return gson.toJson(stringArray);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
    }

}
