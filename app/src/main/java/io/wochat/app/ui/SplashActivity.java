package io.wochat.app.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.wochat.app.R;
import io.wochat.app.utils.TextViewLinkMovementMethod;
import io.wochat.app.viewmodel.UserViewModel;

public class SplashActivity extends PermissionActivity {

	private static final String TAG = "SplashActivity";
	private TextView mReadTermsTV;
	private Button mAgreeBtn;
	private LinearLayout mFirstEnterLL;
	private UserViewModel mUserViewModel;
	private RelativeLayout mProgressRL;
	private TextView mVersionTV;
	private String[] PERMISSIONS = {
		android.Manifest.permission.READ_CONTACTS,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
		android.Manifest.permission.READ_EXTERNAL_STORAGE
	};

	private PackageManager pm;
	private PackageInfo pInfo = null;


	@Override
	protected String[] getPermissions() {
		return PERMISSIONS;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Make the top bar - transparent
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		Fabric.with(this, new Crashlytics());

		setContentView(R.layout.activity_splash);
		boolean hasPermissions = hasPermissions();

		mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

		 mFirstEnterLL = (LinearLayout) findViewById(R.id.first_enter_ll);
		 mProgressRL = (RelativeLayout) findViewById(R.id.progress_rl);
		 mVersionTV = (TextView) findViewById(R.id.version_tv);

		if(hasPermissions) {
			mFirstEnterLL.setVisibility(View.INVISIBLE);
			mProgressRL.setVisibility(View.VISIBLE);
            //Get apps version
			try {
                pm = this.getPackageManager();
				pInfo =  pm.getPackageInfo(this.getPackageName(),0);
			} catch (PackageManager.NameNotFoundException e1) {
				e1.printStackTrace();
			}
			mVersionTV.setText("Version "+pInfo.versionName);
		}else{
			mProgressRL.setVisibility(View.GONE);
			mFirstEnterLL.setVisibility(View.VISIBLE);
		}

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
		checkRegistrationUser();
	}

	private void checkRegistrationUser() {
		mUserViewModel.getSelfUser().observe(this, user -> {
			if (user == null){
				startRegistrationActivity();
			}
			else {
				startMainActivity();
			}
		});
	}

	private void startRegistrationActivity(){
		Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
		finish();
	}

	private void startMainActivity(){
		Intent intent = new Intent(SplashActivity.this, MainActivity.class);
		if(getIntent().hasExtra(Consts.INTENT_CONVERSATION_ID)){
			String conversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
			intent.putExtra(Consts.INTENT_CONVERSATION_ID, conversationId);
		}
		startActivity(intent);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
		finish();
	}


	@Override
	protected void showPermissionsExplanationDialog() {
		new AlertDialog.Builder(this)
			.setTitle(R.string.msg_permissions_explan_title)
			.setMessage(R.string.msg_permissions_explan_body)
			.setPositiveButton(R.string.msg_permissions_explan_ok_btn, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					requestPermissions();
				}
			})
			.setNegativeButton(R.string.msg_permissions_explan_cancel_btn, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "not now");
					mOnPermissionResultListener.OnPermissionGranted(false);
				}
			})
			.show();

	}

	@Override
	protected boolean isNeededPermissionsExplanationDialog() {
		return true;
	}


}
