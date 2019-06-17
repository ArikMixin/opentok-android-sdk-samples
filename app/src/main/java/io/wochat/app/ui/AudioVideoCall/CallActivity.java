package io.wochat.app.ui.AudioVideoCall;

import android.Manifest;
import android.app.PictureInPictureParams;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.util.Rational;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.opentok.android.AudioDeviceManager;
import com.opentok.android.BaseAudioDevice;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
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
import io.wochat.app.db.entity.Call;
import io.wochat.app.db.entity.Message;
import io.wochat.app.model.StateData;
import io.wochat.app.model.VideoAudioCall;
import io.wochat.app.ui.Consts;
import io.wochat.app.utils.CustomAudioDevice;
import io.wochat.app.utils.SpeechToTextUtil;
import io.wochat.app.utils.TextToSpeechUtil;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.VideoAudioCallViewModel;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.opentok.android.Session;

public class CallActivity extends AppCompatActivity
        implements View.OnClickListener,
        WCRepository.OnSessionResultListener,
        EasyPermissions.PermissionCallbacks,
        Session.SessionListener,
        PublisherKit.PublisherListener,
        View.OnTouchListener,
        SubscriberKit.VideoListener,
        CompoundButton.OnCheckedChangeListener,
        SpeechToTextUtil.SpeechUtilsSTTListener,
        AudioManager.OnAudioFocusChangeListener, TextToSpeechUtil.TextToSpeechPlayingListener {

    private static final String TAG = "CallActivity";
    private static final String TOKBOX = "TokBox";

    private CircleImageView mAcceptIncomingCIV;
    private CircleImageView mMicFlagCIV, mMicFlagP2T_CIV, mParticipantPicAudioCIV, mParticipantPicAudioFlagCIV,
            mParticipantPicVideoCIV, mParticipantPicVideoFlagCIV;
    private TextView mTitleTV, mParticipantNameAudioTV, mParticipantLangAudioTV,
            mParticipantNameVideoTV, mParticipantLangVideoTV , mParticipantNumberTV, mStatusTV;
    private Chronometer mTimerChr;
    private ImageView mCameraSwitchIV, mTranslatorMicIV, mTranslatorMicP2T_IV;
    private FrameLayout mBackNavigationFL, mCameraPauseFullFL;
    private RelativeLayout mMainAudioRL, mMainVideoRL, mStatusRL, mUserPicAudioRL, mTranslateRL, mLockRL;
    private ImageView mDeclineInsideIV, mDeclineIncomingIV, mAcceptIV;
    private String mFixedParticipantId;
    private Locale loc;
    private int mFlagDrawable;
    private String mFullLangName;
    private boolean mIsVideoCall, mIsOutGoingCall;
    private String mParticipantId, mParticipantName, mParticipantLang, mParticipantPic, mConversationId;
    private String mSelfId, mSelfLang;
    private VideoAudioCallViewModel videoAudioCallViewModel;
    private VideoAudioCall mVideoAudioCall;
    private String errorMsg;
    private boolean mVideoFlag;
    private WCService mService;
    private MediaPlayer mCallingSound, mDeclineSound, mBusySound, mIncomingCallSound;
    private AlphaAnimation mCallTXTanima;
    private TranslateAnimation mTranslateAnima, mAcceptBtnAnim;
    private ScaleAnimation reSizeAnim;
    private Message message;
    private RTCcodeBR mRTCcodeBR;
    private ToggleButton mCameraBtnVideo, mCameraBtnAudio;
    private ToggleButton mSpeakerIB, mMuteTB;
    private ConstraintLayout mInsideCallBtnsCL, mActionBtnsCL;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private Stream mStream;
    private String mSessionID;
    private FrameLayout mPublisherFL;
    private FrameLayout mSubscriberFL;
    private FrameLayout mEffectFL;
    private FrameLayout cPipModePublisherFL;
    private RelativeLayout mPushToTalkFL;
    private float dX, dY, mCornerX, mCornerY ,y1, y2,deltaY;
    private int screenHeight, screenWidth;
    private boolean callStartedFlag;
    private boolean mCallEndedFlag;
    private boolean mCallerCamOpen = true;
    private boolean mSelfCamOpen = true;
    private PictureInPictureParams pip_params;
    private Rational aspectRatio;
    private RelativeLayout mParticipantNameRL, mDeclineRL, incomingCallBtnsRL, mConnectingRL;
    private TextView mListeningTV, mConnectingTV;
    private ImageView mArrowsIV;
    private boolean mPush2talk_locked;
    private ImageView mLockIV, mRecordingStateAudioIV, mRecordingStateVideoIV;
    volatile boolean sessitonRecivedFlag;
    private Vibrator vibrator;
    private CustomAudioDevice customAudioDevice;
    private boolean mAudioDriveStrted;
    public static final String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO };

    public static final int CLOSING_TIME = 3000;
    public static final int SCREEN_MINIMUM_VER = 25;
    public static final String TOK_BOX_APIKEY = "46296242";
    public static final int RC_SETTINGS_SCREEN_PERM = 123;
    public static final int RC_VIDEO_APP_PERM = 124;
    public static final int MIN_DISTANCE = 500;
    public static final int ANIMATION_DURATION = 500;
    public static boolean activityActiveFlag;

    public static final String CALL_MISSED  = "CALL_MISSED";
    public static final String CALL_INCOMING  = "CALL_INCOMING";
    public static final String CALL_OUTGOING  = "OUTGOING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        initViews();
        requestPermissions();
    }

    private void initViews() {
        activityActiveFlag = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                       getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mDeclineIncomingIV = (ImageView) findViewById(R.id.decline_incoming_iv);
        mAcceptIV = (ImageView) findViewById(R.id.accept_iv);
        mMicFlagCIV = (CircleImageView) findViewById(R.id.mic_flag_civ);
        mMicFlagP2T_CIV = (CircleImageView) findViewById(R.id.mic_flag_p2t_civ);
        mTitleTV = (TextView) findViewById(R.id.title_tv);
        mCameraSwitchIV = (ImageView) findViewById(R.id.camera_switch_iv);
        mBackNavigationFL = (FrameLayout) findViewById(R.id.back_navigation_fl);
        mEffectFL = (FrameLayout) findViewById(R.id.effect_fl);
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
        mMainAudioRL = (RelativeLayout) findViewById(R.id.main_audio_rl);
        mMainVideoRL = (RelativeLayout) findViewById(R.id.main_video_rl);
        mStatusRL = (RelativeLayout) findViewById(R.id.status_rl);
        mUserPicAudioRL = (RelativeLayout) findViewById(R.id.user_pic_audio_rl);
        mPublisherFL = (FrameLayout) findViewById(R.id.publisher_fl);
        mSubscriberFL = (FrameLayout) findViewById(R.id.subscriber_fl);
        mCameraPauseFullFL = (FrameLayout) findViewById(R.id.camera_pause_full_fl);
        mCameraBtnVideo = (ToggleButton) findViewById(R.id.camera_btn_video_tb);
        mCameraBtnAudio = (ToggleButton) findViewById(R.id.camera_btn_audio_tb);
        mSpeakerIB = (ToggleButton) findViewById(R.id.speaker_iv);
        mMuteTB = (ToggleButton) findViewById(R.id.mute_iv);
        mInsideCallBtnsCL = (ConstraintLayout) findViewById(R.id.inside_call_btns_cl);
        cPipModePublisherFL = (FrameLayout) findViewById(R.id.pip_mode_publisher_fl);
        mParticipantNameRL = (RelativeLayout) findViewById(R.id.participant_name_rl);
        mTranslatorMicIV = (ImageView) findViewById(R.id.translator_mic_iv);
        mTranslatorMicP2T_IV = (ImageView) findViewById(R.id.translator_mic_p2t_iv);
        mPushToTalkFL = (RelativeLayout) findViewById(R.id.push_to_talk_fl);
        mTranslateRL = (RelativeLayout) findViewById(R.id.translate_rl);
        mDeclineRL = (RelativeLayout) findViewById(R.id.decline_rl);
        mLockRL = (RelativeLayout) findViewById(R.id.lock_rl);
        mListeningTV = (TextView) findViewById(R.id.listening_tv);
        mArrowsIV = (ImageView) findViewById(R.id.arrows_iv);
        mLockIV = (ImageView) findViewById(R.id.lock_iv);
        incomingCallBtnsRL = (RelativeLayout)findViewById(R.id.incoming_call_btns_rl);
        mDeclineInsideIV = (ImageView) findViewById(R.id.decline_inside_iv);
        mConnectingRL = (RelativeLayout) findViewById(R.id.connecting_rl);
        mActionBtnsCL = (ConstraintLayout) findViewById(R.id.action_btns_cl);
        mConnectingTV = (TextView) findViewById(R.id.connecting_tv);
/*        mRecordingStateAudioIV = (ImageView) findViewById(R.id.recording_state_audio_iv);
        mRecordingStateVideoIV = (ImageView) findViewById(R.id.recording_state_video_iv);*/

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mIsVideoCall = getIntent().getBooleanExtra(Consts.INTENT_IS_VIDEO_CALL, false);
        mParticipantId = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_ID);
        mParticipantName = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
        mParticipantLang = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
        mParticipantPic = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
        mConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
        mSessionID = getIntent().getStringExtra(Consts.INTENT_SESSION_ID);

        mSelfId = getIntent().getStringExtra(Consts.INTENT_SELF_ID);
        mSelfLang = getIntent().getStringExtra(Consts.INTENT_SELF_LANG);
