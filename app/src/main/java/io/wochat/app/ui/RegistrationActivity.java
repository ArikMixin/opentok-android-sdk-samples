package io.wochat.app.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;

import io.wochat.app.R;
import io.wochat.app.WCRepository;
import io.wochat.app.logic.SMSReceiver;
import io.wochat.app.model.StateData;
import io.wochat.app.utils.ImagePickerUtil;
import io.wochat.app.utils.TextViewLinkMovementMethod;
import io.wochat.app.viewmodel.RegistrationViewModel;

public class RegistrationActivity extends PermissionActivity {

	private static final String TAG = "RegistrationActivity";
	private ViewPager mPager;
	private RegistrationPagerAdapter mPagerAdapter;
	private CountryCodePicker mCountryCodePicker;
	private AppCompatTextView mCountryCodeTV;
	private AppCompatEditText mPhoneNumET;

	private static final int REQUEST_SELECT_PHOTO = 100;
	private static final int REQUEST_TAKE_PHOTO = 101;

	private String[] PERMISSIONS = {
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
	private ImageButton mPicCameraIB;
	private ImageButton mPicGalleryIB;
	private ImageView mPicProfileIV;
	private ImageView mPicFlagIV;
	private AppCompatEditText mPicUserNameET;
	private AppCompatButton mPicFinishBtn;
	private TextView mPicPhoneNumTV;
	private byte[] mProfilePicByte;

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
				if (position == PagerModel.ENTER_PHONE.ordinal())
					initPhoneView();
				else if (position == PagerModel.ENTER_SMS_CODE.ordinal())
					initCodeView();
				else if (position == PagerModel.ENTER_PIC.ordinal())
					initPicView();

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
//		if (mRegViewModel.hasUserRegistrationData())
//			mPager.setCurrentItem(PagerModel.ENTER_PIC.ordinal());
//		else
		//mPager.setCurrentItem(PagerModel.ENTER_PHONE.ordinal());
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
		//Toast.makeText(this, "permission ok", Toast.LENGTH_LONG).show();

//		MutableLiveData<StateData<WCRepository.UserRegistrationState>> state = mRegViewModel.getUserRegistrationState();
//		state.observe(this, new Observer<StateData<WCRepository.UserRegistrationState>>() {
//			@Override
//			public void onChanged(@Nullable StateData<WCRepository.UserRegistrationState> userRegistrationStateStateData) {
//				Log.e(TAG, "UserRegistrationState: " + userRegistrationStateStateData.getData().toString());
//			}
//		});

		LiveData<RegistrationViewModel.RegistrationPhase> userRegistrationPhase = mRegViewModel.getUserRegistrationPhase();
		userRegistrationPhase.observe(this, new Observer<RegistrationViewModel.RegistrationPhase>() {
			@Override
			public void onChanged(@Nullable RegistrationViewModel.RegistrationPhase registrationPhase) {
				switch (registrationPhase){
					case reg_phase_1_phone_num:
						mPager.setCurrentItem(PagerModel.ENTER_PHONE.ordinal(), true);
						break;
					case reg_phase_2_sms_code:
						mPager.setCurrentItem(PagerModel.ENTER_SMS_CODE.ordinal(), true);
						break;
					case reg_phase_3_pic:
						mPager.setCurrentItem(PagerModel.ENTER_PIC.ordinal(), true);
						break;
					case reg_phase_4_finish:
						Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
						startActivity(intent);
						RegistrationActivity.this.finish();
						break;
					default:
						break;
				}
			}
		});

		MutableLiveData<StateData<String>> userRegistrationResult = mRegViewModel.getUserRegistrationResult();
		userRegistrationResult.observe(this, new Observer<StateData<String>>() {
			@Override
			public void onChanged(@Nullable StateData<String> stringStateData) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (stringStateData.isSuccess()){
					// do nothing here just error handling
				}
				else if (stringStateData.isErrorComm()){
					showUserErrorMessage(getString(R.string.msg_error_title), getString(R.string.msg_error_comm_body));
				}
				else if (stringStateData.isErrorLogic()){
					showUserErrorMessage(getString(R.string.msg_error_title), getString(R.string.msg_error_general_body));
				}

			}
		});




