package io.wochat.app.ui.Messages;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
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
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.components.CircleImageView;
import io.wochat.app.db.WCSharedPreferences;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.db.entity.Message;
import io.wochat.app.utils.DateFormatter;
import io.wochat.app.utils.SpeechUtils;
import io.wochat.app.utils.TextToSpeechUtil;
import io.wochat.app.utils.Utils;


public class CustomOutcomingSpeechableMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message>
		implements View.OnClickListener, TextToSpeechUtil.TextToSpeechPlayingListener{

	private static final String TAG = "OutSpeechMsgViewHldr" ;
	private final Contact mSelfContact;
	private final CircleImageView mMagicIndicator;
	private String mPictureUrl;
	private CircleFlagImageView mAvatarCIV;
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
	//private SpeechUtils mSpeechUtils;
	private String mMessageText;
	private int mMessageDuration;
	private SeekBarTimer mSeekBarTimer;
	private String mDisplayedLang;

	public CustomOutcomingSpeechableMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);

        mCocheIV = (ImageView) itemView.findViewById(R.id.coche_iv);
        mDurationTV = (TextView) itemView.findViewById(R.id.duration);
        mTimeTV = (TextView) itemView.findViewById(R.id.time);
        mPlayPauseIV = (ImageView) itemView.findViewById(R.id.play_pause_iv);
		mPlayPauseIV.setOnClickListener(this);
        mSeekBar = (SeekBar) itemView.findViewById(R.id.seekbar);
		mSeekBar.setOnTouchListener((v, event) -> {
			return true;
		});

		mAvatarCIV = (CircleFlagImageView)itemView.findViewById(R.id.messageUserAvatar);

		mMagicIndicator = (CircleImageView) itemView.findViewById(R.id.magicIndicatorCIV);

		mSelfContact = (Contact)payload;
		mAvatarCIV.setContact(mSelfContact);


//		mPictureUrl = (String)payload;
//
//		if (mPictureUrl == null)
//			Picasso.get().load(R.drawable.ic_action_empty_contact).placeholder(R.drawable.ic_action_empty_contact).into(mAvatarCIV);
//		else
//			Picasso.get().load(mPictureUrl).error(R.drawable.ic_action_empty_contact).placeholder(R.drawable.ic_action_empty_contact).into(mAvatarCIV);



    }




	@Override
    public void onBind(Message message) {
        super.onBind(message);





        mMessageText = message.getText();
		mDisplayedLang = message.getDisplayedLang();
		mMessageDuration = message.getDuration();

		if (mSeekBarTimer != null)
			mSeekBarTimer.cancel();

		mSeekBarTimer = new SeekBarTimer(mMessageDuration);




		//mSpeechUtils.setSpeechUtilsTTSListener(this);
		String selfLang = WCSharedPreferences.getInstance(this.itemView.getContext()).getUserLang();
		//mSpeechUtils.initSpeech(this.itemView.getContext(), this.itemView.getContext().getPackageName(), selfLang);


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

		if (message.isMagic()){
			mMagicIndicator.setVisibility(View.VISIBLE);
			int country = Utils.getCountryFlagDrawableFromLang(message.getDisplayedLang());
			mMagicIndicator.setImageDrawable(itemView.getResources().getDrawable(country));
		}
		else {
			mMagicIndicator.setVisibility(View.GONE);
		}


    }



	private void setSeekBarProgress(int progress){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			mSeekBar.setProgress(progress, true);
		else
			mSeekBar.setProgress(progress);
	}



    private void play(){
		TextToSpeechUtil.getInstance().setLanguage(mDisplayedLang);
		TextToSpeechUtil.getInstance().startTextToSpeech(mMessageText, this);
		//mSpeechUtils.startTextToSpeech(mMessageText, mDisplayedLang);
		mIsPlaying = true;
		mPlayPauseIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.msg_audio_pause_orange));
	}

	private void pause(){
		if (TextToSpeechUtil.getInstance().isPlaying()){
			TextToSpeechUtil.getInstance().pauseTextToSpeech();
			//mCurrentPosition = mMediaPlayer.getCurrentPosition();
			mPlayPauseIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.msg_audio_play_orange));
		}
	}


	@Override
	public void onClick(View v) {
		try {
			if (TextToSpeechUtil.getInstance().isPlaying()){
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
	public void onBeginPlaying() {
		mSeekBarTimer.start();
	}

	@Override
	public void onFinishedPlaying() {
		mSeekBarTimer.cancel();
		setSeekBarProgress(mSeekBar.getMax());
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				setSeekBarProgress(0);
				mPlayPauseIV.setImageDrawable(itemView.getResources().getDrawable(R.drawable.msg_audio_play_orange));
			}
		}, 200);
	}


	private class SeekBarTimer extends CountDownTimer{

		private final long mMillisInFuture;

		public SeekBarTimer(long messageDuration) {
			super(messageDuration, messageDuration/20);
			mMillisInFuture = messageDuration;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			int progress = (int)(mMillisInFuture - millisUntilFinished);
			setSeekBarProgress(progress);
		}

		@Override
		public void onFinish() {
			setSeekBarProgress(0);
		}
	}

}
