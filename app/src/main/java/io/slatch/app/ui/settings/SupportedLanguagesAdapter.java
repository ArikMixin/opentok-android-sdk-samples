package io.slatch.app.ui.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.slatch.app.R;
import io.slatch.app.model.SupportedLanguage;

public class SupportedLanguagesAdapter  extends RecyclerView.Adapter<SupportedLanguagesAdapter.SupportedLangListViewHolder>  {

	private Context mContext;
	private List<SupportedLanguage>mSupportedLanguages = new ArrayList<>();
	private SupportedLanguageSelectionListener mSupportedLanguageSelectionListener;



	interface SupportedLanguageSelectionListener {
		void onSupportLanguageSelected(SupportedLanguage supportedLanguage);
	}

	public void setSupportedLanguageSelectionListener(SupportedLanguageSelectionListener supportedLanguageSelectionListener) {
		mSupportedLanguageSelectionListener = supportedLanguageSelectionListener;
	}

	interface ItemClickListener {
		void onItemClick(View view, int position);
	}



	public SupportedLanguagesAdapter(Context context) {
		mContext = context;
	}

	@NonNull
	@Override
	public SupportedLangListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.supported_language_item, parent, false);
		return new SupportedLangListViewHolder(view, (view1, position) -> {
			if (mSupportedLanguageSelectionListener != null)
				mSupportedLanguageSelectionListener.onSupportLanguageSelected(mSupportedLanguages.get(position));
		});
	}

	@Override
	public void onBindViewHolder(@NonNull SupportedLangListViewHolder holder, int position) {
        String languageCode = mSupportedLanguages.get(position).getLanguageCode();
		String countryCode = mSupportedLanguages.get(position).getCountryCode();



		//language native name
		Locale loc1 = new Locale(languageCode);
		String languageNativeName = loc1.getDisplayLanguage(loc1);
		String languageNativeNameCap = StringUtils.capitalize(languageNativeName);

		//country native name
		Locale loc2 = new Locale(languageCode, countryCode);
		String countryNativeName = loc2.getDisplayCountry(loc2);

		//language name according to device language
		String languageFromDeviceISO = mSupportedLanguages.get(position).getLanguageNameFromISO();

		//country name according to device language
		Locale loc3 = new Locale("",countryCode);
		String countryFromDeviceISO = loc3.getDisplayCountry();

		holder.mNativeLanguage.setText(languageNativeNameCap + " (" + countryNativeName + ")");
		holder.mDeviceFromLanguage.setText(languageFromDeviceISO + " (" + countryFromDeviceISO + ")");

	}

	@Override
	public int getItemCount() {
		return mSupportedLanguages.size();
	}

	public void setLanguages(List<SupportedLanguage> supportedLanguages) {
		mSupportedLanguages = supportedLanguages;
		notifyDataSetChanged();
	}

	//VIEW HOLDER
	public class SupportedLangListViewHolder extends RecyclerView.ViewHolder
		implements View.OnClickListener {


		private TextView mNativeLanguage;
		private TextView mDeviceFromLanguage;
		private ItemClickListener mItemClickListener;


		public SupportedLangListViewHolder(View itemView, ItemClickListener itemClickListener) {
			super(itemView);
			mNativeLanguage = itemView.findViewById(R.id.language_name_native_tv);
			mDeviceFromLanguage = itemView.findViewById(R.id.language_name_from_device_iso_tv);
			mItemClickListener = itemClickListener;
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (mItemClickListener != null)
				mItemClickListener.onItemClick(v, getAdapterPosition());
		}
	}



}
