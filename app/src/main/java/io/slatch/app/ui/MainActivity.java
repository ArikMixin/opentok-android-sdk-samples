package io.slatch.app.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import io.slatch.app.R;
import io.slatch.app.WCService;
import io.slatch.app.components.BadgedTabLayout;
import io.slatch.app.db.WCSharedPreferences;
import io.slatch.app.db.entity.Conversation;
import io.slatch.app.db.entity.User;
import io.slatch.app.ui.Contact.ContactMultiSelectorActivity;
import io.slatch.app.ui.Contact.ContactSelectorActivity;
import io.slatch.app.ui.Group.NewGroupActivity;
import io.slatch.app.ui.Messages.ConversationActivity;
import io.slatch.app.ui.RecentCalls.RecentCallsFragment;
import io.slatch.app.ui.RecentChats.RecentChatsFragment;
import io.slatch.app.utils.ImagePickerUtil;
import io.slatch.app.viewmodel.ContactViewModel;
import io.slatch.app.viewmodel.ConversationViewModel;
import io.slatch.app.viewmodel.UserViewModel;


public class MainActivity extends AppCompatActivity {

	private static final int TAB_POSITION_CAMERA = 0;
	private static final int TAB_POSITION_CHAT = 1;
	private static final int TAB_POSITION_CALL = 2;


	private int mFragmentsTitles[] = new int[] {
		R.string.camera_title,
		R.string.chat_title,
		R.string.calls_title};


	private static final String TAG = "MainActivity";
	public static final String PLAY_STORE_URL = "market://details?id=io.wochat.app";
	private static final int REQUEST_CONTACT_SELECTOR = 1;
	private static final int REQUEST_SELECT_CAMERA_PHOTO = 2;
	private static final int REQUEST_SELECT_CONTACTS_MULTI = 3;
	private static final int REQUEST_NEW_GROUP_CONTACTS_SELECT = 4;
	private static final int REQUEST_NEW_GROUP_PIC_NAME = 5;

	private SectionsPagerAdapter mSectionsPagerAdapter;

	private ViewPager mViewPager;

	private VelocityTracker mVelocityTracker = null;
	private Boolean mInOpen = false;
	private FloatingActionButton mFab;
	private int mLastSelectioPage;
	private boolean mBound;
	private WCService mService;
	private String mSelfUserId;
	private String mSelfUserLang;
	private String mSelfUserName;
	private ConversationViewModel mConversationViewModel;
	private UserViewModel mUserViewModel;
	private User mSelfUser;
	private String mIntentConversationId;
	private Uri mCameraPhotoFileUri;
	private String mLastSelectedContactsObj;
	private boolean doubleBackPressedFlag = false;
	private String mCurrentVersion;
	private AlertDialog mUpdateDialog = null;
	private AlertDialog.Builder mUpdatebuilder;
	private WCSharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSharedPreferences = WCSharedPreferences.getInstance(this);
		ratingDialog();

		String token = FirebaseInstanceId.getInstance().getToken();
		Log.e(TAG, "Firebase token: " + token);


		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.container_vp);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		int currentItem = TAB_POSITION_CHAT;


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


