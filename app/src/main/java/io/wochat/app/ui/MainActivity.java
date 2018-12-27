package io.wochat.app.ui;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import io.wochat.app.R;
import io.wochat.app.utils.ContactsUtil;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getSupportActionBar().setElevation(0);

//		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
//			ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
		getSupportActionBar().setIcon(R.drawable.ic_action_camera);


		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				//ContactsUtil contactsUtil = new ContactsUtil();
				//contactsUtil.readContacts(MainActivity.this);
			}
		}, 2000);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);

		return true;
	}





}
