package io.wochat.app.ui;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.model.StateData;
import io.wochat.app.ui.Contact.ContactMultiSelectorActivity;
import io.wochat.app.utils.ImagePickerUtil;
import io.wochat.app.viewmodel.GroupViewModel;

public class NewGroupActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int REQUEST_TAKE_PHOTO = 500;
	private static final int REQUEST_IMAGE_PICKER = 501;
	private static final int REQUEST_SELECT_PHOTO = 502;
	private static final String TAG = "NewGroupActivity";

	private RecyclerView mContactsRecyclerView;
	private SelectedAdapter mHeaderAdapter;
	private EditText mGroupNameET;
	private TextView mContactsTitleTV;
	private ImageView mActionOKIV;
	private ImageButton mPicCameraIB;
	private ImageButton mPicGalleryIB;
	private ImageView mPicProfileIV;
	private Uri mCameraPhotoFileUri;
	private byte[] mProfilePicByte;
	private GroupViewModel mGroupViewModel;
	private List<Contact> mContactList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_group);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		String[] contacts = getIntent().getStringArrayExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_RESULT);
		String contactsObj = getIntent().getStringExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_OBJ_RESULT);
		Gson gson = new Gson();
		mContactList = gson.fromJson(contactsObj, new TypeToken<List<Contact>>(){}.getType());

		mContactsRecyclerView = (RecyclerView)findViewById(R.id.contacts_rv);
		LinearLayoutManager headerLayoutManager= new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		mContactsRecyclerView.setLayoutManager(headerLayoutManager);
		mHeaderAdapter = new SelectedAdapter();

		for(Contact contact : mContactList) {
			mHeaderAdapter.addContact(contact);
		}


		mContactsRecyclerView.setAdapter(mHeaderAdapter);


		mGroupNameET = (EditText)findViewById(R.id.group_name_et);
		mContactsTitleTV = (TextView)findViewById(R.id.contact_list_title_tv);
		mActionOKIV = (ImageView)findViewById(R.id.ok_action_iv);
		mActionOKIV.setOnClickListener(this);


		if (mContactList.size()>1)
			mContactsTitleTV.setText(String.format(getString(R.string.persons_in_this_group_title), mContactList.size()));
		else
			mContactsTitleTV.setText(R.string.person_in_this_group_title);


		mPicCameraIB = (ImageButton) findViewById(R.id.pic_camera_ib);
		mPicGalleryIB = (ImageButton) findViewById(R.id.pic_gallery_ib);
		mPicProfileIV = (ImageView) findViewById(R.id.pic_profile_iv);

		mPicCameraIB.setEnabled(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY));
		mPicCameraIB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
				StrictMode.setVmPolicy(builder.build());

				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mCameraPhotoFileUri = ImagePickerUtil.getCaptureImageOutputUri(NewGroupActivity.this);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraPhotoFileUri);
				takePictureIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
					startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
				}

			}
		});

		mPicProfileIV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
				StrictMode.setVmPolicy(builder.build());

				mCameraPhotoFileUri = ImagePickerUtil.getCaptureImageOutputUri(NewGroupActivity.this);
				Intent intent = ImagePickerUtil.getPickImageChooserIntent(NewGroupActivity.this, mCameraPhotoFileUri);
				intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

				startActivityForResult(intent, REQUEST_IMAGE_PICKER);
			}
		});

		mPicGalleryIB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				photoPickerIntent.setType("image/*");
				photoPickerIntent.putExtra("crop", "true");
				photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
			}
		});



		mGroupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);


	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home){
			setResult(Activity.RESULT_CANCELED);
			finish();
			overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private class SelectedAdapter extends RecyclerView.Adapter<SelectedAdapter.SelectedHolder> {

		private final HashMap<String, Contact> mSelectedMap;

		public class SelectedHolder extends RecyclerView.ViewHolder {
			private TextView mContactNameTV;
			public CircleFlagImageView mCircleFlagImageView;

			public SelectedHolder(View v) {
				super(v);
				mCircleFlagImageView = v.findViewById(R.id.contact_cfiv);
				mContactNameTV = v.findViewById(R.id.contact_name_tv);
			}
		}



		public SelectedAdapter(){
			super();
			mSelectedMap = new HashMap<String, Contact>();
		}

		@NonNull
		@Override
		public SelectedAdapter.SelectedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			// create a new view
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_multi_list_selected_item,
				parent, false);

			SelectedHolder vh = new SelectedHolder(v);
			return vh;
		}

		@Override
		public void onBindViewHolder(@NonNull SelectedAdapter.SelectedHolder holder, int position) {
			List<Contact> selectedContactList = new ArrayList<Contact>(mSelectedMap.values());
			Contact c = selectedContactList.get(position);
			holder.mCircleFlagImageView.setContact(c, false, false);
			holder.mCircleFlagImageView.setTag(c);
			holder.mContactNameTV.setText(c.getDisplayName());
		}

		@Override
		public int getItemCount() {
			return mSelectedMap.size();
		}


		public void addContact(Contact contact){
			mSelectedMap.put(contact.getContactId(), contact);
			notifyDataSetChanged();
		}


	}



	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.ok_action_iv:
				if (mGroupNameET.getText().toString().trim().equals("")){
					Toast.makeText(this, "Please enter group name.", Toast.LENGTH_SHORT).show();
					return;
				}
				finishAction();

				break;
		}
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				setBitmapAsProfilePic(mCameraPhotoFileUri);
			}
		}
		else if (requestCode == REQUEST_SELECT_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				mProfilePicByte = ImagePickerUtil.getImageBytes(getContentResolver(), selectedImage);
				setBitmapAsProfilePic(selectedImage);
			}
		}
		else if (requestCode == REQUEST_IMAGE_PICKER){
			if (resultCode == Activity.RESULT_OK) {
				if (data != null) {
					Uri selectedImage = data.getData();
					mProfilePicByte = ImagePickerUtil.getImageBytes(getContentResolver(), selectedImage);
					setBitmapAsProfilePic(selectedImage);
				}
				else {
					setBitmapAsProfilePic(mCameraPhotoFileUri);
				}
			}
		}


	}


	private void setBitmapAsProfilePic(Uri selectedImage){
		try {


			Matrix matrix = null;
			try {
				ExifInterface exif = new ExifInterface(selectedImage.getPath());
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
				Log.d("EXIF", "Exif: " + orientation);
				matrix = new Matrix();
				if (orientation == 6) {
					matrix.postRotate(90);
				} else if (orientation == 3) {
					matrix.postRotate(180);
				} else if (orientation == 8) {
					matrix.postRotate(270);
				}
			} catch (IOException e) {

			}


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

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			if (width > 1300){
				int newWidth, newHeight;
				newWidth = 1300;
				newHeight = 1300 * height / width;

				imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false);

				if (matrix != null)
					imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, newWidth, newHeight, matrix, true);
			}



			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
			mPicProfileIV.setImageBitmap(imageBitmap);
			mProfilePicByte = byteArrayOutputStream.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	private void finishAction() {

		if (mProfilePicByte != null){

			createGroup(mGroupNameET.getText().toString());
//			mGroupViewModel.uploadImage(mProfilePicByte).observe(this, stringStateData -> {
//				if (stringStateData.isSuccess()){
//					String imageUrl = stringStateData.getData();
//					createGroup(imageUrl, mGroupNameET.getText().toString(), mContactList);
//				}
//				else if (stringStateData.isErrorComm()){
//					Toast.makeText(this, "Communication error", Toast.LENGTH_SHORT).show();
//				}
//				else if (stringStateData.isErrorLogic()){
//					Toast.makeText(this, "General error", Toast.LENGTH_SHORT).show();
//				}
//			});
		}


	}
	private void createGroup(String groupName) {
		mGroupViewModel.createNewGroup(groupName, mProfilePicByte, mContactList);
	}
	private void createGroup(String imageUrl, String groupName, List<Contact> contactList) {
		Log.e(TAG, "createGroup: imageUrl: " + imageUrl + " , groupName: " + groupName + " , contactList count: " + contactList.size() );

	}

}
