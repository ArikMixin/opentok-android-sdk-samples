package io.wochat.app.ui.settings;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import android.widget.Toast;

import io.wochat.app.R;

public class SettingsFragment extends PreferenceFragmentCompat implements
	Preference.OnPreferenceClickListener {

	private Preference mTranslationP;
	private Preference mBugReportP;
	private Preference mInviteFriendsP;
	private Preference mHelpP;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.settings_preferences, rootKey);

		//find a Preference based on its key
		mTranslationP = findPreference(getString(R.string.SETTINGS_TRANSLATION_LANGUAGE));
		mBugReportP = findPreference(getString(R.string.SETTINGS_BUG_REPORT));
		mInviteFriendsP = findPreference(getString(R.string.SETTINGS_INVITE_FRIENDS));
		mHelpP = findPreference(getString(R.string.SETTINGS_HELP));

		//setClick
		mTranslationP.setOnPreferenceClickListener(this);
		mBugReportP.setOnPreferenceClickListener(this);
		mInviteFriendsP.setOnPreferenceClickListener(this);
		mHelpP.setOnPreferenceClickListener(this);
	}


	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(getString(R.string.SETTINGS_TRANSLATION_LANGUAGE))) {
			Intent intent = new Intent(getContext(), SettingsLanguagesActivity.class);
			startActivity(intent);
			//TODO: 3/19/2019 startActivityForResult
		}
		else if (preference.getKey().equals(getString(R.string.SETTINGS_BUG_REPORT))) {
			Toast.makeText(getContext(), "bug report", Toast.LENGTH_SHORT).show();
		}
		else if (preference.getKey().equals(getString(R.string.SETTINGS_INVITE_FRIENDS))) {
			Toast.makeText(getContext(), "invite friends", Toast.LENGTH_SHORT).show();
		}
		else if (preference.getKey().equals(getString(R.string.SETTINGS_HELP))) {
			Toast.makeText(getContext(), "help", Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setDivider(new ColorDrawable(Color.TRANSPARENT));
		setDividerHeight(0);
	}
}
