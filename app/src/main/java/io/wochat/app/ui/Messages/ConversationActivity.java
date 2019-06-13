package io.wochat.app.ui.Messages;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.SpeechRecognizer;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.wochat.app.R;
import io.wochat.app.WCService;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.components.MessageReplyLayout;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.GroupMember;
import io.wochat.app.db.entity.Message;
import io.wochat.app.model.ContactOrGroup;
import io.wochat.app.model.SupportedLanguage;
import io.wochat.app.ui.Consts;
import io.wochat.app.ui.Contact.ContactGroupsMultiSelectorActivity;
import io.wochat.app.ui.Contact.ContactMultiSelectorActivity;
import io.wochat.app.ui.ContactInfo.ContactInfoActivity;
import io.wochat.app.ui.Group.GroupInfoActivity;
import io.wochat.app.ui.Languages.LanguageSelectorDialog;
import io.wochat.app.ui.MainActivity;
import io.wochat.app.ui.PermissionActivity;
import io.wochat.app.utils.ImagePickerUtil;
import io.wochat.app.utils.SpeechToTextUtil;
//import io.wochat.app.utils.SpeechUtils;
import io.wochat.app.utils.Utils;
import io.wochat.app.utils.videocompression.MediaController;
import io.wochat.app.viewmodel.ContactViewModel;
import io.wochat.app.viewmodel.ConversationViewModel;
import io.wochat.app.viewmodel.GroupViewModel;
import io.wochat.app.viewmodel.SupportedLanguagesViewModel;

//import com.stfalcon.chatkit.utils.DateFormatter;



public class ConversationActivity extends PermissionActivity implements
	MessagesListAdapter.OnMessageLongClickListener<Message>,
	MessagesListAdapter.OnLoadMoreListener,
	MessageInput.InputListener,
	MessagesListAdapter.OnMessageViewLongClickListener<Message>,
	MessagesListAdapter.OnMessageViewClickListener<Message>,
	MessagesListAdapter.OnMessageClickListener<Message>,
	MessagesListAdapter.OnBindViewHolder,
	MessageInput.TypingListener,
	DateFormatter.Formatter,
	MessageInput.ButtonClickListener,
	MessageHolders.ContentChecker<Message>,
	MessagesListAdapter.SelectionListener,
	SpeechToTextUtil.SpeechUtilsSTTListener,
	MessagesListAdapter.OnMessageForwardListener<Message>,
	View.OnClickListener {

	private static final String TAG = "ConversationActivity";
	private static final int REQUEST_SELECT_IMAGE_VIDEO 	= 1;
	private static final int REQUEST_SELECT_CAMERA_PHOTO 	= 2;
	private static final int REQUEST_SELECT_CAMERA_VIDEO 	= 3;
	private static final int REQUEST_SELECT_CONTACTS 		= 4;
	private static final int REQUEST_SELECT_CONTACTS_W_MSG	= 5;
	private MessagesList mMessagesListRV;
	protected MessagesListAdapter<Message> mMessagesAdapter;
	protected ImageLoader mImageLoader;
	private Date pointerDateUpperList;
	private Date pointerDateBottomList;
	private ImageButton mScrollToEndIB;
	private CircleFlagImageView mContactAvatarCIV;
	private TextView mContactNameTV;
	private MessageInput mMessageInput;
	private ConversationViewModel mConversationViewModel;
	private String mParticipantId;
	private String mParticipantName;
	private String mParticipantPic;
	private String mConversationId;
	private String mSelfId;
	private List<Message> mMessages;
	private int xDelta;
	private int yDelta;
	private float oldX;
	private float oldY;
	private WCService mService;
	private boolean mBound;
	private Conversation mConversation;
	//private Contact mParticipantContactObj;
	private String mParticipantLang;
	private String mSelfLang;
	private TypingSignalBR mTypingSignalBR;
	private TextView mContactDetailsTV;
	private Handler mClearTypingHandler;
	private boolean mIsOnline;
	private long mLastOnlineTime;
	private PhotoView mPreviewImagesIV;
	private Uri mSelectedImageForDelayHandlingUri;
	private ProgressBar mPreviewImagesPB;
	private Uri mCameraPhotoFileUri;
	private ProgressDialog mProgressDialog;
	private File mCameraVideoMediaForDelayHandlingFile;
	private File mCameraVideoThumbForDelayHandlingFile;
	private int mCameraVideoDurationForDelayHandling;
	private ImageView mRecordingBigIV;
	private boolean mIsInputInTextMode;
	private MediaRecorder mRecorder;
	private File mAudioFile;
	private long mRecorderStartTimeStamp;
	private String mSelfPicUrl;
	private boolean mSameLanguageWithParticipant;
	private SpeechRecognizer mSpeechRecognizer;
	private Intent mSpeechRecognizerIntent;
	//private SpeechUtils mSpeechUtils;
	private String mSelfName;
	private Contact mSelfContact;
	private Contact mParticipantContact;
	private boolean mIsInMsgSelectionMode;
	private int mSelectedMessageCount;
	private ContactOrGroup mForwardContactOrGroupSingle;
	private MessageReplyLayout mInputMessageReplyLayout;
	private boolean mClickedFromNotifivation;
	private SupportedLanguagesViewModel mSupportedLanguagesViewModel;
	private List<SupportedLanguage> mSupportedLanguages;
	private String mMagicButtonForceLanguage;
	private String mMagicButtonForceCountry;
	private ContactViewModel mContactViewModel;
	private boolean mIsGroup;
	private List<GroupMember> mGroupMembers;
	private GroupViewModel mGroupViewModel;
	private TextView mNotInGroupMsgTV;
	private boolean mIsFromPushNotifivation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mImageLoader = new ImageLoader() {
			@Override
			public void loadImageWPlaceholder(ImageView imageView, String url, int placeholderResourceId, Object payload) {
				if ((url != null)&& (url.equals("")))
					url = null;
				//Picasso.get().load(url).placeholder(R.drawable.ic_new_contact_invert).error(R.drawable.ic_new_contact_invert).into(imageView);
				Picasso.get().load(url).placeholder(placeholderResourceId).error(placeholderResourceId).into(imageView);
			}

			@Override
			public void loadImageCenterCrop(ImageView imageView, @Nullable String url, @Nullable Object payload) {
				Picasso.get().load(url).resize(300,300).centerCrop().into(imageView);
			}

			@Override
			public void loadImageCenter(ImageView imageView, @Nullable String url, int placeholderResourceId, @Nullable Object payload) {
				if (placeholderResourceId != 0)
					Picasso.get().load(url).placeholder(placeholderResourceId).into(imageView);
				else
					Picasso.get().load(url).into(imageView);
			}

			@Override
			public void loadImageNoPlaceholder(ImageView imageView, int resourceId) {
				Picasso.get().load(resourceId).into(imageView);
			}
		};

		setContentView(R.layout.activity_conversation);


		mNotInGroupMsgTV = (TextView)findViewById(R.id.not_in_group_msg_tv);
		mNotInGroupMsgTV.setVisibility(View.GONE);

		mScrollToEndIB = (ImageButton)findViewById(R.id.scroll_end_ib);
		mScrollToEndIB.setVisibility(View.INVISIBLE);
		mScrollToEndIB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mMessagesListRV.getLayoutManager().smoothScrollToPosition(mMessagesListRV, null, 0);
				mScrollToEndIB.setVisibility(View.INVISIBLE);
			}
		});

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//getActionBar().setHomeButtonEnabled(true);


		mParticipantId = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_ID);
		mParticipantName = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
		mParticipantLang = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
		mParticipantPic = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
		mConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
		String conversationString = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_OBJ);
		if (Utils.isNotNullAndNotEmpty(conversationString)){
			mConversation = Conversation.fromJson(conversationString);
			mIsGroup = mConversation.isGroup();
			if (mIsGroup){
				mParticipantPic = mConversation.getGroupImageUrl();
				mParticipantName = mConversation.getGroupName();
				mParticipantLang = null;
				mParticipantId = null;
			}
		}

		mSelfId = getIntent().getStringExtra(Consts.INTENT_SELF_ID);
		mSelfLang = getIntent().getStringExtra(Consts.INTENT_SELF_LANG);
		mSelfName = getIntent().getStringExtra(Consts.INTENT_SELF_NAME);
		mSelfPicUrl = getIntent().getStringExtra(Consts.INTENT_SELF_PIC_URL);


		if(getIntent().hasExtra(Consts.INTENT_CLICK_FROM_NOTIFICATION)){
			mClickedFromNotifivation = getIntent().getBooleanExtra(Consts.INTENT_CLICK_FROM_NOTIFICATION, false);
		}

		if(getIntent().hasExtra(Consts.INTENT_IS_FROM_PUSH_NOTIFICATION)){
			mIsFromPushNotifivation = getIntent().getBooleanExtra(Consts.INTENT_IS_FROM_PUSH_NOTIFICATION, false);
		}


		mSelfContact = new Contact(mSelfId, mSelfLang, mSelfName, mSelfPicUrl);
		mParticipantContact = new Contact(mParticipantId, mParticipantLang, mParticipantName, mParticipantPic);

		mContactNameTV = (TextView) findViewById(R.id.contact_name_tv);
		mContactDetailsTV = (TextView) findViewById(R.id.contact_details_tv);
		mContactDetailsTV.setText("");
		mContactDetailsTV.setOnClickListener(this);
		mContactNameTV.setText(mParticipantName);
		mContactNameTV.setOnClickListener(this);



		if ((Utils.isHebrew(mParticipantLang)) && (Utils.isHebrew(mSelfLang)))
			mSameLanguageWithParticipant = true;
		else
			mSameLanguageWithParticipant = (mParticipantLang == null) || (mParticipantLang.equals(mSelfLang));

		mContactAvatarCIV = (CircleFlagImageView) findViewById(R.id.contact_avatar_civ);
		mContactAvatarCIV.setInfo(mParticipantPic, mParticipantLang, Contact.getInitialsFromName(mParticipantName));
		//mImageLoader.loadImage(mContactAvatarCIV.get, mParticipantPic, null);
		mContactAvatarCIV.setOnClickListener(v -> {
			//Utils.showImage(ConversationActivity.this, mParticipantPic);
			mPreviewImagesIV.setVisibility(View.VISIBLE);
			mPreviewImagesPB.setVisibility(View.VISIBLE);
			Picasso.get().load(mParticipantPic).into(mPreviewImagesIV, new Callback() {
				@Override
				public void onSuccess() {
					mPreviewImagesPB.setVisibility(View.GONE);
				}

				@Override
				public void onError(Exception e) {
					mPreviewImagesPB.setVisibility(View.GONE);
				}
			});

		});

		mMessagesListRV = (MessagesList) findViewById(R.id.messagesList);
		mInputMessageReplyLayout = (MessageReplyLayout) findViewById(R.id.input_reply_message_layout);
		mInputMessageReplyLayout.setVisibility(View.GONE);
		mInputMessageReplyLayout.setOnCloseListener(v -> {
			mMessagesAdapter.unselectAllItems();
		});

		mPreviewImagesIV = (PhotoView)findViewById(R.id.preview_iv);
		mPreviewImagesPB = (ProgressBar)findViewById(R.id.preview_pb);
		mPreviewImagesIV.setMaximumScale(5.0f);
		mPreviewImagesIV.setOnClickListener(v -> {
			mPreviewImagesIV.setVisibility(View.GONE);
		});
		mPreviewImagesIV.setVisibility(View.GONE);
		mPreviewImagesPB.setVisibility(View.GONE);

		initAdapter();

		mMessageInput = (MessageInput) findViewById(R.id.input);
		mMessageInput.setTypingListener(this);
		mMessageInput.setInputListener(this);
		mMessageInput.setButtonClickListener(this);

		if (mIsGroup)
			setMagicButtonLanguage("UN", false);
		else
			setMagicButtonLanguage(mParticipantLang, false);

