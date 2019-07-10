package io.slatch.app.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity(tableName = "country_table")
public class Country {
//{"name":"Belarus","country_code":"BY","dial_code":"+375"}

    @SerializedName("name")
    @Expose
    private String name;



    @PrimaryKey
    @NonNull
    @SerializedName("country_code")
    @Expose
    private String country_code;


    @SerializedName("dial_code")
    @Expose
    private String dial_code;

    private char header;

    @NonNull
    public String getName() {
        if (name == null)
            return header + "";
        else
            return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String countryCode) {
        this.country_code = countryCode;
    }

    public String getDial_code() {
        return dial_code;
    }

    public void setDial_code(String dialCode) {
        this.dial_code = dialCode;
    }



    @Override
    public String toString() {
		return new ToStringBuilder(this).
			append("name", name).
			append("country_code", country_code).
			append("dial_code", dial_code).
			toString();
    }

    public char getHeader() {
        return header;
    }

    public void setHeader(char header) {
        this.header = header;
    }
}
