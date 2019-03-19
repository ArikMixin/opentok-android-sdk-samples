package io.wochat.app.ui.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.viewmodel.UserViewModel;

public class SettingsProfileEditActivity extends AppCompatActivity implements View.OnClickListener {

	private CircleFlagImageView mCircleFlagIV;
	private ImageView mCameraIV;
	private TextView mNameTV;
	private TextView mStatusTV;
	private TextView mPhoneTV;
	private LinearLayout mNameLL;
	private LinearLayout mStatusLL;
	private LinearLayout mPhoneLL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_profile_edit);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.settings_profile_edit_actionbar_title);

		mCircleFlagIV = findViewById(R.id.cf_iv);
		mCameraIV = findViewById(R.id.camera_iv);

		mNameTV = findViewById(R.id.name_summary_tv);
		mStatusTV = findViewById(R.id.status_summary_tv);
		mPhoneTV = findViewById(R.id.phone_summary_tv);

		mNameLL = findViewById(R.id.edt_name_ll);
		mStatusLL = findViewById(R.id.edt_status_ll);
		mPhoneLL = findViewById(R.id.edt_phone_ll);

		mCircleFlagIV.setOnClickListener(this);
		mCameraIV.setOnClickListener(this);
		mNameLL.setOnClickListener(this);
		mStatusLL.setOnClickListener(this);
		mPhoneLL.setOnClickListener(this);

		UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
		userViewModel.getSelfUser().observe(this, user -> {

			String name = user.getUserName();
			String urlPic = user.getProfilePicUrl();
			String initials = Contact.getInitialsFromName(name);
			String language = user.getLanguage();

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
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.cf_iv:
				Toast.makeText(this, "flag image", Toast.LENGTH_SHORT).show();
				break;

			case R.id.camera_iv:
				Toast.makeText(this, "camera image", Toast.LENGTH_SHORT).show();
				break;

			case R.id.edt_name_ll:
				Toast.makeText(this, "name", Toast.LENGTH_SHORT).show();
				break;

			case R.id.edt_status_ll:
				Toast.makeText(this, "status", Toast.LENGTH_SHORT).show();
				break;

			case R.id.edt_phone_ll:
				Toast.makeText(this, "phone", Toast.LENGTH_SHORT).show();
				break;
		}

	}
}