//		mSelfUserId = WCSharedPreferences.getInstance(this).getUserId();
//		mSelfUserLang = WCSharedPreferences.getInstance(this).getUserLang();

		mLastSelectioPage = currentItem;
		mViewPager.setCurrentItem(currentItem);
		//getSupportActionBar().setTitle(mFragmentsTitles[currentItem]); (Title in the right side)



		BadgedTabLayout tabLayout = (BadgedTabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);
		//tabLayout.setBadgeText(1,"10");
		tabLayout.setBadgeText(1,null);
		tabLayout.setIcon(0, R.drawable.ic_action_camera);


		LinearLayout layout = ((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(TAB_POSITION_CAMERA));
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
		layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		layout.setLayoutParams(layoutParams);

		LinearLayout layout1 = ((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(TAB_POSITION_CHAT));
		LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) layout1.getLayoutParams();
		layoutParams1.weight = 1;
		layoutParams1.width = LinearLayout.LayoutParams.MATCH_PARENT;
		layout1.setLayoutParams(layoutParams1);

		LinearLayout layout2 = ((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(TAB_POSITION_CALL));
		LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) layout2.getLayoutParams();
		layoutParams2.weight = 1;
		layoutParams2.width = LinearLayout.LayoutParams.MATCH_PARENT;
		layout2.setLayoutParams(layoutParams2);


		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {

				// 	getSupportActionBar().setTitle(mFragmentsTitles[position]); (Title in the right side)
				if (position == TAB_POSITION_CHAT) {
					mLastSelectioPage = position;
					mFab.setVisibility(View.VISIBLE);
					mFab.setImageResource(R.drawable.new_chat);
				}
				else if (position == TAB_POSITION_CALL) {
					mLastSelectioPage = position;
					mFab.setVisibility(View.VISIBLE);
					mFab.setImageResource(R.drawable.new_call);
				}
				else {
					openCamera();
					mFab.setVisibility(View.GONE);
					new Handler(getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
							mViewPager.setCurrentItem(mLastSelectioPage);
						}
					}, 1000);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});


		mFab = (FloatingActionButton) findViewById(R.id.fab);
		mFab.setImageResource(R.drawable.new_chat);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, ContactSelectorActivity.class);
				if(mViewPager.getCurrentItem() == TAB_POSITION_CHAT) {
					intent.putExtra("CALL", false);
					intent.putExtra("CHAT", true);
				}
				else if(mViewPager.getCurrentItem() == TAB_POSITION_CALL) {
					intent.putExtra("CALL", true);
					intent.putExtra("CHAT", false);
				}
				startActivityForResult(intent, REQUEST_CONTACT_SELECTOR);
			}
		});






		ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
		contactViewModel.sycContacts();


		mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);
		mConversationViewModel.getUnreadConversationNum().observe(this, num -> {
			if ((num != null)&& (num > 0))
				tabLayout.setBadgeText(1,num + "");
			else
				tabLayout.setBadgeText(1,null);
		});


		if (getIntent().hasExtra(Consts.INTENT_CONVERSATION_ID)){
			mIntentConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
			getIntent().removeExtra(Consts.INTENT_CONVERSATION_ID);
		}
		else {
			mIntentConversationId = null;
		}


	}

	@Override
	protected void onResume() {
		super.onResume();
		getCurrentAppVersion(); // Force update if there is a new version of the app in google play
	}

	public String getIntentConversationId(){ // one timer
		String tmpIntentConversationId = mIntentConversationId;
		mIntentConversationId = null;
		return tmpIntentConversationId;
	}


//	@Override
//	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		if (intent.hasExtra(Consts.INTENT_CONVERSATION_ID)){
//			mIntentConversationId = intent.getStringExtra(Consts.INTENT_CONVERSATION_ID);
//			getIntent().removeExtra(Consts.INTENT_CONVERSATION_ID);
//			mViewPager.setCurrentItem(TAB_POSITION_CHAT);
//			RecentChatsFragment f = (RecentChatsFragment)mSectionsPagerAdapter.getItem(TAB_POSITION_CHAT);
//			f.displayConversation(mIntentConversationId);
//		}
//	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.e(TAG, "onStart, call bindService WCService");
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
			mService.setCurrentConversationId(null);
			mBound = true;

		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.e(TAG, "ServiceConnection: onServiceDisconnected");

			mService = null;
			mBound = false;
		}
	};


	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		public PlaceholderFragment() {
		}

		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static Fragment newInstance(int sectionNumber) {
			if (sectionNumber == TAB_POSITION_CHAT){
				RecentChatsFragment recentChatsFragment = new RecentChatsFragment();
				return recentChatsFragment;
			}
			else if (sectionNumber == TAB_POSITION_CALL){
				RecentCallsFragment recentCallsFragment = new RecentCallsFragment();
				return recentCallsFragment;
			}
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_activity3, container, false);
			TextView textView = (TextView) rootView.findViewById(R.id.section_label);
			textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	
