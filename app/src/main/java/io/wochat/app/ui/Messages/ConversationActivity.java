package io.wochat.app.ui.Messages;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.WCService;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.Message;
import io.wochat.app.db.fixtures.MessagesFixtures;
import io.wochat.app.ui.Consts;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.ConversationViewModel;



public class ConversationActivity extends AppCompatActivity implements
	MessagesListAdapter.OnMessageLongClickListener<Message>,
	MessagesListAdapter.OnLoadMoreListener,
	MessageInput.InputListener,
	MessageInput.AttachmentsListener,
	MessagesListAdapter.OnMessageViewLongClickListener<Message>,
	MessagesListAdapter.OnMessageViewClickListener<Message>,
	MessagesListAdapter.OnMessageClickListener<Message>,
	MessagesListAdapter.OnBindViewHolder,
	MessageInput.TypingListener, DateFormatter.Formatter {

	private static final String TAG = "ConversationActivity";
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
	private WCService mService;
	private boolean mBound;
	private Conversation mConversation;
	//private Contact mParticipantContactObj;
	private String mParticipantLang;
	private String mSelfLang;
	private TypingSignalBR mTypingSignalBR;
	private TextView mContactDetailsTV;
	private Handler mClearTypingHandler;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mImageLoader = new ImageLoader() {
			@Override
			public void loadImage(ImageView imageView, String url, Object payload) {
				if ((url != null)&& (url.equals("")))
					url = null;
				Picasso.get().load(url).placeholder(R.drawable.ic_new_contact_invert).error(R.drawable.ic_new_contact_invert).into(imageView);
			}

			@Override
			public void loadImage(ImageView imageView, int resourceId) {
				Picasso.get().load(resourceId).into(imageView);
			}

			@Override
			public void loadImageNoPlaceholder(ImageView imageView, int resourceId) {
				Picasso.get().load(resourceId).into(imageView);
			}
		};

		setContentView(R.layout.activity_conversation);


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
//		String participantContactString = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ);
//		mParticipantContactObj = Contact.fromJson(participantContactString);
		mSelfId = getIntent().getStringExtra(Consts.INTENT_SELF_ID);
		mSelfLang = getIntent().getStringExtra(Consts.INTENT_SELF_LANG);

		mContactNameTV = (TextView) findViewById(R.id.contact_name_tv);
		mContactDetailsTV = (TextView) findViewById(R.id.contact_details_tv);
		mContactDetailsTV.setText("");
		mContactNameTV.setText(mParticipantName);
		mContactNameTV.setOnClickListener(v -> {
			Toast.makeText(ConversationActivity.this, "open profile for " + mParticipantName, Toast.LENGTH_SHORT).show();
		});

		mContactAvatarCIV = (CircleFlagImageView) findViewById(R.id.contact_avatar_civ);
		mContactAvatarCIV.setInfo(mParticipantPic, mParticipantLang);
		//mImageLoader.loadImage(mContactAvatarCIV.get, mParticipantPic, null);
		mContactAvatarCIV.setOnClickListener(v -> {
			Utils.showImage(ConversationActivity.this, mParticipantPic);
		});

		mMessagesListRV = (MessagesList) findViewById(R.id.messagesList);

		initAdapter();

		mMessageInput = (MessageInput) findViewById(R.id.input);
		mMessageInput.setTypingListener(this);
		mMessageInput.setInputListener(this);
		mMessageInput.setAttachmentsListener(this);
		@DrawableRes int flagDrawable = Utils.getCountryFlagDrawableFromLang(mSelfLang);
		mMessageInput.setMagicButtonDrawable(getDrawable(flagDrawable));

		mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);





		mConversationViewModel.getConversationAndMessages(mConversationId,
			mParticipantId,
			mParticipantPic,
			mParticipantName,
			mParticipantLang,
			mSelfId,
			conversationAndItsMessages -> {
				mConversation = conversationAndItsMessages.getConversation();
				mMessages = conversationAndItsMessages.getMessages();
				startListenToMessagesChanges();
		});



		mTypingSignalBR = new TypingSignalBR();



	}


	private void startListenToMessagesChanges() {
		mConversationViewModel.getMessagesLD(mConversationId).observe(this,
			messages -> {
				mMessages = messages;
				updateUIWithMessages();
			});

	}


	private void initAdapter() {

		//We can pass any data to ViewHolder with payload
		CustomIncomingTextMessageViewHolder.Payload payload = new CustomIncomingTextMessageViewHolder.Payload();
		//For example click listener
		payload.avatarClickListener = new CustomIncomingTextMessageViewHolder.OnAvatarClickListener() {
			@Override
			public void onAvatarClick() {
				Toast.makeText(ConversationActivity.this,
					"Text message avatar clicked", Toast.LENGTH_SHORT).show();
			}
		};
		MessageHolders holdersConfig = new MessageHolders()
			.setIncomingTextConfig(
				CustomIncomingTextMessageViewHolder.class,
				R.layout.item_custom_incoming_text_message,
				payload)
			.setOutcomingTextConfig(
				CustomOutcomingTextMessageViewHolder.class,
				R.layout.item_custom_outcoming_text_message)
			.setIncomingImageConfig(
				CustomIncomingImageMessageViewHolder.class,
				R.layout.item_custom_incoming_image_message)
			.setOutcomingImageConfig(
				CustomOutcomingImageMessageViewHolder.class,
				R.layout.item_custom_outcoming_image_message);

		mMessagesAdapter = new MessagesListAdapter<>(mSelfId, holdersConfig, mImageLoader);
		mMessagesAdapter.setOnMessageLongClickListener(this);
		mMessagesAdapter.setOnMessageViewLongClickListener(this);
		mMessagesAdapter.setLoadMoreListener(this);
		mMessagesAdapter.setOnMessageViewClickListener(this);
		mMessagesAdapter.setOnMessageClickListener(this);
		mMessagesAdapter.setOnBindViewHolder(this);
		mMessagesAdapter.setDateHeadersFormatter(this);

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
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home){
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_conversation, menu);
		Drawable d0 = menu.getItem(0).getIcon(); // change 0 with 1,2 ...
		Drawable d1 = menu.getItem(1).getIcon();
		d0.mutate();
		d1.mutate();
		d0.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
		d1.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
		return true;
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
		Message message = new Message(mParticipantId, mSelfId, mConversationId, msgText, mSelfLang);
		mConversationViewModel.addNewOutcomingMessage(message);
		mMessageInput.getButton().setImageDrawable(getDrawable(R.drawable.msg_in_mic_light));
		mService.sendMessage(message);
		return true;

	}

	@Override
	public void onAddAttachments() {
		mMessagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
	}




	@Override
	public void onMessageViewClick(View view, Message message) {

	}


	@Override
	public void onMessageViewLongClick(View view, Message message) {

	}

	@Override
	public void onMessageClick(Message message) {

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
		}
		mService.sendTypingSignal(mParticipantId, mConversationId, true);
	}

	@Override
	public void onStopTyping() {
		String theText = mMessageInput.getInputEditText().getText().toString();
		if (theText.equals("")) {
			mMessageInput.getButton().setImageDrawable(getDrawable(R.drawable.msg_in_mic_light));
		}
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


	private void updateUIWithMessagesOnLoadMore(){
		ArrayList<Message> messages = getMessagesBefore(pointerDateUpperList);
		if (messages.size()>0) {
			pointerDateUpperList = messages.get(messages.size() - 1).getCreatedAt();
			mMessagesAdapter.addToEnd(messages, false);
		}
	}

	private void updateUIWithMessages(){
		if ((pointerDateUpperList == null)&& (pointerDateBottomList == null)){ // RV is empty
			if (mMessages.size()>0){
				Message message = mMessages.get(0);
				pointerDateUpperList = message.getCreatedAt();
				pointerDateBottomList = message.getCreatedAt();
				mMessagesAdapter.addToStart(message, true);
				return;
			}
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
			Log.e(TAG, "ServiceConnection: onServiceConnected");
			WCService.WCBinder binder = (WCService.WCBinder) service;
			mService = binder.getService();
			mBound = true;
			initMarkAsReadMessagesHandling();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.e(TAG, "ServiceConnection: onServiceDisconnected");

			mService = null;
			mBound = false;
		}
	};

	private void initMarkAsReadMessagesHandling() {
		mConversationViewModel.getUnreadMessagesConversation(mConversationId).
			observe(this, messageList -> {
				Log.e(TAG, "getMarkAsReadAffectedMessages change: " + messageList==null?"null":""+messageList.size());
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

	private class TypingSignalBR extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(WCService.TYPING_SIGNAL_ACTION)) {
				String conversationId = intent.getStringExtra(WCService.CONVERSATION_ID_EXTRA);
				if ((conversationId != null)&&(conversationId.equals(mConversationId))){
					boolean isTyping = intent.getBooleanExtra(WCService.IS_TYPING_EXTRA, false);
					displayUITypingSignal(isTyping);
				}
			}
		}
	}

	private Runnable mClearTypingRunnable = new Runnable() {
		@Override
		public void run() {
			mContactDetailsTV.setText("");
		}
	};



	private void displayUITypingSignal(boolean isTyping) {

		if (isTyping){
			mContactDetailsTV.setText("Typing...");
			mClearTypingHandler.postDelayed(mClearTypingRunnable, 10000);
		}
		else {
			mContactDetailsTV.setText("");
			mClearTypingHandler.removeCallbacks(mClearTypingRunnable);
		}
	}


	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mTypingSignalBR);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mClearTypingHandler = new Handler(getMainLooper());
		IntentFilter filter = new IntentFilter();
		filter.addAction(WCService.TYPING_SIGNAL_ACTION);
		try {
			registerReceiver(mTypingSignalBR, filter);
		} catch (Exception e) {}

	}
}
