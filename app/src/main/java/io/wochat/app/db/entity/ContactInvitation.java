package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "contact_invitation_table")
public class ContactInvitation {




    @PrimaryKey
    @NonNull
    @SerializedName("contact_id")
    @ColumnInfo(name = "contact_id")
    @Expose
    private String contactId;

	public ContactInvitation(String id) {
		contactId = id;
	}

	public ContactInvitation() {
	}

//--------------------------------------------------
//    @SerializedName("is_invited")
//    @ColumnInfo(name = "is_invited")
//    @Expose
//    private boolean isInvited;

//--------------------------------------------------

    @NonNull
    public String getContactId() {
        return contactId;
    }

	public String getId() {
		return contactId;
	}

    public void setContactId(@NonNull String contactId) {
        this.contactId = contactId;
    }

//    public boolean isInvited() {
//        return isInvited;
//    }
//
//    public void setInvited(boolean invited) {
//        isInvited = invited;
//    }

    @Override
    public String toString() {
		return new ToStringBuilder(this).
			append("contactId", contactId).
			//append("isInvited", isInvited).
			toString();
    }


    public static Map<String, Boolean> getMap(List<ContactInvitation> list){
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for (ContactInvitation invitation: list){
			map.put(invitation.getId(), Boolean.TRUE);
		}
		return map;
	}


}
