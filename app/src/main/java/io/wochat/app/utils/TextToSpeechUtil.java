package io.wochat.app.utils;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.nuance.speechkit.Audio;
import com.nuance.speechkit.AudioPlayer;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Interpretation;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Recognition;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import org.json.JSONObject;

import java.util.Locale;
import java.util.UUID;

public class TextToSpeechUtil implements TextToSpeech.OnInitListener, AudioPlayer.Listener {

	private static final String TAG = "TextToSpeechUtil";
	private static TextToSpeechUtil mTextToSpeechUtil;
	private boolean mGoogleAPISupported;
	private String mSelfLang;
	private boolean mNuanceAPISupported;
	private boolean mNuanceIsPlaying;
	// old public static final String NUANCE_APP_KEY = "b19e0af4fc299dda72ec6960f44975d4945171e2be8b84243dc309b5f41842a5b74c9ab375378de723f648173fbdebd30e5cb5cce839f025fcfadaf0121c96c9";
	public static final String NUANCE_APP_KEY = "b4ee9c8d0461ff6b6d2eb0c3b00d7c7aeefa9fc81bb58877c49149483d804df26ee97f724a7edeb7ad8e7a5d9bf6d789f077520c2cda83f16bcc39a94d869bbb";
	// old public static final Uri NUANCE_SERVER_URI = Uri.parse("nmsps://NMDPTRIAL_valentin_valiprod_com20151212050805@sslsandbox-nmdp.nuancemobility.net:443");
	public static final Uri NUANCE_SERVER_URI = Uri.parse("nmsps://NMDPTRIAL_steeve_wochat_io20190402085630@sslsandbox-nmdp.nuancemobility.net:443");


	private TextToSpeech mTextToSpeech;
	private boolean mGoogleAPInitOK = false;

	private Session mNuanceSpeechSession;
	private Transaction.Options mNuanceSpeechOptionsDefault;
	private Transaction.Options mNuanceSpeechOptionsOTF; // on the fly
	private TextToSpeechInitListener mTextToSpeechInitListener;
	private TextToSpeechPlayingListener mTextToSpeechPlayingListener;


	public static TextToSpeechUtil getInstance(){
		if (mTextToSpeechUtil == null){
			mTextToSpeechUtil = new TextToSpeechUtil();
		}
		return mTextToSpeechUtil;
	}


	public static void removeInstance(){
		mTextToSpeechUtil = null;
	}

	public void setTextToSpeechInitListener(TextToSpeechInitListener textToSpeechInitListener) {
		mTextToSpeechInitListener = textToSpeechInitListener;
	}


	public interface TextToSpeechInitListener {
		void onTextToSpeechInitOK();
		void onTextToSpeechInitFAIL();
	}

	public interface TextToSpeechPlayingListener {
		void onBeginPlaying();
		void onFinishedPlaying();
	}


