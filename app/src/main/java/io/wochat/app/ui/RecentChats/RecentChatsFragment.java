package io.wochat.app.ui.RecentChats;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.Date;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.UnreadMessagesConversation;
import io.wochat.app.db.entity.User;
import io.wochat.app.ui.AudioVideoCall.OutGoingCallActivity;
import io.wochat.app.ui.Consts;
import io.wochat.app.ui.Contact.ContactSelectorActivity;
import io.wochat.app.ui.MainActivity;
import io.wochat.app.ui.Messages.ConversationActivity;
import io.wochat.app.ui.settings.SettingsActivity;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.ConversationViewModel;
import io.wochat.app.viewmodel.UserViewModel;


public class RecentChatsFragment extends Fragment  implements
	DialogsListAdapter.OnDialogClickListener<Conversation>,
	DialogsListAdapter.OnDialogLongClickListener<Conversation>,
        DialogsListAdapter.OnCameraOrPhoneClickListener<Conversation>,
	DateFormatter.Formatter{

	private static final int CONTACT_SELECTOR_REQUEST_CODE = 1;
	//private RecentChatsViewModel mViewModel;
	private UserViewModel mUserViewModel;
	private String mSelfUserId;
	protected ImageLoader imageLoader;
	protected DialogsListAdapter<Conversation> dialogsAdapter;
	private DialogsList dialogsList;
	private ConstraintLayout mEmptyFrameCL;
	private ConversationViewModel mConversationViewModel;
	private List<Conversation> mConversation;
	private List<UnreadMessagesConversation> mUnreadMessagesConversation;
	private User mSelfUser;
	private String mSelfUserLang;
	private String mSelfUserName;

	public static RecentChatsFragment newInstance() {
		return new RecentChatsFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.recent_chats_fragment, container, false);
//		TextView tv = (TextView) view.findViewById(R.id.tv);
//		tv.setText("Recent Chats.....");

		setHasOptionsMenu(true);


		//mSelfUserId = WCSharedPreferences.getInstance(getContext()).getUserId();
		//mSelfUserLang = WCSharedPreferences.getInstance(getContext()).getUserLang();


//		mSelfUserId = share
		mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
		mUserViewModel.getSelfUser().observe(this, user -> {
			if (user != null) {
				mSelfUser = user;
				mSelfUserId = user.getUserId();
				mSelfUserLang = user.getLanguage();
				mSelfUserName = user.getUserName();
			}
			else {
				mSelfUserId = null;
				mSelfUserLang = null;
				mSelfUserName = null;
			}
		});

		mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);
		mConversationViewModel.getConversationListLD().observe(this, conversations -> {
			mConversation = conversations;
			if (conversations != null) {
				Log.e("AAA", "conversations count: " + conversations.size());
				mEmptyFrameCL.setVisibility(Utils.booleanToVisibilityInvisible(conversations.isEmpty()));
				for(Conversation cc : conversations) {
					Log.e("AAA", "conversations: " + cc.toString());
				}
				initAdapter();

				String conversationId = ((MainActivity) getActivity()).getIntentConversationId();
				if (conversationId != null){
					Conversation conversation = getConversation(conversationId);
					onDialogClick(conversation);
				}

			}
			else {
				Log.e("AAA", "conversations: null");
			}
		});
//			conversationCompletes -> {
//				mConversationCompletes = conversationCompletes;
//				if (conversationCompletes != null) {
//					for(ConversationComplete cc : conversationCompletes) {
//						Log.e("AAA", "ccList: " + cc.toString());
//					}
//					initAdapter();
//				}
//				else
//					Log.e("AAA", "ccList: null");
//			});


