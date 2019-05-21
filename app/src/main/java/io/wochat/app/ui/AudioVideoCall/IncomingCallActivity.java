package io.wochat.app.ui.AudioVideoCall;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
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

public class IncomingCallActivity extends AppCompatActivity implements View.OnClickListener, WCRepository.OnSessionResultListener, EasyPermissions.PermissionCallbacks, Session.SessionListener, PublisherKit.PublisherListener {

    private static final String TAG = "IncomingCallActivity";
    private static final String TOKBOX = "TokBox";

    private CircleImageView mParticipantPicAudioCIV, mParticipantPicAudioFlagCIV,
            mParticipantPicVideoCIV, mParticipantPicVideoFlagCIV, mDeclineCIV, mAcceptCIV;
    private TextView mTitleTV, mParticipantNameAudioTV, mParticipantLangAudioTV,
            mParticipantNameVideoTV, mParticipantLangVideoTV, mConnectingTV;
    private Chronometer mTimerChr;
    private FrameLayout mBackNavigationFL;
    private RelativeLayout mMainAudioRL, mMainVideoRL, mConnectingRL;
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
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private Stream mStream;
    private FrameLayout mPublisherFL;
    private FrameLayout mSubscriberFL;
    private AlphaAnimation mCallTXTanimation;
    private boolean callStartedFlag;
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

        //connection animation
        mCallTXTanimation = new AlphaAnimation(0.0f, 1.0f);
        mCallTXTanimation.setDuration(1000);
        mCallTXTanimation.setRepeatCount(Animation.INFINITE);
        mCallTXTanimation.setRepeatMode(Animation.REVERSE);
        mConnectingTV.startAnimation(mCallTXTanimation);

        mRTCcodeBR = new RTCcodeBR();

        mBackNavigationFL.setOnClickListener(this);
        mDeclineCIV.setOnClickListener(this);
        mAcceptCIV.setOnClickListener(this);

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
                 createTokenInExistingSession();
        } else {
                 EasyPermissions.requestPermissions(this, getString(R.string.permissions_expl_in),
                                                    OutGoingCallActivity.RC_VIDEO_APP_PERM, OutGoingCallActivity.perms);

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
                Log.d("test", "onClick: ");
                if(callStartedFlag)
                    sendXMPPmsg(Message.RTC_CODE_CLOSE);
                else
                    sendXMPPmsg(Message.RTC_CODE_REJECTED);
                break;

            case R.id.accept_civ:
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
        Log.d("tttt", "onDestroy: ");
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

    private class RTCcodeBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Message.RTC_CODE_REJECTED)) {
                finish();
            }else if(intent.getAction().equals(Message.RTC_CODE_CLOSE)){
                mTimerChr.stop();
                mTitleTV.setText(getResources().getString(R.string.call_ended));
                        Handler handler = new Handler();
                        handler.postDelayed(() -> finish(), 4000);
            }
        }
    }

    private void callStarted() {
        callStartedFlag = true;
        mSoundsPlayer.stop();
        mAnimation.cancel();
        mAcceptCIV.setEnabled(false);

        mConnectingRL.setVisibility(View.VISIBLE);

        Thread thread =  new Thread() {
                @Override
                public void run() {

                    while (!sessitonRecivedFlag);

                    runOnUiThread(() -> {

                        if (mSubscriber == null) {
                            mSubscriber = new Subscriber.Builder(IncomingCallActivity.this, mStream)
                                    .build();
                            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                                    BaseVideoRenderer.STYLE_VIDEO_FILL);
                            mSession.subscribe(mSubscriber);

                            //Wait 2 seconds
                            new Handler().postDelayed(() -> {
                                mConnectingRL.setVisibility(View.GONE);
                                if (mIsVideoCall)
                                    mSubscriberFL.addView(mSubscriber.getView());
                                    mTimerChr.setVisibility(View.VISIBLE);
                                    mTimerChr.setBase(SystemClock.elapsedRealtime());
                                    mTimerChr.start();

                                sendXMPPmsg(Message.RTC_CODE_ANSWER); // Let the caller know that the receiver accept the call
                            } , 2000);

                        }
                        //**************************
                        //Publish back
                        mPublisher = new Publisher.Builder(IncomingCallActivity.this)
                                .videoTrack(mIsVideoCall)
                                .build();
                        mPublisher.setPublisherListener(IncomingCallActivity.this);

                        mSession.publish(mPublisher);

                        //Only for audio calls
                        if (mIsVideoCall){
                            mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                                    BaseVideoRenderer.STYLE_VIDEO_FILL);
                            mPublisherFL.addView(mPublisher.getView());
                            mPublisherFL.setVisibility(View.VISIBLE);
                        }
                    });
                }
            };
        thread.start();

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
//        if (mSubscriber == null) {
//            mSubscriber = new Subscriber.Builder(this, stream).build();
//            mSession.subscribe(mSubscriber);
//            mSubscriberFL.addView(mSubscriber.getView());
//        }

    Log.i(TOKBOX, "Stream Received");
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TOKBOX, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberFL.removeAllViews();

            sendXMPPmsg(Message.RTC_CODE_REJECTED);
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(TOKBOX, "Session error: " + opentokError.getMessage());
    }

}
