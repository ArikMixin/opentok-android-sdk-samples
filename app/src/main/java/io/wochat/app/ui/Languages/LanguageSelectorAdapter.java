package io.wochat.app.ui.Languages;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.wochat.app.R;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.model.SupportedLanguage;

public class LanguageSelectorAdapter extends RecyclerView.Adapter<LanguageSelectorAdapter.LanguageSelectorViewHolder> implements Filterable {

	private static final String TAG = "LanguageSelectorAdapter";
	private Context mContext;
	private List<SupportedLanguage>mLanguages = new ArrayList<>();
	private LanguageSelectionListener mLanguageSelectionListener;
	private List<SupportedLanguage> mLanguagesFiltered;


	interface LanguageSelectionListener {
		void onLanguageSelected(SupportedLanguage supportedLanguage);
	}

	public void setSupportedLanguageSelectionListener(LanguageSelectionListener languageSelectionListener) {
		mLanguageSelectionListener = languageSelectionListener;
	}

	interface ItemClickListener {
		void onItemClick(View view, int position);
	}



	public LanguageSelectorAdapter(Context context) {
		mContext = context;
	}

	@NonNull
	@Override
	public LanguageSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_lang_picker, parent, false);
		return new LanguageSelectorViewHolder(view, (view1, position) -> {
			if (mLanguageSelectionListener != null)
				mLanguageSelectionListener.onLanguageSelected(mLanguagesFiltered.get(position));
		});
	}

	@Override
	public void onBindViewHolder(@NonNull LanguageSelectorViewHolder holder, int position) {
		try {
			holder.mLanguageTV.setText(mLanguagesFiltered.get(position).getLanguageNameFromISO());
			Picasso.get().load(mLanguagesFiltered.get(position).getFlagResID()).into(holder.mCountryFlagIV);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Error onBindViewHolder, lang: " + mLanguagesFiltered.get(position).toString());
		}
	}

	@Override
	public int getItemCount() {
		return mLanguagesFiltered.size();
	}

	public void setLanguages(List<SupportedLanguage> supportedLanguages) {
		mLanguages = supportedLanguages;
		mLanguagesFiltered = supportedLanguages;

		notifyDataSetChanged();
	}

	//VIEW HOLDER
	public class LanguageSelectorViewHolder extends RecyclerView.ViewHolder
		implements View.OnClickListener {


		private TextView mLanguageTV;
		private ImageView mCountryFlagIV;
		private ItemClickListener mItemClickListener;


		public LanguageSelectorViewHolder(View itemView, ItemClickListener itemClickListener) {
			super(itemView);
			mCountryFlagIV = itemView.findViewById(R.id.country_flag_iv);
			mLanguageTV = itemView.findViewById(R.id.language_name_tv);
			mItemClickListener = itemClickListener;
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (mItemClickListener != null)
				mItemClickListener.onItemClick(v, getAdapterPosition());
		}
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				String charString = constraint.toString();
				if (charString.isEmpty()) {
					mLanguagesFiltered = mLanguages;
				} else {
					List<SupportedLanguage> filteredList = new ArrayList<>();
					for (SupportedLanguage language : mLanguages) {

						// name match condition. this might differ depending on your requirement
						// here we are looking for name or phone number match
						if (language.getLanguageNameFromISO().toLowerCase().contains(charString.toLowerCase()) || language.getLanguageNameFromISO().toLowerCase().contains(constraint)) {
							filteredList.add(language);
						}
					}

					mLanguagesFiltered = filteredList;
				}

				FilterResults filterResults = new FilterResults();
				filterResults.values = mLanguagesFiltered;
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults filterResults) {
				mLanguagesFiltered = (ArrayList<SupportedLanguage>) filterResults.values;
				notifyDataSetChanged();
			}
		};
	}


}
