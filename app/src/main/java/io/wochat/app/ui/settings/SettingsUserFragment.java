package io.wochat.app.ui.settings;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.viewmodel.UserViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsUserFragment extends Fragment {

	private CircleFlagImageView mCircleFlagImageView;
	private TextView mNameTV;
	private TextView mStatusTV;

	public SettingsUserFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_settings_user, container, false);
		mCircleFlagImageView = v.findViewById(R.id.user_flag_civ);
		mNameTV = v.findViewById(R.id.user_name_tv);
		mStatusTV = v.findViewById(R.id.user_status_tv);

		UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
		userViewModel.getSelfUser().observe(this, user -> {

			String name = user.getUserName();
			String status = user.getStatus();
			String urlPic = user.getProfilePicUrl();
			String initials = Contact.getInitialsFromName(name);
			String language = user.getLanguage();

			mCircleFlagImageView.setInfo(urlPic, language, initials);

			mNameTV.setText(name);


			if (status == null ) {
				mStatusTV.setText("");
			}
			else{
				mStatusTV.setText(status);
			}
		});

		return v;
	}

}
