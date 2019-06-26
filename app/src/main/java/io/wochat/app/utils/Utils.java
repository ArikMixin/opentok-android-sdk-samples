package io.wochat.app.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.WCApplication;

public class Utils {


	private static final String TAG = "Utils";

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

	public static String getLocaleCountry(Context context){
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String countryCodeValue = tm.getNetworkCountryIso();
		if ((countryCodeValue != null) && (!countryCodeValue.equals("")))
			return countryCodeValue.toUpperCase();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return context.getResources().getConfiguration().getLocales().get(0).getCountry();
		} else {
			return context.getResources().getConfiguration().locale.getCountry();
		}
	}
   public static void setEditTextUnderlineColor(final EditText editText, final int focusedColor, final int unfocusedColor) {
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					editText.getBackground().setColorFilter(focusedColor, PorterDuff.Mode.SRC_ATOP);
					return;
				}
				editText.getBackground().setColorFilter(unfocusedColor, PorterDuff.Mode.SRC_ATOP);
			}
		});

		editText.getBackground().setColorFilter(unfocusedColor, PorterDuff.Mode.SRC_ATOP);
	}

	public static void sendSMS(Context context, String contactNum, String message){


		//https://stackoverflow.com/questions/9798657/send-sms-via-intent
		//https://stackoverflow.com/questions/4967448/show-compose-sms-view-in-android

		Uri uri = Uri.parse("smsto: +" + contactNum);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		intent.putExtra("sms_body", message);
		context.startActivity(intent);


//		Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
//		smsIntent.setType("vnd.android-dir/mms-sms");
//		smsIntent.putExtra("address","your desired phoneNumber");
//		smsIntent.putExtra("sms_body","your desired message");
//		startActivity(smsIntent);
	}


	public static void showImage(Context context, String imageUrl) {
		final Dialog builder = new Dialog(context);
		//builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		builder.getWindow().setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.black50)));


		ImageView imageView = new ImageView(context);
		imageView.setOnClickListener(v -> {
			builder.dismiss();
		});
		Picasso.get().load(imageUrl).into(imageView);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);
		lp.bottomMargin = 400;
		lp.topMargin = 400;
		lp.rightMargin = 100;
		lp.leftMargin = 100;

		builder.addContentView(imageView, lp);
		builder.setCanceledOnTouchOutside(true);
		builder.show();
	}



	public static int dp2px(Context ctx, float dp) {
		final float scale = ctx.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	public static int px2dp(Context ctx, float px) {
		final float scale = ctx.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}


	public static boolean visibilityToBoolean(int visibility){
		return (visibility == View.VISIBLE);
	}

	public static int booleanToVisibilityGone(boolean isVisible){
		return isVisible? View.VISIBLE : View.GONE;
	}

	public static int booleanToVisibilityInvisible(boolean isVisible){
		return isVisible? View.VISIBLE : View.INVISIBLE;
	}



	public static String dateFormatter(Date date) {

		String res;
		String theTime = DateFormatter.format(date, DateFormatter.Template.TIME);
		if (DateFormatter.isToday(date)) {
			res = "Today, " + theTime;
		}
		else if (DateFormatter.isYesterday(date)) {
			res = "Yesterday, " + theTime;
		}
		else if(DateFormatter.isPastWeek(date)){
			res = DateFormatter.format(date, DateFormatter.Template.STRING_DAY_OF_WEEK)+ " " + theTime;
		}
		else if (DateFormatter.isCurrentYear(date)) {
			res = DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH)+ " " + theTime;
		}
		else {
			res = DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)+ " " + theTime;
		}
		Log.e(TAG, "dateFormatter for: " + date.toString() + "  ,  long: " + date.getTime() + "  ,  result: " + res);
		return res;
	}


	public static @DrawableRes int getCountryFlagDrawableFromLang(String language) {
		if (language == null)
			return 0;
		switch (language){
			case "IW":
			case "HE":
				return R.drawable.flag_israel;

			case "UN":
				return R.drawable.flag_un;
			case "DE":
				return R.drawable.flag_germany;
			case "EN":
				return R.drawable.flag_united_states_of_america;
			case "HI":
				return R.drawable.flag_india;
			case "JA":
				return R.drawable.flag_japan;
			case "KO":
				return R.drawable.flag_south_korea;
			case "IT":
				return R.drawable.flag_italy;
			case "RU":
				return R.drawable.flag_russian_federation;
			case "HU":
				return R.drawable.flag_hungary;
			case "FR":
				return R.drawable.flag_france;
			case "ES":
				return R.drawable.flag_spain;
			case "RO":
				return R.drawable.flag_romania;
			case "AR":
				return R.drawable.flag_saudi_arabia;
			case "CS":
				return R.drawable.flag_czech_republic;
			case "DA":
				return R.drawable.flag_denmark;
			case "EL":
				return R.drawable.flag_greece;
			case "FI":
				return R.drawable.flag_finland;
			case "ID":
				return R.drawable.flag_indonesia;
			case "NB":
				return R.drawable.flag_norway;
			case "NL":
				return R.drawable.flag_netherlands;
			case "PL":
				return R.drawable.flag_poland;
			case "PT":
				return R.drawable.flag_portugal;
			case "SK":
				return R.drawable.flag_slovakia;
			case "SV":
				return R.drawable.flag_sweden;
			case "TH":
				return R.drawable.flag_thailand;
			case "TR":
				return R.drawable.flag_turkey;
			case "ZH":
				return R.drawable.flag_china;

			default:
				return R.drawable.flag_united_states_of_america;
		}

	}

	public static String convertMiliSecondsToHMmSs(long miliSec) {
		long seconds = miliSec / 1000;
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;
		if (h > 0)
			return String.format("%d:%02d:%02d", h,m,s);
		else
			return String.format("%02d:%02d", m,s);
	}
	public static String convertSecondsToHMmSs(long miliSec) {
		long seconds = miliSec / 1000;
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;
		if (h > 0)
			return String.format("%d:%02d:%02d", h,m,s);
		else
			return String.format("%02d:%02d", m,s);
	}

	public static boolean isHebrew(String lang) {
		if (lang == null)
			return false;
		return ("iw".equals(lang.toLowerCase()))||("he".equals(lang.toLowerCase()));
	}



	public static Bitmap getCircleBitmap(Bitmap bitmap) {
		Bitmap output;
		Rect srcRect, dstRect;
		float r;
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();

		if (width > height){
			output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
			int left = (width - height) / 2;
			int right = left + height;
			srcRect = new Rect(left, 0, right, height);
			dstRect = new Rect(0, 0, height, height);
			r = height / 2;
		}else{
			output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
			int top = (height - width)/2;
			int bottom = top + width;
			srcRect = new Rect(0, top, width, bottom);
			dstRect = new Rect(0, 0, width, width);
			r = width / 2;
		}

		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawCircle(r, r, r, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

//		if (bitmap != null && !bitmap.isRecycled()) {
//			bitmap.recycle();
//			bitmap = null;
//		}

		return output;
	}

	public static boolean isNullOrEmpty(String string) {
		return (string == null)||(string.trim().isEmpty());
	}

	public static boolean isNotNullAndNotEmpty(String string) {
		return (string != null)&&(!string.trim().isEmpty());
	}


	public static Date stringToDate(String dateString, String formatString){
		//String dtStart = "2010-10-15T09:27:37Z";
		//2019-04-23 07:46:03
		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		try {
			date = format.parse(dateString);
			return date;
		} catch (Exception e) {
			e.printStackTrace();
			return date;
		}
	}

	public static String LogList(int priority, String tag, List<String> list) {
		String msg = null;
		for (String s : list){
			msg = (msg==null?s:(msg + ", " + s));
		}
		switch (priority){
			case Log.VERBOSE:
				Log.v(tag, msg);
				break;
			case Log.DEBUG:
				Log.d(tag, msg);
				break;
			case Log.INFO:
				Log.i(tag, msg);
				break;
			case Log.ERROR:
				Log.e(tag, msg);
				break;
			case Log.WARN:
				Log.w(tag, msg);
				break;
		}
		return msg;
	}

	public static String LogArray(int priority, String tag, String[] array) {
		String msg = null;
		for (int i=0; i<array.length; i++){
			msg = (msg==null?array[i]:(msg + ", " + array[i]));
		}
		switch (priority){
			case Log.VERBOSE:
				Log.v(tag, msg);
				break;
			case Log.DEBUG:
				Log.d(tag, msg);
				break;
			case Log.INFO:
				Log.i(tag, msg);
				break;
			case Log.ERROR:
				Log.e(tag, msg);
				break;
			case Log.WARN:
				Log.w(tag, msg);
				break;
		}
		return msg;
	}


	public static String getUserFirstName(String fullName) {
		String firstName;
		if (Utils.isNullOrEmpty(fullName))
			return "";
		firstName = fullName.trim();
		if (firstName.contains(" "))
			return firstName.split(" ")[0];
		else
			return firstName;
	}

}
