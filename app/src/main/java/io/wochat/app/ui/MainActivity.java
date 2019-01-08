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
import android.widget.TextView;

//import com.rahimlis.badgedtablayout.BadgedTabLayout;
import io.wochat.app.components.BadgedTabLayout;

import io.wochat.app.R;
import io.wochat.app.ui.Contact.ContactSelectorActivity;
import io.wochat.app.viewmodel.ContactViewModel;


public class MainActivity extends AppCompatActivity {

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
	private ImageButton mCameraIB;

	private VelocityTracker mVelocityTracker = null;
	private Boolean mInOpen = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);


		//https://github.com/rahimlis/badgedtablayout
		//TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

		BadgedTabLayout tabLayout = (BadgedTabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);
		tabLayout.setBadgeText(0,"10");


//		BadgedTabLayout tabLayout = (BadgedTabLayout) findViewById(R.id.tabs);
//		tabLayout.setBadgeText(0,"2");
//		tabLayout.setupWithViewPager(mViewPager);



		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
		mViewPager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int index = event.getActionIndex();
				int action = event.getActionMasked();
				int pointerId = event.getPointerId(index);

				switch(action) {
					case (MotionEvent.ACTION_DOWN) :
						Log.d(TAG,"Action was DOWN");
						if(mVelocityTracker == null) {
							mVelocityTracker = VelocityTracker.obtain();
						}
						else {
							mVelocityTracker.clear();
						}
						mVelocityTracker.addMovement(event);
						break;

					case (MotionEvent.ACTION_MOVE) :
						mVelocityTracker.addMovement(event);
						mVelocityTracker.computeCurrentVelocity(1000);

						if (mVelocityTracker.getXVelocity(pointerId) > 100){
							if (mViewPager.getCurrentItem() == 0){
								openCamera();
								return true;
							}
						}
						break;

					case MotionEvent.ACTION_UP:
						try {
							mVelocityTracker.recycle();
						} catch (Exception e) {}
						break;
					case MotionEvent.ACTION_CANCEL:
						try {
							mVelocityTracker.recycle();
						} catch (Exception e) {}

						break;
					case (MotionEvent.ACTION_OUTSIDE) :
						return false;
					default :
						return false;
				}
				return false;

			}
		});

		TabLayout.Tab tabItem = tabLayout.getTabAt(0);


		int wantedTabIndex = 0;

		mCameraIB = (ImageButton)findViewById(R.id.camera_ib);
		mCameraIB.setOnClickListener(v -> {
			dispatchTakePictureIntent();
		});





		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, ContactSelectorActivity.class);
				startActivityForResult(intent, 1);
			}
		});



		ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
		contactViewModel.sycContacts();

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main_activity3, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
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
		public static PlaceholderFragment newInstance(int sectionNumber) {
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

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private String tabTitles[] = new String[] {
			getString(R.string.tab_text_1),
			getString(R.string.tab_text_2)};

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class below).
			return PlaceholderFragment.newInstance(position + 1);
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


	private class MYTabLayoutOnPageChangeListener extends TabLayout.TabLayoutOnPageChangeListener{

		public MYTabLayoutOnPageChangeListener(TabLayout tabLayout) {
			super(tabLayout);
		}


	}

}
