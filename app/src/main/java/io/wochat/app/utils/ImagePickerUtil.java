package io.wochat.app.utils;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImagePickerUtil {

	public static Intent getPickImageChooserIntent(Context context) {

		// Determine Uri of camera image to save.
		Uri outputFileUri = getCaptureImageOutputUri(context);

		List<Intent> allIntents = new ArrayList<>();
		PackageManager packageManager = context.getPackageManager();

		// collect all camera intents
		Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
		for (ResolveInfo res : listCam) {
			Intent intent = new Intent(captureIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			intent.setPackage(res.activityInfo.packageName);
			if (outputFileUri != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			}
			allIntents.add(intent);
		}

		// collect all gallery intents
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
		for (ResolveInfo res : listGallery) {
			Intent intent = new Intent(galleryIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			intent.setPackage(res.activityInfo.packageName);
			allIntents.add(intent);
		}

		// the main intent is the last in the list (fucking android) so pickup the useless one
		Intent mainIntent = allIntents.get(allIntents.size() - 1);
		for (Intent intent : allIntents) {
			if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
				mainIntent = intent;
				break;
			}
		}
		allIntents.remove(mainIntent);

		// Create a chooser from the main intent
		Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

		// Add all other intents
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

		return chooserIntent;
	}


	public static Uri getCaptureImageOutputUri(Context context) {
		Uri outputFileUri = null;
		File getImage = context.getExternalCacheDir();
		String mImageName;
		if (getImage != null) {


			String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm_SSS", Locale.getDefault()).format(new Date());
			File mediaFile;
			mImageName = "PIC_" + timeStamp + ".jpg";
			mediaFile = new File(getImage.getPath() + File.separator + mImageName);
			outputFileUri = Uri.fromFile(mediaFile);
//			return mediaFile;
//			outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
		}
		return outputFileUri;
	}


	public static Uri getPickImageResultUri(Context context, Intent data) {
		boolean isCamera = true;
		if (data != null && data.getData() != null) {
			String action = data.getAction();
			isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
		}
		return isCamera ? getCaptureImageOutputUri(context) : data.getData();
	}

	public static byte[] getImageBytes(ContentResolver contentResolver, Uri uri){
		try {


			Matrix matrix = null;
			try {
				ExifInterface exif = new ExifInterface(uri.getPath());
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
				Log.d("EXIF", "Exif: " + orientation);
				matrix = new Matrix();
				if (orientation == 6) {
					matrix.postRotate(90);
				} else if (orientation == 3) {
					matrix.postRotate(180);
				} else if (orientation == 8) {
					matrix.postRotate(270);
				}
			} catch (IOException e) {

			}


			InputStream imageStream;
			Bitmap imageBitmap = null;
			try {
				imageStream = contentResolver.openInputStream(uri);
				imageBitmap = BitmapFactory.decodeStream(imageStream);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			int width = imageBitmap.getWidth();
			int height = imageBitmap.getHeight();

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			File outputFile = null;

			if (width > 1300){
				int newWidth, newHeight;
				newWidth = 1300;
				newHeight = 1300 * height / width;

				imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false);

				if (matrix != null)
					imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, newWidth, newHeight, matrix, true);

			}





			//imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
			byte[] picByte = byteArrayOutputStream.toByteArray();
			return picByte;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public static byte[] getImageBytes(Context context, Uri uri){
		return getImageBytes(context.getContentResolver(), uri);
	}


	public static Bitmap rotateImage(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
			matrix, true);
	}

}
