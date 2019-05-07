package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
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



	// for stub usage
	public Contact(String id, String lang, String name, String picUrl) {
		this.contactId = id;
		this.contactLocal = new ContactLocal();
		this.contactServer = new ContactServer();
		this.hasServerData = true;
		this.contactLocal.setDisplayName(name);
		this.contactServer.setUserName(name);
		this.contactServer.setLanguage(lang);
		this.contactServer.setProfilePicUrl(picUrl);
		this.contactServer.setContactServerId(id);
	}

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
		this.contactServer.setContactServerId(id);
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
		this.contactServer.setContactServerId(id);
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

	public String getLanguage() {
		if ((contactServer != null)&& (contactServer.getLanguage() != null) && (!contactServer.getLanguage().equals("")))
			return contactServer.getLanguage();
		else
			return null;
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
		else if (contactServer != null)
			return contactServer.getUserName();
		else return "";
	}

	public static String getInitialsFromName(String name){
    	if (name == null)
    		return "XX";
    	String capitalName = name.toUpperCase();
		String[] arr = capitalName.split(" ");
		if (arr.length >= 2){
			return arr[0].substring(0,1) + arr[1].substring(0,1);
		}
		else if (capitalName.length() >= 2)
			return capitalName.substring(0,2);
		else  if (capitalName.length() == 1)
			return capitalName.substring(0,1);
		else return "XX";
	}

	public String getInitials(){
    	String name  = getDisplayName();
    	return getInitialsFromName(name);
	}


	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static Contact fromJson(String json){
		Gson gson = new Gson();
		return gson.fromJson(json, Contact.class);
	}



	public String getNiceFormattedPhone(){
		String niceFormattedPhone = "+" + contactId;

		if ((contactLocal != null) &&
			(contactLocal.getPhoneNumIso() != null)&&
			(!contactLocal.getPhoneNumIso().isEmpty())){
			niceFormattedPhone = contactLocal.getPhoneNumIso();
		}
		else {
			if (contactServer != null) {
				String phoneNo = contactServer.getUserId();
				String localeCountry = contactServer.getCountryCode();
				Phonenumber.PhoneNumber ph = null;
				try {
					ph = PhoneNumberUtil.getInstance().parse(phoneNo, localeCountry);
					niceFormattedPhone = PhoneNumberUtil.getInstance().format(ph, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
				} catch (NumberParseException e) {
					e.printStackTrace();
				}
			}
		}

		return niceFormattedPhone;
	}

}