		MutableLiveData<StateData<String>> userVerificationResult = mRegViewModel.getUserVerificationResult();
		userVerificationResult.observe(this, new Observer<StateData<String>>() {
			@Override
			public void onChanged(@Nullable StateData<String> stringStateData) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (stringStateData.isSuccess()){
					//Toast.makeText(RegistrationActivity.this, "result ok: token: " + stringStateData.getData(), Toast.LENGTH_SHORT).show();
					//mPager.setCurrentItem(PagerModel.ENTER_PIC.ordinal(), true);
				}
				else if (stringStateData.isErrorLogic()){
					if (stringStateData.getErrorLogic().equals("invalid code")){
						mCodeCodeNumET.setError(getString(R.string.error_sms_code_verification_invalid_code));
					}
					else {
						showUserErrorMessage(getString(R.string.msg_error_title), getString(R.string.msg_error_general_body));
					}
				}
				else if (stringStateData.isErrorComm()){
					showUserErrorMessage(getString(R.string.msg_error_title), getString(R.string.msg_error_comm_body));
				}
			}
		});


		MutableLiveData<StateData<String>> userFinishRegistrationResult = mRegViewModel.getUserFinishRegistrationResult();
		userFinishRegistrationResult.observe(this, new Observer<StateData<String>>() {
			@Override
			public void onChanged(@Nullable StateData<String> stringStateData) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (stringStateData.isSuccess()){
					Log.e(TAG, "userFinishRegistrationResult ok : Data: " + stringStateData.getData());

				}
				else if (stringStateData.isErrorLogic()){
					showUserErrorMessage(getString(R.string.msg_error_title), getString(R.string.msg_error_general_body));
				}
				else if (stringStateData.isErrorComm()){
					showUserErrorMessage(getString(R.string.msg_error_title), getString(R.string.msg_error_comm_body));
				}
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
			return PagerModel.values().length;
		}

		@NonNull
		@Override
		public Object instantiateItem(@NonNull ViewGroup container, int position) {
			LayoutInflater inflater = LayoutInflater.from(mContext);

			PagerModel pagerModel = PagerModel.values()[position];
			ViewGroup layout = (ViewGroup) inflater.inflate(pagerModel.getLayoutResId(), container, false);
			container.addView(layout);
			if (position == PagerModel.ENTER_PHONE.ordinal())
				initPhoneView(layout);
			else if (position == PagerModel.ENTER_SMS_CODE.ordinal())
				initCodeView(layout);
			else if (position == PagerModel.ENTER_PIC.ordinal())
				initPicView(layout);
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
			PagerModel pagerModel = PagerModel.values()[position];
			return getString(pagerModel.getTitleResId());
		}
	}



	private enum PagerModel {

		ENTER_PHONE(R.string.reg_page_phone_title, R.layout.registration_phone),
		ENTER_SMS_CODE(R.string.reg_page_code_title, R.layout.registration_code),
		ENTER_PIC(R.string.reg_page_pic_title, R.layout.registration_pic);

		private int mTitleResId;
		private int mLayoutResId;

		PagerModel(int titleResId, int layoutResId) {
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
		nextBtn.setOnClickListener(mPhoneNextBtnClick);
		mPhoneNumET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE){
					mPhoneNextBtnClick.onClick(v);
					return true;
				}
				return false;
			}
		});


	}

	private View.OnClickListener mPhoneNextBtnClick = new View.OnClickListener(){

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
	};

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
					mPager.setCurrentItem(PagerModel.ENTER_PHONE.ordinal(), true);
				}
				return true;
			}
		});
		codeTextTV.setMovementMethod(tvlmm);

		mCodeNextBtn = (AppCompatButton) layout.findViewById(R.id.next_btn);
		mCodeNextBtn.setEnabled(false);
		mCodeNextBtn.setOnClickListener(mCodeNextBtnClick);
		mCodeCodeNumET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE){
					mCodeNextBtnClick.onClick(v);
					return true;
				}
				return false;
			}
		});
	}


	private void initPicView() {
	}

	private void initPicView(ViewGroup layout) {
		mPicCameraIB = (ImageButton) layout.findViewById(R.id.reg_pic_camera_ib);
		mPicGalleryIB = (ImageButton) layout.findViewById(R.id.reg_pic_gallery_ib);
		mPicProfileIV = (ImageView) layout.findViewById(R.id.reg_pic_profile_iv);
		//mPicCardView = (CardView) layout.findViewById(R.id.reg_pic_card_view);
		mPicFlagIV = (ImageView) layout.findViewById(R.id.reg_pic_flag_iv);
		mPicUserNameET = (AppCompatEditText) layout.findViewById(R.id.reg_pic_username_et);
		mPicPhoneNumTV = (TextView) layout.findViewById(R.id.reg_pic_phone_num_tv);
		mPicFinishBtn = (AppCompatButton) layout.findViewById(R.id.reg_pic_finish_btn);

		mPicFlagIV.setImageDrawable(getResources().getDrawable(mCountryCodePicker.getSelectedCountry().getFlagID()));

		mPicFinishBtn.setOnClickListener(mPicFinishBtnClick);

		mPicUserNameET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE){
					mPicFinishBtnClick.onClick(v);
					return true;
				}
				return false;
			}
		});

		mPicGalleryIB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				photoPickerIntent.setType("image/*");
				photoPickerIntent.putExtra("crop", "true");
				photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
			}
		});
		mPicCameraIB.setEnabled(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY));
		mPicCameraIB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
				StrictMode.setVmPolicy(builder.build());

				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri outputFileUri = ImagePickerUtil.getCaptureImageOutputUri(RegistrationActivity.this);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				takePictureIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
					startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
				}

			}
		});

		mPicProfileIV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(ImagePickerUtil.getPickImageChooserIntent(RegistrationActivity.this), 200);
			}
		});

	}

	private View.OnClickListener mPicFinishBtnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String userName = mPicUserNameET.getText().toString().trim();
			if (userName.equals("")){
				mPicUserNameET.setError(getString(R.string.error_reg_pic_enter_name));
				return;
			}
			else {
				mProgressDialog = ProgressDialog.show(RegistrationActivity.this,
					null,
					getString(R.string.reg_pic_progress_body),
					true,
					false);

				mRegViewModel.userFinishRegistration(mProfilePicByte, userName);
			}
		}
	};


	private View.OnClickListener mCodeNextBtnClick = new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			if(mCodeCodeNumET.getText().length() != 6){
				mCodeCodeNumET.setError(getString(R.string.error_sms_code_verification_num_digits));
			}
			else {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mCodeCodeNumET.getWindowToken(), 0);
				userVerification(mCodeCodeNumET.getText().toString());
			}
		}
	};

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


	@Override
	public void onBackPressed() {
		if(mPager.getCurrentItem()== PagerModel.ENTER_SMS_CODE.ordinal()){
			mPager.setCurrentItem(PagerModel.ENTER_PHONE.ordinal());
			return;
		}
		super.onBackPressed();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			Uri imageUri = ImagePickerUtil.getPickImageResultUri(this, data);
			setBitmapAsProfilePic(imageUri);

		}


	}

	private void setBitmapAsProfilePic(Uri selectedImage){
		try {
			InputStream imageStream;
			Bitmap imageBitmap = null;
			try {
				imageStream = getContentResolver().openInputStream(selectedImage);
				imageBitmap = BitmapFactory.decodeStream(imageStream);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			int width = imageBitmap.getWidth();
			int height = imageBitmap.getHeight();
			String newPath = null;

//			File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//			FileOutputStream out = null;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			File outputFile = null;

//			try {
//				outputFile = File.createTempFile("image", ".png", outputDir);
//				newPath = outputFile.getAbsolutePath();
//				out = new FileOutputStream(newPath);
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}


			if (width > 1300){
				int newWidth, newHeight;
				newWidth = 1300;
				newHeight = 1300 * height / width;

				imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false);

			}

			//imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
			mPicProfileIV.setImageBitmap(imageBitmap);
			mProfilePicByte = byteArrayOutputStream.toByteArray();



		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	private void showUserErrorMessage(String title, String body){
		new AlertDialog.Builder(RegistrationActivity.this)
			.setTitle(title)
			.setMessage(body)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}




//	public boolean isUriRequiresPermissions(Uri uri) {
//		try {
//			ContentResolver resolver = getContentResolver();
//			InputStream stream = resolver.openInputStream(uri);
//			stream.close();
//			return false;
//		} catch (FileNotFoundException e) {
//			if (e.getCause() instanceof ErrnoException) {
//				return true;
//			}
//		} catch (Exception e) {
//		}
//		return false;
//	}

}
