package io.slatch.app.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.slatch.app.R;
import io.slatch.app.ui.ui.temp.TempFragment;

public class TempActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.temp_activity);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, TempFragment.newInstance())
				.commitNow();
		}
	}
}
