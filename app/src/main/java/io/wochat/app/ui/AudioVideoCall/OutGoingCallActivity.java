package io.wochat.app.ui.AudioVideoCall;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.util.Locale;
import io.wochat.app.R;
import io.wochat.app.WCRepository;
import io.wochat.app.WCService;
import io.wochat.app.components.CircleImageView;
import io.wochat.app.db.entity.Message;
import io.wochat.app.model.StateData;
import io.wochat.app.model.VideoAudioCall;
import io.wochat.app.ui.Consts;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.VideoAudioCallViewModel;

public class OutGoingCallActivity extends AppCompatActivity implements View.OnClickListener, WCRepository.OnSessionResultListener {

    private static final String TAG = "OutGoingCallActivity";
    private CircleImageView mMicFlagCIV, mParticipantPicAudioCIV, mParticipantPicAudioFlagCIV,
            mParticipantPicVideoCIV, mParticipantPicVideoFlagCIV, mHangUpCIV;
    private TextView mTitleTV, mParticipantNameAudioTV, mParticipantLangAudioTV,
            mParticipantNameVideoTV, mParticipantLangVideoTV , mParticipantNumberTV,mStatusTV;
    private ImageView mCameraSwitchIV;
    private FrameLayout mBackNavigationFL;
    private RelativeLayout mMainAudioRL, mMainVideoRL;
    private String mFixedParticipantId;
    private Locale loc;
    private int mFlagDrawable;
    private String mFullLangName;
    private boolean mIsVideoCall;
    private String mParticipantId, mParticipantName, mParticipantLang, mParticipantPic, mConversationId;
    private String mSelfId;
    private VideoAudioCallViewModel videoAudioCallViewModel;
    private VideoAudioCall mVideoAudioCall;
    private String errorMsg;
    private boolean mVideoFlag;
    private WCService mService;
    private MediaPlayer mCallingSound, mDeclineSound;
    private AlphaAnimation mCallTXTanimation;
    private Message message;
    private RTCcodeBR mRTCcodeBR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

            initViews();
            createSessionAndToken();
    }

    private void initViews() {

        mMicFlagCIV = (CircleImageView) findViewById(R.id.mic_flag_civ);
        mTitleTV = (TextView) findViewById(R.id.title_tv);
        mCameraSwitchIV = (ImageView) findViewById(R.id.camera_switch_iv);
        mBackNavigationFL = (FrameLayout) findViewById(R.id.back_navigation_fl);
        mParticipantNameAudioTV = (TextView) findViewById(R.id.participant_name_audio_tv);
        mParticipantLangAudioTV = (TextView) findViewById(R.id.participant_lang_audio_tv);
        mParticipantNumberTV = (TextView) findViewById(R.id.participant_number_audio_tv);
        mParticipantNameVideoTV = (TextView) findViewById(R.id.participant_name_video_tv);
        mParticipantLangVideoTV = (TextView) findViewById(R.id.participant_lang_video_tv);
        mStatusTV = (TextView) findViewById(R.id.status_tv);
        mParticipantPicAudioCIV = (CircleImageView) findViewById(R.id.participant_pic_audio_civ);
        mParticipantPicAudioFlagCIV = (CircleImageView) findViewById(R.id.participant_pic_flag_audio_civ);
        mParticipantPicVideoCIV = (CircleImageView) findViewById(R.id.participant_pic_video_civ);
        mParticipantPicVideoFlagCIV = (CircleImageView) findViewById(R.id.participant_pic_flag_video_civ);
        mHangUpCIV = (CircleImageView) findViewById(R.id.hang_up_civ);
        mMainAudioRL = (RelativeLayout) findViewById(R.id.main_audio_rl);
        mMainVideoRL = (RelativeLayout) findViewById(R.id.main_video_rl);

        mIsVideoCall = getIntent().getBooleanExtra(Consts.INTENT_IS_VIDEO_CALL, false);
        mParticipantId = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_ID);
        mParticipantName = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
        mParticipantLang = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
        mParticipantPic = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
        mConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);

        mSelfId = getIntent().getStringExtra(Consts.INTENT_SELF_ID);
