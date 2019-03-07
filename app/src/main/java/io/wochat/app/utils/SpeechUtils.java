package io.wochat.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.StringRes;
import android.util.Log;

import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import io.wochat.app.R;

public class SpeechUtils extends Transaction.Listener implements
	RecognitionListener,
	TextToSpeech.OnInitListener,
	AudioPlayer.Listener {


	public static final String NUANCE_APP_KEY = "b19e0af4fc299dda72ec6960f44975d4945171e2be8b84243dc309b5f41842a5b74c9ab375378de723f648173fbdebd30e5cb5cce839f025fcfadaf0121c96c9";
	public static final Uri NUANCE_SERVER_URI = Uri.parse("nmsps://NMDPTRIAL_valentin_valiprod_com20151212050805@sslsandbox-nmdp.nuancemobility.net:443");
	private TextToSpeech mTextToSpeech;
	private Session mNuanceSpeechSession;
	private Transaction.Options mNuanceSpeechOptions;
	private boolean mGoogleTextToSpeachSupported;
	private boolean mNuanceIsPlaying;
	private long mStartSpeechToTextTimeStamp;



	public interface SpeechUtilsSTTListener {
		void onSpeechToTextResult(String text, int duration);
		void onBeginningOfSpeechToText();
		void onEndOfSpeechToText();
		void onErrorOfSpeechToText(@StringRes int resourceString);
	}

	public interface SpeechUtilsTTSListener {
		void onTextToSpeechInitOK();
		void onBeginPlaying();
		void onFinishedPlaying();
	}


	public void setSpeechUtilsSTTListener(SpeechUtilsSTTListener speechUtilsSTTListener) {
		mSpeechUtilsSTTListener = speechUtilsSTTListener;
	}

	public void setSpeechUtilsTTSListener(SpeechUtilsTTSListener speechUtilsTTSListener) {
		mSpeechUtilsTTSListener = speechUtilsTTSListener;
	}

	private static final String TAG = "SpeechUtils";
	private SpeechRecognizer mSpeechRecognizer;
	private String mSelfLang;
	private Intent mSpeechRecognizerIntent;
	private SpeechUtilsSTTListener mSpeechUtilsSTTListener;
	private SpeechUtilsTTSListener mSpeechUtilsTTSListener;

	public void initSpeech(Context context, String packageName, String selfLang) {
		Log.e(TAG, "initSpeech selfLang: " + selfLang);

		//mSelfLang = "en";
		mSelfLang = selfLang;
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
		mSpeechRecognizer.setRecognitionListener(this);
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mSelfLang);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);


		try {
			mTextToSpeech = new TextToSpeech(context, this);
			mTextToSpeech.setOnUtteranceProgressListener(mUtteranceProgressListener);
		} catch (Exception e) {
		}
		initNuanceSpeechkit(context);
	}


	private UtteranceProgressListener mUtteranceProgressListener = new UtteranceProgressListener() {
		@Override
		public void onStart(String utteranceId) {
			Log.e(TAG, "TextToSpeech UtteranceProgressListener onStart");
			if (mSpeechUtilsTTSListener != null)
				mSpeechUtilsTTSListener.onBeginPlaying();
		}

		@Override
		public void onDone(String utteranceId) {
			Log.e(TAG, "TextToSpeech UtteranceProgressListener onDone");
			if (mSpeechUtilsTTSListener != null)
				mSpeechUtilsTTSListener.onFinishedPlaying();
		}

		@Override
		public void onError(String utteranceId) {
			Log.e(TAG, "TextToSpeech UtteranceProgressListener onError");
		}
	};

	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.e(TAG, "RecognitionListener onReadyForSpeech: " + params.toString());
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.e(TAG, "RecognitionListener onBeginningOfSpeech");
		mStartSpeechToTextTimeStamp = System.currentTimeMillis();
		if (mSpeechUtilsSTTListener != null)
			mSpeechUtilsSTTListener.onBeginningOfSpeechToText();
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		//Log.e(TAG, "RecognitionListener onRmsChanged: " + rmsdB);
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		Log.e(TAG, "RecognitionListener onBufferReceived");
	}

	@Override
	public void onEndOfSpeech() {
		Log.e(TAG, "RecognitionListener onEndOfSpeech");
		if (mSpeechUtilsSTTListener != null)
			mSpeechUtilsSTTListener.onEndOfSpeechToText();

	}

	@Override
	public void onError(int error) {
		@StringRes int resourceString = 0;
		switch (error){
			case SpeechRecognizer.ERROR_AUDIO:
				resourceString = R.string.stt_error_network_timeout;
				break;
			case SpeechRecognizer.ERROR_CLIENT:
				resourceString = R.string.stt_error_client;
				break;
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				resourceString = R.string.stt_error_permissions;
				break;
			case SpeechRecognizer.ERROR_NETWORK:
				resourceString = R.string.stt_error_network;
				break;
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				resourceString = R.string.stt_error_network_timeout;
				break;
			case SpeechRecognizer.ERROR_NO_MATCH:
				resourceString = R.string.stt_error_no_match;
				break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				resourceString = R.string.stt_error_recognizer_busy;
				break;
			case SpeechRecognizer.ERROR_SERVER:
				resourceString = R.string.stt_error_network_timeout;
				break;

		}
		Log.e(TAG, "RecognitionListener onError: " + error);
		if ((mSpeechUtilsSTTListener != null) && (error != SpeechRecognizer.ERROR_CLIENT))
			mSpeechUtilsSTTListener.onErrorOfSpeechToText(resourceString);

	}

	@Override
	public void onResults(Bundle results) {
		ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		Log.e(TAG, "RecognitionListener onResults: " + matches.toString());
		if ((matches != null) && (matches.size() > 0)) {
			int duration = (int)(System.currentTimeMillis() - mStartSpeechToTextTimeStamp);
			String text = matches.get(0);
			if (mSpeechUtilsSTTListener != null)
				mSpeechUtilsSTTListener.onSpeechToTextResult(text, duration);
		}

	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		Log.e(TAG, "RecognitionListener onPartialResults: " + partialResults.toString());
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		Log.e(TAG, "RecognitionListener onEvent, eventType: " + eventType + " , params: " + params);
	}

	public void destroy(){
		Log.e(TAG, "destroy");
		mSpeechRecognizer.destroy();
	}


	public void startSpeechToText() {
		Log.e(TAG, "startSpeech");
		mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
	}

	public void stopSpeechToText() {
		Log.e(TAG, "stopSpeech");
		mSpeechRecognizer.stopListening();
	}

	public void cancelSpeechToText() {
		Log.e(TAG, "cancelSpeech");
		mSpeechRecognizer.cancel();
	}


	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS) {
			Log.e(TAG, "TextToSpeech onInit OK: " + status);

			int isSupported = mTextToSpeech.isLanguageAvailable(new Locale(mSelfLang));
			if (isSupported >= 0) {
				mGoogleTextToSpeachSupported = true;
				Log.e(TAG, "TextToSpeech isLanguageAvailable " + mSelfLang + " : true");
				mTextToSpeech.setLanguage(new Locale(mSelfLang));
			}
			else {
				Log.e(TAG, "TextToSpeech isLanguageAvailable " + mSelfLang + " : false");
				mGoogleTextToSpeachSupported = false;

			}

			if (mSpeechUtilsTTSListener != null)
				mSpeechUtilsTTSListener.onTextToSpeechInitOK();
		}
		else {
			Log.e(TAG, "TextToSpeech onInit ERROR");
		}
	}

	public void startTextToSpeech(String text){
		if (mGoogleTextToSpeachSupported) {
			String rand = UUID.randomUUID().toString();
			int res = mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, rand);
			Log.e(TAG, "TextToSpeech speak: " + text + ", result: " + res);
		}
		else {
			mNuanceSpeechSession.speakString(text, mNuanceSpeechOptions, this);
		}
	}

	public void stopTextToSpeech(){
		if (mGoogleTextToSpeachSupported) {
			mTextToSpeech.stop();
		}
		else {
			mNuanceSpeechSession.getAudioPlayer().stop();
		}
	}

	public boolean isPlaying(){
		if (mGoogleTextToSpeachSupported) {
			return ((mTextToSpeech != null)&& (mTextToSpeech.isSpeaking()));
		}
		else {
			return mNuanceIsPlaying;
		}
	}

	public void pauseTextToSpeech(){
		if (mGoogleTextToSpeachSupported) {
			mTextToSpeech.stop();
		}
		else {
			mNuanceSpeechSession.getAudioPlayer().stop();
		}
	}


	private void initNuanceSpeechkit(Context context) {
		mNuanceSpeechSession = Session.Factory.session(context, NUANCE_SERVER_URI, NUANCE_APP_KEY);
		mNuanceSpeechOptions = new Transaction.Options();
		mNuanceSpeechOptions.setRecognitionType(RecognitionType.DICTATION);
		mNuanceSpeechOptions.setDetection(DetectionType.Short);

		// ***********************************************************************
		// https://developer.nuance.com/public/index.php?task=supportedLanguages
		// ***********************************************************************

		if(Utils.isHebrew(mSelfLang))
			mNuanceSpeechOptions.setLanguage(new Language("heb-ISR"));

		else if(mSelfLang.toLowerCase().equals("ar"))
			mNuanceSpeechOptions.setLanguage(new Language("ara-XWW"));

		mNuanceSpeechSession.setDefaultOptions(mNuanceSpeechOptions);

		mNuanceSpeechSession.getAudioPlayer().setListener(this);
	}


	@Override
	public void onBeginPlaying(AudioPlayer audioPlayer, Audio audio) {
		Log.e(TAG, "Nuance TextToSpeech onBeginPlaying " + audio.toString());
		mNuanceIsPlaying = true;
		if (mSpeechUtilsTTSListener != null)
			mSpeechUtilsTTSListener.onBeginPlaying();
	}

	@Override
	public void onFinishedPlaying(AudioPlayer audioPlayer, Audio audio) {
		Log.e(TAG, "Nuance TextToSpeech onFinishedPlaying " + audio.toString());
		mNuanceIsPlaying = false;
		if (mSpeechUtilsTTSListener != null)
			mSpeechUtilsTTSListener.onFinishedPlaying();
	}



	@Override
	public void onAudio(Transaction transaction, Audio audio) {
		Log.e(TAG, "Nuance TextToSpeech onAudio " + audio.toString());
	}

	@Override
	public void onSuccess(Transaction transaction, String s) {
		Log.e(TAG, "Nuance TextToSpeech onSuccess " + s);

	}

	@Override
	public void onError(Transaction transaction, String s, TransactionException e) {
		Log.e(TAG, "Nuance TextToSpeech onError " + s + " , " + e.getMessage());
	}

}
