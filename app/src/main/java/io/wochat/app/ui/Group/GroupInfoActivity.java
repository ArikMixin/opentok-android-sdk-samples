package io.wochat.app.ui.Group;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.GroupMember;
import io.wochat.app.db.entity.GroupMemberContact;
import io.wochat.app.ui.Consts;
import io.wochat.app.ui.Contact.ContactMultiSelectorActivity;
import io.wochat.app.ui.ContactInfo.ContactInfoActivity;
import io.wochat.app.ui.ContactInfo.ContactInfoMediaActivity;
import io.wochat.app.ui.Messages.ConversationActivity;
import io.wochat.app.utils.ImagePickerUtil;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.ContactViewModel;
import io.wochat.app.viewmodel.ConversationViewModel;
import io.wochat.app.viewmodel.GroupViewModel;

public class GroupInfoActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int REQUEST_IMAGE_PICKER = 1;
	private static final int REQUEST_CONTACTS_SELECT = 2;
	private static final String TAG = "GroupInfoActivity";

	private ContactViewModel mContactViewModel;
	private ImageView mContactIV;
	private SubtitleCollapsingToolbarLayout mToolbarLayout;
	private LinearLayout mDeleteConversationLL;
	private LinearLayout mMediaLL;
	private ConversationViewModel mConversationViewModel;
	private String mConversationId;
	private GroupViewModel mGroupViewModel;
	private Conversation mConversation;
	private String mCreatedBy;
	private Date mCreatedDate;
	private List<GroupMemberContact> mGroupMemberContacts;
	private LinearLayout mGroupMembersLL;
	private String mSelfId;
	private boolean mSelfIsAdmin;
	private BottomSheetDialog mBottomSheetDialog;
	private String mSelfName;
	private String mSelfLang;
	private Uri mCameraPhotoFileUri;
	private byte[] mProfilePicByte;
	private LinearLayout mAddMembersLL;
	private LinearLayout mQuitGroupLL;
	private CardView mAddMembersCV;
	private CardView mQuitGroupCV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_info);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarLayout = (SubtitleCollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

		mMediaLL = (LinearLayout)findViewById(R.id.media_ll);

		mGroupMembersLL = (LinearLayout)findViewById(R.id.group_members_ll);
		mAddMembersLL = (LinearLayout)findViewById(R.id.add_members_ll);
		mQuitGroupLL = (LinearLayout)findViewById(R.id.quit_group_ll);
		mQuitGroupCV = (CardView)findViewById(R.id.quit_group_cv);

		mAddMembersCV = (CardView)findViewById(R.id.add_members_cv);

		mMediaLL.setOnClickListener(this);
		mAddMembersLL.setOnClickListener(this);
		mQuitGroupLL.setOnClickListener(this);


		mContactIV = (ImageView)findViewById(R.id.contact_iv);
		mContactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
		mConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
		mSelfId = getIntent().getStringExtra(Consts.INTENT_SELF_ID);
		mSelfName = getIntent().getStringExtra(Consts.INTENT_SELF_NAME);
		mSelfLang = getIntent().getStringExtra(Consts.INTENT_SELF_LANG);


		mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);
		mConversationViewModel.getConversationLD(mConversationId).observe(this, conversation -> {
			mConversation = conversation;
			mGroupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
			mGroupViewModel.getMembersContact(mConversationId).observe(this, groupMemberContacts -> {
				mGroupMemberContacts = groupMemberContacts;
				init();
				invalidateOptionsMenu();
			});
		});







	}

	private void init() {
		if (Utils.isNotNullAndNotEmpty(mConversation.getGroupImageUrl()))
			Picasso.get().load(mConversation.getGroupImageUrl()).into(mContactIV);


		mCreatedBy = mConversation.getGroupCreatedBy();
		mCreatedDate = mConversation.getGroupCreatedDate();
		if (mCreatedBy.equals(mSelfId))
			mToolbarLayout.setSubtitle(String.format(getString(R.string.created_by), "You"));
		else {
			for (GroupMemberContact memberContact : mGroupMemberContacts) {
				if (memberContact.getContact().getContactId().equals(mCreatedBy)) {
					mToolbarLayout.setSubtitle(String.format(getString(R.string.created_by), memberContact.getContact().getDisplayName()));
					break;
				}
			}
		}
		mToolbarLayout.setTitle(mConversation.getGroupName());




		createViewMembers();

		if(mSelfIsAdmin)
			mAddMembersCV.setVisibility(View.VISIBLE);
		else
			mAddMembersCV.setVisibility(View.GONE);
		if(mConversation.isSelfInGroup()){
			mQuitGroupCV.setVisibility(View.VISIBLE);
		}
		else {
			mQuitGroupCV.setVisibility(View.GONE);
		}
	}








	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if ((mConversation != null) && (mConversation.isSelfInGroup()))
			inflater.inflate(R.menu.menu_group_info, menu);

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				finish();
				break;

			case R.id.action_picture:
				if (!mSelfIsAdmin)
					return super.onOptionsItemSelected(item);
				StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
				StrictMode.setVmPolicy(builder.build());
				mCameraPhotoFileUri = ImagePickerUtil.getCaptureImageOutputUri(this);
				Intent intent = ImagePickerUtil.getPickImageChooserIntent(this, mCameraPhotoFileUri);
				intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				startActivityForResult(intent, REQUEST_IMAGE_PICKER);
				break;

			case R.id.action_edit:
				if (!mSelfIsAdmin)
					return super.onOptionsItemSelected(item);
				showNameAlertDialog(mConversation.getGroupName());
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){

			case R.id.message_iv:
				finish();
				break;

			case R.id.add_members_ll:
				Intent intent1 = new Intent(this, ContactMultiSelectorActivity.class);
				intent1.putExtra(Consts.INTENT_TITLE, getString(R.string.select_contacts));
				intent1.putExtra(Consts.INTENT_ACTION_ICON, R.drawable.ic_action_right_arrow);
				String[] array = getGroupMemberContactsArrayIds();
				intent1.putExtra(ContactMultiSelectorActivity.EXCLUDE_CONTACTS, array);
				startActivityForResult(intent1, REQUEST_CONTACTS_SELECT);
				overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
				break;

			case R.id.quit_group_ll:
				showQuitGroupConfirmation();
				break;

			case R.id.media_ll:
				Intent intent = new Intent(this, ContactInfoMediaActivity.class);
				intent.putExtra(Consts.INTENT_CONVERSATION_ID, mConversationId);
				intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, mConversation.getGroupName());
				startActivity(intent);
				break;
		}
	}


	private void showQuitGroupConfirmation(){
		new AlertDialog.Builder(this)
			.setTitle(R.string.quit_group_confirmation_title)
			.setMessage(String.format(getString(R.string.quit_group_confirmation_body), mConversation.getGroupName()))
			.setNegativeButton(R.string.cancel, (dialog, which) -> {dialog.cancel();})
			.setPositiveButton(R.string.quit, (dialog, which) -> {quitGroup();})
			.setCancelable(false)
			.show();
	}

	private void quitGroup() {
		mGroupViewModel.leaveGroup(mConversationId);
		new Handler(getMainLooper()).postDelayed(() ->
			GroupInfoActivity.this.finish(), 1000);

	}


	private void createViewMembers(){
		mGroupMembersLL.removeAllViews();
		mSelfIsAdmin = false;
		for(GroupMemberContact memberContact : mGroupMemberContacts) {

			if (memberContact.getGroupMember().isAdmin() && memberContact.getContact().getContactId().equals(mSelfId))
				mSelfIsAdmin = true;

			View view = LayoutInflater.from(this).inflate(R.layout.group_member_item, null);
			populateMember(view, memberContact);

			view.setOnClickListener(v -> {
				if(!mConversation.isSelfInGroup()) // no operation if not in group
					return;
				openMemberMenu((GroupMemberContact)v.getTag());
			});
			mGroupMembersLL.addView(view);

		}
	}

	private void populateMember(View view, GroupMemberContact memberContact) {
		TextView adminTV = view.findViewById(R.id.member_admin_tv);
		CircleFlagImageView avatarCfiv = view.findViewById(R.id.member_cfiv);
		TextView nameTV = view.findViewById(R.id.member_name_tv);

		if (memberContact.getContact().getContactId().equals(mSelfId))
			nameTV.setText("You");
		else
			nameTV.setText(memberContact.getContact().getDisplayName());

		if(memberContact.getGroupMember().isAdmin())
			adminTV.setVisibility(View.VISIBLE);
		else
			adminTV.setVisibility(View.INVISIBLE);
		avatarCfiv.setContact(memberContact.getContact());
		view.setTag(memberContact);
	}

	private View.OnClickListener mMemberActionClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!mConversation.isSelfInGroup()) // no operation if not in group
				return;
			GroupMemberContact gmc = (GroupMemberContact)v.getTag();
			mBottomSheetDialog.dismiss();
			switch (v.getId()){
				case R.id.message_member_ll:
					Intent intent1 = new Intent(GroupInfoActivity.this, ConversationActivity.class);
					intent1.putExtra(Consts.INTENT_PARTICIPANT_ID, gmc.getContact().getContactId());
					intent1.putExtra(Consts.INTENT_PARTICIPANT_NAME, gmc.getContact().getDisplayName());
					intent1.putExtra(Consts.INTENT_PARTICIPANT_LANG, gmc.getContact().getLanguage());
					intent1.putExtra(Consts.INTENT_PARTICIPANT_PIC, gmc.getContact().getAvatar());
					intent1.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, gmc.getContact().toJson());
					intent1.putExtra(Consts.INTENT_CONVERSATION_ID, Conversation.getConversationId(mSelfId, gmc.getContact().getContactId()));
					intent1.putExtra(Consts.INTENT_SELF_ID, mSelfId);
					intent1.putExtra(Consts.INTENT_SELF_LANG, mSelfLang);
					intent1.putExtra(Consts.INTENT_SELF_NAME, mSelfName);
					startActivity(intent1);
					break;
				case R.id.view_member_ll:
					Intent intent = new Intent(GroupInfoActivity.this, ContactInfoActivity.class);
					intent.putExtra(Consts.INTENT_PARTICIPANT_ID,gmc.getContact().getContactId());
					intent.putExtra(Consts.INTENT_CONVERSATION_ID, Conversation.getConversationId(mSelfId, gmc.getContact().getContactId()));
					intent.putExtra(Consts.INTENT_PARTICIPANT_NAME,  gmc.getContact().getDisplayName());
					intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, gmc.getContact().getLanguage());
					intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, gmc.getContact().getAvatar());
					intent.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, gmc.getContact().toJson());
					intent.putExtra(Consts.INTENT_SELF_ID, mSelfId);
					intent.putExtra(Consts.INTENT_SELF_LANG, mSelfLang);
					intent.putExtra(Consts.INTENT_SELF_NAME, mSelfName);
					intent.putExtra(Consts.INTENT_OPENED_FROM_CONVERSATION, false);
					startActivity(intent);

					break;
				case R.id.make_admin_ll:
					mGroupViewModel.makeAdmin(mConversationId, gmc.getGroupMember().getUserId());
					break;
				case R.id.dismiss_admin_ll:
					mGroupViewModel.removeAdmin(mConversationId, gmc.getGroupMember().getUserId());
					break;
				case R.id.remove_member_ll:
					mGroupViewModel.removeMember(mConversationId, gmc.getGroupMember().getUserId(), getResources());
					break;
			}
		}
	};

	private void openMemberMenu(GroupMemberContact gmc){
		String memberName = gmc.getContact().getDisplayName();
		boolean isMemberAdmin = gmc.getGroupMember().isAdmin();

		mBottomSheetDialog = new BottomSheetDialog(this);
		View sheetView = getLayoutInflater().inflate(R.layout.dialog_group_info_member, null);

		LinearLayout messageMemberLL = sheetView.findViewById(R.id.message_member_ll);
		LinearLayout viewMemberLL = sheetView.findViewById(R.id.view_member_ll);
		LinearLayout makeAdminLL = sheetView.findViewById(R.id.make_admin_ll);
		LinearLayout dismissAdminLL = sheetView.findViewById(R.id.dismiss_admin_ll);
		LinearLayout removeMemberLL = sheetView.findViewById(R.id.remove_member_ll);

		messageMemberLL.setOnClickListener(mMemberActionClickListener);
		viewMemberLL.setOnClickListener(mMemberActionClickListener);
		makeAdminLL.setOnClickListener(mMemberActionClickListener);
		dismissAdminLL.setOnClickListener(mMemberActionClickListener);
		removeMemberLL.setOnClickListener(mMemberActionClickListener);

		messageMemberLL.setTag(gmc);
		viewMemberLL.setTag(gmc);
		makeAdminLL.setTag(gmc);
		dismissAdminLL.setTag(gmc);
		removeMemberLL.setTag(gmc);

		if(gmc.getContact().getContactId().equals(mSelfId))
			messageMemberLL.setVisibility(View.GONE); // cannot message myself
		else
			messageMemberLL.setVisibility(View.VISIBLE);



		viewMemberLL.setVisibility(View.VISIBLE);

		if (!mSelfIsAdmin){
			makeAdminLL.setVisibility(View.GONE);
			dismissAdminLL.setVisibility(View.GONE);
			removeMemberLL.setVisibility(View.GONE);
		}
		else {
			removeMemberLL.setVisibility(View.VISIBLE);
			if (isMemberAdmin){
				makeAdminLL.setVisibility(View.GONE);
				dismissAdminLL.setVisibility(View.VISIBLE);
			}
			else {
				dismissAdminLL.setVisibility(View.GONE);
				makeAdminLL.setVisibility(View.VISIBLE);
			}
		}


		TextView messageMemberTV = (TextView)sheetView.findViewById(R.id.message_member_tv);
		messageMemberTV.setText(String.format(getString(R.string.group_info_member_message), memberName));
		TextView viewMemberTV = (TextView)sheetView.findViewById(R.id.view_member_tv);
		viewMemberTV.setText(String.format(getString(R.string.group_info_member_view), memberName));
		TextView makeAdminTV = (TextView)sheetView.findViewById(R.id.make_admin_tv);
		TextView dismissAdminTV = (TextView)sheetView.findViewById(R.id.dismiss_admin_tv);
		TextView removeMemberTV = (TextView)sheetView.findViewById(R.id.remove_member_tv);
		removeMemberTV.setText(String.format(getString(R.string.group_info_member_remove), memberName));

		mBottomSheetDialog.setContentView(sheetView);
		mBottomSheetDialog.show();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_PICKER) {
			if (resultCode == Activity.RESULT_OK) {
				if (data != null) {
					Uri selectedImage = data.getData();
					mProfilePicByte = ImagePickerUtil.getImageBytes(getContentResolver(), selectedImage);
					setBitmapAsProfilePic(selectedImage);
				}
				else {
					setBitmapAsProfilePic(mCameraPhotoFileUri);
				}
				mGroupViewModel.updateGroupImage(mConversationId, mProfilePicByte, getResources());
			}
		}
		else if (requestCode == REQUEST_CONTACTS_SELECT){
			if (resultCode == RESULT_OK) {
				String[] contacts = data.getStringArrayExtra(ContactMultiSelectorActivity.SELECTED_CONTACTS_RESULT);
				mGroupViewModel.addMembers(mConversationId, contacts, getResources());

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
			//mContactIV.setImageBitmap(imageBitmap);
			mProfilePicByte = byteArrayOutputStream.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	private void showNameAlertDialog(String currentName) {
		final EditText editText = new EditText(this);
		//remove edit text underline
		editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(R.string.enter_group_name_title)
			.setView(editText)
			.setPositiveButton((R.string.dialog_save_button_text), null)
			.setNegativeButton((R.string.dialog_default_negative_button_text), (dialog, which) ->
				dialog.dismiss());

		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		editText.setText(currentName);
		editText.setSelectAllOnFocus(true);
		editText.clearFocus();
		editText.requestFocus();


		//override default positive button DialogInterface.OnClickListener's functionality
		alertDialog.setOnShowListener(dialogInterface -> {
			Button button = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
			button.setOnClickListener(view -> {

				String inputNameS = editText.getText().toString();

				if (inputNameS.isEmpty()) {
					Toast.makeText(GroupInfoActivity.this,
						R.string.settings_profile_edit_dialog_empty_string_warning,
						Toast.LENGTH_SHORT).show();
				}
				else{
					mGroupViewModel.updateGroupName(mConversationId, inputNameS, getResources());
					alertDialog.dismiss();
				}

			});
		});

		alertDialog.show();
	}


	private String[] getGroupMemberContactsArrayIds(){
		int i = 0;
		String[] array = new String[mGroupMemberContacts.size()];
		for(GroupMemberContact memberContact : mGroupMemberContacts) {
			array[i++] = memberContact.getContact().getContactId();
		}
		return array;
	}
}
