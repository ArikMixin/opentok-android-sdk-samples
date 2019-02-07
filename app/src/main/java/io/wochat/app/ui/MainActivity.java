package io.wochat.app.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.wochat.app.R;
import io.wochat.app.WCService;
import io.wochat.app.components.BadgedTabLayout;
import io.wochat.app.db.WCSharedPreferences;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.ui.Contact.ContactSelectorActivity;
import io.wochat.app.ui.Messages.ConversationActivity;
import io.wochat.app.ui.RecentChats.RecentChatsFragment;
import io.wochat.app.viewmodel.ContactViewModel;
import io.wochat.app.viewmodel.ConversationViewModel;


public class MainActivity extends AppCompatActivity {

	private static final int TAB_POSITION_CAMERA = 0;
	private static final int TAB_POSITION_CHAT = 1;
	private static final int TAB_POSITION_CALL = 2;

	private int mFragmentsTitles[] = new int[] {
		R.string.camera_title,
		R.string.chat_title,
		R.string.calls_title};


	private static final String TAG = "MainActivity";
	private static final int CONTACT_SELECTOR_REQUEST_CODE = 1;
	static final int REQUEST_IMAGE_CAPTURE = 2;
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
	private ConversationViewModel mConversationViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.container_vp);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		int currentItem = TAB_POSITION_CHAT;

		mSelfUserId = WCSharedPreferences.getInstance(this).getUserId();
		mSelfUserLang = WCSharedPreferences.getInstance(this).getUserLang();

		mLastSelectioPage = currentItem;
		mViewPager.setCurrentItem(currentItem);
		getSupportActionBar().setTitle(mFragmentsTitles[currentItem]);



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

				getSupportActionBar().setTitle(mFragmentsTitles[position]);
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
				startActivityForResult(intent, CONTACT_SELECTOR_REQUEST_CODE);
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


	}




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
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE){
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				Bitmap imageBitmap = (Bitmap) extras.get("data");
				//mImageView.setImageBitmap(imageBitmap);
			}
		}
		else if (requestCode == CONTACT_SELECTOR_REQUEST_CODE){
			if (resultCode == RESULT_OK){
				String id = data.getStringExtra(Consts.INTENT_PARTICIPANT_ID);
				String name = data.getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
				String lang = data.getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
				String pic = data.getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
				String contactString = data.getStringExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ);
				String conversationId = Conversation.getConversationId(id, mSelfUserId);

				Intent intent = new Intent(this, ConversationActivity.class);
				intent.putExtra(Consts.INTENT_PARTICIPANT_ID, id);
				intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, name);
				intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, lang);
				intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, pic);
				intent.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, contactString);
				intent.putExtra(Consts.INTENT_CONVERSATION_ID, conversationId);
				intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
				intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
				startActivity(intent);

			}
		}
	}


//	private class MYTabLayoutOnPageChangeListener extends TabLayout.TabLayoutOnPageChangeListener{
//
//		public MYTabLayoutOnPageChangeListener(TabLayout tabLayout) {
//			super(tabLayout);
//		}
//
//
//	}

}
