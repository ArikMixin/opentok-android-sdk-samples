package io.wochat.app.ui.settings;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.utils.ImagePickerUtil;
import io.wochat.app.viewmodel.UserViewModel;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class SettingsProfileEditActivity extends AppCompatActivity
	                                     implements View.OnClickListener,
	                                                DialogInterface.OnClickListener {

	private CircleFlagImageView mCircleFlagIV;
	private ImageView mCameraIV;
	private TextView mNameTV;
	private TextView mStatusTV;
	private TextView mPhoneTV;
	private LinearLayout mNameLL;
	private LinearLayout mStatusLL;
	private LinearLayout mPhoneLL;
	private AlertDialog mNameAlertDialog;
	private AlertDialog mStatusAlertDialog;
	private AlertDialog.Builder mNameAlertDialogBuilder;
	private AlertDialog.Builder mStatusAlertDialogBuilder;
	private EditText mInputName;
	private EditText mInputStatus;
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

		mInputName = new EditText(this);
		mInputStatus = new EditText(this);

		//remove edit text underline
		mInputName.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		mInputStatus.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

		//find view by id
		mCircleFlagIV = findViewById(R.id.cf_iv);
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
			String phone = user.getUserId();
			String phoneFormatted = "+" + phone;
			String urlPic = user.getProfilePicUrl();
			String initials = Contact.getInitialsFromName(mName);
			String language = user.getLanguage();

			mCircleFlagIV.setInfo(urlPic, language, initials);

			//name
			mNameTV.setText(mName);

			//status
			if (mStatus == null ) {
				mStatusTV.setText("");
			}
			else{
				mStatusTV.setText(mStatus);
			}

			//phone
				mPhoneTV.setText(phoneFormatted);
		});

		//DIALOG
		//dialog name
		mNameAlertDialogBuilder = new AlertDialog.Builder(this);
		mNameAlertDialogBuilder.setView(mInputName);
		mNameAlertDialogBuilder.setTitle(R.string.settings_profile_edit_dialog_name_title);
		mNameAlertDialogBuilder.setPositiveButton((R.string.dialog_save_button_text), null);
		mNameAlertDialogBuilder.setNegativeButton((R.string.dialog_default_negative_button_text), this);
		mNameAlertDialog = mNameAlertDialogBuilder.create();

		//override default positive button DialogInterface.OnClickListener's functionality
		mNameAlertDialog.setOnShowListener(dialogInterface -> {
			Button button = ((AlertDialog) mNameAlertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
			button.setOnClickListener(view -> {

				String inputNameS = mInputName.getText().toString();

				if (inputNameS.isEmpty()) {
					Toast.makeText(SettingsProfileEditActivity.this,
						R.string.settings_profile_edit_dialog_empty_string_warning,
						Toast.LENGTH_SHORT).show();
				}
				else{
					mUserViewModel.updateUserName(inputNameS);
					// TODO: 3/20/2019 patch to server
					mNameAlertDialog.dismiss();
				}

			});
		});

		//dialog status
		mStatusAlertDialogBuilder = new AlertDialog.Builder(this);
		mStatusAlertDialogBuilder.setView(mInputStatus);
		mStatusAlertDialogBuilder.setTitle(R.string.settings_profile_edit_dialog_status_title);
		mStatusAlertDialogBuilder.setPositiveButton((R.string.dialog_save_button_text), this);
		mStatusAlertDialogBuilder.setNegativeButton((R.string.dialog_default_negative_button_text), this);
		mStatusAlertDialog = mStatusAlertDialogBuilder.create();
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
			case R.id.cf_iv:
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
				requestKeyboard(mNameAlertDialog);
				mInputName.setText(mName);
				mInputName.setSelectAllOnFocus(true);
				mInputName.clearFocus();
				mInputName.requestFocus();
				mNameAlertDialog.show();
				break;

			case R.id.edt_status_ll:
				requestKeyboard(mStatusAlertDialog);
				mInputStatus.setText(mStatus);
				mInputStatus.setSelectAllOnFocus(true);
				mInputStatus.clearFocus();
				mInputStatus.requestFocus();
				mStatusAlertDialog.show();
				break;

			case R.id.edt_phone_ll:
				Toast.makeText(this, "phone", Toast.LENGTH_SHORT).show();
				break;
		}

	}

	//DialogInterface.OnClickListener
	@Override
	public void onClick(DialogInterface dialog, int which) {

		//name
       if (dialog.equals(mNameAlertDialog)) {

       	   if (which == BUTTON_NEGATIVE) {
			   dialog.dismiss();
		   }
	   }

	   //status
	   else if (dialog.equals(mStatusAlertDialog)) {
		   switch (which) {
			   case BUTTON_POSITIVE:
				   // int which = -1
				   String inputStatusS = mInputStatus.getText().toString();
				   mUserViewModel.updateUserStatus(inputStatusS);
				   // TODO: 3/20/2019 patch to server
				   break;

			   case BUTTON_NEGATIVE:
				   // int which = -2
				   dialog.dismiss();
				   break;
		   }
	   }
	}

	private void requestKeyboard (AlertDialog alertDialog) {
		alertDialog.getWindow()
			.setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void setBitmapAsProfilePic(Uri selectedImage) {

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

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			if (width > 1300){
				int newWidth, newHeight;
				newWidth = 1300;
				newHeight = 1300 * height / width;

				imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false);

			}
			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
			mProfilePicByte = byteArrayOutputStream.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO: 3/21/2019 display bitmap like in Registration Activity line 743
		   //upload image
		mUserViewModel.uploadUpdatedProfilePic(mProfilePicByte);
		//todo patch to server
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SELECT_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				setBitmapAsProfilePic(selectedImage);
			}
		}
		else if (requestCode == REQUEST_TAKE_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				setBitmapAsProfilePic(mCameraPhotoFileUri);
			}
		}
	}
}

