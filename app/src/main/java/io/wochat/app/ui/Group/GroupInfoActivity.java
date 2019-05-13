package io.wochat.app.ui.Group;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
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

			case R.id.action_add_members:
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
		for(GroupMemberContact memberContact : mGroupMemberContacts) {
			View view = LayoutInflater.from(this).inflate(R.layout.group_member_item, null);
			populateMember(view, memberContact);
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
	}
}
