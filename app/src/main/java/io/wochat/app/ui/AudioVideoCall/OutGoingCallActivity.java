package io.wochat.app.ui.AudioVideoCall;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import io.wochat.app.R;
import io.wochat.app.components.CircleImageView;
import io.wochat.app.ui.Consts;
import io.wochat.app.utils.Utils;

public class OutGoingCallActivity extends AppCompatActivity {

    private CircleImageView mMicFlagCIV;
    private TextView mTitleTV;
    private boolean mIsVideoCall;
    private String mParticipantId, mParticipantName, mParticipantLang, mParticipantPic, mConversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls);

        initViews();
    }

    private void initViews() {

        mMicFlagCIV = (CircleImageView) findViewById(R.id.mic_flag_civ);
        mTitleTV = (TextView) findViewById(R.id.title_tv);

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

      if(mIsVideoCall)
             videoCall();
      else
             audioCall();
    }

    private void videoCall(){
        mTitleTV.setText(R.string.in_a_video_call);
    }

    private void audioCall(){

        int flagDrawable = Utils.getCountryFlagDrawableFromLang(mParticipantLang);

         mTitleTV.setText(R.string.in_a_voice_call);
         mMicFlagCIV.setImageResource(flagDrawable);
    }

}
