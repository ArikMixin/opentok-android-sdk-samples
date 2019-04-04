package io.wochat.app.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.StringRes;
import android.util.Log;

import java.util.ArrayList;

import io.wochat.app.R;

public class SpeechToTextUtil implements
	RecognitionListener {



	public static SpeechToTextUtil getInstance(){
		if (mSpeechToTextUtil == null){
			mSpeechToTextUtil = new SpeechToTextUtil();
		}
		return mSpeechToTextUtil;
	}


	public static void removeInstance(){
		mSpeechToTextUtil.destroy();
		mSpeechToTextUtil = null;
	}


	public interface SpeechUtilsSTTListener {
		void onSpeechToTextResult(String text, int duration);
		void onBeginningOfSpeechToText();
		void onEndOfSpeechToText();
		void onErrorOfSpeechToText(@StringRes int resourceString);
	}

	public void setSpeechUtilsSTTListener(SpeechUtilsSTTListener speechUtilsSTTListener) {
		mSpeechUtilsSTTListener = speechUtilsSTTListener;
	}

	private SpeechUtilsSTTListener mSpeechUtilsSTTListener;
	private Intent mSpeechRecognizerIntent;
	private SpeechRecognizer mSpeechRecognizer;
	private long mStartSpeechToTextTimeStamp;
	private static final String TAG = "SpeechToTextUtil";

	private static SpeechToTextUtil mSpeechToTextUtil;
	private String mSelfLang;

	private void destroy(){
		Log.e(TAG, "destroy");
		if (mSpeechRecognizer == null)
			return;
		mSpeechRecognizer.destroy();
	}


	public void startSpeechToText() {
		Log.e(TAG, "startSpeech");
		if (mSpeechRecognizer == null)
			return;
		mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
	}

	public void stopSpeechToText() {
		Log.e(TAG, "stopSpeech");
		if (mSpeechRecognizer == null)
			return;
		mSpeechRecognizer.stopListening();
	}

	public void cancelSpeechToText() {
		Log.e(TAG, "cancelSpeech");
		if (mSpeechRecognizer == null)
			return;
		mSpeechRecognizer.cancel();
	}



	public void init(Context context, String packageName, String selfLang){
		mSelfLang = selfLang;
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
		mSpeechRecognizer.setRecognitionListener(this);
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mSelfLang);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

	}

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



}
