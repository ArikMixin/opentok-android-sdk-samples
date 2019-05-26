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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
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

import com.opentok.android.Session;

public class OutGoingCallActivity extends AppCompatActivity
        implements View.OnClickListener,
        WCRepository.OnSessionResultListener,
        EasyPermissions.PermissionCallbacks,
        Session.SessionListener,
        PublisherKit.PublisherListener,
        View.OnTouchListener {

    private static final String TAG = "OutGoingCallActivity";
    private static final String TOKBOX = "TokBox";

    private CircleImageView mMicFlagCIV, mParticipantPicAudioCIV, mParticipantPicAudioFlagCIV,
            mParticipantPicVideoCIV, mParticipantPicVideoFlagCIV, mDeclineCIV;
    private TextView mTitleTV, mParticipantNameAudioTV, mParticipantLangAudioTV,
            mParticipantNameVideoTV, mParticipantLangVideoTV , mParticipantNumberTV, mStatusTV;
    private Chronometer mTimerChr;
    private ImageView mCameraSwitchIV;
    private FrameLayout mBackNavigationFL;
    private RelativeLayout mMainAudioRL, mMainVideoRL, mStatusRL, mUserPicAudioRL;
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
    private ToggleButton mCameraBtnVideo, mCameraBtnAudio;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private FrameLayout mPublisherFL;
    private FrameLayout mSubscriberFL;
    private float dX, dY, mCornerX, mCornerY ;
    private int screenHeight, screenWidth;
    private boolean callStartedFlag;
    private boolean callEndedFlag;

    public static final String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA,
                                                                     Manifest.permission.RECORD_AUDIO };

    public static final int CLOSING_TIME = 3000;
    public static final int SCREEN_MINIMUM_VER = 25;
    public static final String TOK_BOX_APIKEY = "46296242";
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
        mDeclineCIV = (CircleImageView) findViewById(R.id.decline_civ);
        mMainAudioRL = (RelativeLayout) findViewById(R.id.main_audio_rl);
        mMainVideoRL = (RelativeLayout) findViewById(R.id.main_video_rl);
        mStatusRL = (RelativeLayout) findViewById(R.id.status_rl);
        mUserPicAudioRL = (RelativeLayout) findViewById(R.id.user_pic_audio_rl);
        mPublisherFL = (FrameLayout) findViewById(R.id.publisher_fl);
        mSubscriberFL = (FrameLayout) findViewById(R.id.subscriber_fl);
        mCameraBtnVideo = (ToggleButton) findViewById(R.id.camera_btn_video_tb);
        mCameraBtnAudio = (ToggleButton) findViewById(R.id.camera_btn_audio_tb);

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
            mFixedParticipantId = PhoneNumberUtils.formatNumber("+" + mParticipantId,
                                                                mParticipantLangAudioTV.toString());
        mParticipantNumberTV.setText(mFixedParticipantId);

        //Get The Screen Sizes
         screenHeight = getResources().getDisplayMetrics().heightPixels;
         screenWidth = getResources().getDisplayMetrics().widthPixels;

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
        mDeclineCIV.setOnClickListener(this);
        mPublisherFL.setOnTouchListener(this);
        mCameraBtnVideo.setOnClickListener(this);
        mCameraBtnAudio.setOnClickListener(this);

        if (mIsVideoCall)
            videoCall();
        else
            audioCall();
    }

    /**
     * (1) Request permissions if the user has not yet approved them -
     * (2) If there are permissions already, ask for a session and token and connect to the session
     */
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        if (EasyPermissions.hasPermissions(this, perms)) {

              //Show self camera Preview at first
              startCameraPreview();

              createSessionAndToken();
        } else {
              EasyPermissions.requestPermissions(this, getString(R.string.permissions_expl_out),
                                                                              RC_VIDEO_APP_PERM, perms);
        }
    }

    private void startCameraPreview() {
        mPublisher = new Publisher.Builder(this)
                .videoTrack(mIsVideoCall)
                .build();
        mPublisher.setPublisherListener(this);

        if(mIsVideoCall) { //Only if it is video call - show preview
                mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                mPublisher.startPreview();
                mSubscriberFL.addView(mPublisher.getView());
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
                        if(callStartedFlag)
                            sendXMPPmsg(Message.RTC_CODE_CLOSE);
                        else
                            sendXMPPmsg(Message.RTC_CODE_REJECTED);
            break;

            case R.id.decline_civ:
                        if(callStartedFlag)
                            sendXMPPmsg(Message.RTC_CODE_CLOSE);
                        else
                            sendXMPPmsg(Message.RTC_CODE_REJECTED);
            break;

            case R.id.camera_btn_video_tb:
                cameraBtnVideo();
            break;

            case R.id.camera_btn_audio_tb:
                cameraBtnAudio();
            break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(callStartedFlag)
            sendXMPPmsg(Message.RTC_CODE_CLOSE);
        else
            sendXMPPmsg(Message.RTC_CODE_REJECTED);
    }

    private void cameraBtnAudio() {
    }

    private void cameraBtnVideo() {
        //close publisher video
        if(mIsVideoCall) {
            mIsVideoCall = false;
            mPublisher.setPublishVideo(false);
            mPublisherFL.setVisibility(View.GONE);
            mPublisherFL.removeAllViews();
        }else{        //open publisher video
            mIsVideoCall = true;
            mPublisher.setPublishVideo(true);
            mPublisherFL.setVisibility(View.VISIBLE);
            mPublisherFL.addView(mPublisher.getView());
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {

            //Hold the view
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                view.animate()
                        .x(event.getRawX() + dX)
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();
                break;

            //Release the view
            case MotionEvent.ACTION_UP:

                if(event.getRawX() < screenWidth / 2)
                    mCornerX = 25;
                else
                    mCornerX = screenWidth - view.getWidth() -25; //  - 450

                if(event.getRawY() < screenHeight / 2)
                    mCornerY = 25;
                else
                    mCornerY = screenHeight - view.getHeight() -125; // 900

                view.animate()
                        .x(mCornerX)
                        .y(mCornerY)
                        .setDuration(400)
                        .start();
                break;
            default:
                return false;
        }
        return true;
    }

    //Send Massage to the receiver
    public void sendXMPPmsg(String rtcCode){
        message = new Message(mParticipantId, mSelfId, mConversationId,  mSessionID, "","",
                                                                            rtcCode, mVideoFlag, false);

        if ((mService != null) && (mService.isXmppConnected())){
            mService.sendMessage(message);
        }

        if(rtcCode.equals(Message.RTC_CODE_REJECTED) || rtcCode.equals(Message.RTC_CODE_CLOSE))
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
        connectToSession(mVideoAudioCall.getSessionID(), mVideoAudioCall.getToken());
    }

    public void connectToSession(String sessionID, String tokenID){
        mSession = new Session.Builder(this, TOK_BOX_APIKEY, sessionID).build();
        mSession.setSessionListener(this);
        mSession.connect(tokenID);
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
            filter.addAction(Message.RTC_CODE_CLOSE);
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

        if(mSession != null) {
                 mSession.disconnect();
                    if(mSubscriber != null){
                            mSession.unsubscribe(mSubscriber);
                            mSubscriber.destroy();
                    }
                    if(mPublisher != null){
                            mSession.unpublish(mPublisher);
                            mPublisher.destroy();
            }
        }
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
            if(!callEndedFlag && intent.getAction().equals(Message.RTC_CODE_REJECTED) ||
                    intent.getAction().equals(Message.RTC_CODE_BUSY) ||
                    intent.getAction().equals(Message.RTC_CODE_CLOSE))
                             callEnded(intent.getAction());
            else if(intent.getAction().equals(Message.RTC_CODE_ANSWER))
                callStarted();
            }
    }

    private void callEnded(String rtcCode){
        mCallTXTanimation.cancel();
        mCallingSound.stop();

        if(rtcCode.equals(Message.RTC_CODE_REJECTED)) {
            mStatusTV.setText(getResources().getString(R.string.rejected));
            mDeclineSound.setLooping(true);
            mDeclineSound.start();
        }else if(rtcCode.equals(Message.RTC_CODE_BUSY)) {
            mStatusTV.setText(getResources().getString(R.string.busy));
            mBusySound.setLooping(true);
            mBusySound.start();
        }else if(rtcCode.equals(Message.RTC_CODE_CLOSE)){
            mTimerChr.stop();
            mTitleTV.setText(getResources().getString(R.string.call_ended));
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mCallingSound.stop();
                finish();
            }
        }, CLOSING_TIME);
    }

    private void callStarted() {
        callStartedFlag = true;

        //Stop sounds
        mCallingSound.stop();

        mCallTXTanimation.cancel();
        mStatusRL.setVisibility(View.GONE);

        //User pic animation (When the status not sowed anymore)
        mTimerChr.setVisibility(View.VISIBLE);
        mTimerChr.setBase(SystemClock.elapsedRealtime());
        mTimerChr.start();
    }

    //TokBox
    @Override
    public void onConnected(Session session) {
        Log.i(TOKBOX, "Session Connected");
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(TOKBOX, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TOKBOX, "Stream Received");

        //*** Show the receiver video (Small Windows) Only for video calls
        if (mIsVideoCall && Build.VERSION.SDK_INT < SCREEN_MINIMUM_VER){
                mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                ((ViewGroup)mPublisher.getView().getParent()).removeView(mPublisher.getView());
                mPublisherFL.addView(mPublisher.getView());
                mPublisherFL.setVisibility(View.VISIBLE);
        }

        if (mSubscriber == null) {
                        mSubscriber = new Subscriber.Builder(OutGoingCallActivity.this, stream).build();
                        mSession.subscribe(mSubscriber);

                       //Show the caller video (full screen) Only for video calls
                        if (mIsVideoCall) {
                                mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                                mSubscriberFL.addView(mSubscriber.getView());
                        }

                           //*** Show the receiver video (Small Windows) Only for video calls
                           if(mIsVideoCall && Build.VERSION.SDK_INT >= SCREEN_MINIMUM_VER) {
                                   mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                                   ((ViewGroup) mPublisher.getView().getParent()).removeView(mPublisher.getView());
                                   mPublisherFL.addView(mPublisher.getView());
                                   mPublisherFL.setVisibility(View.VISIBLE);
                           }
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TOKBOX, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberFL.removeAllViews();
        }

        if(!callEndedFlag)
               callEnded(Message.RTC_CODE_CLOSE);
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(TOKBOX, "Session error: " + opentokError.getMessage());
    }

    // PublisherListener methods
    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(TOKBOX, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(TOKBOX, "Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(TOKBOX, "Publisher error: " + opentokError.getMessage());
    }

}