//      mSelfName = getIntent().getStringExtra(Consts.INTENT_SELF_NAME);
//      mSelfPicUrl = getIntent().getStringExtra(Consts.INTENT_SELF_PIC_URL);
        mIsOutGoingCall = getIntent().getBooleanExtra(Consts.OUTGOING_CALL_FLAG,true);

        /**View Model **/
        videoAudioCallViewModel = ViewModelProviders.of(this).get(VideoAudioCallViewModel.class);
        videoAudioCallViewModel.getTranslatedText().observe(this, textToPlay -> {if(textToPlay != null)
                                                                                             fireTextToSpeech(textToPlay);});
        //*** Save call details to roomDB (For recent calls section)
        // CALL_MISSED - is a default status - need to change it in the end of a call , call Duration - update in the end of a call;
        String  mCallState;
        if(mIsOutGoingCall)
            mCallState = CALL_OUTGOING;
        else
            mCallState = CALL_MISSED; // nly after the call (if user answer - update to CALL_OUTGOING status;
        Call call = new Call(mParticipantId, mParticipantName, mParticipantPic,
                                    mParticipantLang, mIsVideoCall, mCallState, System.currentTimeMillis(),0);
        videoAudioCallViewModel.addNewCall(call);

        /**Picture-to-Picture-mode (Minimize)**/
        initPIP(); // Picture-to-picture (Minimize feature) initialization

        /**SpeechToTextUtil and Audio Driver initialization**/
        TextToSpeechUtil.getInstance().setLanguage(mSelfLang); //Play in users language
        SpeechToTextUtil.getInstance().setSpeechUtilsSTTListener(this);
        customAudioDevice = new CustomAudioDevice(CallActivity.this);
      if(AudioDeviceManager.getAudioDevice() == null )
                    AudioDeviceManager.setAudioDevice(customAudioDevice);
        AudioDeviceManager.getAudioDevice().initCapturer();
        AudioDeviceManager.getAudioDevice().initRenderer();

        /**Language(Flag), display name, and pic initialization**/
        setLangAndDisplayName();

        mMicFlagCIV.setEnabled(false);
        mMicFlagP2T_CIV.setEnabled(false);

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

        /**Sounds (ringing etc.) initialization**/
        mDeclineSound = MediaPlayer.create(this, R.raw.declined_call);
        mCallingSound = MediaPlayer.create(this, R.raw.phone_calling_tone);
        mIncomingCallSound = MediaPlayer.create(this, R.raw.incoming_call);
        mBusySound = MediaPlayer.create(this, R.raw.phone_busy_signal);


        /**Animation initialization**/
        mCallTXTanima = new AlphaAnimation(0.0f, 1.0f);
        mCallTXTanima.setDuration(1000);
        mCallTXTanima.setRepeatCount(Animation.INFINITE);
        mCallTXTanima.setRepeatMode(Animation.REVERSE);
        mStatusTV.startAnimation(mCallTXTanima);

        mTranslateAnima = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.1f);
        mTranslateAnima.setDuration(ANIMATION_DURATION);
        mTranslateAnima.setRepeatCount(Animation.INFINITE);
        mTranslateAnima.setRepeatMode(Animation.REVERSE);
        mTranslateAnima.setInterpolator(new LinearInterpolator());

        reSizeAnim = new ScaleAnimation(1f, 4f, 1f, 4f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        reSizeAnim.setDuration(ANIMATION_DURATION);

        //Answer the call animation
        mAcceptBtnAnim = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.1f);
        mAcceptBtnAnim.setDuration(500);
        mAcceptBtnAnim.setRepeatCount(Animation.INFINITE);
        mAcceptBtnAnim.setRepeatMode(Animation.REVERSE);
        mAcceptBtnAnim.setInterpolator(new LinearInterpolator());
        mAcceptIV.setAnimation(mAcceptBtnAnim);

        mPushToTalkFL.setVisibility(View.VISIBLE);
        mPushToTalkFL.setAlpha(0.0f);

        mRTCcodeBR = new RTCcodeBR();

        mAcceptIV.setOnClickListener(this);
        mDeclineIncomingIV.setOnClickListener(this);
        mDeclineInsideIV.setOnClickListener(this);

        mBackNavigationFL.setOnClickListener(this);
        mCameraBtnVideo.setOnClickListener(this);
        mCameraBtnAudio.setOnClickListener(this);
        mCameraSwitchIV.setOnClickListener(this);
        mPublisherFL.setOnTouchListener(this);
        mMicFlagCIV.setOnTouchListener(this);
        mMuteTB.setOnCheckedChangeListener(this);
        mSpeakerIB.setOnCheckedChangeListener(this);

        //Outgoing or incoming call
        if(mIsOutGoingCall)
            outGoingCall();
        else
            incomingCall();

        // Video Or Audio
        if (mIsVideoCall)
            videoCall();
        else
            audioCall();
    }

    private void outGoingCall() {
        mInsideCallBtnsCL.setVisibility(View.VISIBLE);
        mActionBtnsCL.setVisibility(View.VISIBLE);
        mStatusRL.setVisibility(View.VISIBLE);

        //Play calling sound in first
        mCallingSound.setLooping(true);
        mCallingSound.start();
    }

    private void incomingCall() {
        incomingCallBtnsRL.setVisibility(View.VISIBLE);

        //Play incoming sound in first
        mIncomingCallSound.setLooping(true);
        mIncomingCallSound.start();
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

        mMainVideoRL.setVisibility(View.VISIBLE);

        mParticipantNameVideoTV.setText(mParticipantName);
        mParticipantLangVideoTV.setText(mFullLangName);

        //Set Participant Flags
        mParticipantPicVideoFlagCIV.setImageResource(mFlagDrawable);

        //Set Participant Pic
        setPhotoByUrl(true);

        if(mIsOutGoingCall)
            mTitleTV.setText(R.string.out_video_call);
        else
            mTitleTV.setText(R.string.in_video_call);
    }

    private void audioCall() {
        mVideoFlag = false;

        //OpenTok enable the speaker by default - we close it manually;
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

        if(mIsOutGoingCall)
            mTitleTV.setText(R.string.out_audio_call);
        else
            mTitleTV.setText(R.string.in_audio_call);
    }

    private void setLangAndDisplayName() {
//        if(mParticipantLang.equals("IW")) mParticipantLang = "HE";
//        if(mSelfId.equals("IW")) mSelfId = "HE";

        mFlagDrawable = Utils.getCountryFlagDrawableFromLang(mParticipantLang);
        try {
            loc = new Locale(mParticipantLang);
            mFullLangName = loc.getDisplayLanguage();
            mMicFlagCIV.setImageResource(mFlagDrawable);
            mMicFlagP2T_CIV.setImageResource(mFlagDrawable);
        } catch (Exception e) {
            Log.d(TAG, "CallActivity - " + e.getMessage());
            e.printStackTrace();
        }
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
                // .videoTrack(mIsVideoCall)
                .build();
        mPublisher.setPublisherListener(this);

        if(mIsVideoCall) { //Only if it is video call - show preview
                    mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                                                                    BaseVideoRenderer.STYLE_VIDEO_FILL);
                    mPublisher.startPreview();
                    mSubscriberFL.addView(mPublisher.getView());
        }else{ // Audio call
                    mPublisher.setPublishVideo(false);
                    // switch from loud speaker to phone speaker (voice session)
                    mSpeakerIB.setChecked(false);
                    AudioDeviceManager.getAudioDevice().setOutputMode(BaseAudioDevice.OutputMode.Handset);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_navigation_fl:
                    if(callStartedFlag) {
                        minimizeActivity();
                    }else
                        sendXMPPmsg(Message.RTC_CODE_CLOSE,"", false);
            break;

            case R.id.decline_incoming_iv:
                    sendXMPPmsg(Message.RTC_CODE_REJECTED,"",false);
            break;

            case R.id.decline_inside_iv:
                    sendXMPPmsg(Message.RTC_CODE_CLOSE,"",false);
            break;

            case R.id.accept_iv:
                callStarted();
                break;

            case R.id.camera_btn_video_tb:
                    cameraBtnVideo();
            break;

            case R.id.camera_btn_audio_tb:
                    cameraBtnAudio();
            break;

            case R.id.camera_switch_iv:
                mPublisher.cycleCamera();
                mCameraSwitchIV.animate().rotationBy(360)
                        .withStartAction(() -> mCameraSwitchIV.setEnabled(false))
                        .withEndAction(() -> mCameraSwitchIV.setEnabled(true))
                        .start();
            break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.mute_iv:

                if (isChecked)
                    mPublisher.setPublishAudio(false);
                else
                    mPublisher.setPublishAudio(true);
                break;

            case R.id.speaker_iv:

                if(isChecked)
                    AudioDeviceManager.getAudioDevice().setOutputMode(BaseAudioDevice.OutputMode.SpeakerPhone);
                else
                    AudioDeviceManager.getAudioDevice().setOutputMode(BaseAudioDevice.OutputMode.Handset);
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch(view.getId()){
            case R.id.publisher_fl:
                publisherWindowTouch(view,event);
                break;
            case R.id.mic_flag_civ:
                micTranslateTouch(view,event); // Push to talk btn
                break;
        }
        return true;
    }

    private void  publisherWindowTouch(View view, MotionEvent event){
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: //--Hold--
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

            case MotionEvent.ACTION_UP: //--Release--

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
        }
    }

    private void micTranslateTouch(View view, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: //--Hold--
                sendXMPPmsg(Message.RTC_CODE_UPDATE_SESSION,"",true);
                mAudioDriveStrted = false;
                mPush2talk_locked = false;
                mPushToTalkFL.setClickable(true);
                mPublisherFL.setVisibility(View.GONE);
                mLockRL.setVisibility(View.VISIBLE);
                ViewCompat.animate(mPushToTalkFL).setDuration(300).alpha(1);

                AudioDeviceManager.getAudioDevice().stopCapturer();
              AudioDeviceManager.getAudioDevice().stopRenderer();
                SpeechToTextUtil.getInstance().startSpeechToText();


                //SlideUp
                y1 = event.getY();

                break;

            case MotionEvent.ACTION_MOVE: //--Move--
                //SlideUp
                y2 = event.getY();
                deltaY = y2 - y1;

                if (Math.abs(deltaY) > MIN_DISTANCE && y2 < y1) {
                    //User lock the push 2 talk

                    // TODO: 6/3/2019 when release - send the voice to text (Make The Translation)

                    //start lock Animation
                    if (!mPush2talk_locked) {

                        vibrator.vibrate(400);

                        mLockIV.startAnimation(reSizeAnim);
                        reSizeAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mLockRL.setVisibility(View.GONE);
                                mTranslatorMicP2T_IV.setImageResource(0);
                                mTranslatorMicP2T_IV.setBackgroundResource(R.drawable.interperter_locked);
                                mMicFlagP2T_CIV.setEnabled(true);
                                mMicFlagP2T_CIV.setOnClickListener(view1 -> sendPush2TalkMsg());
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        mPush2talk_locked = true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP: //--Release--
                        if(!mPush2talk_locked)
                                     sendPush2TalkMsg();

                break;
        }
    }

    public void sendPush2TalkMsg(){
        Log.d(TAG, "sendPush2TalkMsg: ");
        SpeechToTextUtil.getInstance().stopSpeechToText();
//        AudioDeviceManager.getAudioDevice().startRenderer();
//        AudioDeviceManager.getAudioDevice().startCapturer();
        sendXMPPmsg(Message.RTC_CODE_UPDATE_SESSION,"",false);

            ViewCompat.animate(mPushToTalkFL).setDuration(300).alpha(0.0f).withEndAction(()->{
                    mPublisherFL.setVisibility(View.VISIBLE);
                    mTranslatorMicP2T_IV.setImageResource(0);
                    mTranslatorMicP2T_IV.setBackgroundResource(R.drawable.translator_mic_enabled);
            });
            mPushToTalkFL.setClickable(false);
            mMicFlagP2T_CIV.setClickable(false);
            mMicFlagP2T_CIV.setEnabled(false);
            mMicFlagCIV.setClickable(true);
            mMicFlagCIV.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(callStartedFlag) {
            minimizeActivity();
        }else
            sendXMPPmsg(Message.RTC_CODE_REJECTED,"",false);
    }

    /**
     * switches the activity into PIP mode instead of going into the background (Only inside a call)
     */
    @Override
    public void onUserLeaveHint () {
        // Minimize fetchers work only from android 7 - (24)
        if (callStartedFlag) {
            minimizeActivity();
        }
    }

    private void initPIP(){
        //Minimize (PIP) feature don't work in versions lower than 24 - so hide the back nav btn
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N ||
                !getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE))
            mBackNavigationFL.setVisibility(View.GONE);

        //Init picture-in-picture
        aspectRatio = new Rational(1, 2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)){
            pip_params = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build();
            setPictureInPictureParams(pip_params);
        }
    }
    /**
     * Minimize feature (Picture-in-picture) work only from android 7 - (24)
     * Work only if call started
     */
    private void minimizeActivity(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE))
            CallActivity.this.enterPictureInPictureMode();

        //else -
        // TODO: 5/30/2019 Create minimize feature for versions older than android 7
    }

    @Override
    public void onPictureInPictureModeChanged (boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            // Hide the full-screen UI - (picture-in-picture mode)
            mInsideCallBtnsCL.setVisibility(View.GONE);
            mBackNavigationFL.setVisibility(View.GONE);
            mTitleTV.setVisibility(View.GONE);

            if (mVideoFlag){
                mPublisherFL.removeView(mPublisher.getView());
                mPublisherFL.setVisibility(View.GONE);
                cPipModePublisherFL.setVisibility(View.VISIBLE);
                cPipModePublisherFL.addView(mPublisher.getView());
                mMainVideoRL.setVisibility(View.GONE);
            }else {
                mParticipantNameRL.setVisibility(View.GONE);
                mParticipantLangAudioTV.setVisibility(View.GONE);
            }

        } else {
            // Restore the full-screen UI.
            mInsideCallBtnsCL.setVisibility(View.VISIBLE);
            mBackNavigationFL.setVisibility(View.VISIBLE);
            mTitleTV.setVisibility(View.VISIBLE);

            if(mVideoFlag) {
                mMainVideoRL.setVisibility(View.VISIBLE);
                cPipModePublisherFL.removeView(mPublisher.getView());
                cPipModePublisherFL.setVisibility(View.GONE);
                mPublisherFL.setVisibility(View.VISIBLE);
                mPublisherFL.addView(mPublisher.getView());
            }else{
                mParticipantNameRL.setVisibility(View.VISIBLE);
                mParticipantLangAudioTV.setVisibility(View.VISIBLE);
            }
        }
    }



    private void cameraBtnAudio() {
        turnCallType(true);
        mCameraBtnVideo.setChecked(false);
        mSelfCamOpen = true;

        mSubscriberFL.setVisibility(View.VISIBLE);
        mPublisherFL.setVisibility(View.VISIBLE);

        if(mPublisher != null) { // When open video from audio call
            mPublisher.setPublishVideo(true);
            mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mPublisherFL.addView(mPublisher.getView());
        }

        if (mSubscriber != null && Build.VERSION.SDK_INT < CallActivity.SCREEN_MINIMUM_VER) {
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

            //If both (caller and receiver) close the cameras - turn to audio
            if(!mCallerCamOpen)
                turnCallType(false);
        }else{  //open publisher video

            mSelfCamOpen = true;
            mPublisher.setPublishVideo(true);
            mPublisherFL.addView(mPublisher.getView());

            if (Build.VERSION.SDK_INT < CallActivity.SCREEN_MINIMUM_VER) {
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

    //Send Massage to the receiver
    public void sendXMPPmsg(String rtcCode, String msgFromPTT ,boolean isRecording){
        message = new Message(mParticipantId, mSelfId, mConversationId, mSessionID, msgFromPTT,mSelfLang,
                                                                              rtcCode, mVideoFlag, isRecording);

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

        if(mIsOutGoingCall)
             videoAudioCallViewModel.createSessionsAndToken(this,"RELAYED");
        else
             videoAudioCallViewModel.createTokenInExistingSession(this, mSessionID,
                                                  "" + WCRepository.TokenRoleType.PUBLISHER);

    }

    @Override
    public void onSucceedCreateSession(StateData<String> success){
        mVideoAudioCall = videoAudioCallViewModel.getSessionAndToken().getValue();

        if(mIsOutGoingCall) {
             mSessionID = mVideoAudioCall.getSessionID(); // If outGoing call - create new SessionId
                    //Send Massage to the receiver (With the sessionID) - let the receiver know that video/audio call is coming
            sendXMPPmsg(Message.RTC_CODE_OFFER,"", false);
        }
        //Create session connection via TokBox
        connectToSession(mSessionID, mVideoAudioCall.getToken());
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

    public void connectToSession(String sessionID, String tokenID){
     // mSession = new Session(this, TOK_BOX_APIKEY, sessionID);
        mSession = new Session.Builder(this, TOK_BOX_APIKEY, sessionID).build();
        mSession.setSessionListener(this);
        mSession.connect(tokenID);
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
            filter.addAction(Message.RTC_CODE_UPDATE_SESSION);
            filter.addAction(Message.RTC_CODE_TEXT);
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

        //Stop Sounds
        mIncomingCallSound.stop();
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

           videoAudioCallViewModel.resetTranslatedText();

        if(mAudioDriveStrted) {
            AudioDeviceManager.getAudioDevice().stopCapturer();
            AudioDeviceManager.getAudioDevice().stopRenderer();
        }
        AudioDeviceManager.getAudioDevice().destroyCapturer();
        AudioDeviceManager.getAudioDevice().destroyRenderer();
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
            sendXMPPmsg(Message.RTC_CODE_REJECTED,"", false);
    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        sendXMPPmsg(Message.RTC_CODE_REJECTED,"", false);
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {

    }

    @Override // caller
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
        mCallerCamOpen = false;

        if(!mSelfCamOpen) {
            turnCallType(false);
            return;
        }
        mCameraPauseFullFL.setVisibility(View.VISIBLE);
        mSubscriberFL.setVisibility(View.GONE);
    }

    @Override // caller
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
        mCallerCamOpen = true;

        if(!mVideoFlag) {
            turnCallType(true);
            // mSelfCamOpen = false;
        }
        mCameraPauseFullFL.setVisibility(View.GONE);
        mSubscriberFL.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) { }
    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) { }

    @Override
    public void onAudioFocusChange(int i) {

    }
    private class RTCcodeBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("intent", "onReceive: " + intent.getAction());
            if(!mCallEndedFlag && intent.getAction().equals(Message.RTC_CODE_REJECTED) ||
                    intent.getAction().equals(Message.RTC_CODE_BUSY) ||
                    intent.getAction().equals(Message.RTC_CODE_CLOSE))
                callEnded(intent.getAction());
            else if(intent.getAction().equals(Message.RTC_CODE_ANSWER))
                callStarted();
           else if(intent.getAction().equals(Message.RTC_CODE_UPDATE_SESSION))
                         recordingState(intent.getBooleanExtra(WCService.IS_RECORDING,false));
           else if(intent.getAction().equals(Message.RTC_CODE_TEXT)){
                videoAudioCallViewModel.translateText(intent.getStringExtra(WCService.RTC_MESSAGE),
                                                intent.getStringExtra(WCService.RTC_MESSAGE_LANGUAGE));

            }
          }

    }

    private void callEnded(String rtcCode){
        mCallEndedFlag = true;
        mCallTXTanima.cancel();
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
            if(callStartedFlag) {
                mTimerChr.stop();
                mTitleTV.setText(getResources().getString(R.string.call_ended));
            }else{
                mCallingSound.stop();
                finish();
                return;
            }
        }

        // Update call info after call is finished
        // videoAudioCallViewModel.updateCall()

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            mCallingSound.stop();
            finish();
        }, CLOSING_TIME);
    }

    private void callStarted() {
       // customAudioDevice.startCapturer();

        callStartedFlag = true;
            mArrowsIV.startAnimation(mTranslateAnima); // start PushToTalk arrow animation
            mAcceptIV.setEnabled(false);
            mStatusRL.setVisibility(View.GONE);
            incomingCallBtnsRL.setVisibility(View.GONE);

            if(mIsOutGoingCall){
                    //Stop sound
                            mCallingSound.stop();
                            mCallTXTanima.cancel();

                            //Start Timer For Outgoing call
                            mTimerChr.setVisibility(View.VISIBLE);
                            mTimerChr.setBase(SystemClock.elapsedRealtime());
                            mTimerChr.start();
                             // Close & Open Camera button visible only after call started ( Timer Starts )
                            mCameraBtnAudio.setVisibility(View.VISIBLE);
                            mCameraBtnVideo.setVisibility(View.VISIBLE);
           }else{ // IncomingCall
                    //Stop sound and accept call (jumping) animation
                            mIncomingCallSound.stop();
                            mAcceptBtnAnim.cancel();

                            mInsideCallBtnsCL.setVisibility(View.VISIBLE);
                             mConnectingTV.startAnimation(mCallTXTanima);
                             mConnectingRL.setVisibility(View.VISIBLE);

                            startIncomingCallThread();
           }

        //Enable translate btn lang if self and participant have different languages
        if (!mSelfLang.equals(mParticipantLang)) {
            mMicFlagCIV.setEnabled(true);
            mMicFlagCIV.setAlpha(1f);
            mTranslatorMicIV.setEnabled(true);
            mTranslatorMicIV.setImageResource(R.drawable.translator_mic_enabled);
            mTranslatorMicP2T_IV.setEnabled(true);
        }else{
            mTranslatorMicIV.setEnabled(false);
        }
    }

    private void startIncomingCallThread() {

        new Thread (() -> {

            while (!sessitonRecivedFlag);

            runOnUiThread(() -> {

                mSession.publish(mPublisher);


                if (mIsVideoCall && Build.VERSION.SDK_INT < CallActivity.SCREEN_MINIMUM_VER){
                         animateAndAddView();
                }

                if (mSubscriber == null) {
                    mSubscriber = new Subscriber.Builder(CallActivity.this, mStream).build();
                    mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                    mSubscriber.setVideoListener(CallActivity.this);
                    mSession.subscribe(mSubscriber);

                    //Hide the connection layout and start mTimerChr
                    mActionBtnsCL.setVisibility(View.VISIBLE);
                    mConnectingRL.setVisibility(View.GONE);
                    mCallTXTanima.cancel();

                    if(mIsVideoCall)
                        mSubscriberFL.addView(mSubscriber.getView());

                    //Set Publisher
                    //*** Show the receiver video (Small Windows) Only for video calls
                    if(mIsVideoCall && Build.VERSION.SDK_INT >= CallActivity.SCREEN_MINIMUM_VER) {
                        animateAndAddView();
                    }

                    //Wait 2 seconds because of the api black screen - and then start timer and sent massage to caller
                    new Handler().postDelayed(() -> {
                        mTimerChr.setVisibility(View.VISIBLE);
                        mTimerChr.setBase(SystemClock.elapsedRealtime());
                        mTimerChr.start();
                        // Close & Open Camera button visible only after call started ( Timer Starts )
                        mCameraBtnAudio.setVisibility(View.VISIBLE);
                        mCameraBtnVideo.setVisibility(View.VISIBLE);

                        sendXMPPmsg(Message.RTC_CODE_ANSWER, "",false);
                    }, 2000);

                    //Show inside a call btns
                    mCameraBtnVideo.setVisibility(View.VISIBLE);
                    mInsideCallBtnsCL.setVisibility(View.VISIBLE);
                    if(mIsVideoCall)
                        mCameraSwitchIV.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }


    //TokBox
    @Override
    public void onConnected(Session session) {
        Log.i(TOKBOX, "Session Connected");
        if(mIsOutGoingCall)
             mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(TOKBOX, "Session Disconnected");
    }


    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TOKBOX, "Stream Received");

        if(mIsOutGoingCall) {
                        //*** Show the receiver video (Small Windows) Only for video calls
                        if (mIsVideoCall && Build.VERSION.SDK_INT < SCREEN_MINIMUM_VER)
                                 animateAndAddView();

                        if (mSubscriber == null) {
                                mSubscriber = new Subscriber.Builder(CallActivity.this, stream).build();
                                mSubscriber.setVideoListener(CallActivity.this);
                                mSession.subscribe(mSubscriber);

                                //Show the caller video (full screen) Only for video calls
                                if (mIsVideoCall) {
                                    mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                                                                                        BaseVideoRenderer.STYLE_VIDEO_FILL);
                                    mSubscriberFL.addView(mSubscriber.getView());
                                }

                                //*** Show the receiver video (Small Windows) Only for video calls
                                if (mIsVideoCall && Build.VERSION.SDK_INT >= SCREEN_MINIMUM_VER)
                                animateAndAddView();
                         }
        }else{
                            this.mStream = stream;
                            sessitonRecivedFlag = true;
        }
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

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TOKBOX, "Stream Dropped");

        if (mSubscriber != null) {
                mSubscriber = null;
                mSubscriberFL.removeAllViews();
        }

        if(!mCallEndedFlag)
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

    private void recordingState(boolean isRecording) {

        if (mIsVideoCall) {
                if (isRecording)
                       Picasso.get().load(R.drawable.recording_state).into(mParticipantPicVideoCIV);
                else
                       setPhotoByUrl(true);
        } else {
                 if (isRecording)
                       Picasso.get().load(R.drawable.recording_state).into(mParticipantPicVideoCIV);
                 else
                       setPhotoByUrl(false);
        }

        //Disable translator button if other side is recording now
        if (isRecording) {
            mMicFlagCIV.setEnabled(false);
            mMicFlagCIV.setAlpha(0.3f);
            mTranslatorMicIV.setEnabled(false);
            mTranslatorMicIV.setImageResource(R.drawable.translator_mic_disabled);
        }else{
            mMicFlagCIV.setEnabled(true);
            mMicFlagCIV.setAlpha(1f);
            mTranslatorMicIV.setEnabled(true);
            mTranslatorMicIV.setImageResource(R.drawable.translator_mic_enabled);
        }
    }

    @Override
    public void onSpeechToTextResult(String text, int duration) {
        Log.d(TAG, "onSpeechToTextResult: ");
        if(!text.equals(""))
            sendXMPPmsg(Message.RTC_CODE_TEXT,text, false);
    }

    @Override
    public void onBeginningOfSpeechToText() {
        Log.d(TAG, "onBeginningOfSpeechToText: ");
    }

    @Override
    public void onEndOfSpeechToText() {
            if(!mAudioDriveStrted) {
                    AudioDeviceManager.getAudioDevice().startRenderer();
                    AudioDeviceManager.getAudioDevice().startCapturer();
                    mAudioDriveStrted = true;
            }
    }

    @Override
    public void onErrorOfSpeechToText(int resourceString) {
        if (resourceString != 0)
            Toast.makeText(this, getString(resourceString), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Speech recognition error", Toast.LENGTH_SHORT).show();
    }


    //Text To Speech
    private void fireTextToSpeech(String textToPlay) {
        // call view model to translate
       //  AudioDeviceManager.getAudioDevice().stopCapturer();
        AudioDeviceManager.getAudioDevice().stopRenderer();
        TextToSpeechUtil.getInstance().startTextToSpeech(textToPlay, this);
    }

    @Override
    public void onBeginPlaying(){ }

    @Override
    public void onFinishedPlaying() {
        TextToSpeechUtil.getInstance().stopTextToSpeech();
     //   AudioDeviceManager.getAudioDevice().startCapturer();
        AudioDeviceManager.getAudioDevice().startRenderer();
    }
}
