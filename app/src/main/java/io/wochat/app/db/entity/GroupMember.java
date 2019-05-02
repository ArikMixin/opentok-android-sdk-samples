package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.wochat.app.R;
import io.wochat.app.utils.Utils;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "group_memeber_table",
	primaryKeys= { "user_id", "group_id" },
	indices = {@Index("group_id"), @Index("user_id")},
	foreignKeys = {

		@ForeignKey(entity = Contact.class,
			parentColumns = "contact_id",
			childColumns = "user_id",
			onDelete = CASCADE),

		@ForeignKey(entity = Conversation.class,
			parentColumns = "conversation_id",
			childColumns = "group_id",
			onDelete = CASCADE)})

public class GroupMember {

	@NonNull
    @SerializedName("user_id")
	@ColumnInfo(name = "user_id")
    @Expose
    private String userId;

	@NonNull
	@SerializedName("group_id")
	@ColumnInfo(name = "group_id")
	@Expose
	private String groupId;


    @SerializedName("user_name")
	@ColumnInfo(name = "user_name")
    @Expose
    private String userName;

    @SerializedName("color")
	@ColumnInfo(name = "color")
	@Expose
	private @ColorInt int color;

	@SerializedName("is_admin")
	@ColumnInfo(name = "is_admin")
	@Expose
	private boolean isAdmin;



	public GroupMember(String userId, String groupId, String userName, boolean isAdmin){
		this.userId = userId;
		this.groupId = groupId;
		this.userName = userName;
		this.isAdmin = isAdmin;
	}


	@Override
    public String toString() {
		return new ToStringBuilder(this).
			append("userId", userId).
			append("groupId", groupId).
			append("userName", userName).
			append("isAdmin", isAdmin).
			toString();
    }


	@NonNull
	public String getUserId() {
		return userId;
	}

	public void setUserId(@NonNull String userId) {
		this.userId = userId;
	}

	@NonNull
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(@NonNull String groupId) {
		this.groupId = groupId;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserFirstName() {
		if (Utils.isNullOrEmpty(userName))
			return "";
		userName = userName.trim();
		if (userName.contains(" "))
			return userName.split(" ")[0];
		else
			return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}


	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}


	private static int mMemberColorIndex;

	public static void initMemberColorIndex(){
		mMemberColorIndex = 0;
	}

	public static @ColorInt int getNextMemberColor(Resources resources){
		TypedArray colorsArray = resources.obtainTypedArray(R.array.group_member_array);
		int color = colorsArray.getColor(mMemberColorIndex++,0);
		if (mMemberColorIndex >= colorsArray.length())
			mMemberColorIndex = 0;
		return color;
	}
}
