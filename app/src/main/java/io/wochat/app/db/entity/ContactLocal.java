package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;


@Entity(tableName = "contact_local_table")
public class ContactLocal {


	@PrimaryKey
	@NonNull
	@ColumnInfo(name = "phone_num_stripped")
	@SerializedName("phone_num_stripped")
	@Expose
	private String mPhoneNumStripped;
//------------------------------------------

	@ColumnInfo(name = "phone_num_iso")
	@SerializedName("phone_num_iso")
	@Expose
	private String mPhoneNumIso;
//------------------------------------------
	@ColumnInfo(name = "display_name")
	@SerializedName("display_name")
	@Expose
	private String mDisplayName;
//------------------------------------------
	@ColumnInfo(name = "os_id")
	@SerializedName("os_id")
	@Expose
	private String mOSId;

//------------------------------------------

	public ContactLocal(){

	}


	public ContactLocal(String osId, String displayName, String phoneNumIso, String phoneNumStripped) {
		mOSId = osId;
		mDisplayName = displayName;
		mPhoneNumIso = phoneNumIso;
		mPhoneNumStripped = phoneNumStripped;
	}

	@NonNull
	public String getPhoneNumStripped() {
		return mPhoneNumStripped;
	}

	public void setPhoneNumStripped(@NonNull String phoneNumStripped) {
		mPhoneNumStripped = phoneNumStripped;
	}


	public String getDisplayName() {
		return mDisplayName;
	}

	public void setDisplayName(String displayName) {
		mDisplayName = displayName;
	}


	public String getPhoneNumIso() {
		return mPhoneNumIso;
	}

	public void setPhoneNumIso(String phoneNumIso) {
		mPhoneNumIso = phoneNumIso;
	}

	public String getOSId() {
		return mOSId;
	}

	public void setOSId(String OSId) {
		mOSId = OSId;
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("PhoneNumStripped", mPhoneNumStripped).
			append("PhoneNumIso", mPhoneNumIso).
			append("DisplayName", mDisplayName).
			append("OSId", mOSId).
			toString();
	}
}