//		mConversationViewModel.getMediatorConversationLiveData().observe(this, new Observer() {
//			@Override
//			public void onChanged(@Nullable Object o) {
//				if ((o != null)&& (o instanceof List)){
//					List list = (List)o;
//					if (!list.isEmpty()){
//						if (list.get(0) instanceof ConversationComplete){
//							mConversationCompletes = list;
//							initAdapter();
//						}
//						else if (list.get(0) instanceof UnreadMessagesConversation){
//							mUnreadMessagesConversation = list;
//						}
//					}
//				}
//				if (o != null)
//					Log.e("AAA", "getMediatorConversationLiveData:" + o.toString());
//				else
//					Log.e("AAA", "getMediatorConversationLiveData: null");
//			}
//		});



		imageLoader = new ImageLoader() {
			@Override
			public void loadImageWPlaceholder(ImageView imageView, @Nullable String url, int placeholderResourceId, @Nullable Object payload) {
				if ((url != null)&& (url.equals("")))
					url = null;
				Picasso.get().load(url).placeholder(R.drawable.new_contact).error(R.drawable.new_contact).into(imageView);

			}

			@Override
			public void loadImageCenterCrop(ImageView imageView, @Nullable String url, @Nullable Object payload) {
				Picasso.get().load(url).resize(300,300).centerCrop().into(imageView);
			}

			@Override
			public void loadImageCenter(ImageView imageView, @Nullable String url, int placeholderResourceId, @Nullable Object payload) {
				Picasso.get().load(url).into(imageView);
			}

			@Override
			public void loadImageNoPlaceholder(ImageView imageView, int resourceId) {
				Picasso.get().load(resourceId).into(imageView);
			}

		};
		mEmptyFrameCL = (ConstraintLayout) view.findViewById(R.id.empty_frame_fl);
		mEmptyFrameCL.setVisibility(View.INVISIBLE);

		dialogsList = (DialogsList) view.findViewById(R.id.dialogsList);
		//initAdapter();

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//mViewModel = ViewModelProviders.of(this).get(RecentChatsViewModel.class);
		// TODO: Use the ViewModel
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main, menu);

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == R.id.action_settings) {
			Intent intent = new Intent(getContext(), SettingsActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	private void selectContact(){
		Intent intent = new Intent(getContext(), ContactSelectorActivity.class);
		startActivityForResult(intent, CONTACT_SELECTOR_REQUEST_CODE);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CONTACT_SELECTOR_REQUEST_CODE){
			if (resultCode == AppCompatActivity.RESULT_OK){
				String id = data.getStringExtra(Consts.INTENT_PARTICIPANT_ID);
				String name = data.getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
				String lang = data.getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
				String pic = data.getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
				String contactString = data.getStringExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ);
				String conversationId = Conversation.getConversationId(id, mSelfUserId);

				Intent intent = new Intent(getContext(), ConversationActivity.class);
				intent.putExtra(Consts.INTENT_PARTICIPANT_ID, id);
				intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, name);
				intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, lang);
				intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, pic);
				intent.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, contactString);
				intent.putExtra(Consts.INTENT_CONVERSATION_ID, conversationId);
				intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
				intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
				intent.putExtra(Consts.INTENT_SELF_NAME, mSelfUserName);
				intent.putExtra(Consts.INTENT_SELF_PIC_URL, mSelfUser.getProfilePicUrl());
				startActivity(intent);

			}
		}
	}

	@Override
	public void onDialogClick(Conversation conversation) {
		Intent intent = new Intent(getContext(), ConversationActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, conversation.getParticipantId());
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, conversation.getParticipantName());
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, conversation.getParticipantLanguage());
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, conversation.getParticipantProfilePicUrl());
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, conversation.getId());
		intent.putExtra(Consts.INTENT_SELF_PIC_URL, mSelfUser.getProfilePicUrl());
		intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
		intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
		intent.putExtra(Consts.INTENT_SELF_NAME, mSelfUserName);
		startActivity(intent);
	}


	@Override
	public void onCameraOrPhoneClic(Conversation conversation, boolean isVideoCall) {
		Intent intent = new Intent(getContext(), OutGoingCallActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, conversation.getParticipantId());
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, conversation.getParticipantName());
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, conversation.getParticipantLanguage());
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, conversation.getParticipantProfilePicUrl());
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, conversation.getId());
		intent.putExtra(Consts.INTENT_SELF_PIC_URL, mSelfUser.getProfilePicUrl());
		intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
		intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
		intent.putExtra(Consts.INTENT_SELF_NAME, mSelfUserName);
		intent.putExtra(Consts.INTENT_IS_VIDEO_CALL, isVideoCall);
		startActivity(intent);
	}

	@Override
	public void onDialogLongClick(Conversation dialog) {

	}

	@Override
	public String format(Date date) {
		return Utils.dateFormatter(getContext(), date);
	}


	private void initAdapter() {
		dialogsAdapter = new DialogsListAdapter<>(
			R.layout.item_custom_dialog_view_holder_new,
			CustomDialogViewHolder.class,
			imageLoader);

		//dialogsAdapter.setItems(DialogsFixtures.getDialogs());
		dialogsAdapter.setItems(mConversation);

		dialogsAdapter.setOnDialogClickListener(this);

		dialogsAdapter.setOnDialogLongClickListener(this);

		dialogsAdapter.setOnCameraOrPhoneClickListener(this);

		dialogsAdapter.setDatesFormatter(this);

		dialogsList.setAdapter(dialogsAdapter);
	}




//	@Override
//	public void onResume() {
//		super.onResume();
//		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				WCService service = ((MainActivity) getActivity()).getService();
//				if (service != null) {
//					//service.unSubscribeContact(mSelfUserId);
//					service.subscribe(mConversation);
//				}
//			}
//		}, 500);
//
//
//		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				WCService service = ((MainActivity) getActivity()).getService();
//				if (service != null)
//					service.getAllPresence();
//			}
//		}, 3000);
//	}



	private Conversation getConversation(String conversationId){
		for(Conversation conversation : mConversation){
			if (conversationId.equals(conversation.getConversationId()))
				return conversation;
		}
		return null;
	}

//	public void displayConversation(String conversationId) {
//		Conversation conversation = getConversation(conversationId);
//		onDialogClick(conversation);
//	}
}
