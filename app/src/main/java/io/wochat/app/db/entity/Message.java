package io.wochat.app.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.text.BidiFormatter;
import android.text.Html;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.wochat.app.utils.Utils;

import static android.support.v4.text.TextDirectionHeuristicsCompat.LTR;


@Entity(tableName = "message_table",
	indices = {@Index("participant_id"), @Index("sender"), @Index("conversation_id")},
	foreignKeys = {
		@ForeignKey(entity = Conversation.class,
			parentColumns = "conversation_id",
			childColumns = "conversation_id",
			onDelete = ForeignKey.CASCADE),

		@ForeignKey(entity = Contact.class,
			parentColumns = "contact_id",
			childColumns = "participant_id",
			onDelete = ForeignKey.CASCADE),

		@ForeignKey(entity = Contact.class,
			parentColumns = "contact_id",
			childColumns = "sender",
			onDelete = ForeignKey.CASCADE)
	})


public class Message implements IMessage,
        						MessageContentType.Image, /*this is for default image messages implementation*/
								MessageContentType.Video,
								MessageContentType /*and this one is for custom content type (in this case - voice message)*/ {

    public static final String ACK_STATUS_PENDING = "PENDING";
    public static final String ACK_STATUS_SENT = "SENT";
    public static final String ACK_STATUS_RECEIVED = "RECEIVED";
    public static final String ACK_STATUS_READ = "READ";

    public static Comparator<String> getAckStatusComperator(){
    	return (status1, status2) -> getAckStatusValue(status1).compareTo(getAckStatusValue(status2));
	}

	public static Integer getAckStatusValue(@ACK_STATUS String ackStatus){
    	switch (ackStatus){
			case ACK_STATUS_PENDING: return 1;
			case ACK_STATUS_SENT: return 2;
			case ACK_STATUS_RECEIVED: return 3;
			case ACK_STATUS_READ: return 4;
			default: return 1;
		}
	}



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




	public static final String EVENT_CODE_GROUP_CREATED		= "GROUP_CREATED";
	public static final String EVENT_CODE_USER_ADDED		= "USER_ADDED";
	public static final String EVENT_CODE_USER_REMOVED		= "USER_REMOVED";
	public static final String EVENT_CODE_USER_LEFT			= "USER_LEFT";
	public static final String EVENT_CODE_MADE_ADMIN		= "MADE_ADMIN";
	public static final String EVENT_CODE_REMOVED_ADMIN		= "REMOVED_ADMIN";
	public static final String EVENT_CODE_ICON_CHANGED		= "ICON_CHANGED";
	public static final String EVENT_CODE_NAME_CHANGED		= "NAME_CHANGED";

	@StringDef({
		EVENT_CODE_GROUP_CREATED,
		EVENT_CODE_USER_ADDED,
		EVENT_CODE_USER_REMOVED,
		EVENT_CODE_USER_LEFT,
		EVENT_CODE_MADE_ADMIN,
		EVENT_CODE_REMOVED_ADMIN,
		EVENT_CODE_ICON_CHANGED,
		EVENT_CODE_NAME_CHANGED})

	@Retention(RetentionPolicy.SOURCE)
	public @interface EVENT_CODE {}


	public static final String SHOW_TRANSLATION_FALSE = "SHOW_TRANSLATION_FALSE";
	public static final String SHOW_TRANSLATION_TRUE = "SHOW_TRANSLATION_TRUE";
	public static final String SHOW_TRANSLATION_MAGIC = "SHOW_TRANSLATION_MAGIC";
	@StringDef({
		SHOW_TRANSLATION_FALSE,
		SHOW_TRANSLATION_TRUE,
		SHOW_TRANSLATION_MAGIC})

	@Retention(RetentionPolicy.SOURCE)
	public @interface SHOW_TRANSLATION_FLAG {}



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
	@ColumnInfo(name = "sender_name")
	@Expose
	private String senderName;
	/**********************************************/
	@ColumnInfo(name = "sender_color")
	@Expose
	private int senderColor;
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
	@SerializedName("groups")
	@ColumnInfo(name = "groups")
	@Expose
    private String[] groups;
	/**********************************************/
	@SerializedName("timestamp_milliseconds")
	@ColumnInfo(name = "timestamp_milli")
	@Expose
	private long timestampMilli;
	/**********************************************/
	@SerializedName("message_text")
	@ColumnInfo(name = "message_text")
	@Expose
    private String messageText;


	/**********************************************/
	@SerializedName("should_be_displayed")
	@ColumnInfo(name = "should_be_displayed")
	@Expose
	private boolean shouldBeDisplayed;


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
	@SerializedName("media_local_uri")
	@ColumnInfo(name = "media_local_uri")
	@Expose
	private String mediaLocalUri;


	/**********************************************/

	@SerializedName("media_thumbnail_local_uri")
	@ColumnInfo(name = "media_thumbnail_local_uri")
	@Expose
	private String mediaThumbnailLocalUri;

	/**********************************************/
	@SerializedName("duration")
	@ColumnInfo(name = "duration")
	@Expose
	private int duration;

	/**********************************************/
	@SerializedName("duration_mili")
	@ColumnInfo(name = "duration_mili")
	@Expose
	private int durationMili;
	/**********************************************/
	@Ignore
	private Contact contact;
	/**********************************************/
//	@Ignore
//	private Voice voice;
	/**********************************************/
	@SerializedName("original_message_id")
	@Expose
	@Ignore
	private String originalMessageId;


	/**********************************************/
	@SerializedName("show_non_translated")
	@Expose
	@Ignore
	private Boolean showNonTranslated;

	/**********************************************/
	@SerializedName("show_translation_flag")
	@Expose
	@Ignore
	private @SHOW_TRANSLATION_FLAG String showTranslationFlag;
	/**********************************************/

	/****************** for GroupEvent **********************/
	@SerializedName("event_code")
	@ColumnInfo(name = "event_code")
	@Expose
	private @EVENT_CODE String eventCode;
	/**********************************************/
	@SerializedName("acting_user")
	@ColumnInfo(name = "acting_user")
	@Expose
	private String actingUser;
	/**********************************************/
	@SerializedName("other_user")
	@ColumnInfo(name = "other_user")
	@Expose
	private String otherUser;
	/**********************************************/
	@SerializedName("group_id")
	@ColumnInfo(name = "group_id")
	@Expose
	private String groupId;
	/**********************************************/
	@SerializedName("group_name")
	@ColumnInfo(name = "group_name")
	@Expose
	private String groupName;
	/**********************************************/
	@ColumnInfo(name = "acting_user_name")
	@Expose
	private String actingUserName;
	/**********************************************/
	@ColumnInfo(name = "other_user_name")
	@Expose
	private String otherUserName;
	/**********************************************/


	// for outgoing message
	public Message(String participantId, String selfId, String conversationId, String messageText, String messageLang) {
		this.showNonTranslated = null;
		this.showTranslationFlag = null;
		this.messageId = UUID.randomUUID().toString();
		this.conversationId = conversationId;
		if (participantId != null) {
			this.participantId = participantId;
			this.recipients = new String[]{participantId};
			this.groups = new String[]{};
		}
		else {
			this.participantId = null;
			this.recipients = new String[]{};
			this.groups = new String[]{conversationId};
		}
		this.senderId = selfId;
		this.messageType = MSG_TYPE_TEXT;
		this.messageText = messageText;
		this.messageLanguage = messageLang;
		//this.timestamp = System.currentTimeMillis()/1000;
		this.timestampMilli = System.currentTimeMillis();
		this.ackStatus = ACK_STATUS_PENDING;
	}

	// for outgoing Image message
	public Message(String participantId, String selfId, String conversationId, String imageUrl, String thumbUrl, String messageLang) {
		this.showNonTranslated = null;
		this.showTranslationFlag = null;
		this.messageId = UUID.randomUUID().toString();
		this.conversationId = conversationId;
		this.participantId = participantId;
		this.recipients = new String[]{participantId};
		this.senderId = selfId;
		this.messageType = MSG_TYPE_IMAGE;
		this.mediaUrl = imageUrl;
		this.mediaThumbnailUrl = thumbUrl;
		this.messageLanguage = messageLang;
		//this.timestamp = System.currentTimeMillis()/1000;
		this.timestampMilli = System.currentTimeMillis();
		this.ackStatus = ACK_STATUS_PENDING;
	}

//	public Message(String participantId, String selfId, String conversationId, Uri localUri, String messageLang) {
//		this.showNonTranslated = null;
//		this.messageId = UUID.randomUUID().toString();
//		this.conversationId = conversationId;
//		this.participantId = participantId;
//		this.recipients = new String[]{participantId};
//		this.senderId = selfId;
//		this.messageType = MSG_TYPE_IMAGE;
//		this.mediaUrl = "";
//		this.mediaThumbnailUrl = "";
//		this.mediaLocalUri = localUri.toString();
//		this.messageLanguage = messageLang;
//		this.timestamp = System.currentTimeMillis()/1000;
//		this.ackStatus = ACK_STATUS_PENDING;
//	}


	public static Message CreateImageMessage(String participantId, String selfId, String conversationId, Uri localUri, String messageLang){
		Message message = new Message();
		message.showNonTranslated = null;
		message.showTranslationFlag = null;
		message.messageId = UUID.randomUUID().toString();
		message.conversationId = conversationId;
		message.participantId = participantId;
		message.recipients = new String[]{participantId};
		message.senderId = selfId;
		message.messageType = MSG_TYPE_IMAGE;
		message.mediaUrl = "";
		message.mediaThumbnailUrl = "";
		message.mediaLocalUri = localUri.toString();
		message.messageLanguage = messageLang;
		//message.timestamp = System.currentTimeMillis()/1000;
		message.timestampMilli = System.currentTimeMillis();
		message.ackStatus = ACK_STATUS_PENDING;
		return message;
	}

	public static Message CreateVideoMessage(String participantId, String selfId, String conversationId, Uri localMediaUri, Uri localThumbUri, String messageLang, int durationMili){
		Message message = new Message();
		message.showNonTranslated = null;
		message.showTranslationFlag = null;
		message.messageId = UUID.randomUUID().toString();
		message.conversationId = conversationId;
		message.participantId = participantId;
		message.recipients = new String[]{participantId};
		message.senderId = selfId;
		message.messageType = MSG_TYPE_VIDEO;
		message.mediaUrl = "";
		message.mediaThumbnailUrl = "";
		message.mediaLocalUri = localMediaUri.toString();
		message.durationMili = durationMili;
		message.duration = durationMili / 1000;
		message.mediaThumbnailLocalUri = localThumbUri.toString();
		message.messageLanguage = messageLang;
		//message.timestamp = System.currentTimeMillis()/1000;
		message.timestampMilli = System.currentTimeMillis();
		message.ackStatus = ACK_STATUS_PENDING;
		return message;
	}

	public static Message CreateAudioMessage(String participantId, String selfId, String conversationId, Uri localMediaUri, int durationMili){
		Message message = new Message();
		message.messageId = UUID.randomUUID().toString();
		message.conversationId = conversationId;
		message.participantId = participantId;
		message.recipients = new String[]{participantId};
		message.senderId = selfId;
		message.messageType = MSG_TYPE_AUDIO;
		message.mediaUrl = "";
		message.mediaThumbnailUrl = "";
		message.mediaLocalUri = localMediaUri.toString();
		message.durationMili = durationMili;
		message.duration = durationMili / 1000;

		//message.timestamp = System.currentTimeMillis()/1000;
		message.timestampMilli = System.currentTimeMillis();
		message.ackStatus = ACK_STATUS_PENDING;
		return message;
	}


	public Message() {
		showNonTranslated = null;
		showTranslationFlag = null;
	}

	// for stub messages fixtures
	public Message(String id, Contact contact, String text, String messageLang) {
        this(id, contact, text, messageLang, System.currentTimeMillis());
    }

	// for stub messages fixtures
    public Message(String id, Contact contact, String messageText, String messageLang, long timestamp) {
		this.showNonTranslated = null;
		this.showTranslationFlag = null;
        this.messageId = id;
        this.messageText = messageText;
		this.messageLanguage = messageLang;
		this.messageType = MSG_TYPE_TEXT;
        this.contact = contact;
		//this.senderId = selfId;
        //this.timestamp = timestamp;
		this.timestampMilli = timestamp;
        this.ackStatus = ACK_STATUS_PENDING;
    }

    @Override
    public String getId() {
        return messageId;
    }

	@Override
    public boolean isImage() {
		return messageType.equals(MSG_TYPE_IMAGE);
	}

	@Override
	public boolean isVideo() {
		return messageType.equals(MSG_TYPE_VIDEO);
	}

	@Override
	public boolean isAudio() {
		return messageType.equals(MSG_TYPE_AUDIO);
	}

	@Override
	public boolean isGif() {
		return messageType.equals(MSG_TYPE_GIF);
	}

	@Override
	public boolean isText() {
		return messageType.equals(MSG_TYPE_TEXT);
	}

	@Override
	public boolean isSpeechable() {
		return messageType.equals(MSG_TYPE_SPEECHABLE);
	}

	public boolean isGroupEvent() {
		return messageType.equals(MSG_TYPE_GROUP_EVENT);
	}


	public String getDisplayedLang(){
		switch (showTranslationFlag){
			case SHOW_TRANSLATION_MAGIC:
				return forceTranslatedLanguage;
			case SHOW_TRANSLATION_FALSE:
				return messageLanguage;
			case SHOW_TRANSLATION_TRUE:
				return translatedLanguage;
			default:
				return messageLanguage;
		}
	}

	@Override
	public String getTextWithNameHeader(){
		if (messageType.equals(MSG_TYPE_GROUP_EVENT)){
			return getText();
		}
		else if (isGroupMessage() && !isOutgoing()){
			String сolorString = Integer.toString(senderColor, 16);
			//String сolorString = String.format("%X", Color.GREEN).substring(2);
			//String title = "<font color=\"#%s\">" + Utils.getUserFirstName(getSenderName()) + "</font><BR>";
			String title = "<font color=\"#%s\">" + getSenderName() + "</font><BR>";
			//String title = "<font color='#EE0000'>" + Utils.getUserFirstName(getSenderName()) + "</font><BR>";
			return String.format(title + getText(), сolorString);

			//return Utils.getUserFirstName(getSenderName()) + "\n" + getText();
		}
		else {
			return getText();
		}

	}

	@Override
    public String getText() {
		if (messageType.equals(MSG_TYPE_GROUP_EVENT)){
			return getGroupEventMessage();
		}

		if (showTranslationFlag == null){
			if (isMagic())
				showTranslationFlag = SHOW_TRANSLATION_MAGIC;
			else if (isOutgoing())
				showTranslationFlag = SHOW_TRANSLATION_FALSE;
			else
				showTranslationFlag = SHOW_TRANSLATION_TRUE;
		}

		if (showTranslationFlag.equals(SHOW_TRANSLATION_MAGIC) && isMagic())
			return forceTranslatedText;
		else if (showTranslationFlag.equals(SHOW_TRANSLATION_FALSE))
			return messageText;
		else if (isTranslated())
			return translatedText;
		else
			return messageText;





//		if (showNonTranslated == null){
//			if (isOutgoing())
//				showNonTranslated = true;
//			else
//				showNonTranslated = false;
//		}

//		if (showNonTranslated)
//			return messageText;
//		else {
//			if ((translatedText != null) && (!translatedText.equals("")))
//				return translatedText;
//			else
//				return messageText;
//		}
    }


	public void userClickAction(){
		if (showTranslationFlag == null){
			if (isMagic())
				showTranslationFlag = SHOW_TRANSLATION_MAGIC;
			else if (isOutgoing())
				showTranslationFlag = SHOW_TRANSLATION_FALSE;
			else
				showTranslationFlag = SHOW_TRANSLATION_TRUE;
		}
		if (isMagic() && isTranslated()){
			if(showTranslationFlag.equals(SHOW_TRANSLATION_MAGIC))
				showTranslationFlag = SHOW_TRANSLATION_TRUE;
			else if(showTranslationFlag.equals(SHOW_TRANSLATION_TRUE))
				showTranslationFlag = SHOW_TRANSLATION_FALSE;
			else if(showTranslationFlag.equals(SHOW_TRANSLATION_FALSE))
				showTranslationFlag = SHOW_TRANSLATION_MAGIC;
		}
		else if (isTranslated()){ // only translated
			if(showTranslationFlag.equals(SHOW_TRANSLATION_TRUE))
				showTranslationFlag = SHOW_TRANSLATION_FALSE;
			else if(showTranslationFlag.equals(SHOW_TRANSLATION_FALSE))
				showTranslationFlag = SHOW_TRANSLATION_TRUE;
		}
		else if (isMagic()){ // only magic
			if(showTranslationFlag.equals(SHOW_TRANSLATION_MAGIC))
				showTranslationFlag = SHOW_TRANSLATION_FALSE;
			else if(showTranslationFlag.equals(SHOW_TRANSLATION_FALSE))
				showTranslationFlag = SHOW_TRANSLATION_MAGIC;
		}
		else {
			showTranslationFlag = SHOW_TRANSLATION_FALSE;
		}


	}




	@Override
    public Date getCreatedAt() {
        //return new Date(timestamp*1000);
		return new Date(timestampMilli);
    }


	@Override
    public Contact getContact() {
        return this.contact;
    }

    @Override
    public String getImageURL() {
        return mediaUrl;
    }


	@Nullable
	@Override
	public String getVideoURL() {
		return mediaUrl;
	}

	@Nullable
	@Override
	public String getThumbForDisplay() {
		if (isLocal())
			return mediaThumbnailLocalUri;
		else
			return mediaThumbnailUrl;

	}

	@Nullable
	@Override
	public String getVideoForDisplay() {
		if (isLocal())
			return mediaLocalUri;
		else
			return mediaThumbnailUrl;

	}



	@Override
	public boolean isLocal() {
		return (mediaThumbnailUrl == null) ||(mediaThumbnailUrl.equals(""));
	}


	@Nullable
	@Override
	public String getImageForDisplay() {
		if (isLocal()) {
			if ((mediaThumbnailLocalUri != null)&& (!mediaThumbnailLocalUri.equals("")))
				return mediaThumbnailLocalUri;
			else
				return mediaLocalUri;
		}
		else
			return mediaThumbnailUrl;
	}


	@Nullable
	@Override
	public String getImageLocal() {
		return mediaLocalUri;
	}

	@Nullable
	@Override
	public String getThumbLocal() {
		return mediaThumbnailLocalUri;
	}


	public String getImageThumbURL() {
		return mediaThumbnailUrl;
	}


//    public Voice getVoice() {
//        return voice;
//    }

    public @ACK_STATUS String getStatus() {
        return ackStatus;
    }

    public void setCreatedAt(long timestamp) {
        //this.timestamp = timestamp/1000;
		this.timestampMilli = timestamp;
    }

    public void setImage(Image image) {
        this.mediaUrl = image.url;
    }

//    public void setVoice(Voice voice) {
//        this.voice = voice;
//    }

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

	public void setMessageType(@NonNull @MSG_TYPE String messageType) {
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

	public String[] getGroups() {
		return groups;
	}

	public String getGroup() {
		if ((groups != null) && (groups.length > 0))
			return groups[0];
		else
			return null;
	}


	public void setGroups(String[] groups) {
		this.groups = groups;
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

	public void setRepliedMessageId(String repliedMessageId) {
		this.repliedMessageId = repliedMessageId;
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


	public int getDurationMili() {
		return durationMili;
	}

	public void setDurationMili(int durationMili) {
		this.durationMili = durationMili;
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

	public void hideMessageForTranslation(){
		shouldBeDisplayed = false;
	}

	public void displayMessageAfterTranslation(){
		shouldBeDisplayed = true;
	}


	public boolean isShouldBeDisplayed() {
		return shouldBeDisplayed;
	}

	public void setShouldBeDisplayed(boolean shouldBeDisplayed) {
		this.shouldBeDisplayed = shouldBeDisplayed;
	}

//	public boolean isShowNonTranslated() {
//		return showNonTranslated;
//	}
//
	public void showOriginalMessage() {
		showTranslationFlag = SHOW_TRANSLATION_FALSE;
	}




	public String getShowTranslationFlag() {
		return showTranslationFlag;
	}

	public void setShowTranslationFlag(String showTranslationFlag) {
		this.showTranslationFlag = showTranslationFlag;
	}


	public String getMediaLocalUri() {
		return mediaLocalUri;
	}
	public Uri getMediaLocalParseUri() {
		if ((mediaLocalUri != null)&& (!mediaLocalUri.isEmpty()))
			return Uri.parse(mediaLocalUri);
		else
			return null;
	}
	public Uri getMediaParseUri() {
		if ((mediaUrl != null)&& (!mediaUrl.isEmpty()))
			return Uri.parse(mediaUrl);
		else
			return null;
	}

	public String getMediaThumbnailLocalUri() {
		return mediaThumbnailLocalUri;
	}

	public void setMediaThumbnailLocalUri(String mediaThumbnailLocalUri) {
		this.mediaThumbnailLocalUri = mediaThumbnailLocalUri;
	}


	public void setMediaLocalUri(String mediaLocalUri) {
		this.mediaLocalUri = mediaLocalUri;
	}



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

	public long getTimestampMilli() {
		return timestampMilli;
	}

	public void setTimestampMilli(long timestampMilli) {
		this.timestampMilli = timestampMilli;
	}


	public boolean isOutgoing(){
		return (participantId == null)||(!participantId.equals(senderId));
	}


	public Message generateForwardMessage(String selfId, String participantId, String participantLanguage){
		Message newMessage = fromJson(toJson());
		newMessage.messageId = UUID.randomUUID().toString();
		newMessage.participantId = participantId;
		newMessage.senderId = selfId;
		newMessage.conversationId = Conversation.getConversationId(participantId, selfId);
		newMessage.recipients = new String[]{participantId};
		//newMessage.timestamp = System.currentTimeMillis()/1000;
		newMessage.timestampMilli = System.currentTimeMillis();
		newMessage.ackStatus = ACK_STATUS_PENDING;
		newMessage.translatedLanguage = participantLanguage; // in order to invoke translation to the new participant language

		if (selfId.equals(senderId)){ // outgoing - remove translation
			newMessage.translatedText = null;
		}
		else { // incoming - check if translated
			if ((translatedText != null) && (!translatedText.equals(""))){
				newMessage.messageText = translatedText;  // take the text that was translated to me and put it as the message text
				newMessage.messageLanguage = translatedLanguage; // my language
			}
		}

		return newMessage;
	}

	public Message generateForwardMessageForGroup(String selfId, String groupId, String groupName, String selfLang){
		Message newMessage = fromJson(toJson());
		newMessage.messageId = UUID.randomUUID().toString();
		newMessage.groupId = groupId;
		newMessage.groupName = groupName;
		newMessage.participantId = null;
		newMessage.senderId = selfId;
		newMessage.conversationId = groupId;
		newMessage.recipients = new String[]{};
		newMessage.groups = new String[]{groupId};
		//newMessage.timestamp = System.currentTimeMillis()/1000;
		newMessage.timestampMilli = System.currentTimeMillis();
		newMessage.ackStatus = ACK_STATUS_PENDING;
		newMessage.translatedLanguage = null; // in order to invoke translation to the new participant language
		newMessage.messageLanguage = selfLang;
		if (selfId.equals(senderId)){ // outgoing - remove translation
			newMessage.translatedText = null;
		}
		else { // incoming - check if translated
			if ((translatedText != null) && (!translatedText.equals(""))){
				newMessage.messageText = translatedText;  // take the text that was translated to me and put it as the message text
				newMessage.messageLanguage = translatedLanguage; // my language
			}
		}

		return newMessage;
	}


	private String getGroupEventMessage() {
		if (!messageType.equals(MSG_TYPE_GROUP_EVENT))
			return "";

		String res = "";
		switch (eventCode){
			case EVENT_CODE_GROUP_CREATED:
				res = actingUserName + " created group " + groupName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_USER_ADDED:
				res = actingUserName + " added " + otherUserName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_USER_REMOVED:
				res = actingUserName + " removed " + otherUserName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_USER_LEFT:
				res = actingUserName + " left the group";
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_MADE_ADMIN:
				if ("You".equalsIgnoreCase(otherUserName))
					res = "You are now an admin";
				else {
					res = otherUserName + " is now an admin";
					res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				}
				break;
			case Message.EVENT_CODE_REMOVED_ADMIN:
				if ("You".equalsIgnoreCase(otherUserName))
					res = "You are no longer an admin";
				else {
					res = otherUserName + " is no longer an admin";
					res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				}
				break;
			case Message.EVENT_CODE_ICON_CHANGED:
				res = actingUserName + " changed group icon";
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_NAME_CHANGED:
				res = actingUserName + " changed group name to " + groupName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
		}
		return res;
	}

	public String getGroupEventNotificationMessage() {
		if (!messageType.equals(MSG_TYPE_GROUP_EVENT))
			return "";

		String res = "";
		switch (eventCode){
			case EVENT_CODE_GROUP_CREATED:
				res = actingUserName + " added you to group " + groupName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_USER_ADDED:
				if ("You".equalsIgnoreCase(otherUserName))
					res = actingUserName + " added you to group " + groupName;
				else
					res = actingUserName + " added " + otherUserName + " to group " + groupName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_USER_REMOVED:
				if ("You".equalsIgnoreCase(otherUserName))
					res = actingUserName + " removed you from group " + groupName;
				else
					res = actingUserName + " removed " + otherUserName + " from group " + groupName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_USER_LEFT:
				res = actingUserName + " has left the group " + groupName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_MADE_ADMIN:
				if ("You".equalsIgnoreCase(otherUserName))
					res = actingUserName + " made you administrator in group " + groupName;
				else {
					res = actingUserName + " made " + otherUserName + " administrator in group " + groupName;
					res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				}
				break;
			case Message.EVENT_CODE_REMOVED_ADMIN:
				if ("You".equalsIgnoreCase(otherUserName))
					res = actingUserName + " dismissed you as administrator in group " + groupName;
				else {
					res = actingUserName + " dismissed " + otherUserName + " as administrator in group " + groupName;
					res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				}
				break;
			case Message.EVENT_CODE_ICON_CHANGED:
				res = actingUserName + " changed group icon of group " + groupName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
			case Message.EVENT_CODE_NAME_CHANGED:
				res = actingUserName + " changed group name to " + groupName;
				res = BidiFormatter.getInstance().unicodeWrap(res, LTR, true);
				break;
		}
		return res;
	}



	public Boolean getShowNonTranslated() {
		return showNonTranslated;
	}

	public void setShowNonTranslated(Boolean showNonTranslated) {
		this.showNonTranslated = showNonTranslated;
	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}


	public boolean isMagic(){
		return (forceTranslatedLanguage != null)&& (!forceTranslatedLanguage.isEmpty());
	}

	public boolean isTranslated(){
		return (translatedText != null) && (!translatedText.isEmpty());
	}

	public String getActingUser() {
		return actingUser;
	}

	public void setActingUser(String actingUser) {
		this.actingUser = actingUser;
	}

	public String getOtherUser() {
		return otherUser;
	}

	public void setOtherUser(String otherUser) {
		this.otherUser = otherUser;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	public boolean isGroupMessage(){
		return (this.groups != null) && (this.groups.length > 0);
	}

	public String getFirstGroupId(){
		return isGroupMessage()? this.groups[0]:null;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public int getSenderColor() {
		return senderColor;
	}

	public void setSenderColor(int senderColor) {
		this.senderColor = senderColor;
	}

	public String getActingUserName() {
		return actingUserName;
	}

	public void setActingUserName(String actingUserName) {
		this.actingUserName = actingUserName;
	}

	public String getOtherUserName() {
		return otherUserName;
	}

	public void setOtherUserName(String otherUserName) {
		this.otherUserName = otherUserName;
	}


	public static Message getGroupCreatedMessage(Message userAddedMessage){
		Message newMessage = fromJson(userAddedMessage.toJson());
		newMessage.setMessageId(UUID.randomUUID().toString());
		newMessage.setEventCode(EVENT_CODE_GROUP_CREATED);
		newMessage.setTimestampMilli(userAddedMessage.getTimestampMilli()-10);
		return newMessage;
	}


	public static Message getGroupCreatedMessageSelf(String conversationId, String groupName, String selfId) {
		Message newMessage = new Message();
		newMessage.setConversationId(conversationId);
		newMessage.groups = new String[]{conversationId};
		newMessage.setSenderId(selfId);
		newMessage.setActingUser(selfId);
		newMessage.setActingUserName("You");
		newMessage.setShouldBeDisplayed(true);
		newMessage.setGroupName(groupName);
		newMessage.setMessageType(MSG_TYPE_GROUP_EVENT);
		newMessage.setMessageId(UUID.randomUUID().toString());
		newMessage.setEventCode(EVENT_CODE_GROUP_CREATED);
		newMessage.setTimestampMilli(System.currentTimeMillis());
		return newMessage;
	}

}
