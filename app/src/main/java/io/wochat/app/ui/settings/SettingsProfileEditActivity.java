package io.wochat.app.ui.settings;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.model.StateData;
import io.wochat.app.utils.ImagePickerUtil;
import io.wochat.app.viewmodel.UserViewModel;

public class SettingsProfileEditActivity extends AppCompatActivity
	implements View.OnClickListener {

	private CircleFlagImageView mCircleFlagIV;
	private ImageView mCameraIV;
	private TextView mNameTV;
	private TextView mStatusTV;
	private TextView mPhoneTV;
	private LinearLayout mNameLL;
	private LinearLayout mStatusLL;
	private LinearLayout mPhoneLL;
	private String mName;
	private String mStatus;
	private UserViewModel mUserViewModel;
	private static final int REQUEST_SELECT_PHOTO = 103;
	private static final int REQUEST_TAKE_PHOTO = 104;
	private Uri mCameraPhotoFileUri;
	private byte[] mProfilePicByte;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_profile_edit);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.settings_profile_edit_actionbar_title);

		//find view by id
		mCircleFlagIV = findViewById(R.id.profile_cfiv);
		mCircleFlagIV.displayFlag(false);
		mCameraIV = findViewById(R.id.camera_iv);

		mNameTV = findViewById(R.id.name_summary_tv);
		mStatusTV = findViewById(R.id.status_summary_tv);
		mPhoneTV = findViewById(R.id.phone_summary_tv);

		mNameLL = findViewById(R.id.edt_name_ll);
		mStatusLL = findViewById(R.id.edt_status_ll);
		mPhoneLL = findViewById(R.id.edt_phone_ll);

		mCircleFlagIV.setOnClickListener(this);
		mCameraIV.setOnClickListener(this);
		mNameLL.setOnClickListener(this);
		mStatusLL.setOnClickListener(this);
		mPhoneLL.setOnClickListener(this);

		//VIEW MODEL
		mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
		mUserViewModel.getSelfUser().observe(this, user -> {

			mName = user.getUserName();
			mStatus = user.getStatus();

			String phoneFormatted = user.getNiceFormattedPhone();
			String urlPic = user.getProfilePicUrl();
			String initials = Contact.getInitialsFromName(mName);
			String language = user.getLanguage();

			mCircleFlagIV.setInfoNoResize(urlPic, language, initials);

			//name
			mNameTV.setText(mName);

			//status
			if (mStatus == null) {
				mStatusTV.setText("");
			}
			else {
				mStatusTV.setText(mStatus);
			}

			//phone
			mPhoneTV.setText(phoneFormatted);
		});

		//observe profile editing call status
		MutableLiveData<StateData<Void>> userProfileEditResult = mUserViewModel.getUserProfileEditResult();
		userProfileEditResult.observe(this, stringStateData -> {
			if (stringStateData.isSuccess()) {
				// do nothing here just error handling
			}
			else if (stringStateData.isErrorComm()) {
				showUserErrorMessage(getString(R.string.msg_error_title), getString(R.string.msg_error_comm_body));
			}
			else if (stringStateData.isErrorLogic()) {
				showUserErrorMessage(getString(R.string.msg_error_title), getString(R.string.msg_error_general_body));
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.profile_cfiv:
				//start gallery
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				photoPickerIntent.setType("image/*");
				photoPickerIntent.putExtra("crop", "true");
				photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
				break;

			case R.id.camera_iv:
				//take a photo
				StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
				StrictMode.setVmPolicy(builder.build());

				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mCameraPhotoFileUri = ImagePickerUtil.getCaptureImageOutputUri(SettingsProfileEditActivity.this);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraPhotoFileUri);
				takePictureIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
					startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
				}
				break;

			case R.id.edt_name_ll:
				showNameAlertDialog();
				break;

			case R.id.edt_status_ll:
				showStatusAlertDialog();
				break;

		}

	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SELECT_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				mProfilePicByte = ImagePickerUtil.getImageBytes(getContentResolver(), selectedImage);
				//upload image
				mUserViewModel.uploadUpdatedProfilePic(mProfilePicByte);
				//setBitmapAsProfilePic(selectedImage);
			}
		}
		else if (requestCode == REQUEST_TAKE_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				mProfilePicByte = ImagePickerUtil.getImageBytes(getContentResolver(), mCameraPhotoFileUri);
				//upload image
				mUserViewModel.uploadUpdatedProfilePic(mProfilePicByte);
				//setBitmapAsProfilePic(mCameraPhotoFileUri);
			}
		}
	}

	private void showUserErrorMessage(String title, String body) {
		new AlertDialog.Builder(SettingsProfileEditActivity.this)
			.setTitle(title)
			.setMessage(body)
			.setPositiveButton(android.R.string.ok, (dialog, which) ->
				dialog.dismiss())
			.show();
	}

	private void showNameAlertDialog() {
		final EditText editText = new EditText(this);
		//remove edit text underline
		editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		AlertDialog.Builder builder = new AlertDialog.Builder(SettingsProfileEditActivity.this)
			.setTitle(R.string.settings_profile_edit_dialog_name_title)
			.setView(editText)
			.setPositiveButton((R.string.dialog_save_button_text), null)
			.setNegativeButton((R.string.dialog_default_negative_button_text), (dialog, which) ->
				dialog.dismiss());

		AlertDialog alertDialog = builder.create();
		requestKeyboard(alertDialog);

		editText.setText(mName);
		editText.setSelectAllOnFocus(true);
		editText.clearFocus();
		editText.requestFocus();


		//override default positive button DialogInterface.OnClickListener's functionality
		alertDialog.setOnShowListener(dialogInterface -> {
			Button button = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
			button.setOnClickListener(view -> {

				String inputNameS = editText.getText().toString();

				if (inputNameS.isEmpty()) {
					Toast.makeText(SettingsProfileEditActivity.this,
						R.string.settings_profile_edit_dialog_empty_string_warning,
						Toast.LENGTH_SHORT).show();
				}
				else{
					mUserViewModel.updateUserName(inputNameS);
					alertDialog.dismiss();
				}

			});
		});

		alertDialog.show();
	}

	private void showStatusAlertDialog() {
		final EditText editText = new EditText(this);
		//remove edit text underline
		editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

		AlertDialog.Builder builder = new AlertDialog.Builder(SettingsProfileEditActivity.this)
			.setView(editText)
			.setTitle(R.string.settings_profile_edit_dialog_status_title)
			.setPositiveButton((R.string.dialog_save_button_text), (dialog, which) -> {
				String inputStatusS = editText.getText().toString();
				mUserViewModel.updateUserStatus(inputStatusS);
			})
			.setNegativeButton((R.string.dialog_default_negative_button_text), (dialog, which) ->
				dialog.dismiss());

		AlertDialog alertDialog = builder.create();
		requestKeyboard(alertDialog);

		editText.setText(mStatus);
		editText.setSelectAllOnFocus(true);
		editText.clearFocus();
		editText.requestFocus();

		alertDialog.show();
	}

	private void requestKeyboard(AlertDialog alertDialog) {
		alertDialog.getWindow()
			.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

}
