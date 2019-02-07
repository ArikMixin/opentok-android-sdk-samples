package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = "message_table",
	indices = {@Index("participant_id"), @Index("sender"), @Index("conversation_id")},
	foreignKeys = {
		@ForeignKey(entity = Conversation.class,
			parentColumns = "conversation_id",
			childColumns = "conversation_id",
			onDelete = CASCADE),

		@ForeignKey(entity = Contact.class,
			parentColumns = "contact_id",
			childColumns = "participant_id",
			onDelete = CASCADE),

		@ForeignKey(entity = Contact.class,
			parentColumns = "contact_id",
			childColumns = "sender",
			onDelete = CASCADE)
	})


public class Message implements IMessage,
        						MessageContentType.Image, /*this is for default image messages implementation*/
								MessageContentType /*and this one is for custom content type (in this case - voice message)*/ {

    public static final String ACK_STATUS_PENDING = "PENDING";
    public static final String ACK_STATUS_SENT = "SENT";
    public static final String ACK_STATUS_RECEIVED = "RECEIVED";
    public static final String ACK_STATUS_READ = "READ";

	@StringDef({
		ACK_STATUS_PENDING,
		ACK_STATUS_SENT,
		ACK_STATUS_RECEIVED,
		ACK_STATUS_READ})

	@Retention(RetentionPolicy.SOURCE)
	public @interface ACK_STATUS {}



	public static final String MSG_TYPE_ACKNOWLEDGMENT = "ACKNOWLEGMENT";
	public static final String MSG_TYPE_TEXT = "TEXT";
	public static final String MSG_TYPE_IMAGE = "IMAGE";
	public static final String MSG_TYPE_VIDEO = "VIDEO";
	public static final String MSG_TYPE_AUDIO = "AUDIO";
	public static final String MSG_TYPE_GIF = "GIF";
	public static final String MSG_TYPE_TYPING_SIGNAL = "TYPING_SIGNAL";
	public static final String MSG_TYPE_GROUP_EVENT = "GROUP_EVENT";
	public static final String MSG_TYPE_SPEECHABLE = "SPEECHABLE";
	public static final String MSG_TYPE_LOCATION = "LOCATION";
	public static final String MSG_TYPE_MEETING_INVITE = "MEETING_INVITE";
	public static final String MSG_TYPE_WEBRTC_CALL = "WEBRTC_CALL";
	public static final String MSG_TYPE_CALL_EVENT = "CALL_EVENT";
	public static final String MSG_TYPE_CONTACT = "CONTACT";


	@StringDef({
		MSG_TYPE_ACKNOWLEDGMENT,
		MSG_TYPE_TEXT,
		MSG_TYPE_IMAGE,
		MSG_TYPE_VIDEO,
		MSG_TYPE_AUDIO,
		MSG_TYPE_GIF,
		MSG_TYPE_TYPING_SIGNAL,
		MSG_TYPE_GROUP_EVENT,
		MSG_TYPE_SPEECHABLE,
		MSG_TYPE_LOCATION,
		MSG_TYPE_MEETING_INVITE,
		MSG_TYPE_WEBRTC_CALL,
		MSG_TYPE_CALL_EVENT,
		MSG_TYPE_CONTACT})

	@Retention(RetentionPolicy.SOURCE)
	public @interface MSG_TYPE {}




	public static final String EVENT_CODE_USER_ADDED = "USER_ADDED";
	public static final String EVENT_CODE_USER_REMOVED = "USER_REMOVED";
	public static final String EVENT_CODE_USER_LEFT = "USER_LEFT";
	public static final String EVENT_CODE_MADE_ADMIN = "MADE_ADMIN";
	public static final String EVENT_CODE_REMOVED_ADMIN = "REMOVED_ADMIN";
	public static final String EVENT_CODE_ICON_CHANGED = "ICON_CHANGED";
	public static final String EVENT_CODE_NAME_CHANGED = "NAME_CHANGED";

	@StringDef({
		EVENT_CODE_USER_ADDED,
		EVENT_CODE_USER_REMOVED,
		EVENT_CODE_USER_LEFT,
		EVENT_CODE_MADE_ADMIN,
		EVENT_CODE_REMOVED_ADMIN,
		EVENT_CODE_ICON_CHANGED,
		EVENT_CODE_NAME_CHANGED})

	@Retention(RetentionPolicy.SOURCE)
	public @interface EVENT_CODE {}

	/**********************************************/
	@PrimaryKey
	@NonNull
	@SerializedName("id")
	@ColumnInfo(name = "message_id")
	@Expose
    private String messageId;
	/**********************************************/
	@NonNull
	@SerializedName("message_type")
	@ColumnInfo(name = "message_type")
	@Expose
    private @MSG_TYPE String messageType;
	/**********************************************/
	@SerializedName("participant_id")
	@ColumnInfo(name = "participant_id")
	@Expose
	private String participantId;
	/**********************************************/
	@SerializedName("sender")
	@ColumnInfo(name = "sender")
	@Expose
    private String senderId;
	/**********************************************/
	@SerializedName("conversation_id")
	@ColumnInfo(name = "conversation_id")
	@Expose
	private String conversationId;
	/**********************************************/
	@SerializedName("recipients")
	@ColumnInfo(name = "recipients")
	@Expose
    private String[] recipients;
	/**********************************************/
//	@SerializedName("groups")
//	@ColumnInfo(name = "groups")
//	@Expose
//    private String[] groups;
	/**********************************************/
	@SerializedName("timestamp")
	@ColumnInfo(name = "timestamp")
	@Expose
	private long timestamp;
	/**********************************************/
	@SerializedName("message_text")
	@ColumnInfo(name = "message_text")
	@Expose
    private String messageText;


	/**********************************************/

	@SerializedName("message_language")
	@ColumnInfo(name = "message_language")
	@Expose
	private String messageLanguage;
	/**********************************************/
	@SerializedName("translated_text")
	@ColumnInfo(name = "translated_text")
	@Expose
	private String translatedText;
	/**********************************************/
	@SerializedName("translated_language")
	@ColumnInfo(name = "translated_language")
	@Expose
	private String translatedLanguage;
	/**********************************************/
	@SerializedName("is_incognito")
	@ColumnInfo(name = "is_incognito")
	@Expose
	private boolean isIncognito;
	/**********************************************/
	@SerializedName("force_translated_text")
	@ColumnInfo(name = "force_translated_text")
	@Expose
	private String forceTranslatedText;
	/**********************************************/
	@SerializedName("force_translated_language")
	@ColumnInfo(name = "force_translated_language")
	@Expose
	private String forceTranslatedLanguage;
	/**********************************************/
	@SerializedName("force_translated_country")
	@ColumnInfo(name = "force_translated_country")
	@Expose
	private String forceTranslatedCountry;

	/**********************************************/
	@SerializedName("replied_message_id")
	@ColumnInfo(name = "replied_message_id")
	@Expose
	private String repliedMessageId;
	/**********************************************/
	@SerializedName("hitshot_id")
	@ColumnInfo(name = "hitshot_id")
	@Expose
	private int hitshotId;
	/**********************************************/
	@SerializedName("is_typing")
	@ColumnInfo(name = "is_typing")
	@Expose
    private boolean isTyping;
	/**********************************************/
	@SerializedName("ack_status")
	@ColumnInfo(name = "ack_status")
	@Expose
	private @ACK_STATUS
	String ackStatus;
	/**********************************************/
	@SerializedName("media_url")
	@ColumnInfo(name = "media_url")
	@Expose
	private String mediaUrl;
	/**********************************************/
	@SerializedName("media_thumbnail_url")
	@ColumnInfo(name = "media_thumbnail_url")
	@Expose
	private String mediaThumbnailUrl;



	/**********************************************/
	@SerializedName("duration")
	@ColumnInfo(name = "duration")
	@Expose
	private int duration;
	/**********************************************/
	@Ignore
	private Contact contact;
	/**********************************************/
	@Ignore
	private Voice voice;
	/**********************************************/
	@SerializedName("original_message_id")
	@Expose
	@Ignore
	private String originalMessageId;
	/**********************************************/

//	private Image image;
//  private int status;
//  private String text;
//  private Date createdAt;


	// for outgoing message
	public Message(String participantId, String selfId, String conversationId, String messageText, String messageLang) {
		this.messageId = UUID.randomUUID().toString();
		this.conversationId = conversationId;
		this.participantId = participantId;
		this.recipients = new String[]{participantId};
		this.senderId = selfId;
		this.messageType = MSG_TYPE_TEXT;
		this.messageText = messageText;
		this.messageLanguage = messageLang;
		this.timestamp = System.currentTimeMillis()/1000;
		this.ackStatus = ACK_STATUS_PENDING;
	}

	public Message() {
	}

	public Message(String id, Contact contact, String text, String messageLang) {
        this(id, contact, text, messageLang, System.currentTimeMillis());
    }

    public Message(String id, Contact contact, String messageText, String messageLang, long timestamp) {
        this.messageId = id;
        this.messageText = messageText;
		this.messageLanguage = messageLang;
		this.messageType = MSG_TYPE_TEXT;
        this.contact = contact;
		//this.senderId = selfId;
        this.timestamp = timestamp;
        this.ackStatus = ACK_STATUS_PENDING;
    }

    @Override
    public String getId() {
        return messageId;
    }

    @Override
    public String getText() {
        return messageText;
    }

    @Override
    public Date getCreatedAt() {
        return new Date(timestamp*1000);
    }

    @Override
    public Contact getContact() {
        return this.contact;
    }

    @Override
    public String getImageUrl() {
        return mediaUrl;
    }

    public Voice getVoice() {
        return voice;
    }

    public @ACK_STATUS String getStatus() {
        return ackStatus;
    }

    public void setCreatedAt(long timestamp) {
        this.timestamp = timestamp/1000;
    }

    public void setImage(Image image) {
        this.mediaUrl = image.url;
    }

    public void setVoice(Voice voice) {
        this.voice = voice;
    }

    public static class Image {

        private String url;

        public Image(String url) {
            this.url = url;
        }
    }

    public static class Voice {

        private String url;
        private int duration;

        public Voice(String url, int duration) {
            this.url = url;
            this.duration = duration;
        }

        public String getUrl() {
            return url;
        }

        public int getDuration() {
            return duration;
        }
    }


	@NonNull
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(@NonNull String messageId) {
		this.messageId = messageId;
	}

	@NonNull
	public @MSG_TYPE String getMessageType() {
		return messageType;
	}

	public void setMessageType(@NonNull String messageType) {
		this.messageType = messageType;
	}

	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String sender) {
		this.senderId = sender;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String[] getRecipients() {
		return recipients;
	}

	public void setRecipients(String[] recipients) {
		this.recipients = recipients;
	}

//	public String[] getGroups() {
//		return groups;
//	}
//
//	public void setGroups(String[] groups) {
//		this.groups = groups;
//	}

	public long getTimestamp() {
		return timestamp;
	}

//	public long getTimestampInSec() {
//		return timestamp*1000;
//	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getRepliedMessageId() {
		return repliedMessageId;
	}

	public void setRepliedMessageId(String replied_message_id) {
		this.repliedMessageId = replied_message_id;
	}

	public int getHitshotId() {
		return hitshotId;
	}

	public void setHitshotId(int hitshotId) {
		this.hitshotId = hitshotId;
	}

	public boolean isTyping() {
		return isTyping;
	}

	public void setIsTyping(boolean b) {
		this.isTyping = b;
	}

	public String getAckStatus() {
		return ackStatus;
	}

	public void setAckStatus(String ackStatus) {
		this.ackStatus = ackStatus;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getMediaThumbnailUrl() {
		return mediaThumbnailUrl;
	}

	public void setMediaThumbnailUrl(String mediaThumbnailUrl) {
		this.mediaThumbnailUrl = mediaThumbnailUrl;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	@Override
	public String getAuthorId() {
		return senderId;
	}

	public void setOriginalMessageId(String originalMessageId) {
		this.originalMessageId = originalMessageId;
	}

	public String getOriginalMessageId() {
		return this.originalMessageId;
	}

	public String getMessageLanguage() {
		return messageLanguage;
	}

	public void setMessageLanguage(String messageLanguage) {
		this.messageLanguage = messageLanguage;
	}

	public String getTranslatedText() {
		return translatedText;
	}

	public void setTranslatedText(String translatedText) {
		this.translatedText = translatedText;
	}

	public String getTranslatedLanguage() {
		return translatedLanguage;
	}

	public void setTranslatedLanguage(String translatedLanguage) {
		this.translatedLanguage = translatedLanguage;
	}

	public boolean isIncognito() {
		return isIncognito;
	}

	public void setIncognito(boolean incognito) {
		isIncognito = incognito;
	}

	public String getForceTranslatedText() {
		return forceTranslatedText;
	}

	public void setForceTranslatedText(String forceTranslatedText) {
		this.forceTranslatedText = forceTranslatedText;
	}

	public String getForceTranslatedLanguage() {
		return forceTranslatedLanguage;
	}

	public void setForceTranslatedLanguage(String forceTranslatedLanguage) {
		this.forceTranslatedLanguage = forceTranslatedLanguage;
	}

	public String getForceTranslatedCountry() {
		return forceTranslatedCountry;
	}

	public void setForceTranslatedCountry(String forceTranslatedCountry) {
		this.forceTranslatedCountry = forceTranslatedCountry;
	}



//	@Override
//	public boolean isOutcoming() {
//		return (!senderId.equals(participantId));
//	}

	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static Message fromJson(String json){
		try {
			Gson gson = new Gson();
			return gson.fromJson(json, Message.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}



}