//		@DrawableRes int flagDrawable = Utils.getCountryFlagDrawableFromLang(mParticipantLang);
//		mMessageInput.setMagicButtonDrawable(getDrawable(flagDrawable));
		mGroupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
		mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);
		mSupportedLanguagesViewModel = ViewModelProviders.of(this).get(SupportedLanguagesViewModel.class);
		if ((mParticipantId != null)&& (!mIsGroup)) {
			mContactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
			mContactViewModel.refreshContact(mParticipantId).observe(this, contact -> {
				mParticipantPic = contact.getAvatar();
				mParticipantLang = contact.getLanguage();
				mParticipantName = contact.getName();
				mContactAvatarCIV.setInfo(mParticipantPic, mParticipantLang, Contact.getInitialsFromName(mParticipantName));
			});
		}


		mSupportedLanguagesViewModel.getSupportedLanguages().observe(this, supportedLanguages -> {
			mSupportedLanguages = supportedLanguages;
			mConversationViewModel.getMagicButtonLangCode(mConversationId).observe(this, langCode -> {
				if (langCode != null) {
					for (SupportedLanguage supportedLanguage : mSupportedLanguages) {
						if (supportedLanguage.getLanguageCode().equals(langCode)) {
							mMagicButtonForceLanguage = supportedLanguage.getLanguageCode();
							mMagicButtonForceCountry = supportedLanguage.getCountryCode();
							setMagicButtonLanguage(mMagicButtonForceLanguage, true);

						}
					}
				}
				else {
					if (mIsGroup)
						setMagicButtonLanguage("UN", false);
					else
						setMagicButtonLanguage(mParticipantLang, false);

					mMagicButtonForceLanguage = null;
					mMagicButtonForceCountry = null;
				}
			});

		});
		mSupportedLanguagesViewModel.loadLanguages(Locale.getDefault().getLanguage());



		mConversationViewModel.getConversationAndMessages(mConversationId,
			mParticipantId,
			mParticipantPic,
			mParticipantName,
			mParticipantLang,
			mSelfId,
			conversationAndItsMessages -> {
				mConversation = conversationAndItsMessages.getConversation();
				mMessages = conversationAndItsMessages.getMessages();
				mGroupMembers = conversationAndItsMessages.getGroupMembers();
				displayUITypingSignal(false, null);
				startListenToMessagesChanges();
			});



		mConversationViewModel.getConversationLD(mConversationId).observe(this, conversation -> {
			mConversation = conversation;
			mIsGroup = mConversation.isGroup();
			if (mIsGroup){
				mParticipantPic = mConversation.getGroupImageUrl();
				mParticipantName = mConversation.getGroupName();
				mContactNameTV.setText(mParticipantName);
				mContactAvatarCIV.setInfo(mParticipantPic, mParticipantLang, Contact.getInitialsFromName(mParticipantName));

				if (mConversation.isSelfInGroup()) {
					mNotInGroupMsgTV.setVisibility(View.GONE);
					mMessageInput.setVisibility(View.VISIBLE);
				}
				else {
					mNotInGroupMsgTV.setVisibility(View.VISIBLE);
					mMessageInput.setVisibility(View.GONE);
				}
			}
		});

		if(mIsGroup) {
			mGroupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
			mGroupViewModel.getMembersLD(mConversationId).observe(this, groupMembers -> {
				mGroupMembers = groupMembers;
				displayUITypingSignal(false, null);
			});
		}



		mTypingSignalBR = new TypingSignalBR();

		mRecordingBigIV = (ImageView) findViewById(R.id.recording_big_iv);
		mRecordingBigIV.setImageDrawable(getDrawable(R.drawable.mic_recording_empty));
		mRecordingBigIV.setOnTouchListener(mRecordingOnTouchListener);