	@Override   // onInit of google api TextToSpeech.OnInitListener
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS) {
			mGoogleAPInitOK = true;
			Log.e(TAG, "TextToSpeech onInit OK");
			if (mTextToSpeechInitListener != null)
				mTextToSpeechInitListener.onTextToSpeechInitOK();
		}
		else {
			mGoogleAPInitOK = false;
			Log.e(TAG, "TextToSpeech onInit ERROR");
			if (mTextToSpeechInitListener != null)
				mTextToSpeechInitListener.onTextToSpeechInitFAIL();
		}
	}










	private TextToSpeechUtil(){
	}

	public void init(Application application, String selfLang){
		mSelfLang = selfLang;
		try {
			mTextToSpeech = new TextToSpeech(application.getApplicationContext(), this);
			mTextToSpeech.setOnUtteranceProgressListener(mUtteranceProgressListener);
		} catch (Exception e) {
			Log.e(TAG, "init exception: " + e.getMessage());
		}
		mNuanceAPISupported = initNuanceSpeechkit(application.getApplicationContext());
		Log.e(TAG, "initNuanceSpeechkit res: " + mNuanceAPISupported);
	}



	private UtteranceProgressListener mUtteranceProgressListener = new UtteranceProgressListener() {
		@Override // google api callback
		public void onStart(String utteranceId) {
			Log.e(TAG, "TextToSpeech UtteranceProgressListener onStart");
			if (mTextToSpeechPlayingListener != null)
				mTextToSpeechPlayingListener.onBeginPlaying();
		}

		@Override // google api callback
		public void onDone(String utteranceId) {
			Log.e(TAG, "TextToSpeech UtteranceProgressListener onDone");
			if (mTextToSpeechPlayingListener != null)
				mTextToSpeechPlayingListener.onFinishedPlaying();
		}

		@Override  // google api callback
		public void onError(String utteranceId) {
			Log.e(TAG, "TextToSpeech UtteranceProgressListener onError");
		}
	};



	public boolean setLanguage(String langCode){
		Locale locale = new Locale(langCode);
		int isSupported = mTextToSpeech.isLanguageAvailable(locale);
		if (isSupported >= 0) {
			mGoogleAPISupported = true;
			Log.e(TAG, "TextToSpeech isLanguageAvailable " + langCode + " : true");
			mTextToSpeech.setLanguage(locale);
			return true;
		}
		else {
			Log.e(TAG, "TextToSpeech isLanguageAvailable " + langCode + " : false");
			mGoogleAPISupported = false;
			String nuanceLangCode = getNuanceLanguageCode(langCode);
			Log.e(TAG, "getNuanceLanguageCode for " + langCode + " - " + nuanceLangCode);
			if (nuanceLangCode == null){
				mNuanceAPISupported = false;
				return false;
			}
			else {
				Language la = new Language(nuanceLangCode);
				//mNuanceSpeechOptionsOTF.setLanguage(la);
				mNuanceSpeechOptionsDefault.setLanguage(la);
				mNuanceAPISupported = true;
				return true;
			}
		}
	}


	public boolean initNuanceSpeechkit(Context context) {
		mNuanceSpeechSession = Session.Factory.session(context, NUANCE_SERVER_URI, NUANCE_APP_KEY);

		mNuanceSpeechOptionsDefault = new Transaction.Options();
		mNuanceSpeechOptionsDefault.setRecognitionType(RecognitionType.DICTATION);
		mNuanceSpeechOptionsDefault.setDetection(DetectionType.Short);

		mNuanceSpeechOptionsOTF = new Transaction.Options();
		mNuanceSpeechOptionsOTF.setRecognitionType(RecognitionType.DICTATION);
		mNuanceSpeechOptionsOTF.setDetection(DetectionType.Short);


		// ***********************************************************************
		// https://developer.nuance.com/public/index.php?task=supportedLanguages
		// ***********************************************************************


		String nuanceLangCode = getNuanceLanguageCode(mSelfLang);
		if (nuanceLangCode == null) // language is not supported
			return false;

		Language la = new Language(nuanceLangCode);
		mNuanceSpeechOptionsDefault.setLanguage(la);

		mNuanceSpeechSession.setDefaultOptions(mNuanceSpeechOptionsDefault);
		mNuanceSpeechSession.getAudioPlayer().setListener(this);
		return true;
	}


	@Override  // Nuance AudioPlayer.Listener
	public void onBeginPlaying(AudioPlayer audioPlayer, Audio audio) {
		Log.e(TAG, "Nuance TextToSpeech onBeginPlaying " + audio.toString());
		mNuanceIsPlaying = true;
		if (mTextToSpeechPlayingListener != null)
			mTextToSpeechPlayingListener.onBeginPlaying();

	}

	@Override   // Nuance AudioPlayer.Listener
	public void onFinishedPlaying(AudioPlayer audioPlayer, Audio audio) {
		Log.e(TAG, "Nuance TextToSpeech onFinishedPlaying " + audio.toString());
		mNuanceIsPlaying = false;
		if (mTextToSpeechPlayingListener != null)
			mTextToSpeechPlayingListener.onFinishedPlaying();
	}



	public boolean startTextToSpeech(String text, TextToSpeechPlayingListener playingListener){
		Log.e(TAG, "startTextToSpeech: " + text);
		if (isPlaying()) {
			Log.e(TAG, "startTextToSpeech is playing, perform stop");
			stopTextToSpeech();
		}

		mTextToSpeechPlayingListener = playingListener;
		if (mGoogleAPISupported && mGoogleAPInitOK){
			String rand = UUID.randomUUID().toString();
			int res = mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, rand);
			Log.e(TAG, "TextToSpeech speak: " + text + ", result: " + res);
			return (TextToSpeech.SUCCESS == res);
		}
		else {
			if (mNuanceAPISupported){
				mNuanceSpeechSession.speakString(text, mNuanceSpeechOptionsDefault, mNuanceTransactionListener);
				return true;
			}
			else {
				return false;
			}
		}
	}



	public void stopTextToSpeech(){
		Log.e(TAG, "stopTextToSpeech");
		if (mGoogleAPISupported) {
			mTextToSpeech.stop();
		}
		else {
			mNuanceSpeechSession.getAudioPlayer().stop();
		}
	}

	public boolean isPlaying(){
		if (mGoogleAPISupported) {
			boolean res = ((mTextToSpeech != null)&& (mTextToSpeech.isSpeaking()));
			Log.e(TAG, "isPlaying? google res: " + res);
			return res;
		}
		else {
			Log.e(TAG, "isPlaying? Nuance res: " + mNuanceIsPlaying);
			return mNuanceIsPlaying;
		}
	}

	public void pauseTextToSpeech(){
		Log.e(TAG, "pauseTextToSpeech");
		if (mGoogleAPISupported) {
			mTextToSpeech.stop();
		}
		else {
			mNuanceSpeechSession.getAudioPlayer().stop();
		}
	}



	private Transaction.Listener mNuanceTransactionListener = new Transaction.Listener() {
		@Override
		public void onStartedRecording(Transaction transaction) {
			super.onStartedRecording(transaction);
			Log.e(TAG, "Nuance onStartedRecording");
		}

		@Override
		public void onFinishedRecording(Transaction transaction) {
			super.onFinishedRecording(transaction);
			Log.e(TAG, "Nuance onFinishedRecording");
		}

		@Override
		public void onRecognition(Transaction transaction, Recognition recognition) {
			super.onRecognition(transaction, recognition);
			Log.e(TAG, "Nuance onRecognition : " + recognition.getText());
		}

		@Override
		public void onInterpretation(Transaction transaction, Interpretation interpretation) {
			super.onInterpretation(transaction, interpretation);
			Log.e(TAG, "Nuance onInterpretation");
		}

		@Override
		public void onServiceResponse(Transaction transaction, JSONObject jsonObject) {
			super.onServiceResponse(transaction, jsonObject);
			Log.e(TAG, "Nuance onServiceResponse: " + jsonObject.toString());
		}

		@Override
		public void onAudio(Transaction transaction, Audio audio) {
			super.onAudio(transaction, audio);
			Log.e(TAG, "Nuance onAudio: " + audio.toString());
		}

		@Override
		public void onSuccess(Transaction transaction, String s) {
			super.onSuccess(transaction, s);
			Log.e(TAG, "Nuance onSuccess s: " + s);
		}

		@Override
		public void onError(Transaction transaction, String s, TransactionException e) {
			super.onError(transaction, s, e);
			Log.e(TAG, "Nuance onError s: " + s + " , excption: " + e.getMessage());
		}
	};




	public static String getNuanceLanguageCode(String langCode){
		if ((langCode == null) || (langCode.isEmpty()))
			return null;

		String langCodeFormatted = langCode.toUpperCase().trim();

		switch (langCodeFormatted){
			case "IW": // ivrit
			case "HE": // hebrew
				return "heb-ISR";
			case "AR":  // arabic
				return "ara-XWW";
			case "ID": // Indonesian
				return "ind-IDN";
			case "CS":  // Czech
				return "ces-CZE";
			case "DA": //Danish
				return "dan-DNK";
			case "NL": //Dutch
				return "nld-NLD";
			case "EN": //English
				return "eng-USA";
			case "FI": //Finnish
				return "fin-FIN";
			case "FR": //French
				return "fra-FRA";
			case "DE": //German
				return "deu-DEU";
			case "EL": //Greek
				return "ell-GRC";
			case "HI": //Hindi
				return "hin-IND";
			case "HU": //Hungarian
				return "hun-HUN";
			case "IT": //Italian
				return "ita-ITA";
			case "JA": //Japanese
				return "jpn-JPN";
			case "KO": //Korean
				return "kor-KOR";
			case "NB": //Norwegian Bokmål
				return "nor-NOR";
			case "PL": //Polish
				return "pol-POL";
			case "PT": //Portuguese
				return "por-PRT";
			case "RO": //Romanian
				return "ron-ROU";
			case "RU": //Russian
				return "rus-RUS";
			case "SK": //Slovak
				return "slk-SVK";
			case "ES": //Spanish
				return "spa-ESP";
			case "SV": //Swedish
				return "swe-SWE";
			case "TH": //Thai
				return "tha-THA";
			case "TR": //Turkish
				return "tur-TUR";


			default:
				return null;

		}
	}
}


