package io.wochat.app.db.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Location {

    @SerializedName("long")
    @Expose
    private long lon;
    @SerializedName("lat")
    @Expose
    private long lat;

    public Location(String locationString) {
    }

    public long getLon() {
        return lon;
    }

    public void setLon(long val) {
        this.lon = val;
    }

    public long getLat() {
        return lat;
    }

    public void setLat(long val) {
        this.lat = val;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lon", lon).append("lat", lat).toString();
    }

}