//		mSpeechUtils = new SpeechUtils();
//		mSpeechUtils.setSpeechUtilsSTTListener(this);

		SpeechToTextUtil.getInstance().setSpeechUtilsSTTListener(this);


		if (mClickedFromNotifivation){
			mConversationViewModel.updateNotificationClicked(mConversationId);
		}



		if (mIsGroup){
			mGroupViewModel.getGroupDetailsAndInsertToDB(mConversationId, getResources());
		}
	}



	private void startListenToMessagesChanges() {
		mConversationViewModel.getMessagesLD(mConversationId).observe(this,
			messages -> {
				mMessages = messages;
				updateUIWithMessages();
			});

	}


	private void initAdapter() {

		CustomIncomingTextMessageViewHolder.Payload payloadIncoming = new CustomIncomingTextMessageViewHolder.Payload();
		payloadIncoming.onPayloadListener = new CustomIncomingTextMessageViewHolder.OnPayloadListener() {
			@Override
			public Message getRepliedMessage(String repliedMessageId) {
				return getMessageById(repliedMessageId);
			}

			@Override
			public String getSenderName(String repliedMessageId) {
				Message message = getMessageById(repliedMessageId);
				if (message != null) {
					if (message.isOutgoing())
						return "You";
					else {
						return mParticipantName;
					}
				}
				else return "";
			}
		};
		CustomOutcomingTextMessageViewHolder.Payload payloadOutcoming = new CustomOutcomingTextMessageViewHolder.Payload();
		payloadOutcoming.onPayloadListener = new CustomOutcomingTextMessageViewHolder.OnPayloadListener() {
			@Override
			public Message getRepliedMessage(String repliedMessageId) {
				return getMessageById(repliedMessageId);
			}

			@Override
			public String getSenderName(String repliedMessageId) {
				Message message = getMessageById(repliedMessageId);
				if (message != null) {
					if (message.isOutgoing())
						return "You";
					else {
						return mParticipantName;
					}
				}
				else return "";
			}
		};

		MessageHolders holdersConfig = new MessageHolders()

			.setIncomingTextConfig(
				CustomIncomingTextMessageViewHolder.class,
				R.layout.item_custom_incoming_text_message,
				payloadIncoming)

			.setOutcomingTextConfig(
				CustomOutcomingTextMessageViewHolder.class,
				R.layout.item_custom_outcoming_text_message,
				payloadOutcoming)

			.setIncomingImageConfig(
				CustomIncomingImageMessageViewHolder.class,
				R.layout.item_custom_incoming_image_message)

			.setOutcomingImageConfig(
				CustomOutcomingImageMessageViewHolder.class,
				R.layout.item_custom_outcoming_image_message)

			.setIncomingVideoConfig(
				CustomIncomingVideoMessageViewHolder.class,
				R.layout.item_custom_incoming_video_message)

			.setOutcomingVideoConfig(
				CustomOutcomingVideoMessageViewHolder.class,
				R.layout.item_custom_outcoming_video_message)

			.registerContentType(
				MessageHolders.VIEW_TYPE_AUDIO_MESSAGE,
				CustomIncomingAudioMessageViewHolder.class,
				mParticipantContact,
				R.layout.item_custom_incoming_audio_message_new,
				CustomOutcomingAudioMessageViewHolder.class,
				mSelfContact,
				R.layout.item_custom_outcoming_audio_message_new,
				this)

			.registerContentType(
				MessageHolders.VIEW_TYPE_SPEECH_MESSAGE,
				CustomIncomingSpeechableMessageViewHolder.class,
				mParticipantContact,
				R.layout.item_custom_incoming_audio_message_new,
				CustomOutcomingSpeechableMessageViewHolder.class,
				mSelfContact,
				R.layout.item_custom_outcoming_audio_message_new,
				this)

			.registerContentType(
				MessageHolders.VIEW_TYPE_INFO_MESSAGE,
				CustomInfoTextMessageViewHolder.class,
				mParticipantContact,
				R.layout.item_custom_info_text_message,
				CustomInfoTextMessageViewHolder.class,
				mSelfContact,
				R.layout.item_custom_info_text_message,
				this);



		mMessagesAdapter = new MessagesListAdapter<>(mSelfId, holdersConfig, mImageLoader);
		mMessagesAdapter.setOnMessageLongClickListener(this);
		mMessagesAdapter.setOnMessageViewLongClickListener(this);
		mMessagesAdapter.setLoadMoreListener(this);
		mMessagesAdapter.setOnMessageViewClickListener(this);
		mMessagesAdapter.setOnMessageClickListener(this);
		mMessagesAdapter.setOnMessageForwardListener(this);
		mMessagesAdapter.setOnBindViewHolder(this);
		mMessagesAdapter.setDateHeadersFormatter(this);
		mMessagesAdapter.enableSelectionMode(this);

		mMessagesListRV.setAdapter(mMessagesAdapter, true);
		mMessagesListRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == 0){
					LinearLayoutManager lm = (LinearLayoutManager) mMessagesListRV.getLayoutManager();
					int firstVisible = lm.findFirstCompletelyVisibleItemPosition();
					Log.i("SCL", "onScrollStateChanged newState=0, firstVisible: "+ firstVisible);
					if (firstVisible == 0)
						mScrollToEndIB.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				Log.i("SCL", "onScrolled dx: " + dx + ", dy:" + dy);
				if (dy!= 0)
					mScrollToEndIB.setVisibility(View.VISIBLE);
			}
		});

	}


	@Override
	public boolean hasContentFor(Message message, short type) {
		switch (type) {
			case MessageHolders.VIEW_TYPE_AUDIO_MESSAGE:
				return (message.isAudio() && (message.getMediaUrl() != null) && (!message.getMediaUrl().isEmpty()));
			case MessageHolders.VIEW_TYPE_SPEECH_MESSAGE:
				//return ((message.getTranslatedText() != null) && (!message.getTranslatedText().isEmpty()));
				return message.isSpeechable();
			case MessageHolders.VIEW_TYPE_INFO_MESSAGE:
				return message.getMessageType().equals(Message.MSG_TYPE_GROUP_EVENT);
		}
		return false;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id){
			case android.R.id.home:
				if (mSelectedMessageCount > 0){
					mMessagesAdapter.unselectAllItems();
					mInputMessageReplyLayout.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, "isTaskRoot: " + isTaskRoot());
					if (isTaskRoot()){
						Intent intent = new Intent(this, MainActivity.class);
						startActivity(intent);
					}
					finish();
					return true;
				}
			case R.id.action_phone:
			case R.id.action_video:
			case R.id.action_reply:
				mInputMessageReplyLayout.showReplyMessage(mMessagesAdapter.getSelectedMessage(), mParticipantName);
				mMessagesAdapter.unselectAllItems();
				mMessageInput.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mMessageInput.getInputEditText(), InputMethodManager.SHOW_IMPLICIT);
				break;
			case R.id.action_delete:
				actionDelete();
				break;
			case R.id.action_copy:
				actionCopy();
				break;
			case R.id.action_frwrd:
				Intent intent = new Intent(this, ContactGroupsMultiSelectorActivity.class);
				intent.putExtra(Consts.INTENT_TITLE, getString(R.string.search_frwrd_to));
				intent.putExtra(Consts.INTENT_ACTION_ICON, R.drawable.ic_action_send);
				startActivityForResult(intent, REQUEST_SELECT_CONTACTS);
				overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
				break;

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if (mSelectedMessageCount > 0){
			inflater.inflate(R.menu.menu_conversation_selection, menu);
			mContactAvatarCIV.setVisibility(View.GONE);
			mContactNameTV.setText(mSelectedMessageCount + "");
			mContactDetailsTV.setVisibility(View.GONE);
		}
		else if (mIsGroup){
			inflater.inflate(R.menu.menu_conversation_group, menu);
			mContactAvatarCIV.setVisibility(View.VISIBLE);
			mContactDetailsTV.setVisibility(View.VISIBLE);
			//mContactNameTV.setText(mParticipantName);
		}
		else {
			inflater.inflate(R.menu.menu_conversation, menu);
			mContactAvatarCIV.setVisibility(View.VISIBLE);
			mContactDetailsTV.setVisibility(View.VISIBLE);
			mContactNameTV.setText(mParticipantName);
			if (isSelectedMessagesTextOnly()){

			}
		}
		int count = menu.size();
		for (int i=0; i<count; i++){
			Drawable d1 = menu.getItem(i).getIcon();
			d1.mutate();
			d1.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
		}

		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuCopy = menu.findItem(R.id.action_copy);
		MenuItem menuReply = menu.findItem(R.id.action_reply);
		if (menuCopy != null)
			menuCopy.setVisible(isSelectedMessagesTextOnly());

		if (menuReply != null)
			menuReply.setVisible(mSelectedMessageCount == 1);


		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onLoadMore(int page, int totalItemsCount) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				updateUIWithMessagesOnLoadMore();
			}
		}, 200);

	}

	@Override
	public boolean onSubmit(CharSequence input) {
		String msgText = input.toString();
		Message message;
		if (mIsGroup) {
			message = new Message(mParticipantId, mSelfId, mConversationId, msgText, mSelfLang);
			message.setRecipients(getGroupMembersArray());
		}
		else
			message = new Message(mParticipantId, mSelfId, mConversationId, msgText, mSelfLang);

		message.setTranslatedLanguage(mParticipantLang);
		if (isMagicButtonOn()) {
			message.setForceTranslatedLanguage(mMagicButtonForceLanguage);
			message.setForceTranslatedCountry(mMagicButtonForceCountry);
			mConversationViewModel.getMessage(message.getMessageId()).observe(this, mMessageObserver);
		}

		if (mInputMessageReplyLayout.getVisibility() == View.VISIBLE){
			message.setRepliedMessageId(mInputMessageReplyLayout.getMessage().getId());
			mInputMessageReplyLayout.setVisibility(View.GONE);
		}

		mConversationViewModel.addNewOutcomingMessage(message);
		mMessageInput.getButton().setImageDrawable(getDrawable(R.drawable.msg_in_mic_light));
		mIsInputInTextMode = false;
		if (mIsGroup){
			if (!message.isMagic())
				mService.sendGroupMessage(message, mGroupMembers, mSelfId);
		}
		else {
			if (!message.isMagic())
				mService.sendMessage(message);
		}
		returnRecordingButtonToPlace(false);
		return true;

	}


	private String[] getGroupMembersArray(){
		String[] res = new String[mGroupMembers.size()];
		int i=0;
		for (GroupMember groupMember : mGroupMembers){
			res[i++] = groupMember.getUserId();
		}
		return res;
	}

	@Override
	public void onClick(int buttonId) {
		//Log.e(TAG, "onClick: " + buttonId);
		switch (buttonId){
			case R.id.attachmentButton:
				selectImage();
				break;
			case R.id.cameraButton:
				selectPhotoCamera();
				break;

			case R.id.videoButton:
				selectVideoCamera();
				break;


			case R.id.magicButton:
				magicButtonClicked();
				break;
		}
		//mMessagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
	}


	private void selectImage() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		//photoPickerIntent.setType("image/* video/*");
		photoPickerIntent.setType("*/*");
		//photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
		photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"video/*", "image/*"});
		//photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(photoPickerIntent, REQUEST_SELECT_IMAGE_VIDEO);
	}



	private void selectPhotoCamera(){
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mCameraPhotoFileUri = ImagePickerUtil.getCaptureImageOutputUri(this);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraPhotoFileUri);
		takePictureIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_SELECT_CAMERA_PHOTO);
		}
	}


	private void selectVideoCamera(){
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takeVideoIntent, REQUEST_SELECT_CAMERA_VIDEO);
		}
	}


	@Override
	public void onMessageViewClick(View view, Message message) {
//		if (mIsInMsgSelectionMode){
//			onMessageViewLongClick(view, message);
//		}
	}


	@Override
	public void onMessageViewLongClick(View view, Message message) {
//		if (view.getTag() == null) {
//			mIsInMsgSelectionMode = true;
//			mMsgSelectionCount++;
//			view.setBackground(getResources().getDrawable(R.color.blue_semi_transparent));
//			view.setTag(1);
//		}
//		else {
//			view.setBackground(getResources().getDrawable(R.color.transparent));
//			view.setTag(null);
//			mMsgSelectionCount--;
//			if (mMsgSelectionCount == 0) {
//				mIsInMsgSelectionMode = false;
//				Toast.makeText(this, "exit selection", Toast.LENGTH_SHORT).show();
//			}
//		}


	}


	@Override
	public void onMessageForward(Message message) {
		Intent intent = new Intent(this, ContactGroupsMultiSelectorActivity.class);
		intent.putExtra(Consts.INTENT_TITLE, getString(R.string.search_frwrd_to));
		intent.putExtra(Consts.INTENT_MESSAGE_OBJ, message.toJson());
		intent.putExtra(Consts.INTENT_ACTION_ICON, R.drawable.ic_action_send);
		startActivityForResult(intent, REQUEST_SELECT_CONTACTS_W_MSG);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

	}


	@Override
	public void onMessageClick(Message message) {
		if (Utils.isNotNullAndNotEmpty(message.getRepliedMessageId())){
			int pos = mMessagesAdapter.getMessagePositionById(message.getRepliedMessageId());
			if (pos > -1) {
				mMessagesListRV.smoothScrollToPosition(pos);
				mScrollToEndIB.setVisibility(View.VISIBLE);
				highlightMessage(message.getRepliedMessageId());
			}
			else {
				Message repliedMessage = getMessageById(message.getRepliedMessageId());
				updateUIWithMessagesOnGoTo(repliedMessage);
				pos = mMessagesAdapter.getMessagePositionById(message.getRepliedMessageId());
				if (pos > -1) {
					mMessagesListRV.scrollToPosition(pos);
					mScrollToEndIB.setVisibility(View.VISIBLE);
					highlightMessage(message.getRepliedMessageId());
				}
			}
		}
		if (message.isText()) {
			message.userClickAction();
			mMessagesAdapter.update(message);
		}
		else if (message.isSpeechable()){
			message.userClickAction();
			mMessagesAdapter.update(message);
		}
		else if (message.isImage()) {
			if ((message.getImageURL() != null) && (!message.getImageURL().equals(""))) {
				mPreviewImagesIV.setVisibility(View.VISIBLE);
				mPreviewImagesPB.setVisibility(View.VISIBLE);
				Picasso.get().load(message.getImageURL()).into(mPreviewImagesIV, new Callback() {
					@Override
					public void onSuccess() {
						mPreviewImagesPB.setVisibility(View.GONE);
					}

					@Override
					public void onError(Exception e) {
						mPreviewImagesPB.setVisibility(View.GONE);
					}
				});

				//Utils.showImage(ConversationActivity.this, message.getImageURL());
			}
		}
		else if (message.isVideo()){
			StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
			StrictMode.setVmPolicy(builder.build());

			// try locally
			//Log.e(TAG, "play file check local: " + message.getMediaLocalUri());
			if ((message.getMediaLocalUri() != null) && (!message.getMediaLocalUri().equals(""))){
				Uri uri = Uri.parse(message.getMediaLocalUri());
				//Log.e(TAG, "play file local uri: " + uri);
				String s = ImagePickerUtil.getFilePathFromContentUriNoStreaming(this, uri);
				//Log.e(TAG, "play file local file: " + s);
				if (s != null){
					//Log.e(TAG, "play file local file exist: " + s);

					//Uri accessibleUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".my.package.name.provider", createImageFile());
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					intent.setDataAndType(uri, "video/mp4");
					startActivity(intent);
					return;
				}
			}

			// play remotely
			Uri uri = Uri.parse(message.getMediaUrl());
			//Log.e(TAG, "play file remotely: " + uri);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setDataAndType(uri, "video/mp4");
			startActivity(intent);
		}

	}


	@Override
	public void onMessageLongClick(Message message) {

	}

	@Override
	public void OnBindPosition(int pos) {
		LinearLayoutManager lm = (LinearLayoutManager) mMessagesListRV.getLayoutManager();
		int firstVisible = lm.findFirstCompletelyVisibleItemPosition();
		Log.i("SCL", "OnBindPosition: position: " + pos + " , firstVisible: "+ firstVisible);
		if (firstVisible == 0)
			mScrollToEndIB.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onStartTyping() {
		String theText = mMessageInput.getInputEditText().getText().toString();
		if (!theText.equals("")) {
			mMessageInput.getButton().setImageDrawable(getDrawable(R.drawable.msg_in_send_light));
			mIsInputInTextMode = true;
		}
		if (mIsGroup)
			mService.sendTypingSignalForGroup(mConversationId, true, mGroupMembers, mSelfId);
		else
			mService.sendTypingSignal(mParticipantId, mConversationId, true);
	}

	@Override
	public void onStopTyping() {
		String theText = mMessageInput.getInputEditText().getText().toString();
		if (theText.equals("")) {
			mMessageInput.getButton().setImageDrawable(getDrawable(R.drawable.msg_in_mic_light));
			mIsInputInTextMode = false;
		}
		if (mIsGroup)
			mService.sendTypingSignalForGroup(mConversationId, false, mGroupMembers, mSelfId);
		else
			mService.sendTypingSignal(mParticipantId, mConversationId, false);
	}


	@Override
	public String format(Date date) {
		return Utils.dateFormatter(this, date);
	}


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.menu_conversation, menu);
//		return true;
//	}


	private void updateUIWithMessagesOnGoTo(Message message){
		ArrayList<Message> messages = getMessagesBetween(pointerDateUpperList, message.getCreatedAt());
		if (messages.size()>0) {
			pointerDateUpperList = messages.get(messages.size() - 1).getCreatedAt();
			mMessagesAdapter.addToEnd(messages, false);
		}
	}


	private void updateUIWithMessagesOnLoadMore(){
		ArrayList<Message> messages = getMessagesBefore(pointerDateUpperList);
		if (messages.size()>0) {
			pointerDateUpperList = messages.get(messages.size() - 1).getCreatedAt();
			mMessagesAdapter.addToEnd(messages, false);
		}
	}

	private void updateUIWithMessages(){
		if ((mMessages == null)) {
			return;
		}

		if (mMessages.isEmpty()){
			mMessagesAdapter.clear();
			return;
		}

		if ((pointerDateUpperList == null)&& (pointerDateBottomList == null)){ // RV is empty
			Message message = mMessages.get(0);
			pointerDateUpperList = message.getCreatedAt();
			pointerDateBottomList = message.getCreatedAt();
			mMessagesAdapter.addToStart(message, true);
			return;
		}
		else {
			Message message = mMessages.get(0);
			if(message.getCreatedAt().after(pointerDateBottomList)){
				ArrayList<Message> messages = getMessagesAfter(pointerDateBottomList);
				Message messageLast = messages.get(0);
				pointerDateBottomList = messageLast.getCreatedAt();
				mMessagesAdapter.addToStart(message, true);
			}
			ArrayList<Message> messagesB = getMessagesBetween(pointerDateBottomList, pointerDateUpperList);
			for (Message msg : messagesB){
				mMessagesAdapter.update(msg);
			}

		}
	}

	private ArrayList<Message> getMessagesBetween(Date newDate, Date oldDate) {
		ArrayList<Message> messages = new ArrayList<>();
		for(int i=0; i<mMessages.size(); i++) {
			Message message = mMessages.get(i);
			if (((message.getCreatedAt().before(newDate))&&(message.getCreatedAt().after(oldDate)))||
				(message.getCreatedAt().equals(newDate))||
				(message.getCreatedAt().equals(oldDate))) {
				messages.add(message);
			}
		}
		return messages;

	}

	public ArrayList<Message> getMessagesAfter(Date date) {
		ArrayList<Message> messages = new ArrayList<>();
		for(int i=mMessages.size()-1; i>=0; i--) {
			Message message = mMessages.get(i);
			if (message.getCreatedAt().after(date)) {
				messages.add(0, message);
			}
		}
		return messages;
	}

	public ArrayList<Message> getMessagesBefore(Date date) {
			ArrayList<Message> messages = new ArrayList<>();
			for(int i=0; i<mMessages.size(); i++) {
				Message message = mMessages.get(i);
				if (message.getCreatedAt().before(date)) {
					messages.add(message);
					if (messages.size() >= 5)
						break;
				}
			}
			return messages;
	}










	private void returnRecordingButtonToPlace(boolean withAnimation){
		if (withAnimation) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRecordingBigIV.getLayoutParams();

			final int rightMarginStart = params.rightMargin;
			final int rightMarginEnd = Utils.dp2px(this, -10);

//			Log.e("GIL", "angle rightMarginStart: " + rightMarginStart);
//			Log.e("GIL", "angle rightMarginEnd: " + rightMarginEnd);
//
//			Log.e("GIL", "angle deltaRight: " + (rightMarginStart - rightMarginEnd));

			final int deltaRight = rightMarginStart - rightMarginEnd;

			int absDeltaRight = Math.abs(deltaRight);

			Animation a = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRecordingBigIV.getLayoutParams();
					params.rightMargin = rightMarginStart + (int) ((rightMarginEnd - rightMarginStart) * interpolatedTime);
					mRecordingBigIV.setLayoutParams(params);
				}
			};
			a.setDuration(300);
			a.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mRecordingBigIV.setImageDrawable(getDrawable(R.drawable.mic_recording_empty));
					if (deltaRight > 500)
						cancelRecordOrSpeech();
					else
						finishRecordOrSpeech();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			mRecordingBigIV.startAnimation(a);
		}
		else {
			RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) mRecordingBigIV.getLayoutParams();
			layoutParams1.rightMargin = 0;
			mRecordingBigIV.setLayoutParams(layoutParams1);
		}

	}
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, WCService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}


	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mServiceConnection);
		mBound = false;
	}



	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			//Log.e(TAG, "ServiceConnection: onServiceConnected");
			WCService.WCBinder binder = (WCService.WCBinder) service;
			mService = binder.getService();
			mBound = true;
			mService.setCurrentConversationId(mConversationId);
			initMarkAsReadMessagesHandling();
			if (mParticipantId != null)
				mService.getLastOnline(mParticipantId);
			if (mSelectedImageForDelayHandlingUri != null) {
				submitTempImageMessage(mSelectedImageForDelayHandlingUri);
				mSelectedImageForDelayHandlingUri = null;
			}
			if (mCameraVideoMediaForDelayHandlingFile != null) {
				submitTempVideoMessage(mCameraVideoMediaForDelayHandlingFile, mCameraVideoThumbForDelayHandlingFile, mCameraVideoDurationForDelayHandling);
				mCameraVideoMediaForDelayHandlingFile = null;
				mCameraVideoThumbForDelayHandlingFile = null;
				mCameraVideoDurationForDelayHandling = 0;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			//Log.e(TAG, "ServiceConnection: onServiceDisconnected");

			mService.setCurrentConversationId(null);
			mService = null;
			mBound = false;
		}
	};

	private void initMarkAsReadMessagesHandling() {
		mConversationViewModel.getUnreadMessagesConversation(mConversationId).
			observe(this, messageList -> {
				//Log.e(TAG, "getMarkAsReadAffectedMessages change: " + messageList==null?"null":""+messageList.size());
				if ((mService != null)&&(messageList != null)&&(!messageList.isEmpty())){
					mService.sendAckStatusForIncomingMessages(messageList, Message.ACK_STATUS_READ);
					mConversationViewModel.markAllMessagesAsRead(mConversationId);
				}


			});

	}



	private Drawable getMessageSelector(@ColorInt int normalColor, @ColorInt int selectedColor,
										@ColorInt int pressedColor, @DrawableRes int shape) {

		Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(this, shape)).mutate();
		DrawableCompat.setTintList(
			drawable,
			new ColorStateList(
				new int[][]{
					new int[]{android.R.attr.state_selected},
					new int[]{android.R.attr.state_pressed},
					new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected}
				},
				new int[]{selectedColor, pressedColor, normalColor}
			));
		return drawable;
	}

	@Override
	public void onSelectionChanged(int count) {
		mSelectedMessageCount = count;
		invalidateOptionsMenu();
	}



	private class TypingSignalBR extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(WCService.TYPING_SIGNAL_ACTION)) {
				String conversationId = intent.getStringExtra(WCService.CONVERSATION_ID_EXTRA);
				if ((conversationId != null)&&(conversationId.equals(mConversationId))){
					boolean isTyping = intent.getBooleanExtra(WCService.IS_TYPING_EXTRA, false);
					String participantId = intent.getStringExtra(WCService.PARTICIPANT_ID_EXTRA);
					displayUITypingSignal(isTyping, participantId);
				}
			}
			else if(intent.getAction().equals(WCService.PRESSENCE_ACTION)) {
				String contactId = intent.getStringExtra(WCService.PRESSENCE_CONTACT_ID_EXTRA);
				boolean isAvailiable = intent.getBooleanExtra(WCService.PRESSENCE_IS_AVAILABLE_EXTRA, false);
				if (contactId.equals(mParticipantId)){
					mIsOnline = isAvailiable;
					if (mIsOnline)
						mContactDetailsTV.setText(R.string.online);
					else {
						mService.getLastOnline(mParticipantId);
						mContactDetailsTV.setText(getDisplayOnlineDateTime());
					}
				}
			}
			else if(intent.getAction().equals(WCService.LAST_ONLINE_ACTION)) {
				String contactId = intent.getStringExtra(WCService.LAST_ONLINE_CONTACT_ID_EXTRA);
				long time = intent.getLongExtra(WCService.LAST_ONLINE_TIME_EXTRA, 0);
				boolean isOnline = intent.getBooleanExtra(WCService.LAST_ONLINE_IS_AVAILABLE_EXTRA, false);

				if (mParticipantId != null) {
					if (mParticipantId.equals(contactId)) {
						mIsOnline = isOnline;
						mLastOnlineTime = time;
						if (mIsOnline)
							mContactDetailsTV.setText(R.string.online);
						else
							mContactDetailsTV.setText(getDisplayOnlineDateTime());
					}
				}
			}
		}
	}

	private Runnable mClearTypingRunnable = new Runnable() {
		@Override
		public void run() {
			if (mIsOnline)
				mContactDetailsTV.setText(R.string.online);
			else
				mContactDetailsTV.setText(getDisplayOnlineDateTime());
		}
	};


	private String getDisplayOnlineDateTime(){
		if (mLastOnlineTime > 0) {
			Date lastOnlineDate = new Date(mLastOnlineTime);
			if (DateFormatter.isToday(lastOnlineDate)){
				return getString(R.string.last_seen_today) + " " + DateFormatter.format(lastOnlineDate, DateFormatter.Template.TIME);
			}
			else if (DateFormatter.isYesterday(lastOnlineDate)){
				return getString(R.string.last_seen_yesterday) + " " + DateFormatter.format(lastOnlineDate, DateFormatter.Template.TIME);
			}
			else if (DateFormatter.isPastWeek(lastOnlineDate)){
				return getString(R.string.last_seen_past_week) + " " + DateFormatter.format(lastOnlineDate, DateFormatter.Template.STRING_DAY_OF_WEEK_TIME);
			}
			else {
				return getString(R.string.last_seen_past) + " " + DateFormatter.format(lastOnlineDate, DateFormatter.Template.STRING_DAY_MONTH);
			}
		}
		else
			return "";
	}




	private void displayUITypingSignal(boolean isTyping, String participantId) {
		if (mIsGroup){
			if (isTyping){
				String firstName = getGroupMemberFirstName(participantId);
				mContactDetailsTV.setText(firstName + " is typing...");
				mClearTypingHandler.postDelayed(mClearTypingRunnable, 10000);
			}
			else {
				mContactDetailsTV.setText(getGroupNameList());
				mClearTypingHandler.removeCallbacks(mClearTypingRunnable);
			}

		}
		else {
			if (isTyping) {
				mContactDetailsTV.setText("Typing...");
				mClearTypingHandler.postDelayed(mClearTypingRunnable, 10000);
			}
			else {
				if (mIsOnline)
					mContactDetailsTV.setText("Online");
				else
					mContactDetailsTV.setText(getDisplayOnlineDateTime());
				mClearTypingHandler.removeCallbacks(mClearTypingRunnable);
			}
		}
	}


	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mTypingSignalBR);
		//mSpeechUtils.destroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//mSpeechUtils.initSpeech(this, this.getPackageName(), mSelfLang);
		mClearTypingHandler = new Handler(getMainLooper());
		IntentFilter filter = new IntentFilter();
		filter.addAction(WCService.TYPING_SIGNAL_ACTION);
		filter.addAction(WCService.PRESSENCE_ACTION);
		filter.addAction(WCService.LAST_ONLINE_ACTION);
		try {
			registerReceiver(mTypingSignalBR, filter);
		} catch (Exception e) {}

	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_SELECT_IMAGE_VIDEO) {
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = ImagePickerUtil.getPickImageResultUri(this, intent);
				String mimeType = ImagePickerUtil.getMimeType(this, uri);
				if (ImagePickerUtil.MIME_TYPE_IMAGE.equals(mimeType)) {
					if ((mService != null) && (mService.isXmppConnected())) {
						submitTempImageMessage(uri);
					} else {
						mSelectedImageForDelayHandlingUri = uri;
					}
					//Log.e(TAG, "onActivityResult select image: " + mSelectedImageForDelayHandlingUri);
				}
				else if (ImagePickerUtil.MIME_TYPE_VIDEO.equals(mimeType)) {
					//Log.e(TAG, "video: " + uri);
					File f = getExternalCacheDir();
					showProgressDialog();
					String videoPath = ImagePickerUtil.getFilePathFromContentUri(ConversationActivity.this, uri );
					hideProgressDialog();
					if (videoPath == null){
					}
					//Log.e(TAG, "video file: " + videoPath);
					if (f.mkdirs() || f.isDirectory()) {
						//Log.e(TAG, "before compression: " + videoPath);
						new VideoCompressor().execute(videoPath);
					}

				}

			}
		}
		else if (requestCode == REQUEST_SELECT_CAMERA_PHOTO){
			if (resultCode == Activity.RESULT_OK) {
				if ((mService != null)&& (mService.isXmppConnected())){
					submitTempImageMessage(mCameraPhotoFileUri);
				}
				else {
					mSelectedImageForDelayHandlingUri = mCameraPhotoFileUri;
				}

				//Log.e(TAG, "onActivityResult select image: " + mSelectedImageForDelayHandlingUri);

			}
		}
		else if (requestCode == REQUEST_SELECT_CAMERA_VIDEO){
			if (resultCode == Activity.RESULT_OK) {
				Uri videoUri = intent.getData();
				//Log.e(TAG, "video: " + videoUri);
				File f = getExternalCacheDir();
				showProgressDialog();
				String videoPath = ImagePickerUtil.getFilePathFromContentUri(this, videoUri);
				hideProgressDialog();
				//Log.e(TAG, "video file: " + videoPath);
				if (f.mkdirs() || f.isDirectory()) {
					//Log.e(TAG, "before compression: " + videoPath);
					new VideoCompressor().execute(videoPath);
				}
			}
		}
		else if (requestCode == REQUEST_SELECT_CONTACTS){
			if (resultCode == Activity.RESULT_OK) {
				String[] contacts = intent.getStringArrayExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_RESULT);
				String contactsGroupObj = intent.getStringExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_OBJ_RESULT);
				Type listType = new TypeToken<List<ContactOrGroup>>() {}.getType();
				Gson gson = new Gson();
				List<ContactOrGroup> contactsGroupList = gson.fromJson(contactsGroupObj, listType);
				ArrayList<Message> selectedMessages = mMessagesAdapter.getSelectedMessages();
				forwardMessagesToContactsGroups(contactsGroupList, selectedMessages);
			}
		}
		else if (requestCode == REQUEST_SELECT_CONTACTS_W_MSG){
			if (resultCode == Activity.RESULT_OK) {
				String[] contacts = intent.getStringArrayExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_RESULT);
				String messageString = intent.getStringExtra(Consts.INTENT_MESSAGE_OBJ);
				Message message = Message.fromJson(messageString);
				String contactsGroupObj = intent.getStringExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_OBJ_RESULT);
				Type listType = new TypeToken<List<ContactOrGroup>>() {}.getType();
				Gson gson = new Gson();
				List<ContactOrGroup> contactsGroupList = gson.fromJson(contactsGroupObj, listType);
				ArrayList<Message> selectedMessages = new ArrayList<>();
				selectedMessages.add(message);
				forwardMessagesToContactsGroups(contactsGroupList, selectedMessages);
			}
		}
	}


