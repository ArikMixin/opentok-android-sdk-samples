package io.wochat.app.components;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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

		Picasso.get().
			load(flagDrawable).
			resize(flagSizeDp,flagSizeDp).
			centerCrop().
			into(mContactFlagCIV);


	}

	public void setContact(Contact contact){
		String picUrl = contact.getAvatar();
		String language = contact.getContactServer().getLanguage();
		setInfo(picUrl, language, contact.getInitials());
	}

	public void displayFlag(boolean withFlag){
		mContactFlagCIV.setVisibility(Utils.booleanToVisibilityInvisible(withFlag));
	}


}
