package io.wochat.app.ui.RecentCalls;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import io.wochat.app.R;
import io.wochat.app.db.entity.Call;
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.ui.RecentChats.CustomDialogViewHolder;
import io.wochat.app.ui.settings.SettingsActivity;
import io.wochat.app.viewmodel.RecentCallsViewModel;


public class RecentCallsFragment extends Fragment {

	private RecentCallsViewModel mViewModel;
	private static final String TAG = "RecentCallsFragment";
	protected DialogsListAdapter<Call> dialogsAdapter;

	private View view;

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
		mViewModel = ViewModelProviders.of(this).get(RecentCallsViewModel.class);
		mViewModel.getConversationListLD().observe(this,calls -> {
			Log.d(TAG, "calls.size(): " + calls.size());
		});

		//initAdapter();
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

//	private void initAdapter() {
//		dialogsAdapter = new DialogsListAdapter<>(
//				R.layout.item_custom_dialog_view_holder_new,
//				CustomDialogViewHolder.class,
//				imageLoader);
//
//		//dialogsAdapter.setItems(DialogsFixtures.getDialogs());
//		dialogsAdapter.setItems(mConversation);
//
//		dialogsAdapter.setOnDialogClickListener(this);
//
//		dialogsAdapter.setOnDialogLongClickListener(this);
//
//		dialogsAdapter.setOnButtonClickListener(this);
//
//		dialogsAdapter.setDatesFormatter(this);
//
//		dialogsList.setAdapter(dialogsAdapter);
//	}
}
