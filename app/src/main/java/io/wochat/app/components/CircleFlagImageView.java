package io.wochat.app.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.wochat.app.R;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.utils.Utils;

public class CircleFlagImageView extends LinearLayout {
	private CircleImageView mContactPicCIV;
	private CircleImageView mContactFlagCIV;
	private TextView mContactInitialsTV;
	private boolean mWithFlag;
	private boolean mIsChecked;
	private boolean mIsCanceled;

	public CircleFlagImageView(Context context) {
		super(context);
		init(context);
	}

	public CircleFlagImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CircleFlagImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public CircleFlagImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}


	private void init(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.contact_clircle_flag_new, this);
		mContactPicCIV = (CircleImageView)findViewById(R.id.contact_pic_civ);
		mContactFlagCIV = (CircleImageView)findViewById(R.id.contact_flag_civ);
		mContactInitialsTV = (TextView)findViewById(R.id.contact_initials_tv);
	}


	public void setInfoNoResize(String picUrl, String language, String initials){
		@DrawableRes int flagDrawable = Utils.getCountryFlagDrawableFromLang(language);


		if((picUrl != null)&& (!picUrl.trim().equals(""))){
			mContactInitialsTV.setVisibility(GONE);
			Picasso.get().
				load(picUrl).
				placeholder(R.drawable.ic_empty_contact).
				into(mContactPicCIV);
		}
		else {
			mContactInitialsTV.setVisibility(VISIBLE);
			mContactInitialsTV.setText(initials);
			Picasso.get().
				load(R.drawable.ic_empty_contact).
				into(mContactPicCIV);
			mContactInitialsTV.bringToFront();
		}

		if (flagDrawable == 0)
			mContactFlagCIV.setVisibility(View.INVISIBLE);
		else
			mContactFlagCIV.setVisibility(View.VISIBLE);


		if ((flagDrawable != 0) && (!mIsChecked) && (!mIsCanceled)){
			Picasso.get().
				load(flagDrawable).
				into(mContactFlagCIV);
		}
		else if (mIsChecked){
//			mContactFlagCIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_contact_checked));
			Picasso.get().
				load(R.drawable.ic_contact_checked).
				into(mContactFlagCIV);

			//mContactFlagCIV.setBackgroundColor(getResources().getColor(R.color.white));
		}
		else if (mIsCanceled){
			//mContactFlagCIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel));
			Picasso.get().
				load(R.drawable.ic_contact_cancel).
				into(mContactFlagCIV);
		}
	}


	public void setInfo(String picUrl, String language, String initials){
		@DrawableRes int flagDrawable = Utils.getCountryFlagDrawableFromLang(language);

		int imageSizeDp = Utils.dp2px(getContext(), 50);
		int flagSizeDp = Utils.dp2px(getContext(), 20);

		if((picUrl != null)&& (!picUrl.trim().equals(""))){
			mContactInitialsTV.setVisibility(GONE);
			Picasso.get().
				load(picUrl).
				resize(imageSizeDp,imageSizeDp).
				placeholder(R.drawable.ic_empty_contact).
				centerCrop().
				into(mContactPicCIV);
		}
		else {
			mContactInitialsTV.setVisibility(VISIBLE);
			mContactInitialsTV.setText(initials);
			Picasso.get().
				load(R.drawable.ic_empty_contact).
				resize(imageSizeDp,imageSizeDp).
				centerCrop().
				into(mContactPicCIV);
			mContactInitialsTV.bringToFront();
		}

		if (flagDrawable == 0)
			mContactFlagCIV.setVisibility(View.INVISIBLE);
		else
			mContactFlagCIV.setVisibility(View.VISIBLE);

		if ((flagDrawable != 0) && (!mIsChecked) && (!mIsCanceled)){
			Picasso.get().
				load(flagDrawable).
				resize(flagSizeDp, flagSizeDp).
				centerCrop().
				into(mContactFlagCIV);
		}
		else if (mIsChecked){
//			mContactFlagCIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_contact_checked));
			Picasso.get().
				load(R.drawable.ic_contact_checked).
				resize(flagSizeDp, flagSizeDp).
				centerCrop().
				into(mContactFlagCIV);

			//mContactFlagCIV.setBackgroundColor(getResources().getColor(R.color.white));
		}
		else if (mIsCanceled){
			//mContactFlagCIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel));
			Picasso.get().
				load(R.drawable.ic_contact_cancel).
				resize(flagSizeDp, flagSizeDp).
				centerCrop().
				into(mContactFlagCIV);
		}


	}

	public void setContact(Contact contact){
		String picUrl = contact.getAvatar();
		String language = contact.getContactServer().getLanguage();
		setInfo(picUrl, language, contact.getInitials());
	}


	public void setContact(Contact contact, boolean isChecked, boolean isCanceled){
		mIsChecked = isChecked;
		mIsCanceled = isCanceled;
		String picUrl = contact.getAvatar();
		String language = contact.getContactServer().getLanguage();
		setInfo(picUrl, language, contact.getInitials());
	}


	public void displayFlag(boolean withFlag){
		mWithFlag = withFlag;
		mContactFlagCIV.setVisibility(Utils.booleanToVisibilityInvisible(withFlag));
	}


	public void displayChecked(boolean isDisplayed){
		if (isDisplayed) {
			int flagSizeDp = Utils.dp2px(getContext(), 20);
			mContactFlagCIV.setVisibility(VISIBLE);
			mContactFlagCIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_contact_checked));
			mContactFlagCIV.bringToFront();
//			Picasso.get().
//				load(R.drawable.ic_checked).
//				resize(flagSizeDp, flagSizeDp).
//				centerCrop().
//				into(mContactFlagCIV);
		}
		else {
			displayFlag(mWithFlag);
		}
	}


	public void displayCanceled(boolean isDisplayed){
		if (isDisplayed) {
			int flagSizeDp = Utils.dp2px(getContext(), 20);
			mContactFlagCIV.setVisibility(VISIBLE);
			Picasso.get().
				load(R.drawable.ic_contact_cancel).
				resize(flagSizeDp, flagSizeDp).
				centerCrop().
				into(mContactFlagCIV);
		}
		else {
			displayFlag(mWithFlag);
		}
	}


}