/*
*
*
Arabic	ara-XWW	Laila	F
Arabic	ara-XWW	Maged	M
Arabic	ara-XWW	Tarik	M
Bahasa (Indonesia)	ind-IDN	Damayanti	F
Basque	baq-ESP	Miren	F
Cantonese	yue-CHN	Sin-Ji	F
Catalan	cat-ESP	Jordi	M
Catalan	cat-ESP	Montserrat	F
Czech	ces-CZE	Iveta	F
Czech	ces-CZE	Zuzana	F
Danish	dan-DNK	Ida	F
Danish	dan-DNK	Magnus	M
Dutch	nld-NLD	Claire	F
Dutch	nld-NLD	Xander	M
Dutch (Belgium)	nld-BEL	Ellen	F
English (Australia)	eng-AUS	Karen	F
English (Australia)	eng-AUS	Lee	M
English (GB)	eng-GBR	Kate	F
English (GB)	eng-GBR	Serena	F
English (GB)	eng-GBR	Daniel	M
English (GB)	eng-GBR	Oliver	M
English (India)	eng-IND	Veena	F
English (Ireland)	eng-IRL	Moira	F
English (Scotland)	eng-SCT	Fiona	F
English (South Africa)	eng-ZAF	Tessa	F
English (US)	eng-USA	Ava	F
English (US)	eng-USA	Allison	F
English (US)	eng-USA	Samantha	F
English (US)	eng-USA	Susan	F
English (US)	eng-USA	Zoe	F
English (US)	eng-USA	Tom	M
Finnish	fin-FIN	Satu	F
French	fra-FRA	Audrey-ML	F
French	fra-FRA	Thomas	M
French	fra-FRA	Aurelie	F
French (Canada)	fra-CAN	Amelie	F
French (Canada)	fra-CAN	Chantal	F
French (Canada)	fra-CAN	Nicolas	M
Galician	glg-ESP	Carmela	F
German	deu-DEU	Anna-ML	F
German	deu-DEU	Petra-ML	F
German	deu-DEU	Markus	M
German	deu-DEU	Yannick	M
Language	6 char *	Voice	M / F
Greek	ell-GRC	Melina	F
Greek	ell-GRC	Nikos	M
Hebrew	heb-ISR	Carmit	F
Hindi	hin-IND	Lekha	F
Hungarian	hun-HUN	Mariska	F
Italian	ita-ITA	Alice-ML	F
Italian	ita-ITA	Federica	F
Italian	ita-ITA	Paola	F
Italian	ita-ITA	Luca	M
Japanese	jpn-JPN	Kyoko	F
Japanese	jpn-JPN	Otoya	M
Korean	kor-KOR	Sora	F
Mandarin (China)	cmn-CHN	Tian-Tian	F
Mandarin (Taiwan)	cmn-TWN	Mei-Jia	F
Norwegian	nor-NOR	Nora	F
Norwegian	nor-NOR	Henrik	M
Polish	pol-POL	Ewa	F
Polish	pol-POL	Zosia	F
Portuguese (Brazil)	por-BRA	Luciana	F
Portuguese (Brazil)	por-BRA	Felipe	M
Portuguese (Portugal)	por-PRT	Catarina	F
Portuguese (Portugal)	por-PRT	Joana	F
Romanian	ron-ROU	Ioana	F
Russian	rus-RUS	Katya	F
Russian	rus-RUS	Milena	F
Russian	rus-RUS	Yuri	M
Slovak	slk-SVK	Laura	F
Spanish (Castilian)	spa-ESP	Monica	F
Spanish (Castilian)	spa-ESP	Jorge	M
Spanish (Columbia)	spa-COL	Soledad	F
Spanish (Columbia)	spa-COL	Carlos	M
Spanish (Mexico)	spa-MEX	Angelica	F
Spanish (Mexico)	spa-MEX	Paulina	F
Spanish (Mexico)	spa-MEX	Juan	M
Swedish	swe-SWE	Alva	F
Swedish	swe-SWE	Oskar	M
Thai	tha-THA	Kanya	F
Turkish	tur-TUR	Cem	M
Turkish	tur-TUR	Yelda	F
Valencian	spa-ESP	Empar	F
*
*
* */
