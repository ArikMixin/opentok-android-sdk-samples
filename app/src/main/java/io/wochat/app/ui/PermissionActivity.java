package io.wochat.app.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import io.wochat.app.R;

public class PermissionActivity extends AppCompatActivity {

	private static final int PERMISSIONS_REQUEST = 1;
	private static final String TAG = "PermissionActivity";

	private String[] PERMISSIONS = {
		android.Manifest.permission.READ_CONTACTS,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
	};

	private OnPermissionResultListener mOnPermissionResultListener;

	protected interface OnPermissionResultListener{
		void OnPermissionGranted(boolean result);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	protected boolean hasPermissions(){
		boolean hasContactPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
		boolean hasStoragePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		return hasContactPermission && hasStoragePermission;
	}

	protected void checkPermissions(OnPermissionResultListener onPermissionResultListener) {
		mOnPermissionResultListener = onPermissionResultListener;
		boolean hasContactPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
		boolean hasStoragePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

		if(hasContactPermission && hasStoragePermission){
			mOnPermissionResultListener.OnPermissionGranted(true);
		}
		else{

			// Permission is not granted
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.READ_CONTACTS)) {
				showPermissionsExplanationDialog();
			} else {
				showPermissionsExplanationDialog();

			}
		}
	}

	private void showPermissionsExplanationDialog() {
		new AlertDialog.Builder(this)
			.setTitle(R.string.msg_permissions_explan_title)
			.setMessage(R.string.msg_permissions_explan_body)
			.setPositiveButton(R.string.msg_permissions_explan_ok_btn, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ActivityCompat.requestPermissions(PermissionActivity.this,PERMISSIONS, PERMISSIONS_REQUEST);
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		if (requestCode == PERMISSIONS_REQUEST){
			for (int i=0; i<grantResults.length;i++){
				if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
					mOnPermissionResultListener.OnPermissionGranted(false);
				}
			}
			mOnPermissionResultListener.OnPermissionGranted(true);
		}

	}
}
