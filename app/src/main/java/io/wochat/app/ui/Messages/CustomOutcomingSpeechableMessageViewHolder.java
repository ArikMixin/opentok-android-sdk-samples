package io.wochat.app.ui.Messages;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.messages.MessageHolders;

import io.wochat.app.R;
import io.wochat.app.components.CircleImageView;
import io.wochat.app.db.WCSharedPreferences;
import io.wochat.app.db.entity.Message;
import io.wochat.app.utils.DateFormatter;
import io.wochat.app.utils.SpeechUtils;
import io.wochat.app.utils.Utils;


public class CustomOutcomingSpeechableMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message>
		implements View.OnClickListener, SpeechUtils.SpeechUtilsTTSListener{

	private static final String TAG = "OutSpeechMsgViewHldr" ;
	private String mPictureUrl;
	private CircleImageView mAvatarCIV;
	private ImageView mCocheIV;
	private TextView mDurationTV;
	private TextView mTimeTV;
	private ImageView mPlayPauseIV;
	private SeekBar mSeekBar;
	private boolean mIsPlaying;
	private int mCurrentPosition;
	private int mUserSelectedPosition;
	private boolean mUserIsSeeking;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private SpeechUtils mSpeechUtils;
	private String mMessageText;
	private int mMessageDuration;

	public CustomOutcomingSpeechableMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);

        mCocheIV = (ImageView) itemView.findViewById(R.id.coche_iv);
        mDurationTV = (TextView) itemView.findViewById(R.id.duration);
        mTimeTV = (TextView) itemView.findViewById(R.id.time);
        mPlayPauseIV = (ImageView) itemView.findViewById(R.id.play_pause_iv);
		mPlayPauseIV.setOnClickListener(this);
        mSeekBar = (SeekBar) itemView.findViewById(R.id.seekbar);
		mAvatarCIV = (CircleImageView)itemView.findViewById(R.id.messageUserAvatar);
		mPictureUrl = (String)payload;

		if (mPictureUrl == null)
			Picasso.get().load(R.drawable.ic_action_empty_contact).placeholder(R.drawable.ic_action_empty_contact).into(mAvatarCIV);
		else
			Picasso.get().load(mPictureUrl).error(R.drawable.ic_action_empty_contact).placeholder(R.drawable.ic_action_empty_contact).into(mAvatarCIV);



    }


//    private void initMediaPlayer(){
//		if (mMediaPlayer != null){
//			if (mMediaPlayer.isPlaying())
//				mMediaPlayer.stop();
//			mMediaPlayer.release();
//			mMediaPlayer = null;
//		}
//		mMediaPlayer = new MediaPlayer();
//		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//			@Override
//			public void onCompletion(MediaPlayer mp) {
//				mHandler.removeCallbacks(mUpdateSeekbarRunnable);
//				mDurationTV.setText(Utils.convertSecondsToHMmSs(mMediaPlayer.getDuration()));
//				setSeekBarProgress(0);
//				mPlayPauseIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.msg_audio_play_orange));
//			}
//		});
//		mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//			@Override
//			public void onPrepared(MediaPlayer mp) {
//				mSeekBar.post(() -> {
//					int duration = mMediaPlayer.getDuration();
//					Log.e(TAG, "onPrepared, duration: " + duration );
//					mSeekBar.setMax(duration);
//					mDurationTV.setText(Utils.convertSecondsToHMmSs(duration));
//				});
//
//			}
//		});
//
//	}



	@Override
    public void onBind(Message message) {
        super.onBind(message);

        mMessageText = message.getText();

		mSpeechUtils = new SpeechUtils();
		mSpeechUtils.setSpeechUtilsTTSListener(this);
		String selfLang = WCSharedPreferences.getInstance(this.itemView.getContext()).getUserLang();
		mSpeechUtils.initSpeech(this.itemView.getContext(), this.itemView.getContext().getPackageName(), selfLang);


		mPlayPauseIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.msg_audio_play_orange));
		mSeekBar.setProgress(0);
		mSeekBar.setMax(message.getDuration());
        mDurationTV.setText(Utils.convertSecondsToHMmSs(message.getDuration()));
        mTimeTV.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));

		mUserSelectedPosition = 0;
		mCurrentPosition = 0;


		switch (message.getStatus()){
			case Message.ACK_STATUS_PENDING:
				mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_pending));
				break;
			case Message.ACK_STATUS_SENT:
				mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_sent));
				break;
			case Message.ACK_STATUS_RECEIVED:
				mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_arrived));
				break;
			case Message.ACK_STATUS_READ:
				mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_seen));
				break;
			default:
				mCocheIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.coche_pending));
				break;
		}


    }

	private Runnable mUpdateSeekbarRunnable = new Runnable() { // this one is on the main thread
		@Override
		public void run() {
//			mCurrentPosition = mMediaPlayer.getCurrentPosition();
//			Log.e(TAG, "updateSeekBar Runnable, CurrentPosition: " + mCurrentPosition);
//			setSeekBarProgress(mCurrentPosition);
//			String time = Utils.convertSecondsToHMmSs(mCurrentPosition);
//			mDurationTV.setText(time);
//			if (mMediaPlayer.isPlaying())
//				mHandler.postDelayed(mUpdateSeekbarRunnable, 200);
//			else {
//				mDurationTV.setText(Utils.convertSecondsToHMmSs(mMediaPlayer.getDuration()));
//				if (mCurrentPosition == mMediaPlayer.getDuration()) {
//					setSeekBarProgress(0);
//				}
//			}
		}
	};

	private void setSeekBarProgress(int progress){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			mSeekBar.setProgress(progress, true);
		else
			mSeekBar.setProgress(progress);
	}

	private void updateSeekBar(){
		Log.e(TAG, "updateSeekBar");
		mHandler.post(mUpdateSeekbarRunnable);
	}

    private void play(){
		mSpeechUtils.startTextToSpeech(mMessageText);
		mIsPlaying = true;
		mPlayPauseIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.msg_audio_pause_orange));
		updateSeekBar();
	}

	private void pause(){
		if (mSpeechUtils.isPlaying()){
			mSpeechUtils.pauseTextToSpeech();
			//mCurrentPosition = mMediaPlayer.getCurrentPosition();
			mPlayPauseIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.msg_audio_play_orange));
		}
	}


	@Override
	public void onClick(View v) {
		try {
			if (mSpeechUtils.isPlaying()){
				pause();
			}
			else {
				play();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onTextToSpeechInitOK() {

	}

	@Override
	public void onBeginPlaying() {

	}

	@Override
	public void onFinishedPlaying() {
		setSeekBarProgress(0);
		mPlayPauseIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.msg_audio_play_orange));

	}
}
