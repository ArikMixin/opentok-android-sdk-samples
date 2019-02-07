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
		inflater.inflate(R.layout.contact_clircle_flag_new, this);
		mContactPicCIV = (CircleImageView)findViewById(R.id.contact_pic_civ);
		mContactFlagCIV = (CircleImageView)findViewById(R.id.contact_flag_civ);
	}

	public void setInfo(String picUrl, String language){
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
				resize(imageSizeDp,imageSizeDp).
				centerCrop().
				into(mContactPicCIV);
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
		setInfo(picUrl, language);
	}

	private @DrawableRes int getCountryFlagDrawableFromLang(String language) {
		switch (language){
			case "IW":
			case "HE":
				return R.drawable.flag_israel;

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
}
