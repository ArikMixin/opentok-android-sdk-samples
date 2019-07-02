package io.wochat.app.ui.ContactInfo;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.text.DateFormat;
import java.util.Date;

import io.wochat.app.R;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.ui.AudioVideoCall.CallActivity;
import io.wochat.app.ui.Consts;
import io.wochat.app.ui.Group.GroupInfoActivity;
import io.wochat.app.ui.Messages.ConversationActivity;
import io.wochat.app.viewmodel.ContactViewModel;
import io.wochat.app.viewmodel.ConversationViewModel;

public class ContactInfoActivity extends AppCompatActivity implements View.OnClickListener {

	private ContactViewModel mContactViewModel;
	private String mParticipantId;
	private Contact mParticipant;
	private ImageView mContactIV;
	private CollapsingToolbarLayout mToolbarLayout;
	private LinearLayout mDeleteConversationLL;
	private LinearLayout mMediaLL;
	private TextView mStatusTV;
	private TextView mLastOnlineTV;
	private TextView mPhoneNumTV;
	private ImageView mMessageIV;
	private ImageView mCallIV;
	private ImageView mVideoIV;
	private long mParticipantLastOnline;
	private boolean mParticipantIsOnline;
	private ConversationViewModel mConversationViewModel;
	private String mConversationId;
	private String mParticipantName;
	private String mParticipantLang;
	private String mParticipantPic;
	private String mSelfId;
	private String mSelfLang;
	private String mSelfName;
	private String mContactString;
	private boolean mOpendFromConv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_info);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
		mDeleteConversationLL = (LinearLayout)findViewById(R.id.delete_conversation_ll);
		mMediaLL = (LinearLayout)findViewById(R.id.media_ll);
		mStatusTV = (TextView)findViewById(R.id.status_tv);
		mLastOnlineTV = (TextView)findViewById(R.id.last_online_tv);
		mPhoneNumTV = (TextView)findViewById(R.id.phone_num_tv);
		mMessageIV = (ImageView)findViewById(R.id.message_iv);
		mCallIV = (ImageView)findViewById(R.id.call_iv);
		mVideoIV = (ImageView)findViewById(R.id.video_iv);

		mMessageIV.setOnClickListener(this);
		mCallIV.setOnClickListener(this);
		mVideoIV.setOnClickListener(this);
		mMediaLL.setOnClickListener(this);
		mDeleteConversationLL.setOnClickListener(this);

		mContactIV = (ImageView)findViewById(R.id.contact_iv);
		mContactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
		mParticipantLastOnline = getIntent().getLongExtra(Consts.INTENT_LAST_ONLINE, 0);
		mParticipantIsOnline = getIntent().getBooleanExtra(Consts.INTENT_IS_ONLINE, false);
		mParticipantId = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_ID);
		mConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
		mParticipantName = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
		mParticipantLang = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
		mParticipantPic = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
		mSelfId = getIntent().getStringExtra(Consts.INTENT_SELF_ID);
		mSelfLang = getIntent().getStringExtra(Consts.INTENT_SELF_LANG);
		mSelfName = getIntent().getStringExtra(Consts.INTENT_SELF_NAME);
		mSelfName = getIntent().getStringExtra(Consts.INTENT_SELF_NAME);
		mContactString = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ);
		mOpendFromConv = getIntent().getBooleanExtra(Consts.INTENT_OPENED_FROM_CONVERSATION, false);

		mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);

		mContactViewModel.getContact(mParticipantId).observe(this, contact -> {
			mParticipant = contact;
			init();
		});
	}

	private void init() {
		Picasso.get().load(mParticipant.getAvatar()).into(mContactIV);

		mToolbarLayout.setTitle(mParticipant.getDisplayName());
		mStatusTV.setText(mParticipant.getContactServer().getStatus());
		long date = (long)mParticipant.getContactServer().getLastUpdateDate()*1000;
		mLastOnlineTV.setText(DateFormat.getDateInstance(DateFormat.FULL).format(date));

//		String niceFormattedPhone = "+" + mParticipantId;
//
//		if ((mParticipant.getContactLocal() != null) &&
//			(mParticipant.getContactLocal().getPhoneNumIso() != null)&&
//			(!mParticipant.getContactLocal().getPhoneNumIso().isEmpty())){
//			niceFormattedPhone = mParticipant.getContactLocal().getPhoneNumIso();
//		}
//		else {
//			String phoneNo = mParticipant.getContactServer().getUserId();
//			String localeCountry = mParticipant.getContactServer().getCountryCode();
//			Phonenumber.PhoneNumber ph = null;
//			try {
//				ph = PhoneNumberUtil.getInstance().parse(phoneNo, localeCountry);
//				niceFormattedPhone = PhoneNumberUtil.getInstance().format(ph, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
//			} catch (NumberParseException e) {
//				e.printStackTrace();
//			}
//		}
		mPhoneNumTV.setText(mParticipant.getNiceFormattedPhone());

	}



	private String getDisplayLastUpdateDateTime(){
		if (mParticipantIsOnline){
			return getString(R.string.online);
		}


		long date = mParticipant.getContactServer().getLastUpdateDate();

		if (date > 0) {
			Date lastOnlineDate = new Date(date);
			if (DateFormatter.isToday(lastOnlineDate)){
				return getString(R.string.today) + " " + DateFormatter.format(lastOnlineDate, DateFormatter.Template.TIME);
			}
			else if (DateFormatter.isYesterday(lastOnlineDate)){
				return getString(R.string.yesterday) + " " + DateFormatter.format(lastOnlineDate, DateFormatter.Template.TIME);
			}
			else if (DateFormatter.isPastWeek(lastOnlineDate)){
				return DateFormatter.format(lastOnlineDate, DateFormatter.Template.STRING_DAY_OF_WEEK_TIME);
			}
			else {
				return DateFormatter.format(lastOnlineDate, DateFormatter.Template.STRING_DAY_MONTH);
			}
		}
		else
			return "";
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_info, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				finish();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){

			case R.id.message_iv:
					if(mOpendFromConv)
							finish();
					else
							openConversationActivity();
				break;

			case R.id.call_iv:
				openCallActivity(false);
				break;

			case R.id.video_iv:
				openCallActivity(true);
				break;

			case R.id.media_ll:
				Intent intent = new Intent(this, ContactInfoMediaActivity.class);
				intent.putExtra(Consts.INTENT_CONVERSATION_ID, mConversationId);
				intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, mParticipant.getDisplayName());
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


	private void openCallActivity(boolean isVideoCall){
		if(CallActivity.activityActiveFlag)
			return; // Prevent multi open
		Intent intent = new Intent(this, CallActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, mParticipantId);
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, mParticipantName);
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, mParticipantLang);
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, mParticipantPic);
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, mConversationId);
		intent.putExtra(Consts.INTENT_SELF_ID, mSelfId);
		intent.putExtra(Consts.INTENT_SELF_LANG, mSelfLang);
		intent.putExtra(Consts.INTENT_IS_VIDEO_CALL, isVideoCall);
		intent.putExtra(Consts.OUTGOING_CALL_FLAG, true);
		startActivity(intent);
	}

	private void openConversationActivity() {
		Intent intent = new Intent(this, ConversationActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, mParticipantId);
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, mParticipantName);
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, mParticipantLang);
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, mParticipantPic);
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, mConversationId);
		intent.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, mContactString);
		intent.putExtra(Consts.INTENT_SELF_ID, mSelfId);
		intent.putExtra(Consts.INTENT_SELF_LANG, mSelfLang);
		intent.putExtra(Consts.INTENT_SELF_NAME, mSelfName);
		startActivity(intent);
	}
}
