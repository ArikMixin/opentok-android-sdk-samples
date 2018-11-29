package io.wochat.app.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
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
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
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
import io.wochat.app.com.WochatApi;
import io.wochat.app.logic.SMSReceiver;
import io.wochat.app.utils.TextViewLinkMovementMethod;
import io.wochat.app.viewmodel.RegistrationViewModel;

public class RegistrationActivity extends PermissionActivity {

	private static final String TAG = "RegistrationActivity";
	private ViewPager mPager;
	private RegistrationPagerAdapter mPagerAdapter;
	private CountryCodePicker mCountryCodePicker;
	private AppCompatTextView mCountryCodeTV;
	private AppCompatEditText mPhoneNumET;


	private String[] PERMISSIONS = {
		//Manifest.permission.READ_SMS
		Manifest.permission.RECEIVE_SMS
	};
	private String mUserTrimmedPhone;
	private String mUserCountryCode;
	private AppCompatEditText mCodePhoneNumET;
	private AppCompatEditText mCodeCodeNumET;
	private String mUserNiceFormattedPhone;
	private AppCompatButton mCodeNextBtn;
	private RegistrationViewModel mRegViewModel;
	private ProgressDialog mProgressDialog;

	@Override
	protected String[] getPermissions() {
		return PERMISSIONS;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);

		String token = FirebaseInstanceId.getInstance().getToken();
		Log.e(TAG, "token: " + token);


		mRegViewModel = ViewModelProviders.of(this).get(RegistrationViewModel.class);




		mPager = (ViewPager) findViewById(R.id.view_pager);
		mPagerAdapter = new RegistrationPagerAdapter(this);
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				if (position == ModelObject.ENTER_PHONE.ordinal())
					initPhoneView();
				else if (position == ModelObject.ENTER_SMS_CODE.ordinal())
					initCodeView();

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

		MutableLiveData<String> userRegistrationResult = mRegViewModel.getUserRegistrationResult();
		userRegistrationResult.observe(this, new Observer<String>() {
			@Override
			public void onChanged(@Nullable String s) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (s == null) {
					mPager.setCurrentItem(1, true);
					Toast.makeText(RegistrationActivity.this, "result ok", Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(RegistrationActivity.this, "result error: " + s, Toast.LENGTH_SHORT).show();
			}
		});


		MutableLiveData<String> userVerificationResult = mRegViewModel.getUserVerificationResult();
		userVerificationResult.observe(this, new Observer<String>() {
			@Override
			public void onChanged(@Nullable String s) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (s == null) {
					mPager.setCurrentItem(2, true);
					Toast.makeText(RegistrationActivity.this, "result ok", Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(RegistrationActivity.this, "result error: " + s, Toast.LENGTH_SHORT).show();
			}
		});



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
			else if (position == ModelObject.ENTER_SMS_CODE.ordinal())
				initCodeView(layout);
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



		@Override
		public void finishUpdate(@NonNull ViewGroup container) {
			//Log.e(TAG, "finishUpdate: " + container.getTransitionName());
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

	private void initPhoneView() {

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

		AppCompatButton nextBtn = (AppCompatButton) layout.findViewById(R.id.next_btn);
		nextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int cc = mCountryCodePicker.getSelectedCountryCodeAsInt();
				String phone1 = mPhoneNumET.getText().toString().trim();
				mUserTrimmedPhone = StringUtils.stripStart(phone1,"0");
				final String phoneForValidation = "+" + cc + mUserTrimmedPhone;
				boolean phoneOK = Patterns.PHONE.matcher(phoneForValidation).matches();
				if (phoneOK && (mUserTrimmedPhone.length() > 6) && (mUserTrimmedPhone.length() < 13)){
					mPhoneNumET.setError(null);
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mPhoneNumET.getWindowToken(), 0);
					mUserNiceFormattedPhone = PhoneNumberUtils.formatNumber(phoneForValidation, Locale.getDefault().getCountry());
					new AlertDialog.Builder(RegistrationActivity.this)
						.setMessage("We will be verifying the phone number:\n\n" + mUserNiceFormattedPhone + "\n\nIs this OK or would you like to edit the number?" )
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (mCodePhoneNumET != null)
									mCodePhoneNumET.setText(mUserNiceFormattedPhone);
								mUserCountryCode = mCountryCodePicker.getSelectedCountryNameCode(); // IL
								handlePermissions();
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

	private void initCodeView() {
		mCodeCodeNumET.setText("");
		mCodePhoneNumET.setText(mUserNiceFormattedPhone);
	}

	private void initCodeView(ViewGroup layout) {
		mCodePhoneNumET = (AppCompatEditText) layout.findViewById(R.id.code_phone_num_et);
		mCodeCodeNumET = (AppCompatEditText) layout.findViewById(R.id.code_code_et);
		mCodeCodeNumET.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString().length() == 6){
					mCodeCodeNumET.setError(null);
					mCodeNextBtn.setEnabled(true);
				}
				else if(s.toString().length() == 0){
					mCodeNextBtn.setEnabled(false);
				}
				else
					mCodeNextBtn.setEnabled(true);
			}
		});

		TextView codeTextTV = (TextView) layout.findViewById(R.id.code_text_tv);
		String bottomString = String.format(getString(R.string.reg_code_text),
			getString(R.string.reg_code_wrong_num));

		codeTextTV.setText(Html.fromHtml(bottomString));
		TextViewLinkMovementMethod tvlmm = TextViewLinkMovementMethod.getInstance();
		tvlmm.setOnTouchListener(new TextViewLinkMovementMethod.OnTouchListener() {
			@Override
			public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					mPager.setCurrentItem(0, true);
				}
				return true;
			}
		});
		codeTextTV.setMovementMethod(tvlmm);

		mCodeNextBtn = (AppCompatButton) layout.findViewById(R.id.next_btn);
		mCodeNextBtn.setEnabled(false);
		mCodeNextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCodeCodeNumET.getText().length() != 6){
					mCodeCodeNumET.setError("code must be 6 decimal digits");
				}
				else {
					userRegistration();
				}

			}
		});
	}


	private void handlePermissions() {
		if (hasPermissions())
			userRegistration();
		else {
			checkPermissions(new OnPermissionResultListener() {
				@Override
				public void OnPermissionGranted(boolean result) {
					userRegistration();
				}
			});
		}
	}

	private void userRegistration() {
		mProgressDialog = ProgressDialog.show(RegistrationActivity.this,
			null,
			getString(R.string.reg_phone_num_progress_body),
			true,
			false);

		mRegViewModel.userRegistration(mUserTrimmedPhone, mUserCountryCode);

	}

	private void userVerification(String code){
		mProgressDialog = ProgressDialog.show(RegistrationActivity.this,
			null,
			getString(R.string.reg_code_progress_body),
			true,
			false);

		mRegViewModel.userVerification(code);

	}



	private SMSReceiver smsReceiver = new SMSReceiver(new SMSReceiver.OnSmsReceivedListener() {
		@Override
		public void onSmsReceived(String code) {
			if(code == null) {
				Log.e(TAG, "onSmsReceived: null");
			}
			else {
				if (mCodeCodeNumET != null)
					mCodeCodeNumET.setText(code);

				userVerification(code);

				Log.e(TAG, "onSmsReceived: " + code);
			}
		}
	});

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(smsReceiver);
	}


	@Override
	protected void showPermissionsExplanationDialog() {

	}

	@Override
	protected boolean isNeededPermissionsExplanationDialog() {
		return false;
	}


}
