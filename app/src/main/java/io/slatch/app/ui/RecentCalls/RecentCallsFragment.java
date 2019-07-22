package io.slatch.app.ui.RecentCalls;

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

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.Date;
import java.util.List;

import io.slatch.app.R;
import io.slatch.app.db.entity.Call;
import io.slatch.app.db.entity.User;
import io.slatch.app.ui.AudioVideoCall.CallActivity;
import io.slatch.app.ui.Consts;
import io.slatch.app.ui.MainActivity;
import io.slatch.app.ui.RecentChats.RecentChatsViewHolder;
import io.slatch.app.ui.settings.SettingsActivity;
import io.slatch.app.utils.Utils;
import io.slatch.app.viewmodel.RecentCallsViewModel;
import io.slatch.app.viewmodel.UserViewModel;


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
        Log.d(TAG, "initView: ");
		dialogsList = (DialogsList) view.findViewById(R.id.dialogsList);
		mEmptyFrameCL = (ConstraintLayout) view.findViewById(R.id.empty_frame_fl);

		mCallsViewModel = ViewModelProviders.of(this).get(RecentCallsViewModel.class);
		mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
		initAdapter();

		//	ViewModel
		mCallsViewModel.getCallsListLD().observe(this, calls -> {
			Log.e(TAG, "calls count: " + calls.size());

			if (calls != null && calls.size() > 0)
				mEmptyFrameCL.setVisibility((View.GONE));
			else
				mEmptyFrameCL.setVisibility((View.VISIBLE));

            dialogsAdapter.setItems(calls); // notifyDataSetChanged
        });

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
	public void onDialogClick(View view, Call call) {
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
	public void onDialogLongClick(View view, Call call) {
		Log.d("call", "call: " + call.getCallID());
		// TODO: 6/19/2019 create delete mechanism
		mCallsViewModel.deleteCall(call.getCallID());
	}

	@Override
	public void onButtonClick(Call call, int buttonID) {
		if(CallActivity.activityActiveFlag)
			return; // Prevent multi open

		boolean isVideoCall = false;
		if(buttonID == RecentChatsViewHolder.BTN_CAMERA)
			isVideoCall = true;
		else if(buttonID == RecentChatsViewHolder.BTN_PHONE)
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
		return Utils.dateFormatter(date);
	}

	private void initAdapter() {
		dialogsAdapter = new DialogsListAdapter<>(
				R.layout.item_recent_calls_view_holder,
				RecentCallsViewHolder.class,
				imageLoader);

		dialogsAdapter.setOnDialogClickListener(this);

		dialogsAdapter.setOnDialogLongClickListener(this);

		dialogsAdapter.setOnButtonClickListener(this);

		dialogsAdapter.setDatesFormatter(this);

		dialogsList.setAdapter(dialogsAdapter);
	}
}
