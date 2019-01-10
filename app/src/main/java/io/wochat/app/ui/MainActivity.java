package io.wochat.app.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.rahimlis.badgedtablayout.BadgedTabLayout;
import io.wochat.app.components.BadgedTabLayout;

import io.wochat.app.R;
import io.wochat.app.ui.Contact.ContactSelectorActivity;
import io.wochat.app.viewmodel.ContactViewModel;


public class MainActivity extends AppCompatActivity {

	private static final int TAB_POSITION_CAMERA = 0;
	private static final int TAB_POSITION_CHAT = 1;
	private static final int TAB_POSITION_CALL = 2;

	private int mFragmentsTitles[] = new int[] {
		R.string.camera_title,
		R.string.chat_title,
		R.string.calls_title};


	private static final String TAG = "MainActivity";
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;

	private VelocityTracker mVelocityTracker = null;
	private Boolean mInOpen = false;
	private FloatingActionButton mFab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		int currentItem = 1;
		mViewPager.setCurrentItem(currentItem);
		getSupportActionBar().setTitle(mFragmentsTitles[currentItem]);



		BadgedTabLayout tabLayout = (BadgedTabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);
		tabLayout.setBadgeText(1,"10");
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
					mFab.setVisibility(View.VISIBLE);
					mFab.setImageResource(R.drawable.new_chat);
				}
				else if (position == TAB_POSITION_CALL) {
					mFab.setVisibility(View.VISIBLE);
					mFab.setImageResource(R.drawable.new_call);
				}
				else {
					openCamera();
					mFab.setVisibility(View.GONE);
					new Handler(getMainLooper()).postDelayed(new Runnable() {
						@Override
						public void run() {
							mViewPager.setCurrentItem(TAB_POSITION_CHAT);
						}
					}, 1000);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});


		mFab = (FloatingActionButton) findViewById(R.id.fab);
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
				startActivityForResult(intent, 1);
			}
		});





		ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
		contactViewModel.sycContacts();

	}




	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}


		public static Fragment newInstance() {
			PlaceholderFragment fragment = new PlaceholderFragment();
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_activity3, container, false);
			return rootView;
		}
	}



	public Fragment newFragmentInstance(int position) {
		if (position == TAB_POSITION_CHAT){
			RecentChatsFragment recentChatsFragment = new RecentChatsFragment();
			return recentChatsFragment;
		}
		else if (position == TAB_POSITION_CALL){
			RecentCallsFragment recentCallsFragment = new RecentCallsFragment();
			return recentCallsFragment;
		}
		else {
			return PlaceholderFragment.newInstance();
		}
	}


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
			return newFragmentInstance(position );
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


	static final int REQUEST_IMAGE_CAPTURE = 1;

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			Bitmap imageBitmap = (Bitmap) extras.get("data");
			//mImageView.setImageBitmap(imageBitmap);
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
