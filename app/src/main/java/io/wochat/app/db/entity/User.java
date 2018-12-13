package io.wochat.app.db.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "user_table")
public class User {

	@PrimaryKey
	@NonNull
    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("user_name")
    @Expose
    private String userName;

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("country_code")
    @Expose
    private String countryCode;

    @SerializedName("language")
    @Expose
    private String language;

    @SerializedName("profile_pic_url")
    @Expose
    private String profilePicUrl;

    @SerializedName("location")
    @Expose
    private Location location;

    @SerializedName("gender")
    @Expose
    private String gender;

    @SerializedName("birthdate")
    @Expose
    private long birthdate;

    @SerializedName("last_update_date")
    @Expose
    private long lastUpdateDate;

    @SerializedName("discoverable")
    @Expose
    private boolean discoverable;

    @SerializedName("os")
    @Expose
    private String os;

    @SerializedName("language_locale")
    @Expose
    private String languageLocale;

    @SerializedName("app_version")
    @Expose
    private String appVersion;



    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Boolean getDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(Boolean discoverable) {
        this.discoverable = discoverable;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getLanguageLocale() {
        return languageLocale;
    }

    public void setLanguageLocale(String languageLocale) {
        this.languageLocale = languageLocale;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
		return new ToStringBuilder(this).
			append("userId", userId).
			append("userName", userName).
			append("status", status).
			append("countryCode", countryCode).
			append("language", language).
			append("profilePicUrl", profilePicUrl).
			append("location", location).
			append("gender", gender).
			append("birthdate", birthdate).
			append("lastUpdateDate", lastUpdateDate).
			append("discoverable", discoverable).
			append("os", os).
			append("languageLocale", languageLocale).
			append("appVersion", appVersion).
			toString();
    }

}