//	private void updateImageMessage(ImageInfo imageInfo) {
//		if (mService == null)
//			return;
//		Message message = new Message(mParticipantId, mSelfId, mConversationId, imageInfo.getImageUrl(), imageInfo.getThumbUrl(), mSelfLang);
//		mConversationViewModel.addNewOutcomingMessage(message);
//
//	}

	private class VideoCompressor extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			showProgressDialog();
		}

		@Override
		protected Boolean doInBackground(String... filePath) {
			//Log.e(TAG, "start video compression");
			MediaController.cachDir = getExternalCacheDir().getPath();
			boolean res = MediaController.getInstance().convertVideo(filePath[0]);
			return res;
		}

		@Override
		protected void onPostExecute(Boolean compressed) {
			super.onPostExecute(compressed);
			hideProgressDialog();
			//Log.e(TAG, "compression result: " + compressed);
			if (compressed) {
				File compressedVideo = MediaController.cachedFile;
				Bitmap snapshotImage = MediaController.bitmapFrame;
				File thumbFile = MediaController.thumbFile;
				int duration = Integer.valueOf(MediaController.duration);

				//Log.e(TAG, "Compression successfully!");
				//Log.e(TAG, "Compressed File Path: " + compressedVideo);
				//Log.e(TAG, "video duration: " + duration);
				//Log.e(TAG, "bitmap: "+ snapshotImage);
				//Log.e(TAG, "thumb: "+ thumbFile.getPath());

				if ((mService != null)&& (mService.isXmppConnected())){
					submitTempVideoMessage(compressedVideo, thumbFile, duration);
				}
				else {
					mCameraVideoMediaForDelayHandlingFile = compressedVideo;
					mCameraVideoThumbForDelayHandlingFile = thumbFile;
					mCameraVideoDurationForDelayHandling = duration;

				}


			}

		}
	}




	private Observer<Message> mMessageObserver = new Observer<Message>() {
		@Override
		public void onChanged(@Nullable Message message) {
			if (message != null) {
				if (message.isAudio() || message.isVideo() || message.isImage()) {
					if ((Utils.isNotNullAndNotEmpty(message.getMediaUrl())) && (message.getAckStatus().equals(Message.ACK_STATUS_PENDING))) { // need to upload - one time
						//mConversationViewModel.getMessage(message.getMessageId()).removeObserver(mMessageObserver);
						if ((mService != null) && (mService.isXmppConnected())) {
							if (mIsGroup)
								mService.sendGroupMessage(message, mGroupMembers, mSelfId);
							else
								mService.sendMessage(message);
						}
						else {
							new Handler(getMainLooper()).postDelayed(() -> {
								if ((mService != null) && (mService.isXmppConnected())) {
									if (mIsGroup)
										mService.sendGroupMessage(message, mGroupMembers, mSelfId);
									else
										mService.sendMessage(message);
								}
							}, 1500);
						}
						//mMessagesAdapter.update(message);
					}
				}
				else if (message.isMagic()){
					if (Utils.isNotNullAndNotEmpty(message.getForceTranslatedText())){
						if (message.getAckStatus().equals(Message.ACK_STATUS_PENDING)) {
							new Handler(getMainLooper()).postDelayed(() -> {
								if ((mService != null) && (mService.isXmppConnected())) {
									Log.e("GIL", "call sendMessage: " + message.toJson());
									if (mIsGroup)
										mService.sendGroupMessage(message, mGroupMembers, mSelfId);
									else
										mService.sendMessage(message);

								}
							}, 1500);
						}
					}
				}
				else if (message.isSpeechable()||message.isText()){
					if (message.getAckStatus().equals(Message.ACK_STATUS_PENDING)) {
						new Handler(getMainLooper()).postDelayed(() -> {
							if ((mService != null) && (mService.isXmppConnected())) {
								Log.e("GIL", "call sendMessage: " + message.toJson());
								if (mIsGroup)
									mService.sendGroupMessage(message, mGroupMembers, mSelfId);
								else
									mService.sendMessage(message);

							}
						}, 1500);
					}
				}
			}
		}
	};

	private void addGroupInfo(Message message) {
		message.setRecipients(getGroupMembersArray());
		message.setGroupName(mParticipantName);
		message.setGroupId(mConversationId);
		message.setGroups(new String[]{mConversationId});
	}


	private void submitTempImageMessage(Uri imageUri) {
		//Log.e(TAG, "submitTempImageMessage: " + imageUri);
		if (mService == null) {
			//Log.e(TAG, "submitTempImageMessage mService is NULL - cancel");
			return;
		}

		Message message = Message.CreateImageMessage(mParticipantId, mSelfId, mConversationId, imageUri, mSelfLang);
		if (mIsGroup)
			addGroupInfo(message);
		mConversationViewModel.getMessage(message.getMessageId()).observe(this, mMessageObserver);
		mConversationViewModel.addNewOutcomingMessage(message);
	}


	private void submitTempVideoMessage(File compressedVideoFile, File thumbFile, int duration) {

		Uri compressedVideoUri = Uri.fromFile(compressedVideoFile);
		Uri thumbUri = Uri.fromFile(thumbFile);

		//Log.e(TAG, "submitTempVideoMessage: " + compressedVideoFile);
		if (mService == null) {
			//Log.e(TAG, "submitTempVideoMessage mService is NULL - cancel");
			return;
		}

		Message message = Message.CreateVideoMessage(mParticipantId, mSelfId, mConversationId, compressedVideoUri, thumbUri, mSelfLang, duration);
		if (mIsGroup)
			addGroupInfo(message);
		mConversationViewModel.getMessage(message.getMessageId()).observe(this, mMessageObserver);
		mConversationViewModel.addNewOutcomingMessage(message);
	}

	private void submitTempAudioMessage(File audioFile, int duration) {

		Uri audioUri = Uri.fromFile(audioFile);

		//Log.e(TAG, "submitTempAudioMessage: " + audioFile);
		if (mService == null) {
			//Log.e(TAG, "submitTempAudioMessage mService is NULL - cancel");
			return;
		}

		Message message = Message.CreateAudioMessage(mParticipantId, mSelfId, mConversationId, audioUri, duration);
		if (mIsGroup)
			addGroupInfo(message);

		mConversationViewModel.getMessage(message.getMessageId()).observe(this, mMessageObserver);
		mConversationViewModel.addNewOutcomingMessage(message);
	}

	private void submitTempSpeechableMessage(String text, int duration) {
		Message message = new Message(mParticipantId, mSelfId, mConversationId, text, mSelfLang);
		message.setTranslatedLanguage(mParticipantLang);
		if (mIsGroup) {
			addGroupInfo(message);
		}


		if (isMagicButtonOn()) {
			message.setForceTranslatedLanguage(mMagicButtonForceLanguage);
			message.setForceTranslatedCountry(mMagicButtonForceCountry);
		}

		message.setMessageType(Message.MSG_TYPE_SPEECHABLE);
		message.setDurationMili(duration);
		message.setDuration(duration/1000);
		mConversationViewModel.getMessage(message.getMessageId()).observe(this, mMessageObserver);
		mConversationViewModel.addNewOutcomingMessage(message);
	}


	@Override
	public void onBackPressed() {
		if (mPreviewImagesIV.getVisibility() == View.VISIBLE) {
			mPreviewImagesIV.setVisibility(View.GONE);
			mPreviewImagesPB.setVisibility(View.GONE);
		}
		else if (mSelectedMessageCount > 0){
			mMessagesAdapter.unselectAllItems();
			mInputMessageReplyLayout.setVisibility(View.GONE);
		}
		else {
			super.onBackPressed();
		}
	}


	private void showProgressDialog(){
		mProgressDialog = ProgressDialog.show(ConversationActivity.this,
			null,
			"Processing",
			true,
			false);
	}

	private void hideProgressDialog(){
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

	}



	private View.OnTouchListener mRecordingOnTouchListener = new View.OnTouchListener(){

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			final int x = (int) event.getRawX();
			final int y = (int) event.getRawY();

			switch (event.getAction() & MotionEvent.ACTION_MASK) {

				case MotionEvent.ACTION_DOWN:
					if (!mIsInputInTextMode){
						if (hasPermissions()) {
							mRecordingBigIV.setImageDrawable(getDrawable(R.drawable.mic_recording_big));
							startRecordOrSpeech();

							oldX = event.getX();
							oldY = event.getY();
							RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
							xDelta = lParams.rightMargin + x;
							yDelta = y - lParams.topMargin;
						}
						else {
							requestPermissions();
						}
					}
					break;

				case MotionEvent.ACTION_UP:
					if (mIsInputInTextMode){
						//Toast.makeText(ConversationActivity.this, "simple click", Toast.LENGTH_SHORT).show();
						mMessageInput.getButton().callOnClick();
					}
					else {
						returnRecordingButtonToPlace(true);
					}

					break;

				case MotionEvent.ACTION_MOVE:

					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

					//Log.e("GIL", "xDelta: " + xDelta);
					int newMargin = xDelta - x;
					if ((layoutParams.rightMargin <= 500) && (newMargin > 500)){
						//mMessageInput.getImojiButton().setImageDrawable(getDrawable(R.drawable.mic_recording_delete));
					}
					else if ((layoutParams.rightMargin >= 500) && (newMargin < 500)){
						//mMessageInput.getImojiButton().setImageDrawable(getDrawable(R.drawable.mic_recording_small));
					}
					layoutParams.rightMargin = newMargin;
					//Log.e("GIL", "rightMargin: " + layoutParams.rightMargin);

					layoutParams.topMargin = 0;
					layoutParams.leftMargin = 0;
					layoutParams.bottomMargin = 0;//y1 - yDelta;
					view.setLayoutParams(layoutParams);


					break;
			}

			return true;


		}
	};

	private boolean isRecordConditionOfRecordOrSpeach(){
		return mSameLanguageWithParticipant && (!isMagicButtonOn()) && (!mIsGroup);
	}


	private void startRecordOrSpeech(){
		if (isRecordConditionOfRecordOrSpeach()){
			startRecord();
		}
		else  {
//			mSpeechUtils.initSpeech(this, this.getPackageName(), mSelfLang);
//			mSpeechUtils.startSpeechToText();
			SpeechToTextUtil.getInstance().startSpeechToText();
		}
	}


	private void cancelRecordOrSpeech(){
		if (isRecordConditionOfRecordOrSpeach()){
			cancelRecord();
		}
		else  {
			//mSpeechUtils.cancelSpeechToText();
			SpeechToTextUtil.getInstance().cancelSpeechToText();
			mRecordingTimer.cancel();
			mRecordingStarted = false;
			mMessageInput.getInputEditText().setHint(R.string.hint_enter_a_message);
		}

	}

	private void finishRecordOrSpeech(){
		if (isRecordConditionOfRecordOrSpeach()){
			finishRecord();
		}
		else  {
			//mSpeechUtils.stopSpeechToText();
			SpeechToTextUtil.getInstance().stopSpeechToText();
			mRecordingTimer.cancel();
			mRecordingStarted = false;
			mMessageInput.getInputEditText().setHint(R.string.hint_enter_a_message);
		}

	}


	@Override
	public void onSpeechToTextResult(String text, int duration) {
		//Log.e(TAG, "onSpeechResult: " + text);
		//mSpeechUtils.destroy();
		submitTempSpeechableMessage(text, duration);
	}

	@Override
	public void onBeginningOfSpeechToText() {
		mRecordingTimer.startCountUp();
	}

	@Override
	public void onEndOfSpeechToText() {
		mRecordingTimer.cancel();
		mRecordingStarted = false;
		mMessageInput.getInputEditText().setHint(R.string.hint_enter_a_message);

	}

	@Override
	public void onErrorOfSpeechToText(@StringRes int resourceString) {
		if (resourceString != 0)
			Toast.makeText(this, getString(resourceString), Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, "Speech recognition error", Toast.LENGTH_SHORT).show();

		mRecordingTimer.cancel();
		mRecordingStarted = false;
		mMessageInput.getInputEditText().setHint(R.string.hint_enter_a_message);

	}



	private boolean mRecordingStarted = false;


	private void startRecord(){
		mRecordingStarted = true;
		mRecorder = new MediaRecorder();
		mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				//Log.e(TAG, "MediaRecorder onError what: " + what + " , extra: " + extra);
			}
		});

		mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {

			}
		});


		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		//mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mAudioFile = ImagePickerUtil.getAudioOutputFile(this);
		mRecorder.setOutputFile(mAudioFile.getPath());
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setAudioChannels(2);

		//Log.e(TAG, "startRecord, file: " + mAudioFile.getPath());
		try {
			mRecorder.prepare();
			//Log.e(TAG, "startRecord, after prepare");
		} catch (IOException e) {
			//Log.e(TAG, "startRecord prepare failed");
		}

		mRecorder.start();
		mRecorderStartTimeStamp = System.currentTimeMillis();
		//Log.e(TAG, "startRecord started");
		mRecordingTimer.startCountUp();

	}

	private void finishRecord(){
		if ((!mRecordingStarted) || (mRecorder == null))
			return;

		int duration = (int)(System.currentTimeMillis() - mRecorderStartTimeStamp);

		mRecordingTimer.cancel();
		mRecordingStarted = false;
		mMessageInput.getInputEditText().setHint(R.string.hint_enter_a_message);


		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;

		//Log.e(TAG, "finishRecord, file: " + mAudioFile.getPath());
		submitTempAudioMessage(mAudioFile, duration);

	}

	private void cancelRecord(){
		if ((!mRecordingStarted) || (mRecorder == null))
			return;
		//Log.e(TAG, "cancelRecord");
		//mMessageInput.getImojiButton().setImageDrawable(getDrawable(R.drawable.smiley));

		mRecordingStarted = false;
		Toast.makeText(ConversationActivity.this, "Recording canceled", Toast.LENGTH_SHORT).show();
		mRecordingTimer.cancel();
		mMessageInput.getInputEditText().setHint(R.string.hint_enter_a_message);

		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
		mAudioFile.delete();
		mAudioFile = null;
	}


	private RecordingTimer mRecordingTimer = new RecordingTimer();

	private class RecordingTimer extends CountDownTimer {


		private int mCounter;

		public RecordingTimer() {
			super(60*60*1000, 1000);
		}

		public void startCountUp() {
			mCounter = 0;
			super.start();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mCounter++;
			String time = Utils.convertSecondsToHMmSs(mCounter*1000);
			String displayText = time + "  < " + getString(R.string.hint_slide_to_cancel);
			mMessageInput.getInputEditText().setHint(displayText);
		}

		@Override
		public void onFinish() {
			finishRecordOrSpeech(); // should never be called only in case the time reach the end
		}
	};


	private String[] PERMISSIONS = {
		android.Manifest.permission.RECORD_AUDIO
	};

	@Override
	protected String[] getPermissions() {
		return PERMISSIONS;
	}

	@Override
	protected void showPermissionsExplanationDialog() {

	}

	@Override
	protected boolean isNeededPermissionsExplanationDialog() {
		return false;
	}


	private boolean isSelectedMessagesTextOnly(){
		ArrayList<Message> msgs = mMessagesAdapter.getSelectedMessages();
		for (Message message : msgs){
			if (!message.isText())
				return false;
		}
		return true;
	}


	private void actionDelete(){
		List<Message> messages = mMessagesAdapter.getSelectedMessages();
		mConversationViewModel.deleteMessages(messages);
		if (mSelectedMessageCount == 1) {
			Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(this, String.format("%d messages deleted", mSelectedMessageCount), Toast.LENGTH_SHORT).show();
		}
		mMessagesAdapter.delete(messages);
		mMessagesAdapter.unselectAllItems();
	}

	private void actionCopy(){
		if (mSelectedMessageCount == 1) {
			Toast.makeText(this, "Message copied", Toast.LENGTH_SHORT).show();
			mMessagesAdapter.copySelectedMessagesText(this, message -> message.getText(), true);
		}
		else {
			Toast.makeText(this, String.format("%d messages copied", mSelectedMessageCount), Toast.LENGTH_SHORT).show();
			mMessagesAdapter.copySelectedMessagesText(this, message -> message.getText(), true);
		}
		mMessagesAdapter.unselectAllItems();
	}


	private void forwardMessagesToContactsGroups(List<ContactOrGroup> contactOrGroupList, ArrayList<Message> messages) {
		Log.e(TAG, "forwardMessagesToContactsGroups, num contacts: " + contactOrGroupList.size() + " , num msgs: " + messages.size());

		int totalMessagesToSend = contactOrGroupList.size() * messages.size();

		if (contactOrGroupList.size() == 1)
			mForwardContactOrGroupSingle = contactOrGroupList.get(0);
		else
			mForwardContactOrGroupSingle = null;

		mConversationViewModel.forwardMessagesToContactsGroups(contactOrGroupList, messages);
		mMessagesAdapter.unselectAllItems();

		Log.e(TAG, "forwardMessagesToContactsGroups, getOutgoingPendingMessages() observe");
		mConversationViewModel.getOutgoingPendingMessagesLD().observe(this, pendingMessages -> {
			Log.e(TAG, "forwardMessagesToContactsGroups, getOutgoingPendingMessages() result: " + pendingMessages.size());

			if ((pendingMessages != null)&&(pendingMessages.size()>= totalMessagesToSend)) {

				if (mIsGroup)
					mService.sendGroupMessages(pendingMessages, mSelfId);
				else
					mService.sendMessages(pendingMessages);

				Log.e(TAG, "forwardMessagesToContactsGroups, mForwardContactOrGroupSingle: " + mForwardContactOrGroupSingle);
				if (mForwardContactOrGroupSingle != null) {
					String conversationId;
					if (mForwardContactOrGroupSingle.isContact())
						conversationId = Conversation.getConversationId(mSelfId, mForwardContactOrGroupSingle.getContact().getId());
					else
						conversationId = mForwardContactOrGroupSingle.getConversation().getId();
					Log.e(TAG, "forwardMessagesToContactsGroups, new conversationId: " + conversationId);
					mConversationViewModel.getConversationLD(conversationId).observe(this, conversation -> {
						OpenConversationActivity(conversation);
					});
				}
			}
		});

	}

	private void OpenConversationActivity(Conversation conversation) {
		Log.e(TAG, "OpenConversationActivity : conversationId: " + conversation.getConversationId());
		Intent intent = new Intent(this, ConversationActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, conversation.getParticipantId());
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, conversation.getParticipantName());
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, conversation.getParticipantLanguage());
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, conversation.getParticipantProfilePicUrl());
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, conversation.getId());
		intent.putExtra(Consts.INTENT_SELF_PIC_URL, mSelfPicUrl);
		intent.putExtra(Consts.INTENT_SELF_ID, mSelfId);
		intent.putExtra(Consts.INTENT_SELF_LANG, mSelfLang);
		intent.putExtra(Consts.INTENT_SELF_NAME, mSelfName);

		setIntent(intent);
		recreate();
