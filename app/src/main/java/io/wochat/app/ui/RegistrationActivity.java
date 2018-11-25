package io.wochat.app.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Locale;

import io.wochat.app.R;

public class RegistrationActivity extends AppCompatActivity {

	private static final String TAG = "RegistrationActivity";
	private ViewPager mPager;
	private RegistrationPagerAdapter mPagerAdapter;
	private CountryCodePicker mCountryCodePicker;
	private AppCompatTextView mCountryCodeTV;
	private AppCompatEditText mPhoneNumET;
	private AppCompatButton mNextBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);

		String token = FirebaseInstanceId.getInstance().getToken();
		Log.e(TAG, "token: " + token);


		mPager = (ViewPager) findViewById(R.id.view_pager);
		mPagerAdapter = new RegistrationPagerAdapter(this);
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				//setTitle(mPagerAdapter.getPageTitle(position));
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mPager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(0);
		//setTitle(mPagerAdapter.getPageTitle(0));





//		new Handler(getMainLooper()).postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				mPager.setCurrentItem(1);
//			}
//		}, 2000);

		init();
	}

	private void init() {
		Toast.makeText(this, "permission ok", Toast.LENGTH_LONG).show();
	}


	private class RegistrationPagerAdapter extends PagerAdapter {

		private Context mContext;

		public RegistrationPagerAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return ModelObject.values().length;
		}

		@NonNull
		@Override
		public Object instantiateItem(@NonNull ViewGroup container, int position) {
			LayoutInflater inflater = LayoutInflater.from(mContext);

			ModelObject modelObject = ModelObject.values()[position];
			ViewGroup layout = (ViewGroup) inflater.inflate(modelObject.getLayoutResId(), container, false);
			container.addView(layout);
			if (position == ModelObject.ENTER_PHONE.ordinal())
				initPhoneView(layout);
			return layout;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			container.removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
			return view == object;
		}


		@Nullable
		@Override
		public CharSequence getPageTitle(int position) {
			ModelObject modelObject = ModelObject.values()[position];
			return getString(modelObject.getTitleResId());
		}
	}



	private enum ModelObject {

		ENTER_PHONE(R.string.reg_page_phone_title, R.layout.registration_phone),
		ENTER_SMS_CODE(R.string.reg_page_code_title, R.layout.registration_code),
		ENTER_PIC(R.string.reg_page_pic_title, R.layout.registration_pic);

		private int mTitleResId;
		private int mLayoutResId;

		ModelObject(int titleResId, int layoutResId) {
			mTitleResId = titleResId;
			mLayoutResId = layoutResId;
		}

		public int getTitleResId() {
			return mTitleResId;
		}

		public int getLayoutResId() {
			return mLayoutResId;
		}

	}


	private void initPhoneView(ViewGroup layout) {
		mCountryCodeTV = (AppCompatTextView)layout.findViewById(R.id.country_code_tv);
		mPhoneNumET = (AppCompatEditText)layout.findViewById(R.id.phone_num_et);

		mCountryCodePicker = (CountryCodePicker) layout.findViewById(R.id.ccp);
		mCountryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
			@Override
			public void onCountrySelected() {
				int cc = mCountryCodePicker.getSelectedCountryCodeAsInt();
				mCountryCodeTV.setText("+" + cc);
			}
		});

		int cc = mCountryCodePicker.getSelectedCountryCodeAsInt();
		mCountryCodeTV.setText("+" + cc);

		mNextBtn = (AppCompatButton)layout.findViewById(R.id.next_btn);
		mNextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int cc = mCountryCodePicker.getSelectedCountryCodeAsInt();
				String phone = mPhoneNumET.getText().toString().trim();
				phone = StringUtils.stripStart(phone,"0");
				final String phoneForValidation = "+" + cc + phone;
				boolean phoneOK = Patterns.PHONE.matcher(phoneForValidation).matches();
				if (phoneOK && (phone.length() > 6) && (phone.length() < 13)){
					mPhoneNumET.setError(null);
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mPhoneNumET.getWindowToken(), 0);
					String nicePhone = PhoneNumberUtils.formatNumber(phoneForValidation, Locale.getDefault().getCountry());

					new AlertDialog.Builder(RegistrationActivity.this)
						.setMessage("We will be verifying the phone number:\n\n" + nicePhone + "\n\nIs this OK or would you like to edit the number?" )
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SendNumberToServer(phoneForValidation);
							}
						})
						.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.showSoftInput(mPhoneNumET,InputMethodManager.SHOW_IMPLICIT);

							}
						})
						.show();
				}
				else {
					mPhoneNumET.setError("invalid phone");
				}
			}
		});

	}

	private void SendNumberToServer(String phoneForValidation) {
		String url = "http://my-json-feed";
		RequestQueue queue = Volley.newRequestQueue(this);
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
			(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject response) {
					Log.e(TAG, "Response: " + response.toString());
				}
			}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					Log.e(TAG, "Error: " + error.getMessage());

				}
			});

// Access the RequestQueue through your singleton class.
		queue.add(jsonObjectRequest);
	}


}
