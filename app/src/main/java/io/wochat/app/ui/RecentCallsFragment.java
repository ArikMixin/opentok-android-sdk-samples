package io.wochat.app.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import io.wochat.app.R;
import io.wochat.app.ui.settings.SettingsActivity;


public class RecentCallsFragment extends Fragment {

	//private RecentChatsViewModel mViewModel;

	public static RecentCallsFragment newInstance() {
		return new RecentCallsFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.recent_calls_fragment, container, false);
//		TextView tv = (TextView) view.findViewById(R.id.tv);
//		tv.setText("Recent Chats.....");

		setHasOptionsMenu(true);


		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//mViewModel = ViewModelProviders.of(this).get(RecentChatsViewModel.class);
		// TODO: Use the ViewModel
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
