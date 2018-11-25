package io.wochat.app.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.wochat.app.R;
import io.wochat.app.utils.TextViewLinkMovementMethod;

public class SplashActivity extends PermissionActivity {

	private TextView mReadTermsTV;
	private Button mAgreeBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		boolean hasPermissions = hasPermissions();

		LinearLayout bottomLL = (LinearLayout) findViewById(R.id.bottom_ll);
		bottomLL.setVisibility(hasPermissions? View.INVISIBLE : View.VISIBLE);

		mReadTermsTV = (TextView) findViewById(R.id.read_terms_tv);
		String bottomString = String.format(getString(R.string.splash_read_terms),
			getString(R.string.splash_privacy),
			getString(R.string.splash_terms));

		mReadTermsTV.setText(Html.fromHtml(bottomString));
		TextViewLinkMovementMethod tvlmm = TextViewLinkMovementMethod.getInstance();
		tvlmm.setOnTouchListener(new TextViewLinkMovementMethod.OnTouchListener() {
			@Override
			public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
				}
				return false;
			}
		});
		mReadTermsTV.setMovementMethod(tvlmm);


		mAgreeBtn = (Button)findViewById(R.id.agree_btn);
		mAgreeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkPermissions(new OnPermissionResultListener() {
					@Override
					public void OnPermissionGranted(boolean result) {
						if (result){
							goOn();
						}
						else {
							SplashActivity.this.finish();
						}
					}
				});

			}
		});

		if (hasPermissions){
			new Handler(getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					goOn();
				}
			},1000);
		}

	}


	private void goOn(){
		Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
	}
}
