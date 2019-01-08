package io.wochat.app.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.wochat.app.db.entity.ContactLocal;

public class ContactsUtil {

	private static final String TAG = "ContactsUtil";

	public Map<String, ContactLocal> readContacts(ContentResolver contentResolver, String localeCountry){
		Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		Log.e(TAG, "cursor.getCount: " + cursor.getCount());
		int sum = 0;

		Map<String, ContactLocal> contactMap = new HashMap<>();

		if ((cursor != null ? cursor.getCount() : 0) > 0) {
			while (cursor != null && cursor.moveToNext()) {
				String id = 				cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				String name=				cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {

					Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI.buildUpon()
						.appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "1")
						.build();

					//Cursor pCur = contentResolver.query(uri,
					Cursor pCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
						new String[]{id}, null);
					Log.i(TAG, "----   Name: " + name);
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
						String niceFormattedPhone = "";
						String striptFormatNumber = "";
						try {

							Phonenumber.PhoneNumber ph = phoneUtil.parse(phoneNo, localeCountry);
							niceFormattedPhone = phoneUtil.format(ph, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
							striptFormatNumber = niceFormattedPhone.replaceAll("\\D+", "");
							Log.i(TAG, "Name: " + name + " , Phone Number: " + niceFormattedPhone);
							contactMap.put(striptFormatNumber, new ContactLocal(id, name, niceFormattedPhone, striptFormatNumber));
						} catch (NumberParseException e) {
							System.err.println("NumberParseException was thrown: " + e.toString());
						}

					}
					pCur.close();
				}

			}
		}
		if(cursor!=null)
			cursor.close();

		Log.e(TAG, "sum: " + sum);

		return contactMap;

	}

	public static Uri getThumbPhoto(Context context, long contactId){
		try {
			Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
			Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
			return photoUri;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InputStream openPhoto(Context context, long contactId) {
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = context.getContentResolver().query(photoUri,
			new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
		if (cursor == null) {
			return null;
		}
		try {
			if (cursor.moveToFirst()) {
				byte[] data = cursor.getBlob(0);
				if (data != null) {
					return new ByteArrayInputStream(data);
					//return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
				}
			}
		} finally {
			cursor.close();
		}
		return null;
	}
}