//	public static class PlaceholderFragment extends Fragment {
//
//		public PlaceholderFragment() {
//		}
//
//
//		public static Fragment newInstance() {
//			PlaceholderFragment fragment = new PlaceholderFragment();
//			return fragment;
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//								 Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main_activity3, container, false);
//			return rootView;
//		}
//	}
//
//
//
//	public Fragment newFragmentInstance(int position) {
//		if (position == TAB_POSITION_CHAT){
//			RecentChatsFragment recentChatsFragment = new RecentChatsFragment();
//			return recentChatsFragment;
//		}
//		else if (position == TAB_POSITION_CALL){
//			RecentCallsFragment recentCallsFragment = new RecentCallsFragment();
//			return recentCallsFragment;
//		}
//		else {
//			return PlaceholderFragment.newInstance();
//		}
//	}


	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private String tabTitles[] = new String[] {
			"",
			getString(R.string.tab_text_1),
			getString(R.string.tab_text_2)};


		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return PlaceholderFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			return tabTitles.length;
		}

		@Nullable
		@Override
		public CharSequence getPageTitle(int position) {
			return tabTitles[position];

		}






	}


	private void openCamera(){
		synchronized (mInOpen) {
			if (!mInOpen){
				mInOpen = true;
				new Handler(getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						mInOpen = false;
					}
				}, 1000);
				Log.d(TAG,"OOOOOOOOOOOOOOOOOOOOO");
				dispatchTakePictureIntent();
			}
		}

	}




	private void dispatchTakePictureIntent() {

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_SELECT_CAMERA_PHOTO){
			if (resultCode == RESULT_OK) {
				//mCameraPhotoFileUri // the image result
				Intent intent2 = new Intent(this, ContactMultiSelectorActivity.class);
				startActivityForResult(intent2, REQUEST_SELECT_CONTACTS_MULTI);
				overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
			}
		}
		else if (requestCode == REQUEST_SELECT_CONTACTS_MULTI){
			if (resultCode == RESULT_OK) {
				//mCameraPhotoFileUri // the image result
				String[] contacts = intent.getStringArrayExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_RESULT);
				sendImageToContacts(contacts, mCameraPhotoFileUri);

			}
		}
		else if (requestCode == REQUEST_CONTACT_SELECTOR){
			if (resultCode == RESULT_OK){
				String id = intent.getStringExtra(Consts.INTENT_PARTICIPANT_ID);
				String name = intent.getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
				String lang = intent.getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
				String pic = intent.getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
				String contactString = intent.getStringExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ);
				String conversationId = Conversation.getConversationId(id, mSelfUserId);

				if (id.equals("")){
					createGroup();
					return;
				}

				Intent intent1 = new Intent(this, ConversationActivity.class);
				intent1.putExtra(Consts.INTENT_PARTICIPANT_ID, id);
				intent1.putExtra(Consts.INTENT_PARTICIPANT_NAME, name);
				intent1.putExtra(Consts.INTENT_PARTICIPANT_LANG, lang);
				intent1.putExtra(Consts.INTENT_PARTICIPANT_PIC, pic);
				intent1.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, contactString);
				intent1.putExtra(Consts.INTENT_CONVERSATION_ID, conversationId);
				intent1.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
				intent1.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
				intent1.putExtra(Consts.INTENT_SELF_NAME, mSelfUserName);
				startActivity(intent1);

			}
		}
		else if (requestCode == REQUEST_NEW_GROUP_CONTACTS_SELECT){
			if (resultCode == RESULT_OK) {
				String[] contacts = intent.getStringArrayExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_RESULT);
				mLastSelectedContactsObj = intent.getStringExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_OBJ_RESULT);
				Intent intent1 = new Intent(this, NewGroupActivity.class);
				intent1.putExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_RESULT, contacts);
				intent1.putExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_OBJ_RESULT, mLastSelectedContactsObj);
				startActivityForResult(intent1, REQUEST_NEW_GROUP_PIC_NAME);
				overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
				Log.e(TAG, "contacts: " + contacts);
			}
		}
		else if (requestCode == REQUEST_NEW_GROUP_PIC_NAME){
			if (resultCode == RESULT_OK) {
				Conversation conversation = Conversation.fromJson(intent.getStringExtra(Consts.INTENT_CONVERSATION_OBJ));
				openConversationActivity(conversation);
			}
			else {
				createGroup(mLastSelectedContactsObj);
			}
		}
	}



	public WCService getService(){
		return mService;
	}

	private void sendImageToContacts(String[] contacts, Uri cameraPhotoFileUri) {
		Log.e(TAG, "sendImageToContacts: " + contacts.toString() + " , " + cameraPhotoFileUri);
		int totalMessagesToSend = contacts.length;
		mConversationViewModel.sendImageToContacts(contacts, cameraPhotoFileUri);
		mConversationViewModel.getOutgoingPendingMessagesLD().observe(this, pendingMessages -> {
			Log.e(TAG, "forwardMessagesToContacts, getOutgoingPendingMessages() result: " + pendingMessages.size());

			if ((pendingMessages != null)&&(pendingMessages.size()>= totalMessagesToSend)) {
				mService.sendMessages(pendingMessages);
//				if (mForwardContactId != null) {
//					String conversationId = Conversation.getConversationId(mSelfId, mForwardContactId);
//					Log.e(TAG, "forwardMessagesToContacts, new conversationId: " + conversationId);
//					mConversationViewModel.getConversationLD(conversationId).observe(this, conversation -> {
//						OpenConversationActivity(conversation);
//					});
//				}
			}
		});


	}

	@Override
	public void onBackPressed(){
		if (doubleBackPressedFlag) {
			super.onBackPressed();
			return;
		}

		this.doubleBackPressedFlag = true;
		Toast.makeText(this, getResources().getString(R.string.please_click_back), Toast.LENGTH_SHORT).show();

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleBackPressedFlag = false;
			}
		}, 2000);
	}

	public void createGroup(){
		Intent intent = new Intent(this, ContactMultiSelectorActivity.class);
		intent.putExtra(Consts.INTENT_TITLE, getString(R.string.new_group));
		intent.putExtra(Consts.INTENT_ACTION_ICON, R.drawable.ic_action_right_arrow);
		startActivityForResult(intent, REQUEST_NEW_GROUP_CONTACTS_SELECT);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
	}

	public void createGroup(String contactsObj){
		Intent intent = new Intent(this, ContactMultiSelectorActivity.class);
		intent.putExtra(Consts.INTENT_TITLE, getString(R.string.new_group));
		intent.putExtra(Consts.INTENT_ACTION_ICON, R.drawable.ic_action_right_arrow);
		intent.putExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_OBJ_RESULT, contactsObj);
		startActivityForResult(intent, REQUEST_NEW_GROUP_CONTACTS_SELECT);
		overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
	}


	private void openConversationActivity(Conversation conversation) {
		Intent intent = new Intent(this, ConversationActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, conversation.getParticipantId());
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, conversation.getParticipantName());
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, conversation.getParticipantLanguage());
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, conversation.getParticipantProfilePicUrl());
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, conversation.getId());
		intent.putExtra(Consts.INTENT_CONVERSATION_OBJ, conversation.toJson());
		intent.putExtra(Consts.INTENT_SELF_PIC_URL, mSelfUser.getProfilePicUrl());
		intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
		intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
		intent.putExtra(Consts.INTENT_SELF_NAME, mSelfUserName);
		startActivity(intent);
	}

	private void getCurrentAppVersion(){
		PackageManager pm = this.getPackageManager();
		PackageInfo pInfo = null;

		try {
			pInfo =  pm.getPackageInfo(this.getPackageName(),0);
		} catch (PackageManager.NameNotFoundException e1) {
			e1.printStackTrace();
		}
		mCurrentVersion = pInfo.versionName;

		mConversationViewModel.checkLatestVersion();
		mConversationViewModel.getLatestVersion().observe(this, latestVersion -> {
			if(latestVersion != null) {
						if(Integer.parseInt(mCurrentVersion.replace(".", "")) <
								Integer.parseInt(latestVersion.replace(".", ""))){
							showUpdateDialog();
						}
			}
		});
	}

	private void showUpdateDialog(){
		mUpdatebuilder = new AlertDialog.Builder(this);
		mUpdatebuilder.setTitle("Update Required");
		mUpdatebuilder.setMessage("WoChat is out of date. Please visit the Google Play Store to update to the latest version");
		mUpdatebuilder.setPositiveButton("Update", (dialog, which) -> {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL)));
			dialog.dismiss();
		});

		mUpdatebuilder.setNegativeButton("Cancel", (dialog, which) -> finishAndRemoveTask());
		mUpdatebuilder.setCancelable(false);
		if ((mUpdateDialog == null) || !mUpdateDialog.isShowing())
							mUpdateDialog = mUpdatebuilder.show();
	}

	private void ratingDialog() {
		///Reset session (For Tests)
//		SharedPreferences aa =  getSharedPreferences("RatingDialog", Context.MODE_PRIVATE);
//		aa.edit().clear().apply();

		//Wait 1 day in the first apps enter start session counting (In order not to nag at the user)
		if(mSharedPreferences.geFirstEnterMillis() == -1) {
			mSharedPreferences.saveFirstEnterMillis(System.currentTimeMillis()); // Save first enter time
		}else {
			if (System.currentTimeMillis() < mSharedPreferences.geFirstEnterMillis() + TimeUnit.DAYS.toMillis(1))
												return;
		}
		//Start rating dialog process
		RatingDialog ratingDialog = new RatingDialog.Builder(this)
				.threshold(3) // Only if user rate the app 4-5 , we open the activity - if user rate 1-3 - we open feedback form
				.session(5) // After 5 entry to app - show the dialog - if user click later - the session will start counting 5 times
				.title(getString(R.string.rate_us))
				.playstoreUrl(PLAY_STORE_URL)
				.build();
		ratingDialog.show();

	}
}
