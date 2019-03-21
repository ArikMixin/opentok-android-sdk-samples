package io.wochat.app.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;


import io.wochat.app.R;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		ActionBar bar = getSupportActionBar();
		bar.setTitle(R.string.settings_actionbar_title);
		bar.setDisplayHomeAsUpEnabled(true);

		getSupportFragmentManager().beginTransaction().replace(R.id.container_settings_user_fragment,
			new SettingsUserFragment()).commit();
		getSupportFragmentManager().beginTransaction().replace(R.id.container_settings_fragment,
			new SettingsFragment()).commit();

		LinearLayout settingsUserFragmentContainer = findViewById(R.id.container_settings_user_fragment);
		settingsUserFragmentContainer.setOnClickListener(v -> {
			Intent intent = new Intent(SettingsActivity.this,
				SettingsProfileEditActivity.class);
			startActivity(intent);
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
}
