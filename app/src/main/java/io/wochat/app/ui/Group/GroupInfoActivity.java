package io.wochat.app.ui.Group;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.db.entity.GroupMember;
import io.wochat.app.db.entity.GroupMemberContact;
import io.wochat.app.ui.Consts;
import io.wochat.app.ui.ContactInfo.ContactInfoMediaActivity;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.ContactViewModel;
import io.wochat.app.viewmodel.ConversationViewModel;
import io.wochat.app.viewmodel.GroupViewModel;

public class GroupInfoActivity extends AppCompatActivity implements View.OnClickListener {

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



		mMediaLL.setOnClickListener(this);




		mContactIV = (ImageView)findViewById(R.id.contact_iv);
		mContactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
		mConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
		mSelfId = getIntent().getStringExtra(Consts.INTENT_SELF_ID);

		mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);
		mConversationViewModel.getConversationLD(mConversationId).observe(this, conversation -> {
			mConversation = conversation;
			mGroupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
			mGroupViewModel.getMembersContact(mConversationId).observe(this, groupMemberContacts -> {
				mGroupMemberContacts = groupMemberContacts;
				init();
			});
		});







	}

	private void init() {
		Picasso.get().load(mConversation.getGroupImageUrl()).into(mContactIV);


		mCreatedBy = mConversation.getGroupCreatedBy();
		mCreatedDate = mConversation.getGroupCreatedDate();
		for(GroupMemberContact memberContact : mGroupMemberContacts) {
			if(memberContact.getContact().getContactId().equals(mCreatedBy)){
				mToolbarLayout.setSubtitle("Created By " + memberContact.getContact().getDisplayName());
				break;
			}
		}
		mToolbarLayout.setTitle(mConversation.getGroupName());




		createViewMembers();
	}








	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
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
				break;
			case R.id.action_edit:
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

			case R.id.call_iv:
				Toast.makeText(this, "call", Toast.LENGTH_SHORT).show();
				break;

			case R.id.video_iv:
				Toast.makeText(this, "video", Toast.LENGTH_SHORT).show();
				break;

			case R.id.media_ll:
				Intent intent = new Intent(this, ContactInfoMediaActivity.class);
				intent.putExtra(Consts.INTENT_CONVERSATION_ID, mConversationId);
				intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, mConversation.getGroupName());
				startActivity(intent);
				break;


			case R.id.delete_conversation_ll:
				showConfirmationDelete();
				break;

		}
	}


	private void showConfirmationDelete(){
		new AlertDialog.Builder(this)
			.setTitle("Confirmation")
			.setMessage("All messages will be deleted.\nAre you sure?")
			.setNegativeButton("Cancel", (dialog, which) -> {dialog.cancel();})
			.setPositiveButton("Delete", (dialog, which) -> {clearConversation();})
			.setCancelable(false)
			.show();
	}

	private void clearConversation() {
		mConversationViewModel.clearConversation(mConversationId);
		Toast.makeText(this, "Messages Deleted", Toast.LENGTH_SHORT).show();
	}


	private void createViewMembers(){
		mGroupMembersLL.removeAllViews();
		mSelfIsAdmin = false;
		for(GroupMemberContact memberContact : mGroupMemberContacts) {

			if (memberContact.getGroupMember().isAdmin() && memberContact.getContact().getContactId().equals(mSelfId))
				mSelfIsAdmin = true;

			View view = LayoutInflater.from(this).inflate(R.layout.group_member_item, null);
			populateMember(view, memberContact);

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openMemberMenu((GroupMemberContact)v.getTag());
				}
			});
			mGroupMembersLL.addView(view);

		}
	}

	private void populateMember(View view, GroupMemberContact memberContact) {
		TextView adminTV = view.findViewById(R.id.member_admin_tv);
		CircleFlagImageView avatarCfiv = view.findViewById(R.id.member_cfiv);
		TextView nameTV = view.findViewById(R.id.member_name_tv);

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
			GroupMemberContact gmc = (GroupMemberContact)v.getTag();
			mBottomSheetDialog.dismiss();
			switch (v.getId()){
				case R.id.message_member_ll:
					break;
				case R.id.view_member_ll:
					break;
				case R.id.make_admin_ll:
					mGroupViewModel.makeAdmin(mConversationId, gmc.getGroupMember().getUserId());
					break;
				case R.id.dismiss_admin_ll:
					mGroupViewModel.removeAdmin(mConversationId, gmc.getGroupMember().getUserId());
					break;
				case R.id.remove_member_ll:
					mGroupViewModel.removeMember(mConversationId, gmc.getGroupMember().getUserId());
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
}