//		startActivity(intent);
//		finish();
	}


	private Message getMessageById(String id){
		for(Message message : mMessages){
			if (message.getId().equals(id))
				return message;
		}

		return null;
	}

	private void setMagicButtonLanguage(String participantLang, boolean highlightBorder) {

		if (participantLang == null)
			return;

		@DrawableRes int flagDrawable = Utils.getCountryFlagDrawableFromLang(participantLang);
		mMessageInput.setMagicButtonDrawable(getDrawable(flagDrawable));
		if (highlightBorder)
			mMessageInput.setMagicButtonBorders(Color.BLUE, Utils.dp2px(this, 3));
		else
			mMessageInput.setMagicButtonBorders(Color.GRAY, Utils.dp2px(this, 2));
	}


	private void highlightMessage(String messageId) {
		mMessagesAdapter.selectMessage(messageId);
		new Handler(getMainLooper()).postDelayed(() -> mMessagesAdapter.unselectAllItems(),
			2000);
	}

	private void magicButtonClicked() {
		if (!isMagicButtonOn()) {
			LanguageSelectorDialog dialog = new LanguageSelectorDialog();
			dialog.showDialog(this, mSupportedLanguages, supportedLanguage -> {
				mMagicButtonForceLanguage = supportedLanguage.getLanguageCode();
				mMagicButtonForceCountry = supportedLanguage.getCountryCode();
				setMagicButtonLanguage(mMagicButtonForceLanguage, true);
				mConversationViewModel.updateMagicButtonLangCode(mConversationId, mMagicButtonForceLanguage);
			});
		}
		else {
			if (mIsGroup)
				setMagicButtonLanguage("UN", false);
			else
				setMagicButtonLanguage(mParticipantLang, false);
			mMagicButtonForceLanguage = null;
			mMagicButtonForceCountry = null;
			mConversationViewModel.updateMagicButtonLangCode(mConversationId, null);
		}


	}

	private boolean isMagicButtonOn(){
		return (mMagicButtonForceLanguage != null);
	}


	private String getGroupNameList(){
		String res = "";
		if ((mGroupMembers != null) && (!mGroupMembers.isEmpty())){
			for (GroupMember groupMember : mGroupMembers){

				String firstName = groupMember.getUserFirstName();
				if (groupMember.getUserId().equals(mSelfId))
					firstName = "You";

				if (res.equals(""))
					res = firstName;
				else
					res = res + ", " + firstName;
			}
		}
		return res;
	}


	private String getGroupMemberFirstName(String id){
		for(GroupMember groupMember : mGroupMembers){
			if(groupMember.getUserId().equals(id)){
				return groupMember.getUserFirstName();
			}
		}
		return "";

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.contact_name_tv:
			case R.id.contact_details_tv:
				if (mIsGroup) {
					Intent intent = new Intent(this, GroupInfoActivity.class);
					intent.putExtra(Consts.INTENT_CONVERSATION_ID, mConversationId);
					intent.putExtra(Consts.INTENT_SELF_ID, mSelfId);
					intent.putExtra(Consts.INTENT_SELF_NAME, mSelfName);
					intent.putExtra(Consts.INTENT_SELF_LANG, mSelfLang);
					startActivity(intent);
				}
				else {
					Intent intent = new Intent(this, ContactInfoActivity.class);
					intent.putExtra(Consts.INTENT_PARTICIPANT_ID, mParticipantId);
					intent.putExtra(Consts.INTENT_CONVERSATION_ID, mConversationId);
					intent.putExtra(Consts.INTENT_LAST_ONLINE, mLastOnlineTime);
					intent.putExtra(Consts.INTENT_IS_ONLINE, mIsOnline);
					startActivity(intent);
				}
		}

	}

}
