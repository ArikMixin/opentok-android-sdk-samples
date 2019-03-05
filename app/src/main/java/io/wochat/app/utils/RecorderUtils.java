package io.wochat.app.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import io.wochat.app.R;

public class RecorderUtils {


	public interface RecorderUtilsListener{
		void onFinishRecording(File file, int duration);
		void onCancelRecording();
		void onRecordingTimer(String time);
	}

	public void setRecorderUtilsListener(RecorderUtilsListener recorderUtilsListener) {
		mRecorderUtilsListener = recorderUtilsListener;
	}


	private static final String TAG = "RecorderUtils";
	private boolean mRecordingStarted;
	private MediaRecorder mRecorder;
	private File mAudioFile;
	private long mRecorderStartTimeStamp;
	private RecorderUtilsListener mRecorderUtilsListener;

	private void startRecord(Context context){
		mRecordingStarted = true;
		mRecorder = new MediaRecorder();
		mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				Log.e(TAG, "MediaRecorder onError what: " + what + " , extra: " + extra);
			}
		});

		mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {

			}
		});


		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mAudioFile = ImagePickerUtil.getAudioOutputFile(context);
		mRecorder.setOutputFile(mAudioFile.getPath());
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setAudioChannels(2);

		Log.e(TAG, "startRecord, file: " + mAudioFile.getPath());
		try {
			mRecorder.prepare();
			Log.e(TAG, "startRecord, after prepare");
		} catch (IOException e) {
			Log.e(TAG, "startRecord prepare failed");
		}

		mRecorder.start();
		mRecorderStartTimeStamp = System.currentTimeMillis();
		Log.e(TAG, "startRecord started");
		mRecordingTimer.startCountUp();

	}


	private void finishRecord(Context context){
		if ((!mRecordingStarted) || (mRecorder == null))
			return;

		int duration = (int)(System.currentTimeMillis() - mRecorderStartTimeStamp);

		mRecordingTimer.cancel();
		mRecordingStarted = false;
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;

		Log.e(TAG, "finishRecord, file: " + mAudioFile.getPath());

		mRecorderUtilsListener.onFinishRecording(mAudioFile, duration);
		//mMessageInput.getInputEditText().setHint(R.string.hint_enter_a_message);
	}

	private void cancelRecord(Context context){
		if ((!mRecordingStarted) || (mRecorder == null))
			return;
		Log.e(TAG, "cancelRecord");
		mRecordingStarted = false;
		Toast.makeText(context, "Recording canceled", Toast.LENGTH_SHORT).show();
		mRecordingTimer.cancel();

		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
		mAudioFile.delete();
		mAudioFile = null;

		mRecorderUtilsListener.onCancelRecording();
		//mMessageInput.getInputEditText().setHint(R.string.hint_enter_a_message);
	}


	private RecordingTimer mRecordingTimer = new RecordingTimer();

	private class RecordingTimer extends CountDownTimer {


		private int mCounter;

		public RecordingTimer() {
			super(60*60*1000, 1000);
		}

		public void startCountUp() {
			mCounter = 0;
			super.start();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mCounter++;
			String time = Utils.convertSecondsToHMmSs(mCounter*1000);

			mRecorderUtilsListener.onRecordingTimer(time);
//			String displayText = time + "  < " + getString(R.string.hint_slide_to_cancel);
//			mMessageInput.getInputEditText().setHint(displayText);
		}

		@Override
		public void onFinish() {
			//finishRecordOrSpeech();
		}
	};
}
