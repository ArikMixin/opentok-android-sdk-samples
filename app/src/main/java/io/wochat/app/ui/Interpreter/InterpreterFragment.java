package io.wochat.app.ui.Interpreter;

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

import io.wochat.app.R;
import io.wochat.app.db.entity.Call;
import io.wochat.app.db.entity.User;
import io.wochat.app.ui.AudioVideoCall.CallActivity;
import io.wochat.app.ui.Consts;
import io.wochat.app.ui.RecentCalls.RecentCallsViewHolder;
import io.wochat.app.ui.RecentChats.RecentChatsViewHolder;
import io.wochat.app.ui.settings.SettingsActivity;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.RecentCallsViewModel;
import io.wochat.app.viewmodel.UserViewModel;


public class InterpreterFragment extends Fragment {

	private View view;

	public static InterpreterFragment newInstance() { return new InterpreterFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.interpreter_fragment, container, false);
		setHasOptionsMenu(true);

		initView();
		return view;
	}

	private void initView() {
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
}
