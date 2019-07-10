package io.slatch.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;


public class ContactServer {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "contact_server_id")
    @SerializedName("user_id")
    @Expose
    private String contactServerId;
//------------------------------------

    @SerializedName("user_name")
    @ColumnInfo(name = "user_name")
    @Expose
    private String userName;
//------------------------------------


    @SerializedName("status")
    @ColumnInfo(name = "status")
    @Expose
    private String status;
//------------------------------------

    @SerializedName("country_code")
    @ColumnInfo(name = "country_code")
    @Expose
    private String countryCode;
//------------------------------------

    @SerializedName("language")
    @ColumnInfo(name = "language")
    @Expose
    private String language;
//------------------------------------


    @SerializedName("profile_pic_url")
    @ColumnInfo(name = "profile_pic_url")
    @Expose
    private String profilePicUrl;
//------------------------------------


    @SerializedName("location")
    @ColumnInfo(name = "location")
    @Expose
    private Location location;
//------------------------------------
    @SerializedName("gender")
    @ColumnInfo(name = "gender")
    @Expose
    private String gender;
//------------------------------------

    @SerializedName("birthdate")
    @ColumnInfo(name = "birthdate")
    @Expose
    private String birthdate;
//------------------------------------

    @SerializedName("last_update_date")
    @ColumnInfo(name = "last_update_date")
    @Expose
    private Integer lastUpdateDate;
//------------------------------------
    @SerializedName("discoverable")
    @ColumnInfo(name = "discoverable")
    @Expose
    private Boolean discoverable;
//------------------------------------
    @SerializedName("os")
    @ColumnInfo(name = "os")
    @Expose
    private String os;
//------------------------------------
    @SerializedName("language_locale")
    @ColumnInfo(name = "language_locale")
    @Expose
    private String languageLocale;
//------------------------------------
    @SerializedName("app_version")
    @ColumnInfo(name = "app_version")
    @Expose
    private String appVersion;

//------------------------------------

//	@Embedded(prefix="cntct_local_")
//    private ContactLocal contactLocal;

//------------------------------------

    public String getContactServerId() {
        return contactServerId;
    }

    public void setContactServerId(String id) {
        this.contactServerId = id;
    }

    public String getUserId() {
        return contactServerId;
    }

    public void setUserId(String userId) {
        this.contactServerId = userId;
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

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public Integer getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Integer lastUpdateDate) {
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

//	public ContactLocal getContactLocal() {
//		return contactLocal;
//	}
//
//	public void setContactLocal(ContactLocal contactLocal) {
//		this.contactLocal = contactLocal;
//	}


	@Override
    public String toString() {
        return new ToStringBuilder(this).
            append("userId", contactServerId).
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
			//append("contactLocal", contactLocal==null?"null":contactLocal.toString()).
            toString();
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static ContactServer fromJson(String json){
        Gson gson = new Gson();
        return gson.fromJson(json, ContactServer.class);
    }



/*
    private String contactServerId;
    private String userName;
    private String status;
    private String countryCode;
    private String language;
    private String profilePicUrl;
    private Location location;
    private String gender;
    private String birthdate;
    private Integer lastUpdateDate;
    private Boolean discoverable;
    private String os;
    private String languageLocale;
    private String appVersion;
    */
}
