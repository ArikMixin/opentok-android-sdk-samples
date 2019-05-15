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
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Chronometer;
import android.widget.FrameLayout;
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

public class IncomingCallActivity extends AppCompatActivity implements View.OnClickListener, WCRepository.OnSessionResultListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "IncomingCallActivity";
    private CircleImageView mParticipantPicAudioCIV, mParticipantPicAudioFlagCIV,
            mParticipantPicVideoCIV, mParticipantPicVideoFlagCIV, mDeclineCIV, mAcceptCIV;
    private TextView mTitleTV, mParticipantNameAudioTV, mParticipantLangAudioTV,
            mParticipantNameVideoTV, mParticipantLangVideoTV;
    private Chronometer mTimerChr;
    private FrameLayout mBackNavigationFL;
    private RelativeLayout mMainAudioRL, mMainVideoRL;
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
    private RelativeLayout mAcceptRL;
    private MediaPlayer mSoundsPlayer;
    private TranslateAnimation mAnimation;
    private String mSessionId;
    private Message message;
    private RTCcodeBR mRTCcodeBR;
    public static boolean activityActiveFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_incoming_call);

             initViews();
             requestPermissions();
    }

    private void initViews() {
        activityActiveFlag = true;

        mTitleTV = (TextView) findViewById(R.id.title_tv);
        mBackNavigationFL = (FrameLayout) findViewById(R.id.back_navigation_fl);
        mParticipantNameAudioTV = (TextView) findViewById(R.id.participant_name_audio_tv);
        mParticipantLangAudioTV = (TextView) findViewById(R.id.participant_lang_audio_tv);
        mParticipantNameVideoTV = (TextView) findViewById(R.id.participant_name_video_tv);
        mParticipantLangVideoTV = (TextView) findViewById(R.id.participant_lang_video_tv);
        mParticipantPicAudioCIV = (CircleImageView) findViewById(R.id.participant_pic_audio_civ);
        mParticipantPicAudioFlagCIV = (CircleImageView) findViewById(R.id.participant_pic_flag_audio_civ);
        mParticipantPicVideoCIV = (CircleImageView) findViewById(R.id.participant_pic_video_civ);
        mParticipantPicVideoFlagCIV = (CircleImageView) findViewById(R.id.participant_pic_flag_video_civ);
        mDeclineCIV = (CircleImageView) findViewById(R.id.decline_civ);
        mAcceptCIV = (CircleImageView) findViewById(R.id.accept_civ);
        mAcceptRL = (RelativeLayout) findViewById(R.id.accept_rl);
        mMainAudioRL = (RelativeLayout) findViewById(R.id.main_audio_rl);
        mMainVideoRL = (RelativeLayout) findViewById(R.id.main_video_rl);
        mTimerChr = (Chronometer) findViewById(R.id.timer_chr);

        mIsVideoCall = getIntent().getBooleanExtra(Consts.INTENT_IS_VIDEO_CALL, false);
        mParticipantId = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_ID);
        mParticipantName = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
        mParticipantLang = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
        mParticipantPic = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
        mConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
        mSessionId = getIntent().getStringExtra(Consts.INTENT_SESSION_ID);


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

        //Play calling sound in first
        mSoundsPlayer = MediaPlayer.create(this, R.raw.incoming_call);
        mSoundsPlayer.setLooping(true);
        mSoundsPlayer.start();

        //Answer the call animation
        mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.1f);
        mAnimation.setDuration(600);
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAcceptRL .setAnimation(mAnimation);

        mRTCcodeBR = new RTCcodeBR();

        mBackNavigationFL.setOnClickListener(this);
        mDeclineCIV.setOnClickListener(this);
        mAcceptCIV.setOnClickListener(this);

        if (mIsVideoCall)
            videoCall();
        else
            audioCall();
    }

    @AfterPermissionGranted(OutGoingCallActivity.RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        if (EasyPermissions.hasPermissions(this, OutGoingCallActivity.perms)) {
                 createTokenInExistingSession();
        } else {
                 EasyPermissions.requestPermissions(this, getString(R.string.permissions_expl_in), OutGoingCallActivity.RC_VIDEO_APP_PERM, OutGoingCallActivity.perms);

        }
    }

    private void videoCall() {
        mVideoFlag = true;
        mMainVideoRL.setVisibility(View.VISIBLE);

        mParticipantNameVideoTV.setText(mParticipantName);
        mParticipantLangVideoTV.setText(mFullLangName);

        //Set Participant Flags
        mParticipantPicVideoFlagCIV.setImageResource(mFlagDrawable);

        //Set Participant Pic
        setPhotoByUrl(true);

        mTitleTV.setText(R.string.in_video_call);
    }

    private void audioCall() {
        mVideoFlag = false;
        mMainAudioRL.setVisibility(View.VISIBLE);

        mParticipantNameAudioTV.setText(mParticipantName);
        mParticipantLangAudioTV.setText(mFullLangName);

        //Set Participant Flags
        mParticipantPicAudioFlagCIV.setImageResource(mFlagDrawable);

        //Set Participant Pic
        setPhotoByUrl(false);
//
//        mParticipantNumberTV.setText(mFixedParticipantId);
        mTitleTV.setText(R.string.in_audio_call);
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

            case R.id.accept_civ:
                    sendXMPPmsg(Message.RTC_CODE_ANSWER); // Let the caller know that the receiver accept the call
                    callStarted();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendXMPPmsg(Message.RTC_CODE_REJECTED);
    }

    //Send Massage to the caller
    public void sendXMPPmsg(String rtcCode){
        message = new Message(mParticipantId, mSelfId, mConversationId, mSessionId, "",
                                    "", rtcCode, mVideoFlag, false);

        if ((mService != null) && (mService.isXmppConnected()))
                mService.sendMessage(message);

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

    public void createTokenInExistingSession(){
          videoAudioCallViewModel = ViewModelProviders.of(this).get(VideoAudioCallViewModel.class);
          videoAudioCallViewModel.createTokenInExistingSession(this, mSessionId, "" + WCRepository.TokenRoleType.PUBLISHER);
    }

    //*** The caller already creates the session
    @Override
    public void onSucceedCreateSession(StateData<String> success){
        mVideoAudioCall = videoAudioCallViewModel.getSessionAndToken().getValue();
        Log.d("testttt", "Session and token received, session is: " + mSessionId
                                           + " , token is: " + mVideoAudioCall.getToken() );

        // TODO: 5/14/2019 start a video/audioCall via TokBox
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
        activityActiveFlag = false;
        mSoundsPlayer.stop();
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
        if(perms.size() == 3) // Number of perms
            createTokenInExistingSession();
        else
            sendXMPPmsg(Message.RTC_CODE_REJECTED);
    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
            sendXMPPmsg(Message.RTC_CODE_REJECTED);
    }

    private class RTCcodeBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Message.RTC_CODE_REJECTED)) {
                finish();
            }
        }
    }

    private void callStarted() {
        mAnimation.cancel();
        mAcceptCIV.setEnabled(false);

        mTimerChr.setVisibility(View.VISIBLE);
        mTimerChr.setBase(SystemClock.elapsedRealtime());
        mTimerChr.start();
    }
}
