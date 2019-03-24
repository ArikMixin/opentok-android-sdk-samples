package io.wochat.app.ui.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.wochat.app.R;
import io.wochat.app.model.SupportedLanguage;
import io.wochat.app.viewmodel.SupportedLanguagesViewModel;

public class SupportedLanguagesActivity extends AppCompatActivity implements
	SupportedLanguagesAdapter.SupportedLanguageSelectionListener{

	private List<SupportedLanguage> mSupportedLanguages = new ArrayList<>();
	public static final String INTENT_LANGUAGE_CODE = "LANGUAGE_CODE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_languages);


		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.settings_translation_languages_actionbar_title);

		RecyclerView recyclerView = findViewById(R.id.supported_languages_rv);
		SupportedLanguagesAdapter supportedLanguagesAdapter = new SupportedLanguagesAdapter(this);
		supportedLanguagesAdapter.setSupportedLanguageSelectionListener(this);
		recyclerView.setAdapter(supportedLanguagesAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		//device language
		String deviceLanguageCode = Locale.getDefault().getLanguage();

		SupportedLanguagesViewModel supportedLanguagesViewModel = ViewModelProviders.of(this).get(SupportedLanguagesViewModel.class);
        //load languages
		supportedLanguagesViewModel.loadLanguages(deviceLanguageCode);
		supportedLanguagesViewModel.getSupportedLanguages().observe(this,
			supportedLanguages -> {
                   supportedLanguagesAdapter.setLanguages(supportedLanguages);
			});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSupportLanguageSelected(SupportedLanguage supportedLanguage) {
                   if (supportedLanguage != null) {
                   	   Intent resultIntent = new Intent();
                   	   Gson gson = new Gson();
                   	   //pass result as json
					   resultIntent.putExtra(INTENT_LANGUAGE_CODE, gson.toJson(supportedLanguage));
					   setResult(RESULT_OK, resultIntent);
				   }
				   else {
				   	   setResult(RESULT_CANCELED);
				   }
				   finish();
	}
}
