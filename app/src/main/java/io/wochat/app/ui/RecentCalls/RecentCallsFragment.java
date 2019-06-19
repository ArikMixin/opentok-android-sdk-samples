package io.wochat.app.ui.RecentCalls;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.Date;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.db.entity.Call;
import io.wochat.app.db.entity.User;
import io.wochat.app.ui.AudioVideoCall.CallActivity;
import io.wochat.app.ui.Consts;
import io.wochat.app.ui.RecentChats.CustomDialogViewHolder;
import io.wochat.app.ui.settings.SettingsActivity;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.RecentCallsViewModel;
import io.wochat.app.viewmodel.UserViewModel;


public class RecentCallsFragment extends Fragment  implements
		DialogsListAdapter.OnDialogClickListener<Call>,
		DialogsListAdapter.OnDialogLongClickListener<Call>,
		DialogsListAdapter.OnButtonClickListener<Call>, DateFormatter.Formatter{

	private RecentCallsViewModel mCallsViewModel;
	private UserViewModel mUserViewModel;
	private static final String TAG = "RecentCallsFragment";
	protected DialogsListAdapter<Call> dialogsAdapter;
	private List<Call> mCalls;
	private ConstraintLayout mEmptyFrameCL;
	private View view;
	protected ImageLoader imageLoader;
	private DialogsList dialogsList;
	private User mSelfUser;
	private String mSelfUserId;
	private String mSelfUserLang;
	private String mSelfUserName;

	public static RecentCallsFragment newInstance() { return new RecentCallsFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		 view = inflater.inflate(R.layout.recent_calls_fragment, container, false);
		 setHasOptionsMenu(true);

		initView();
		return view;
	}

	private void initView() {
		dialogsList = (DialogsList) view.findViewById(R.id.dialogsList);
		mEmptyFrameCL = (ConstraintLayout) view.findViewById(R.id.empty_frame_fl);

		//	ViewModel
		mCallsViewModel = ViewModelProviders.of(this).get(RecentCallsViewModel.class);
		mCallsViewModel.getConversationListLD().observe(this, calls -> {
			mCalls = calls;
            if (calls != null && calls.size() > 0)  {
                Log.e(TAG, "calls count: " + calls.size());
                mEmptyFrameCL.setVisibility((View.GONE));
				initAdapter();
            } else {
                Log.e(TAG, "calls null");
            }
		});

		mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
		mUserViewModel.getSelfUser().observe(this, user -> {
			if (user != null) {
				mSelfUser = user;
				mSelfUserId = user.getUserId();
				mSelfUserLang = user.getLanguage();
				mSelfUserName = user.getUserName();
			}
			else {
				mSelfUserId = null;
				mSelfUserLang = null;
				mSelfUserName = null;
			}
		});
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main_activity3, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == R.id.action_settings) {
			Intent intent = new Intent(getContext(), SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDialogClick(Call call) {
		if(CallActivity.activityActiveFlag)
			return; // Prevent multi open
		Intent intent = new Intent(getContext(), CallActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, call.getParticipantId());
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, call.getParticipantName());
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, call.getParticipantLanguage());
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, call.getParticipantProfilePicUrl());
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, call.getId());
		intent.putExtra(Consts.INTENT_SELF_PIC_URL, mSelfUser.getProfilePicUrl());
		intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
		intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
		intent.putExtra(Consts.INTENT_SELF_NAME, mSelfUserName);
		intent.putExtra(Consts.INTENT_IS_VIDEO_CALL, call.isVideoCall());
		intent.putExtra(Consts.OUTGOING_CALL_FLAG, true);
		startActivity(intent);
	}

	@Override
	public void onDialogLongClick(Call call) {
		Log.d("call", "call: " + call.getCallID());
		// TODO: 6/19/2019 create delete mechanism
		mCallsViewModel.deleteCall(call.getCallID());
	}

	@Override
	public void onButtonClick(Call call, int buttonID) {
		if(CallActivity.activityActiveFlag)
			return; // Prevent multi open

		boolean isVideoCall = false;
		if(buttonID == CustomDialogViewHolder.BTN_CAMERA)
			isVideoCall = true;
		else if(buttonID == CustomDialogViewHolder.BTN_PHONE)
			isVideoCall = false;

		Intent intent = new Intent(getContext(), CallActivity.class);
		intent.putExtra(Consts.INTENT_PARTICIPANT_ID, call.getParticipantId());
		intent.putExtra(Consts.INTENT_PARTICIPANT_NAME, call.getParticipantName());
		intent.putExtra(Consts.INTENT_PARTICIPANT_LANG, call.getParticipantLanguage());
		intent.putExtra(Consts.INTENT_PARTICIPANT_PIC, call.getParticipantProfilePicUrl());
		intent.putExtra(Consts.INTENT_CONVERSATION_ID, call.getId());
		intent.putExtra(Consts.INTENT_SELF_PIC_URL, mSelfUser.getProfilePicUrl());
		intent.putExtra(Consts.INTENT_SELF_ID, mSelfUserId);
		intent.putExtra(Consts.INTENT_SELF_LANG, mSelfUserLang);
		intent.putExtra(Consts.INTENT_SELF_NAME, mSelfUserName);
		intent.putExtra(Consts.INTENT_IS_VIDEO_CALL, isVideoCall);
		intent.putExtra(Consts.OUTGOING_CALL_FLAG, true);
		startActivity(intent);
	}

	@Override
	public String format(Date date) {
		return Utils.dateFormatter(getContext(), date);
	}

	private void initAdapter() {
		dialogsAdapter = new DialogsListAdapter<>(
				R.layout.item_recent_calls_view_holder,
				RecentCallsViewHolder.class,
				imageLoader);

		//dialogsAdapter.setItems(DialogsFixtures.getDialogs());
		dialogsAdapter.setItems(mCalls);

		dialogsAdapter.setOnDialogClickListener(this);

		dialogsAdapter.setOnDialogLongClickListener(this);

		dialogsAdapter.setOnButtonClickListener(this);

		dialogsAdapter.setDatesFormatter(this);

		dialogsList.setAdapter(dialogsAdapter);
	}
}
