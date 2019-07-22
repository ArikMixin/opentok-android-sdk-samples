package io.slatch.app.ui.Interpreter;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.slatch.app.AppExecutors;
import io.slatch.app.R;
import io.slatch.app.components.CircleImageView;
import io.slatch.app.db.WCSharedPreferences;
import io.slatch.app.model.SupportedLanguage;
import io.slatch.app.ui.Languages.LanguageSelectorDialog;
import io.slatch.app.ui.settings.SettingsActivity;
import io.slatch.app.utils.SpeechToTextUtil;
import io.slatch.app.utils.TextToSpeechUtil;
import io.slatch.app.utils.Utils;
import io.slatch.app.viewmodel.SupportedLanguagesViewModel;
import io.slatch.app.viewmodel.VideoAudioCallViewModel;
import me.grantland.widget.AutofitTextView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class InterpreterFragment extends Fragment implements
		View.OnClickListener,
		View.OnTouchListener,
		SpeechToTextUtil.SpeechUtilsSTTListener,
		TextToSpeechUtil.TextToSpeechPlayingListener {

	private static final String TAG = "InterpreterFragment" ;
	private View view;
	private RelativeLayout mTopInsideRL;
	private RelativeLayout mBottomRL;
    private ImageView mRotationIV;
    private String selfLang;
	private ImageView mTopIV;
	private ImageView mBottomIV;
	private ImageView mMicTopIV;
	private ImageView mMicBottomIV;
	private AutofitTextView mTopTV;
	private AutofitTextView mBottomTV;
	private TextView mListeningBottomTV;
	private TextView mListeningTopTV;
	private CircleImageView mTopFlagCIV;
	private CircleImageView mBottomFlagCIV;
	private VideoAudioCallViewModel videoAudioCallViewModel;
	private SupportedLanguagesViewModel mSupportedLanguagesViewModel;
	private List<SupportedLanguage> mSupportedLanguages;
	private LanguageSelectorDialog mLangugesDialog;
	private AppExecutors mAppExecutors;
	private AlphaAnimation mConnectingAnim;
	private boolean initLang;
	private boolean mBottomFlag;
	private boolean mBottomMic;
	private boolean rotationFlag = true;
	boolean languageChanged;
	private String mLastSelectedLang;
	private String mBottomLang, mTopLang;
	private boolean mShowTranslation;
	public static final int RC_VIDEO_APP_PERM = 124;
	public static final String[] perms = {Manifest.permission.RECORD_AUDIO };
	private boolean mErrorFlag;
	private long mHoldTime;
	private long mRealeseTime = -1;
	private double mDifTouch;
	private boolean mDealayFlag;

	public static InterpreterFragment newInstance() { return new InterpreterFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.interpreter_fragment, container, false);

		setHasOptionsMenu(true);
		initView();
		return view;
	}

	private void initView() {
		mTopInsideRL = (RelativeLayout) view.findViewById(R.id.top_inside_rl);
		mBottomRL = (RelativeLayout) view.findViewById(R.id.bottom_rl);
		mTopIV = (ImageView) view.findViewById(R.id.top_iv);
		mBottomIV = (ImageView) view.findViewById(R.id.bottom_iv);
        mRotationIV = (ImageView) view.findViewById(R.id.rotation_iv);
		mMicTopIV = (ImageView) view.findViewById(R.id.mic_top_iv);
		mMicBottomIV = (ImageView) view.findViewById(R.id.mic_bottom_iv);
		mTopTV = (AutofitTextView) view.findViewById(R.id.top_tv);
		mBottomTV = (AutofitTextView) view.findViewById(R.id.bottom_tv);
		mTopFlagCIV = (CircleImageView) view.findViewById(R.id.top_flag_civ);
		mBottomFlagCIV = (CircleImageView) view.findViewById(R.id.bottom_flag_civ);
        mListeningBottomTV = (TextView) view.findViewById(R.id.listening_bottom_tv);
		mListeningTopTV = (TextView) view.findViewById(R.id.listening_top_tv);

		// enable mics btns only after load languages
		mMicBottomIV.setEnabled(false);
		mMicTopIV.setEnabled(false);

		//Get users (Self) language
        mAppExecutors = new AppExecutors();
        mLangugesDialog = new LanguageSelectorDialog();
		selfLang = WCSharedPreferences.getInstance(view.getContext()).getUserLang().toLowerCase();

		//View Model
        SpeechToTextUtil.getInstance(getContext()).setSpeechUtilsSTTListener(this);
		mSupportedLanguagesViewModel = ViewModelProviders.of(this).get(SupportedLanguagesViewModel.class);
		mSupportedLanguagesViewModel.loadLanguages(WCSharedPreferences.getInstance(getContext()).getUserLang().toLowerCase());
        mSupportedLanguagesViewModel.getSupportedLanguages().observe(getActivity(), supportedLanguages ->{
																									mSupportedLanguages = supportedLanguages;
																									mLangugesDialog.changLangList(supportedLanguages); });

		videoAudioCallViewModel = ViewModelProviders.of(this).get(VideoAudioCallViewModel.class);
        videoAudioCallViewModel.translateText("Press and hold the microphone","EN", selfLang);
		videoAudioCallViewModel.getTranslatedText().observe(this, translatedText -> {
			if(translatedText != null)
																									if(mShowTranslation) {
																										mShowTranslationAndPlay(translatedText);
																										return;
																									}
																							  	 	changeLanguages(translatedText);
		});

		//Animation
		mConnectingAnim = new AlphaAnimation(0.0f, 1.0f);
		mConnectingAnim.setDuration(1000);
		mConnectingAnim.setRepeatCount(Animation.INFINITE);
		mConnectingAnim.setRepeatMode(Animation.REVERSE);

		//OnClicks
		mRotationIV.setOnClickListener(this);
		mMicTopIV.setOnTouchListener(this);
		mMicBottomIV.setOnTouchListener(this);
		mTopFlagCIV.setOnClickListener(this);
		mBottomFlagCIV.setOnClickListener(this);
	}



	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main_activity3, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(getContext(), SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.rotation_iv:
                    if(rotationFlag)
                        rotationFlag = false;
                    else
                        rotationFlag = true;

 			         mTopInsideRL.setRotation(mTopInsideRL.getRotation()-180);
                break;
			case R.id.top_flag_civ:
                        mBottomFlag = false;
                        getSportedLanguages();
				break;
			case R.id.bottom_flag_civ:
                        mBottomFlag = true;
                        getSportedLanguages();
				break;
        }
    }

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {

		//Ask for permeation first
		if(!requestPermissions())
				return false;

		//Prevent multi fingers touch
		if(motionEvent.getPointerCount() == 2)
			return true;

		switch(view.getId()){
			case R.id.mic_top_iv:
					onTouchActions(view, motionEvent);
			break;

			case R.id.mic_bottom_iv:
					onTouchActions(view, motionEvent); // Push to talk btn
			break;
		}
		return true;
	}



	private void  onTouchActions(View view, MotionEvent event){
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: // --Hold--
				mHoldTime = System.currentTimeMillis();

				if(mRealeseTime != -1) {
					mDifTouch = (mHoldTime - mRealeseTime) / 1000f;

					//Prevent monkey clicking on the mic, the button will be release only after 0.3 seconds
					if(mDifTouch < 0.3) {
						mDealayFlag = true;
						return;
					}
				}
				mDealayFlag = false;

         		speechToTextStarted(view);
			break;

			case MotionEvent.ACTION_MOVE:
			break;

			case MotionEvent.ACTION_UP: // --Release--
				if(!mDealayFlag)
				mRealeseTime = System.currentTimeMillis();

				if(!mErrorFlag)
						speechToTextStoped(view);
			break;

			default: // --Release--
				if(!mDealayFlag)
				mRealeseTime = System.currentTimeMillis();

				if(!mErrorFlag)
						speechToTextStoped(view);
            break;
		}
	}

	public void  speechToTextStarted(View view){
		view.setBackgroundResource(R.drawable.interpreter_active);

		Log.d(TAG, "speechToTextStarted: ");
				mErrorFlag = false;
                mBottomTV.setText("");
                mTopTV.setText("");

                if(mMicBottomIV.getId() == view.getId()){
                        mBottomMic = true;
                        mMicTopIV.setBackgroundResource(R.drawable.interpreter_disable);
                        SpeechToTextUtil.getInstance(getContext()).changeLanguage(mBottomLang.toLowerCase());
						mListeningBottomTV.startAnimation(mConnectingAnim);
						mListeningBottomTV.setVisibility(View.VISIBLE);
				}else if(mMicTopIV.getId() == view.getId()){
                        mBottomMic = false;
                        mMicBottomIV.setBackgroundResource(R.drawable.interpreter_disable);
                        SpeechToTextUtil.getInstance(getContext()).changeLanguage(mTopLang.toLowerCase());
						mListeningTopTV.startAnimation(mConnectingAnim);
						mListeningTopTV.setVisibility(View.VISIBLE);
                }

                SpeechToTextUtil.getInstance(getContext()).startSpeechToText();
    }

	public void  speechToTextStoped(View view){
		Log.d(TAG, "speechToTextStoped: ");
		SpeechToTextUtil.getInstance(getContext()).stopSpeechToText();

//		if(mDifTouch / 1000 < 0.5) {
//				onEndOfSpeechToText();
//			return;
//		}

		mMicBottomIV.setEnabled(false);
		mMicTopIV.setEnabled(false);

		//Set talk button (blue)
		view.setBackgroundResource(R.drawable.interpreter_talk);

		//Cancel "Listeining.." massage
		if(mListeningBottomTV.getVisibility() == View.VISIBLE) {
				mListeningBottomTV.setVisibility(View.GONE);
				mConnectingAnim.cancel();
		}
		if(mListeningTopTV.getVisibility() == View.VISIBLE) {
				mListeningTopTV.setVisibility(View.GONE);
				mConnectingAnim.cancel();
		}

//		SpeechToTextUtil.getInstance(getContext()).stopSpeechToText();
	}
	private void changeLanguages(String translatedTxt) {
		if (!initLang) {
				/** TOP **/
				//by default - Top language should be "French"
				//if users self lang is French - change the top language to english
				if (selfLang.equals("fr")) {
					Picasso.get()
							.load("file:///android_asset/interpreter_en.png")
							.error(R.drawable.interpeter_general)
							.into(mTopIV);

					//Flag
					mTopFlagCIV.setVisibility(View.VISIBLE);
					mTopFlagCIV.setImageResource(R.drawable.flag_united_states_of_america);

					//Set text in english
					mTopTV.setVisibility(View.VISIBLE);
					mTopTV.setText("Press and hold the microphone");
					mTopLang = "EN";
				} else {
					Picasso.get()
							.load("file:///android_asset/interpreter_fr.png")
							.error(R.drawable.interpeter_general)
							.into(mTopIV);

					mTopFlagCIV.setVisibility(View.VISIBLE);
					mTopTV.setVisibility(View.VISIBLE);
					mTopLang = "FR";
				}

				/** Bottom (Self) **/
				//Bottom language should be Users languge
				Picasso.get()
						.load("file:///android_asset/interpreter_" + selfLang + ".png")
						.error(R.drawable.interpeter_general)
						.into(mBottomIV);
				//Flag
				mBottomFlagCIV.setVisibility(View.VISIBLE);
				mBottomFlagCIV.setImageResource(Utils.getCountryFlagDrawableFromLang(selfLang.toUpperCase()));

				mBottomTV.setVisibility(View.VISIBLE);
				mBottomTV.setText(translatedTxt);
				mBottomLang = selfLang.toUpperCase();

				/** AFTER INIT **/
				mMicBottomIV.setEnabled(true);
				mMicTopIV.setEnabled(true);
               	initLang = true;

        } else if (!mBottomFlag && mLastSelectedLang != null && languageChanged){
				//Bottom language should be Users language
				Picasso.get()
						.load("file:///android_asset/interpreter_" + mLastSelectedLang.toLowerCase() + ".png")
						.error(R.drawable.interpeter_general)
						.placeholder(R.color.white)
						.into(mTopIV);
				//Flag
				mTopFlagCIV.setImageResource(Utils.getCountryFlagDrawableFromLang(mLastSelectedLang.toUpperCase()));
				mTopTV.setText(translatedTxt);
				mTopLang = mLastSelectedLang.toUpperCase();

				mMicTopIV.setEnabled(true);
				mMicBottomIV.setEnabled(true);
				languageChanged = false;
		}else if (mBottomFlag && mLastSelectedLang != null && languageChanged){
				//Bottom language should be Users language
				Picasso.get()
						.load("file:///android_asset/interpreter_" + mLastSelectedLang.toLowerCase() + ".png")
						.error(R.drawable.interpeter_general)
						.placeholder(R.color.white)
						.into(mBottomIV);
				//Flag
				mBottomFlagCIV.setImageResource(Utils.getCountryFlagDrawableFromLang(mLastSelectedLang.toUpperCase()));
				mBottomTV.setText(translatedTxt);
				mBottomLang = mLastSelectedLang.toUpperCase();

				mMicTopIV.setEnabled(true);
				mMicBottomIV.setEnabled(true);
				languageChanged = false;
		}
	}

	private void getSportedLanguages() {
		mMicTopIV.setEnabled(false);
		mMicBottomIV.setEnabled(false);

	String updatedSelfLang = WCSharedPreferences.getInstance(getContext()).getUserLang().toLowerCase();

		//If user change app's settings language - change s supported language
		if(!selfLang.equals(updatedSelfLang)) {
				selfLang =	updatedSelfLang;
				mSupportedLanguagesViewModel.loadLanguages(updatedSelfLang);
		}

           openLanguagesDialog();
    }

	private void openLanguagesDialog() {
        boolean rotationFlag_result = false;
	    if(!mBottomFlag && rotationFlag)
            rotationFlag_result = true;

		mLangugesDialog.showDialog(getActivity(),rotationFlag_result, mSupportedLanguages, supportedLanguage -> {
				mLastSelectedLang = supportedLanguage.getLanguageCode().toUpperCase();
				languageChanged = true;

				videoAudioCallViewModel.translateText("Press and hold the microphone","EN", mLastSelectedLang);

		});
	}

	//Speech to text
    @Override
    public void onSpeechToTextResult(String text, int duration) {
		Log.d(TAG, "onSpeechToTextResult: ");
		if(mBottomMic) {
			mBottomTV.setText(text);
			videoAudioCallViewModel.translateText(mBottomTV.getText().toString(), mBottomLang, mTopLang);
		}else {
			mTopTV.setText(text);
			videoAudioCallViewModel.translateText(mTopTV.getText().toString(),mTopLang, mBottomLang);
		}
		mShowTranslation = true;
		SpeechToTextUtil.getInstance(getContext()).changeLanguage(selfLang.toUpperCase());
    }

    @Override
    public void onBeginningOfSpeechToText() {
    }

    @Override
    public void onEndOfSpeechToText() {
		Log.d(TAG, "onEndOfSpeechToText: ");
		mMicTopIV.setBackgroundResource(R.drawable.interpreter_talk);
		mMicBottomIV.setBackgroundResource(R.drawable.interpreter_talk);
		mMicTopIV.setEnabled(true);
		mMicBottomIV.setEnabled(true);

		if(mListeningBottomTV.getVisibility() == View.VISIBLE) {
			mListeningBottomTV.setVisibility(View.GONE);
			mConnectingAnim.cancel();
		}
		if(mListeningTopTV.getVisibility() == View.VISIBLE) {
			mListeningTopTV.setVisibility(View.GONE);
			mConnectingAnim.cancel();
		}
	}

    @Override
    public void onErrorOfSpeechToText(int resourceString) {
		Log.d(TAG, "onErrorOfSpeechToText: ");
			mErrorFlag = true;
		mMicTopIV.setBackgroundResource(R.drawable.interpreter_talk);
		mMicBottomIV.setBackgroundResource(R.drawable.interpreter_talk);
		mMicTopIV.setEnabled(true);
		mMicBottomIV.setEnabled(true);

		if(mListeningBottomTV.getVisibility() == View.VISIBLE) {
			mListeningBottomTV.setVisibility(View.GONE);
			mConnectingAnim.cancel();
		}
		if(mListeningTopTV.getVisibility() == View.VISIBLE) {
			mListeningTopTV.setVisibility(View.GONE);
			mConnectingAnim.cancel();
		}
	}

	private void mShowTranslationAndPlay(String translatedText) {
		Log.d(TAG, "mShowTranslationAndPlay: ");
		mMicTopIV.setBackgroundResource(R.drawable.interpreter_talk);
        mMicBottomIV.setBackgroundResource(R.drawable.interpreter_talk);
		mMicTopIV.setEnabled(true);
        mMicBottomIV.setEnabled(true);

        if(mBottomMic) {
			mTopTV.setText(translatedText);
			TextToSpeechUtil.getInstance(getContext()).setLanguage(mTopLang); //Play in users language

		}else {
			mBottomTV.setText(translatedText);
			TextToSpeechUtil.getInstance(getContext()).setLanguage(mBottomLang); //Play in users language

		}
		mShowTranslation = false;

		//playText
		TextToSpeechUtil.getInstance(getContext()).startTextToSpeech(translatedText, this);
	}

	@Override
	public void onBeginPlaying() { }

	@Override
	public void onFinishedPlaying() {
		Log.d(TAG, "onFinishedPlaying: ");
		TextToSpeechUtil.getInstance(getContext()).stopTextToSpeech();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@AfterPermissionGranted(RC_VIDEO_APP_PERM)
	private boolean requestPermissions() {
		if (EasyPermissions.hasPermissions(getContext(), perms)) {
			return true;
		} else {
			EasyPermissions.requestPermissions(this, getString(R.string.permissions_interpreter),
					RC_VIDEO_APP_PERM, perms);
			return false;
		}
	}
}
