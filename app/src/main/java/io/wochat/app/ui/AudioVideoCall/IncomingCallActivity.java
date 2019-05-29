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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
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
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
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

public class IncomingCallActivity extends AppCompatActivity implements
        View.OnClickListener,
        WCRepository.OnSessionResultListener,
        EasyPermissions.PermissionCallbacks,
        Session.SessionListener,
        View.OnTouchListener,
        PublisherKit.PublisherListener, SubscriberKit.VideoListener {

    private static final String TAG = "IncomingCallActivity";
    private static final String TOKBOX = "TokBox";

    private CircleImageView mParticipantPicAudioCIV, mParticipantPicAudioFlagCIV,
            mParticipantPicVideoCIV, mParticipantPicVideoFlagCIV, mDeclineCIV, mAcceptCIV;
    private TextView mTitleTV, mParticipantNameAudioTV, mParticipantLangAudioTV,
            mParticipantNameVideoTV, mParticipantLangVideoTV, mConnectingTV;
    private Chronometer mTimerChr;
    private FrameLayout mBackNavigationFL, mCameraPauseFullFL;
    private RelativeLayout mMainAudioRL, mMainVideoRL, mConnectingRL, mIncomingCallBtnsRl;
    private ConstraintLayout mInsideCallBtnsCL;
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
    private RelativeLayout mAcceptRL , mDeclineInsideRL;
    private MediaPlayer mSoundsPlayer;
    private TranslateAnimation mAnimation;
    private String mSessionId;
    private Message message;
    private RTCcodeBR mRTCcodeBR;
    private ImageView mCameraSwitchIV;
    private ToggleButton mCameraBtnVideo, mCameraBtnAudio;


    public static boolean activityActiveFlag;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private Stream mStream;
    private FrameLayout mPublisherFL;
    private FrameLayout mSubscriberFL;
    private float dX, dY, mCornerX, mCornerY ;
    private int screenHeight, screenWidth;
    private AlphaAnimation mCallTXTanimation;
    private boolean mCallStartedFlag;
    private boolean mCallEndedFlag;
    private boolean mCallerCamOpen = true;
    private boolean mSelfCamOpen = true;
    volatile boolean sessitonRecivedFlag;


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
        mPublisherFL = (FrameLayout)findViewById(R.id.publisher_fl);
        mSubscriberFL = (FrameLayout)findViewById(R.id.subscriber_fl);
        mConnectingRL = (RelativeLayout) findViewById(R.id.connecting_rl);
        mConnectingTV = (TextView) findViewById(R.id.connecting_tv);
        mIncomingCallBtnsRl = (RelativeLayout) findViewById(R.id.incoming_call_btns_rl);
        mDeclineInsideRL = (RelativeLayout) findViewById(R.id.decline_inside_rl);
        mInsideCallBtnsCL = (ConstraintLayout) findViewById(R.id.inside_call_btns_cl);
        mCameraSwitchIV = (ImageView) findViewById(R.id.camera_switch_iv);
        mCameraBtnVideo = (ToggleButton) findViewById(R.id.camera_btn_video_tb);
        mCameraBtnAudio = (ToggleButton) findViewById(R.id.camera_btn_audio_tb);
        mCameraPauseFullFL = (FrameLayout) findViewById(R.id.camera_pause_full_fl);

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

        //Get The Screen Sizes
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;

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

        //connection animation
        mCallTXTanimation = new AlphaAnimation(0.0f, 1.0f);
        mCallTXTanimation.setDuration(1000);
        mCallTXTanimation.setRepeatCount(Animation.INFINITE);
        mCallTXTanimation.setRepeatMode(Animation.REVERSE);
        mConnectingTV.startAnimation(mCallTXTanimation);

        mRTCcodeBR = new RTCcodeBR();

        mBackNavigationFL.setOnClickListener(this);
        mDeclineCIV.setOnClickListener(this);
        mDeclineInsideRL.setOnClickListener(this);
        mAcceptCIV.setOnClickListener(this);
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
    @AfterPermissionGranted(OutGoingCallActivity.RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        if (EasyPermissions.hasPermissions(this, OutGoingCallActivity.perms)) {
                    //Show self camera Preview at first (Full Screen)
                        startCameraPreview();
                        createTokenInExistingSession();
        } else {
                 EasyPermissions.requestPermissions(this, getString(R.string.permissions_expl_in),
                                                    OutGoingCallActivity.RC_VIDEO_APP_PERM, OutGoingCallActivity.perms);

        }
    }

    private void startCameraPreview() {
        mPublisher = new Publisher.Builder(this)
//                .videoTrack(mIsVideoCall)
                .build();
        mPublisher.setPublisherListener(this);

        if(mIsVideoCall) { //Only if it is video call - show preview
                mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                mPublisher.startPreview();
                mSubscriberFL.addView(mPublisher.getView());
        }else{
                mPublisher.setPublishVideo(false);
        }
    }

    private void videoCall() {
        mVideoFlag = true;
        mSelfCamOpen = true;

        if(!mCallerCamOpen)
          mCameraPauseFullFL.setVisibility(View.VISIBLE);
        else
          mCameraPauseFullFL.setVisibility(View.GONE);

        if(mMainAudioRL.getVisibility() == View.VISIBLE)
              mMainAudioRL.setVisibility(View.GONE);
        if(mCameraSwitchIV.getVisibility() != View.VISIBLE)
              mCameraSwitchIV.setVisibility(View.VISIBLE);
        if(mSubscriberFL.getVisibility() != View.VISIBLE)
            mSubscriberFL.setVisibility(View.VISIBLE);

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

//        if(mCameraPauseFullFL.getVisibility() == View.VISIBLE)
//              mCameraPauseFullFL.setVisibility(View.GONE);
        if(mMainVideoRL.getVisibility() == View.VISIBLE)
              mMainVideoRL.setVisibility(View.GONE);
        if(mCameraSwitchIV.getVisibility() == View.VISIBLE)
              mCameraSwitchIV.setVisibility(View.GONE);

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
                        if(mCallStartedFlag)
                            sendXMPPmsg(Message.RTC_CODE_CLOSE);
                        else
                            sendXMPPmsg(Message.RTC_CODE_REJECTED);
                break;

            case R.id.decline_civ:
                        if(mCallStartedFlag)
                            sendXMPPmsg(Message.RTC_CODE_CLOSE);
                        else
                            sendXMPPmsg(Message.RTC_CODE_REJECTED);
                break;

            case R.id.decline_inside_rl:
                        if(mCallStartedFlag)
                            sendXMPPmsg(Message.RTC_CODE_CLOSE);
                        else
                            sendXMPPmsg(Message.RTC_CODE_REJECTED);
                break;

            case R.id.accept_civ:
                        callStarted();
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
            if(mCallStartedFlag)
                sendXMPPmsg(Message.RTC_CODE_CLOSE);
            else
                sendXMPPmsg(Message.RTC_CODE_REJECTED);
    }

    private void cameraBtnAudio() {
            turnCallType(true);
            mCameraBtnVideo.setChecked(false);
            mSelfCamOpen = true;

            mSubscriberFL.setVisibility(View.VISIBLE);
            mPublisherFL.setVisibility(View.VISIBLE);

//            if(mCallerCamOpen)
//                mCameraPauseFullFL.setVisibility(View.VISIBLE);
//            else
//                mCameraPauseFullFL.setVisibility(View.GONE);

            if(mPublisher != null) { // When open video from audio call
                    mPublisher.setPublishVideo(true);
                    mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                    mPublisherFL.addView(mPublisher.getView());
            }

            if (mSubscriber != null && Build.VERSION.SDK_INT < OutGoingCallActivity.SCREEN_MINIMUM_VER) {
                        ((ViewGroup) mSubscriber.getView().getParent()).removeView(mSubscriber.getView());
                        mSubscriberFL.addView(mSubscriber.getView());
            }

    }

    private void cameraBtnVideo() {

        //close publisher (self)  video
        if(mSelfCamOpen) {
               mSelfCamOpen = false;

                        mPublisher.setPublishVideo(false);
                        mPublisherFL.removeView(mPublisher.getView());

                         if(!mCallerCamOpen)
                                  turnCallType(false);
        }else{  //open publisher video
             mSelfCamOpen = true;

                        mPublisher.setPublishVideo(true);
                        mPublisherFL.addView(mPublisher.getView());

                             if (Build.VERSION.SDK_INT < OutGoingCallActivity.SCREEN_MINIMUM_VER) {
                                   ((ViewGroup) mSubscriber.getView().getParent()).removeView(mSubscriber.getView());
                                   mSubscriberFL.addView(mSubscriber.getView());
                             }
       }
    }

    private void turnCallType(boolean video){

        //Open Audio Call
        if(video) {
            videoCall();
        }else{
            mCameraBtnAudio.setChecked(false);
            mCameraPauseFullFL.setVisibility(View.GONE);
            mSubscriberFL.setVisibility(View.GONE);
            mPublisherFL.setVisibility(View.GONE);

            audioCall();
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

    //Send Massage to the caller
    public void sendXMPPmsg(String rtcCode){
        message = new Message(mParticipantId, mSelfId, mConversationId, mSessionId, "",
                                    "", rtcCode, mVideoFlag, false);

        if ((mService != null) && (mService.isXmppConnected()))
                mService.sendMessage(message);

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

    public void createTokenInExistingSession(){
          videoAudioCallViewModel = ViewModelProviders.of(this).get(VideoAudioCallViewModel.class);
          videoAudioCallViewModel.createTokenInExistingSession(this, mSessionId, "" + WCRepository.TokenRoleType.PUBLISHER);
    }

    //*** The caller already creates the session
    @Override
    public void onSucceedCreateSession(StateData<String> success){
        mVideoAudioCall = videoAudioCallViewModel.getSessionAndToken().getValue();
        Log.d(TAG, "Session and token received, session is: " + mSessionId
                                           + " , token is: " + mVideoAudioCall.getToken() );
        //Create session connection via TokBox
        connectToSession(mSessionId, mVideoAudioCall.getToken());
    }

    public void connectToSession(String sessionID, String tokenID){
        mSession = new Session.Builder(this, OutGoingCallActivity.TOK_BOX_APIKEY, sessionID).build();
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
            filter.addAction(Message.RTC_CODE_CLOSE);
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
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        activityActiveFlag = false;
        mSoundsPlayer.stop();

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

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
    }

    @Override// caller
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
        mCallerCamOpen = false;

        if(!mSelfCamOpen) {
                turnCallType(false);
                   return;
        }
        mSubscriberFL.setVisibility(View.GONE); //Caller full screen video
        mCameraPauseFullFL.setVisibility(View.VISIBLE);
    }

    @Override// caller
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
        mCallerCamOpen = true;

        if(!mVideoFlag) {
            turnCallType(true);
             //return;
        }
        mSubscriberFL.setVisibility(View.VISIBLE); //Caller full screen video
        mCameraPauseFullFL.setVisibility(View.GONE);
    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {
    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {
    }

    private class RTCcodeBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Message.RTC_CODE_REJECTED)) { //Close immediately - User Ignore the call
                         finish();
            }else if(!mCallEndedFlag && intent.getAction().equals(Message.RTC_CODE_CLOSE)){
                callEnded();
            }
        }
    }

    private void callEnded(){
        mCallEndedFlag =  true;

        mTimerChr.stop();
        mTitleTV.setText(getResources().getString(R.string.call_ended));

        Handler handler = new Handler();
        handler.postDelayed(() -> finish(),
               OutGoingCallActivity.CLOSING_TIME);
    }

    private void callStarted() {
        mCallStartedFlag = true;

        //Hide incoming call btns and show inside call btns
        mIncomingCallBtnsRl.setVisibility(View.GONE);
        mSoundsPlayer.stop();
        mAnimation.cancel();
        mAcceptCIV.setEnabled(false);
        mConnectingRL.setVisibility(View.VISIBLE);

        Thread thread =  new Thread() {
                @Override
                public void run() {

                    while (!sessitonRecivedFlag);

                    runOnUiThread(() -> {

                        mSession.publish(mPublisher);

                        if (mIsVideoCall && Build.VERSION.SDK_INT < OutGoingCallActivity.SCREEN_MINIMUM_VER){
                            animateAndAddView();
                        }

              if (mSubscriber == null) {

                         mSubscriber = new Subscriber.Builder(IncomingCallActivity.this, mStream).build();
                         mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                         mSubscriber.setVideoListener(IncomingCallActivity.this);
                         mSession.subscribe(mSubscriber);

                                //Hide the connection layout and start mTimerChr
                                mConnectingRL.setVisibility(View.GONE);
                                if(mIsVideoCall)
                                    mSubscriberFL.addView(mSubscriber.getView());



                                //Set Publisher
                                //*** Show the receiver video (Small Windows) Only for video calls
                                if(mIsVideoCall && Build.VERSION.SDK_INT >= OutGoingCallActivity.SCREEN_MINIMUM_VER) {
                                    animateAndAddView();
                                }

                                //Wait 2 seconds because of the api black screen - and then start timer and sent massage to caller
                                new Handler().postDelayed(() -> {
                                        mTimerChr.setVisibility(View.VISIBLE);
                                        mTimerChr.setBase(SystemClock.elapsedRealtime());
                                        mTimerChr.start();

                                        sendXMPPmsg(Message.RTC_CODE_ANSWER);
                                }, 2000);

                                //Show inside a call btns
                                mCameraBtnVideo.setVisibility(View.VISIBLE);
                                mInsideCallBtnsCL.setVisibility(View.VISIBLE);
                                if(mIsVideoCall)
                                    mCameraSwitchIV.setVisibility(View.VISIBLE);
                      }
                    });
                }
        };
        thread.start();

    }

    private void animateAndAddView(){

        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        ((ViewGroup) mPublisher.getView().getParent()).removeView(mPublisher.getView());
        mPublisherFL.addView(mPublisher.getView());
        mPublisherFL.setVisibility(View.VISIBLE);

     /*          mEffectFL.animate()
                       //.setStartDelay(2000)
                       .scaleX(0.3f).scaleY(0.3f)//scale to quarter(half x,half y)
                       .translationY((mEffectFL.getHeight()/4 + 225)).translationX((-mEffectFL.getWidth()/4 -120))// move to bottom / right
                       .alpha(1) // make it less visible
                       .setDuration(800)
                       .withEndAction(() -> {
                              mEffectFL.setVisibility(View.VISIBLE);
                               mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                               ((ViewGroup)mPublisher.getView().getParent()).removeView(mPublisher.getView());
                               mPublisherFL.addView(mPublisher.getView());
                               mPublisherFL.setVisibility(View.VISIBLE);

                           Log.d("tigtog", "after: " + mEffectFL.getWidth());
                       });*/
    }

    //TokBox
    @Override
    public void onConnected(Session session) {
        Log.i(TOKBOX, "Session Connected");
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(TOKBOX, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TOKBOX, "Session Received");
         this.mStream = stream;

                 sessitonRecivedFlag = true;
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TOKBOX, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberFL.removeAllViews();

            if(!mCallEndedFlag)
                    callEnded();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(TOKBOX, "Session error: " + opentokError.getMessage());
    }

}
