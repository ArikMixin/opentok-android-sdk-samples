package io.wochat.app.ui.AudioVideoCall;

import android.Manifest;
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
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import java.util.List;
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
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class OutGoingCallActivity extends AppCompatActivity implements View.OnClickListener, WCRepository.OnSessionResultListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "OutGoingCallActivity";
    private CircleImageView mMicFlagCIV, mParticipantPicAudioCIV, mParticipantPicAudioFlagCIV,
            mParticipantPicVideoCIV, mParticipantPicVideoFlagCIV, mHangUpCIV;
    private TextView mTitleTV, mParticipantNameAudioTV, mParticipantLangAudioTV,
            mParticipantNameVideoTV, mParticipantLangVideoTV , mParticipantNumberTV, mStatusTV;
    private Chronometer mTimerChr;
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
    private MediaPlayer mCallingSound, mDeclineSound, mBusySound ;
    private AlphaAnimation mCallTXTanimation;
    private Message message;
    private RTCcodeBR mRTCcodeBR;
    private String mSessionID = "";
    public static final String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
    public static final int TOK_BOX_APIKEY = 46296242;
    public static final int RC_SETTINGS_SCREEN_PERM = 123;
    public static final int RC_VIDEO_APP_PERM = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

            initViews();
            requestPermissions();
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
        mTimerChr = (Chronometer) findViewById(R.id.timer_chr);

        mParticipantPicAudioCIV = (CircleImageView) findViewById(R.id.participant_pic_audio_civ);
        mParticipantPicAudioFlagCIV = (CircleImageView) findViewById(R.id.participant_pic_flag_audio_civ);
        mParticipantPicVideoCIV = (CircleImageView) findViewById(R.id.participant_pic_video_civ);
        mParticipantPicVideoFlagCIV = (CircleImageView) findViewById(R.id.participant_pic_flag_video_civ);
        mHangUpCIV = (CircleImageView) findViewById(R.id.decline_civ);
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

        //Phone number
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mFixedParticipantId = PhoneNumberUtils.formatNumber(mParticipantId);
        else
            mFixedParticipantId = PhoneNumberUtils.formatNumber("+" + mParticipantId, mParticipantLangAudioTV.toString());
        mParticipantNumberTV.setText(mFixedParticipantId);

        //Sounds Init
        mDeclineSound = MediaPlayer.create(this, R.raw.declined_call);
        mCallingSound = MediaPlayer.create(this, R.raw.phone_calling_tone);
        mBusySound = MediaPlayer.create(this, R.raw.phone_busy_signal);
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

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        if (EasyPermissions.hasPermissions(this, perms)) {
                createSessionAndToken();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_expl_out), RC_VIDEO_APP_PERM, perms);
        }
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

        //Set Participant Flags
        mParticipantPicAudioFlagCIV.setImageResource(mFlagDrawable);
        mMicFlagCIV.setImageResource(mFlagDrawable);

        //Set Participant Pic
        setPhotoByUrl(false);


        mTitleTV.setText(R.string.out_audio_call);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_navigation_fl:
                    sendXMPPmsg(Message.RTC_CODE_REJECTED);
                break;

            case R.id.decline_civ:
                   sendXMPPmsg(Message.RTC_CODE_REJECTED);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendXMPPmsg(Message.RTC_CODE_REJECTED);
    }
    //Send Massage to the receiver
    public void sendXMPPmsg(String rtcCode){
        message = new Message(mParticipantId, mSelfId, mConversationId,  mSessionID, "","",
                                                                            rtcCode, mVideoFlag, false);

        if ((mService != null) && (mService.isXmppConnected())){
            mService.sendMessage(message);
        }

        if(rtcCode.equals(Message.RTC_CODE_REJECTED))
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
        mSessionID = mVideoAudioCall.getSessionID();
        //Send Massage to the receiver (With the sessionID) - let the receiver know that video/audio call is coming
        sendXMPPmsg(Message.RTC_CODE_OFFER);
        //Create session connection via TokBox
        // TODO: 5/15/2019 //MAKE THE SESSION CONNECTION
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
            filter.addAction(Message.RTC_CODE_BUSY);
            filter.addAction(Message.RTC_CODE_ANSWER);
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
        mBusySound.stop();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    /**
     * continue the process only if client accept all the permissions - if not - finish this activity
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(perms.size() != 3)  //if the client not accept all 3 permissions reject the call (close the activity)
            sendXMPPmsg(Message.RTC_CODE_REJECTED);
    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
            sendXMPPmsg(Message.RTC_CODE_REJECTED);
    }

    private class RTCcodeBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Message.RTC_CODE_REJECTED))
                finishCall(true);
            else if(intent.getAction().equals(Message.RTC_CODE_BUSY))
                finishCall(false);
            else if(intent.getAction().equals(Message.RTC_CODE_ANSWER))
                callStarted();
            }
    }

    private void finishCall(boolean rejectedFlag){
        mCallTXTanimation.cancel();
        mCallingSound.stop();

        if(rejectedFlag) {
            mStatusTV.setText(getResources().getString(R.string.rejected));
            mDeclineSound.setLooping(true);
            mDeclineSound.start();
        }else {
            mStatusTV.setText(getResources().getString(R.string.busy));
            mBusySound.setLooping(true);
            mBusySound.start();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mCallingSound.stop();
                finish();
            }
        }, 4000);
    }

    private void callStarted() {

        mCallTXTanimation.cancel();
        mStatusTV.setVisibility(View.INVISIBLE);

        mTimerChr.setVisibility(View.VISIBLE);
        mTimerChr.setBase(SystemClock.elapsedRealtime());
        mTimerChr.start();
    }


}
