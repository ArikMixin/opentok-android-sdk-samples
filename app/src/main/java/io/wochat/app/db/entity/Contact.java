package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IContact;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.wochat.app.db.entity.ContactLocal;
import io.wochat.app.db.entity.ContactServer;

@Entity(tableName = "contact_table")
public class Contact implements IContact {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "contact_id")
    @SerializedName("contact_id")
    @Expose
    private String contactId;
//------------------------------------

	@Embedded(prefix="cntct_srvr_")
	private ContactServer contactServer;


//------------------------------------

    @Embedded(prefix="cntct_local_")
    private ContactLocal contactLocal;

//------------------------------------

	@ColumnInfo(name = "has_server_data")
	@SerializedName("has_server_data")
	private boolean hasServerData;

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String id) {
        this.contactId = id;
    }

	public Contact(){

	}

	public Contact(String id){
		this.contactId = id;
		this.contactLocal = new ContactLocal();
		this.contactServer = new ContactServer();
		this.hasServerData = false;
	}

	public Contact(ContactServer contactServer){
		this.contactId = contactServer.getContactServerId();
		this.contactLocal = new ContactLocal();
		this.contactServer = contactServer;
		this.hasServerData = true;
	}


	// for stub only
	public Contact(String id, String name, String avatar, boolean online) {
		this.contactLocal = new ContactLocal();
		this.contactServer = new ContactServer();
		this.contactId = id;
		this.contactLocal.setDisplayName(name);
		this.contactServer.setProfilePicUrl(avatar);
		//this.online = online;
	}

	public ContactLocal getContactLocal() {
		return contactLocal;
	}

	public void setContactLocal(ContactLocal contactLocal) {
		this.contactLocal = contactLocal;
	}

    @Override
    public String toString() {
        return new ToStringBuilder(this).
            append("id", contactId).
			append("contactServer", contactServer.toString()).
            append("contactLocal", contactLocal.toString()).
            toString();
    }

	public void setContactServer(ContactServer contactServer) {
		this.contactServer = contactServer;
	}

	public ContactServer getContactServer() {
		return this.contactServer;
	}

	public boolean hasServerData() {
		return hasServerData;
	}

	public void setHasServerData(boolean hasServerData) {
		this.hasServerData = hasServerData;
	}


	@Override
	public String getId() {
		return contactId;
	}

	@Override
	public String getName() {
		return getDisplayName();
	}

	@Override
	public String getAvatar() {
		if ((contactServer != null)&& (contactServer.getProfilePicUrl() != null) && (!contactServer.getProfilePicUrl().equals("")))
			return contactServer.getProfilePicUrl();
		else
			return null;
	}

	public String getLocalOSId(){
		if (contactLocal != null)
			return contactLocal.getOSId();
		else
			return "0";
	}
	public String getDisplayName(){
		if ((contactLocal != null)&&(contactLocal.getDisplayName()!= null)&&(!contactLocal.getDisplayName().equals("")))
			return contactLocal.getDisplayName();
		else
			return contactServer.getUserName();
	}

}
