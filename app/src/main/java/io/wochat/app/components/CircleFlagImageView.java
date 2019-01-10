package io.wochat.app.components;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import io.wochat.app.R;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.utils.Utils;

public class CircleFlagImageView extends LinearLayout {
	private CircleImageView mContactPicCIV;
	private CircleImageView mContactFlagCIV;

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
		inflater.inflate(R.layout.contact_clircle_flag, this);
		mContactPicCIV = (CircleImageView)findViewById(R.id.contact_pic_civ);
		mContactFlagCIV = (CircleImageView)findViewById(R.id.contact_flag_civ);
	}

	public void setContact(Contact contact){
		String picUrl = contact.getContactServer().getProfilePicUrl();
		String language = contact.getContactServer().getLanguage();
		@DrawableRes int flagDrawable = getCountryFlagDrawableFromLang(language);

		int imageSizeDp = Utils.dp2px(getContext(), 50);
		int flagSizeDp = Utils.dp2px(getContext(), 20);

		if((picUrl != null)&& (!picUrl.trim().equals(""))){
			Picasso.get().
				load(picUrl).
				resize(imageSizeDp,imageSizeDp).
				placeholder(R.drawable.ic_empty_contact_1).
				centerCrop().
				into(mContactPicCIV);
		}
		else {
			Picasso.get().
				load(R.drawable.ic_empty_contact_1).
				into(mContactPicCIV);
		}

		Picasso.get().
			load(flagDrawable).
			resize(flagSizeDp,flagSizeDp).
			centerCrop().
			into(mContactFlagCIV);

	}

	private @DrawableRes int getCountryFlagDrawableFromLang(String language) {
		switch (language){
			case "IL":
				return R.drawable.flag_israel;


			default:
				return R.drawable.flag_united_states_of_america;
		}

	}
}
