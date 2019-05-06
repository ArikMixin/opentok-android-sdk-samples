package io.wochat.app.ui.AudioVideoCall;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.Locale;
import io.wochat.app.R;
import io.wochat.app.WCRepository;
import io.wochat.app.components.CircleImageView;
import io.wochat.app.model.StateData;
import io.wochat.app.ui.Consts;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.VideoAudioCallViewModel;

public class OutGoingCallActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView mMicFlagCIV, mParticipantPicAudioCIV, mParticipantPicAudioFlagCIV, mParticipantPicVideoCIV, mParticipantPicVideoFlagCIV;
    private TextView mTitleTV, mParticipantNameAudioTV, mParticipantLangAudioTV,  mParticipantNameVideoTV, mParticipantLangVideoTV , mParticipantNumberTV;
    private ImageView mCameraSwitchIV;
    private FrameLayout mBackNavigationFL;
    private RelativeLayout mMainAudioRL, mMainVideoRL;
    private String mFixedParticipantId;
    private Locale loc;
    private int mFlagDrawable;
    private String mFullLangName;
    private boolean mIsVideoCall;
    private String mParticipantId, mParticipantName, mParticipantLang, mParticipantPic, mConversationId;
    private VideoAudioCallViewModel videoAudioCallViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

            initViews();
            createSessionAndToken(); // TokBox
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

        mParticipantPicAudioCIV = (CircleImageView) findViewById(R.id.participant_pic_audio_civ);
        mParticipantPicAudioFlagCIV = (CircleImageView) findViewById(R.id.participant_pic_flag_audio_civ);
        mParticipantPicVideoCIV = (CircleImageView) findViewById(R.id.participant_pic_video_civ);
        mParticipantPicVideoFlagCIV = (CircleImageView) findViewById(R.id.participant_pic_flag_video_civ);
        mMainAudioRL = (RelativeLayout) findViewById(R.id.main_audio_rl);
        mMainVideoRL = (RelativeLayout) findViewById(R.id.main_video_rl);

        mIsVideoCall = getIntent().getBooleanExtra(Consts.INTENT_IS_VIDEO_CALL, false);
        mParticipantId = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_ID);
        mParticipantName = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
        mParticipantLang = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_LANG);
        mParticipantPic = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_PIC);
        mConversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);

//        mSelfId = getIntent().getStringExtra(Consts.INTENT_SELF_ID);
//        mSelfLang = getIntent().getStringExtra(Consts.INTENT_SELF_LANG);
//        mSelfName = getIntent().getStringExtra(Consts.INTENT_SELF_NAME);
//        mSelfPicUrl = getIntent().getStringExtra(Consts.INTENT_SELF_PIC_URL);

        //Set lang flag , language display name and pic
        mFlagDrawable = Utils.getCountryFlagDrawableFromLang(mParticipantLang);
        loc = new Locale(mParticipantLang);
        mFullLangName = loc.getDisplayLanguage();

        mBackNavigationFL.setOnClickListener(this);

        if (mIsVideoCall)
            videoCall();
        else
            audioCall();
    }

    private void videoCall() {
        mMainVideoRL.setVisibility(View.VISIBLE);

        mParticipantNameVideoTV.setText(mParticipantName);
        mParticipantLangVideoTV.setText(mFullLangName);

        //Set Participant Flags
        mParticipantPicVideoFlagCIV.setImageResource(mFlagDrawable);
        mMicFlagCIV.setImageResource(mFlagDrawable);

        //Set Participant Pic
        setPhotoByUrl(true);

        mTitleTV.setText(R.string.in_a_video_call);
    }

    private void audioCall() {
        mMainAudioRL.setVisibility(View.VISIBLE);

        mCameraSwitchIV.setVisibility(View.GONE); // No need camera switch button in audio call
        mParticipantNameAudioTV.setText(mParticipantName);
        mParticipantLangAudioTV.setText(mFullLangName);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mFixedParticipantId = PhoneNumberUtils.formatNumber(mParticipantId);
        } else {
            mFixedParticipantId = PhoneNumberUtils.formatNumber("+" + mParticipantId, mParticipantLangAudioTV.toString());
        }

        //Set Participant Flags
        mParticipantPicAudioFlagCIV.setImageResource(mFlagDrawable);
        mMicFlagCIV.setImageResource(mFlagDrawable);

        //Set Participant Pic
        setPhotoByUrl(false);
//        mParticipantPicAudioCIV.addShadow();

        mParticipantNumberTV.setText(mFixedParticipantId);
        mTitleTV.setText(R.string.in_a_voice_call);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_navigation_fl:
                finish();
                break;
        }
    }

    public void setPhotoByUrl(boolean videoCallFlag){
        if ((mParticipantPic != null) && (!mParticipantPic.trim().equals(""))) {
            if(videoCallFlag)
                  Picasso.get().load(mParticipantPic).into(mParticipantPicVideoCIV);
            else
                  Picasso.get().load(mParticipantPic).into(mParticipantPicAudioCIV);
        }else {
            if(videoCallFlag)
                 Picasso.get().load(R.drawable.ic_empty_contact).into(mParticipantPicVideoCIV);
            else
                 Picasso.get().load(R.drawable.ic_empty_contact).into(mParticipantPicAudioCIV);
        }
    }

    public void createSessionAndToken(){
         videoAudioCallViewModel = ViewModelProviders.of(this).get(VideoAudioCallViewModel.class);
         videoAudioCallViewModel.createSessionsAndToken("RELAYED");

         videoAudioCallViewModel.getVideoSessionResult().observe(this, new Observer<StateData<String>>() {
             @Override
             public void onChanged(@Nullable StateData<String> res) {
                 if(res.isSuccess())
                        Log.d("stringStateData", "stringStateData: " + res.isSuccess());
                 else if(res.getErrorLogic() != null)
                        Log.d("stringStateData", "notttttttttt: " + res.getErrorLogic());
                 else if(res.getErrorCom() != null)
                         Log.d("stringStateData", "notttttttttt: " + res.getErrorCom());
             }
         });
    }
}
