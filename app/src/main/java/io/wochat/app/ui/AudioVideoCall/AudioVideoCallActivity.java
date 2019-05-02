package io.wochat.app.ui.AudioVideoCall;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import io.wochat.app.R;
import io.wochat.app.ui.Consts;

public class AudioVideoCallActivity extends AppCompatActivity {

    private Intent intent;
    private boolean mIsVideoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls);

        initViews();
    }

    private void initViews() {

        mIsVideoCall = getIntent().getBooleanExtra(Consts.INTENT_IS_VIDEO_CALL, false);

      if(mIsVideoCall)
          Toast.makeText(this, "This is video", Toast.LENGTH_LONG).show();
      else
          Toast.makeText(this, "This is audio", Toast.LENGTH_LONG).show();

    }
}
