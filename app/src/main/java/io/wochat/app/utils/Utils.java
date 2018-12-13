package io.wochat.app.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.List;

import io.wochat.app.R;

public class Utils {


	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}



	/**
	 * Turn drawable resource into byte array.
	 *
	 * @param context parent context
	 * @param id      drawable resource id
	 * @return byte array
	 */
	public static byte[] getFileDataFromDrawable(Context context, int id) {
		Drawable drawable = ContextCompat.getDrawable(context, id);
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * Turn drawable into byte array.
	 *
	 * @param drawable data
	 * @return byte array
	 */
	public static byte[] getFileDataFromDrawable(Context context, Drawable drawable) {
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}



	public static void changeColorToAlertDialogComp(android.support.v7.app.AlertDialog al){
		try {
			final Resources res = al.getContext().getResources();
			int dividerId = res.getIdentifier("android:id/titleDivider", null, null);
			View divider = al.findViewById(dividerId);
			divider.setBackgroundColor(res.getColor(R.color.app_blue));

			int textViewId = res.getIdentifier("android:id/alertTitle", null, null);
			TextView tv = (TextView) al.findViewById(textViewId);
			tv.setTextColor(res.getColor(R.color.app_blue));
			//tv.setBackgroundColor(res.getColor(R.color.single_cue_main_menu_background));
		} catch (Exception e) {

			e.printStackTrace();
		}



	}

	public static void overrideFontsToAlertDialog(Context cntext, android.support.v7.app.AlertDialog al, String font){
		try {
			final Resources res = al.getContext().getResources();

			int textViewId = res.getIdentifier("android:id/alertTitle", null, null);
			TextView tv = (TextView) al.findViewById(textViewId);


			if (tv != null){
				tv.setTypeface(Typeface.createFromAsset(cntext.getAssets(),font));
			}
			textViewId = res.getIdentifier("android:id/message", null, null);
			tv = (TextView) al.findViewById(textViewId);
			if (tv != null)
				tv.setTypeface(Typeface.createFromAsset(cntext.getAssets(),font));

			Button positiveButton = al.getButton(DialogInterface.BUTTON_POSITIVE);
			if (positiveButton != null)
				positiveButton.setTypeface(Typeface.createFromAsset(cntext.getAssets(),font));

			Button negativeButton = al.getButton(DialogInterface.BUTTON_NEGATIVE);
			if (negativeButton != null)
				negativeButton.setTypeface(Typeface.createFromAsset(cntext.getAssets(),font));

			Button neturalButton = al.getButton(DialogInterface.BUTTON_NEUTRAL);
			if (neturalButton != null)
				neturalButton.setTypeface(Typeface.createFromAsset(cntext.getAssets(),font));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public static String byteArrayImageToString(byte[] byteArray){
		if (byteArray == null)
			return null;

		String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
		return encodedString;
	}

	public static byte[] stringToByteArrayImage(String encodedString){
		if (encodedString == null)
			return null;

		byte[] byteArray = Base64.decode(encodedString, Base64.DEFAULT);
		return byteArray;
	}
}
