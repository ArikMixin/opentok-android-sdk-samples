package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.wochat.app.db.entity.ContactLocal;
import io.wochat.app.db.entity.ContactServer;

@Entity(tableName = "contact_table")
public class Contact {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("id")
    @Expose
    private String id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
            append("id", id).
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
}
