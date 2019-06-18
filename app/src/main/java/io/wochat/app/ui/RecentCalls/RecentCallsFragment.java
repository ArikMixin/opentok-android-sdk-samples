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
import io.wochat.app.db.entity.Conversation;
import io.wochat.app.ui.MainActivity;
import io.wochat.app.ui.RecentChats.CustomDialogViewHolder;
import io.wochat.app.ui.settings.SettingsActivity;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.RecentCallsViewModel;


public class RecentCallsFragment extends Fragment  implements
		DialogsListAdapter.OnDialogClickListener<Call>,
		DialogsListAdapter.OnDialogLongClickListener<Call>,
		DialogsListAdapter.OnButtonClickListener<Call>, DateFormatter.Formatter{

	private RecentCallsViewModel mViewModel;
	private static final String TAG = "RecentCallsFragment";
	protected DialogsListAdapter<Call> dialogsAdapter;
	private List<Call> mCalls;
	private ConstraintLayout mEmptyFrameCL;
	private View view;
	protected ImageLoader imageLoader;
	private DialogsList dialogsList;

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

		mViewModel = ViewModelProviders.of(this).get(RecentCallsViewModel.class);
		mViewModel.getConversationListLD().observe(this,calls -> {
			mCalls = calls;
            if (calls != null && calls.size() > 0)  {
                Log.e(TAG, "calls count: " + calls.size());
                mEmptyFrameCL.setVisibility((View.GONE));
				initAdapter();
            } else {
                Log.e(TAG, "calls null");
            }
		});

		imageLoader = new ImageLoader() {
			@Override
			public void loadImageWPlaceholder(ImageView imageView, @Nullable String url, int placeholderResourceId, @Nullable Object payload) {
				if ((url != null)&& (url.equals("")))
					url = null;
				Picasso.get().load(url).placeholder(R.drawable.new_contact).error(R.drawable.new_contact).into(imageView);

			}

			@Override
			public void loadImageCenterCrop(ImageView imageView, @Nullable String url, @Nullable Object payload) {
				Picasso.get().load(url).resize(300,300).centerCrop().into(imageView);
			}

			@Override
			public void loadImageCenter(ImageView imageView, @Nullable String url, int placeholderResourceId, @Nullable Object payload) {
				Picasso.get().load(url).into(imageView);
			}

			@Override
			public void loadImageNoPlaceholder(ImageView imageView, int resourceId) {
				Picasso.get().load(resourceId).into(imageView);
			}
		};
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

	@Override
	public void onDialogClick(Call dialog) {

	}

	@Override
	public void onDialogLongClick(Call dialog) {

	}

	@Override
	public void onButtonClick(Call dialog, int i) {

	}

	@Override
	public String format(Date date) {
		return null;
	}
}
