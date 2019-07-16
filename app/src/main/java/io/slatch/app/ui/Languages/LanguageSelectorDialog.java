package io.slatch.app.ui.Languages;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.FastScroller;

import java.util.List;

import io.slatch.app.R;
import io.slatch.app.model.SupportedLanguage;
import io.slatch.app.utils.Utils;

public class LanguageSelectorDialog implements LanguageSelectorAdapter.LanguageSelectionListener {


	private LanguageSelectorAdapter mAdapter;
	private RecyclerView recyclerView_countryDialog;

	public interface LanguageSelectionListener {
		void onLanguageSelected(SupportedLanguage supportedLanguage);
	}


	private Dialog mDialog;

	private LanguageSelectionListener mLanguageSelectionListener;

	public void showDialog(Context context, boolean mRotateFlag, List<SupportedLanguage> supportedLanguages, LanguageSelectionListener lsnr) {
		mLanguageSelectionListener = lsnr;

		mDialog = new Dialog(context);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.getWindow().setContentView(R.layout.dialog_lang_picker);
		mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		 recyclerView_countryDialog = (RecyclerView) mDialog.findViewById(R.id.languages_rv);
		final TextView titleTV = (TextView) mDialog.findViewById(R.id.title_tv);
		RelativeLayout rlQueryHolder = (RelativeLayout) mDialog.findViewById(R.id.query_holder_rl);
		ImageView imgClearQuery = (ImageView) mDialog.findViewById(R.id.clear_query_iv);
		final EditText searchET = (EditText) mDialog.findViewById(R.id.search_et);
		TextView noResultTV = (TextView) mDialog.findViewById(R.id.noresult_tv);
		RelativeLayout rlHolder = (RelativeLayout) mDialog.findViewById(R.id.holder_rl);
		ImageView imgDismiss = (ImageView) mDialog.findViewById(R.id.dismiss_iv);

		//Rotation
		if(mRotateFlag) {
			rlHolder.setRotation(180);
	//		Activity activity = (Activity) context;
	//		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
		}

		imgClearQuery.setVisibility(View.GONE);
		imgClearQuery.setOnClickListener(v -> searchET.setText(""));

		titleTV.setText("Select Language");
		searchET.setHint("Search...");
		noResultTV.setText("Results not found");



		searchET.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				mAdapter.getFilter().filter(s.toString());
				//applyQuery(s.toString());
				if (s.toString().trim().equals("")) {
					imgClearQuery.setVisibility(View.GONE);
				} else {
					imgClearQuery.setVisibility(View.VISIBLE);
				}
			}
		});

		searchET.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
				return true;
			}

			return false;
		});

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView_countryDialog.getLayoutParams();
		params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
		recyclerView_countryDialog.setLayoutParams(params);

		mAdapter = new LanguageSelectorAdapter(context);
		mAdapter.setLanguages(supportedLanguages);
		mAdapter.setLanguageSelectionListener(this);

		recyclerView_countryDialog.setLayoutManager(new LinearLayoutManager(context));
		recyclerView_countryDialog.setAdapter(mAdapter);


		FastScroller fastScroller = (FastScroller) mDialog.findViewById(R.id.fastscroll);
		fastScroller.setRecyclerView(recyclerView_countryDialog);

		imgDismiss.setOnClickListener(view -> mDialog.dismiss());

		mDialog.setOnDismissListener(dialogInterface -> {
			hideKeyboard(context);
				mDialog.dismiss();
				mDialog = null;
		});

		mDialog.setOnCancelListener(dialogInterface -> {
			hideKeyboard(context);
		});

		mDialog.setOnShowListener(dialogInterface -> {
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(mDialog.getWindow().getAttributes());
			int dialogWidth = lp.width;
			int dialogHeight = lp.height;

			int maxH = Utils.dp2px(context, 400);
//			if(dialogHeight > maxH) {
				mDialog.getWindow().setLayout(dialogWidth,maxH);
//			}
		});
				mDialog.show();
	}

	public void changLangList(List<SupportedLanguage> supportedLanguages){
	    if(mDialog != null && mDialog.isShowing()){
				mAdapter.setLanguages(supportedLanguages);
	       	    mAdapter.notifyDataSetChanged();
        }

    }
	public void setLanguageSelectionListener(LanguageSelectionListener languageSelectionListener) {
		mLanguageSelectionListener = languageSelectionListener;
	}



	@Override
	public void onLanguageSelected(SupportedLanguage supportedLanguage) {
		mDialog.dismiss();
		mLanguageSelectionListener.onLanguageSelected(supportedLanguage);
	}


	private static void hideKeyboard(Context context) {
		if (context instanceof Activity) {
			Activity activity = (Activity) context;
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
			//Find the currently focused view, so we can grab the correct window token from it.
			View view = activity.getCurrentFocus();
			//If no view currently has focus, create a new one, just so we can grab a window token from it
			if (view == null) {
				view = new View(activity);
			}
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
}