//      mSelfLang = getIntent().getStringExtra(Consts.INTENT_SELF_LANG);
//      mSelfName = getIntent().getStringExtra(Consts.INTENT_SELF_NAME);
//      mSelfPicUrl = getIntent().getStringExtra(Consts.INTENT_SELF_PIC_URL);

        //Set lang flag , language display name and pic
        mFlagDrawable = Utils.getCountryFlagDrawableFromLang(mParticipantLang);
        try {
            loc = new Locale(mParticipantLang);
            mFullLangName = loc.getDisplayLanguage();
        } catch (Exception e) {
            Log.d(TAG,"OutGoingCallActivity - " + e.getMessage());
                e.printStackTrace();
        }

        //Sounds Init
        mDeclineSound = MediaPlayer.create(this, R.raw.declined_call);
        mCallingSound = MediaPlayer.create(this, R.raw.phone_calling_tone);
        mCallingSound.setLooping(true);
        mCallingSound.start();

        //Init calling animation
        mCallTXTanimation = new AlphaAnimation(0.0f, 1.0f);
        mCallTXTanimation.setDuration(1000);
        mCallTXTanimation.setRepeatCount(Animation.INFINITE);
        mCallTXTanimation.setRepeatMode(Animation.REVERSE);
        mStatusTV.startAnimation(mCallTXTanimation);

        mRTCcodeBR = new RTCcodeBR();

        mBackNavigationFL.setOnClickListener(this);
        mHangUpCIV.setOnClickListener(this);

        if (mIsVideoCall)
            videoCall();
        else
            audioCall();
    }

    private void videoCall() {
        mVideoFlag = true;
        mMainVideoRL.setVisibility(View.VISIBLE);

        mParticipantNameVideoTV.setText(mParticipantName);
        mParticipantLangVideoTV.setText(mFullLangName);

        //Set Participant Flags
        mParticipantPicVideoFlagCIV.setImageResource(mFlagDrawable);
        mMicFlagCIV.setImageResource(mFlagDrawable);

        //Set Participant Pic
        setPhotoByUrl(true);

        mTitleTV.setText(R.string.out_video_call);
    }

    private void audioCall() {
        mVideoFlag = false;
        mMainAudioRL.setVisibility(View.VISIBLE);

        mCameraSwitchIV.setVisibility(View.GONE); // No need camera switch button in audio call
        mParticipantNameAudioTV.setText(mParticipantName);
        mParticipantLangAudioTV.setText(mFullLangName);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mFixedParticipantId = PhoneNumberUtils.formatNumber(mParticipantId);
        else
            mFixedParticipantId = PhoneNumberUtils.formatNumber("+" + mParticipantId, mParticipantLangAudioTV.toString());

        //Set Participant Flags
        mParticipantPicAudioFlagCIV.setImageResource(mFlagDrawable);
        mMicFlagCIV.setImageResource(mFlagDrawable);

        //Set Participant Pic
        setPhotoByUrl(false);

        mParticipantNumberTV.setText(mFixedParticipantId);
        mTitleTV.setText(R.string.out_audio_call);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_navigation_fl:
                rejectCall();
                break;

            case R.id.hang_up_civ:
                   rejectCall();
                break;
        }
    }

    public void rejectCall(){
        //Send Massage to the receiver - let the receiver know that video/audio call is coming
        message = new Message(mParticipantId, mSelfId, mConversationId,  mVideoAudioCall.getSessionID(), "",
                "", Message.RTC_CODE_REJECTED, mVideoFlag, false);

        if ((mService != null) && (mService.isXmppConnected()))
            mService.sendMessage(message);
        Log.d(TAG, "ServiceConnection: call was rejected");
        finish();
    }


    public void setPhotoByUrl(boolean videoCallFlag){
        if ((mParticipantPic != null) && (!mParticipantPic.trim().equals(""))) {
                if(videoCallFlag)
                       Picasso.get().load(mParticipantPic).into(mParticipantPicVideoCIV);
                else
                       Picasso.get().load(mParticipantPic).into(mParticipantPicAudioCIV);
        }else{
                if(videoCallFlag)
                       Picasso.get().load(R.drawable.ic_empty_contact).into(mParticipantPicVideoCIV);
                else
                       Picasso.get().load(R.drawable.ic_empty_contact).into(mParticipantPicAudioCIV);
        }
    }

    public void createSessionAndToken(){
         videoAudioCallViewModel = ViewModelProviders.of(this).get(VideoAudioCallViewModel.class);
         videoAudioCallViewModel.createSessionsAndToken(this,"RELAYED");
    }

    @Override
    public void onSucceedCreateSession(StateData<String> success){
        mVideoAudioCall = videoAudioCallViewModel.getSessionAndToken().getValue();
        Log.d(TAG, "Session and token received, session is: " + mVideoAudioCall.getSessionID()
                                                                    + " , token is: " + mVideoAudioCall.getToken() );

        //Send Massage to the receiver - let the receiver know that video/audio call is coming
         message = new Message(mParticipantId, mSelfId, mConversationId, mVideoAudioCall.getSessionID(), "",
                "", Message.RTC_CODE_OFFER, mVideoFlag, false);
        if ((mService != null) && (mService.isXmppConnected())) {
            mService.sendMessage(message);
            Log.d(TAG, "ServiceConnection: massage sent ");
        }
    }

    @Override
    public void onFailedCreateSession(StateData<String> errorMsg) {
        if(errorMsg.getErrorLogic() != null)
              this.errorMsg = errorMsg.getErrorLogic();
        else if(errorMsg.getErrorCom() != null)
              this.errorMsg = errorMsg.getErrorCom().toString();

        Toast.makeText(this, "Failed to make a call: " + this.errorMsg, Toast.LENGTH_LONG).show();
         this.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart, call bindService WCService");
        Intent intent = new Intent(this, WCService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Message.RTC_CODE_REJECTED);
                registerReceiver(mRTCcodeBR,filter);
        } catch (Exception e) {}
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
        unregisterReceiver(mRTCcodeBR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCallingSound.stop();
        mDeclineSound.stop();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "ServiceConnection: onServiceConnected");
            WCService.WCBinder binder = (WCService.WCBinder) service;
            mService = binder.getService();
            mService.setCurrentConversationId(null);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "ServiceConnection: onServiceDisconnected");
            mService = null;
        }
    };

    private void callRejected(){
        mCallTXTanimation.cancel();
        mCallingSound.stop();
        mDeclineSound.setLooping(true);
        mDeclineSound.start();
        mStatusTV.setText(getResources().getString(R.string.rejected));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mCallingSound.stop();
                finish();
            }
        }, 3000);
    }

    private class RTCcodeBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Message.RTC_CODE_REJECTED)) {
                callRejected();
            }
        }
    }
}
