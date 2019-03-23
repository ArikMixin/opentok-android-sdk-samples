package io.wochat.app.ui.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import io.wochat.app.R;
import io.wochat.app.viewmodel.UserViewModel;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragmentCompat implements
	Preference.OnPreferenceClickListener {

	private Preference mTranslationP;
	private Preference mBugReportP;
	private Preference mInviteFriendsP;
	private Preference mHelpP;
	private UserViewModel mUserViewModel;
	private final int REQUEST_CODE_SUPPORTED_LANGUAGE = 7;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.settings_preferences, rootKey);

		//find a Preference based on its key
		mTranslationP = findPreference(getString(R.string.SETTINGS_TRANSLATION_LANGUAGE));
		mBugReportP = findPreference(getString(R.string.SETTINGS_BUG_REPORT));
		mInviteFriendsP = findPreference(getString(R.string.SETTINGS_INVITE_FRIENDS));
		mHelpP = findPreference(getString(R.string.SETTINGS_HELP));

		mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
		mUserViewModel.getSelfUser().observe(this, user -> {

			String languageCode = user.getLanguage();
			String countryCode = user.getCountryCode();

			//language name  in its original language
			Locale loc1 = new Locale(languageCode);
			String languageName = loc1.getDisplayLanguage(loc1);
			String languageNameCap = StringUtils.capitalize(languageName);

			//country name in original language
			Locale loc2 = new Locale(languageCode, countryCode);
			String countryName = loc2.getDisplayCountry(loc2);

			mTranslationP.setSummary(languageNameCap + " (" + countryName + ")");
		});

		//setClick
		mTranslationP.setOnPreferenceClickListener(this);
		mBugReportP.setOnPreferenceClickListener(this);
		mInviteFriendsP.setOnPreferenceClickListener(this);
		mHelpP.setOnPreferenceClickListener(this);
	}


	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(getString(R.string.SETTINGS_TRANSLATION_LANGUAGE))) {
			Intent intent = new Intent(getContext(), SupportedLanguagesActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SUPPORTED_LANGUAGE);
		}
		else if (preference.getKey().equals(getString(R.string.SETTINGS_BUG_REPORT))) {
			Toast.makeText(getContext(), "bug report", Toast.LENGTH_SHORT).show();
		}
		else if (preference.getKey().equals(getString(R.string.SETTINGS_INVITE_FRIENDS))) {
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_invite_friends_invitation_text) +
			"\n \n" + getString(R.string.settings_invite_friends_download_link));
			sendIntent.setType("text/plain");
			startActivity(sendIntent);
		}
		else if (preference.getKey().equals(getString(R.string.SETTINGS_HELP))) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(getString(R.string.settings_help_link)));
			startActivity(intent);
		}
		return true;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setDivider(new ColorDrawable(Color.TRANSPARENT));
		setDividerHeight(0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SUPPORTED_LANGUAGE) {
			String json = data.getStringExtra(SupportedLanguagesActivity.INTENT_LANGUAGE_CODE);
			SupportedLanguage supportedLanguage = SupportedLanguage.fromJson(json);
			mUserViewModel.updateUserLanguage(supportedLanguage.getLanguageCode());
			mUserViewModel.updateUserCountryCode(supportedLanguage.getCountryCode());
		}
	}

}
