package io.wochat.app.ui.RecentCalls;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import java.util.Date;
import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Call;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.ui.AudioVideoCall.CallActivity;
import io.wochat.app.utils.DateFormatter;


/*
 * Created by Anton Bevza on 1/18/17.
 */
public class RecentCallsViewHolder
        extends DialogsListAdapter.DialogViewHolder<Call> implements View.OnClickListener {

    private final ImageView mCocheIV;
	private final CircleFlagImageView mAvatarcfiv;
	private final ImageView mMsgTypeIV;
	private final ImageButton mCameraIB, mPhoneIB;
	private final TextView mCallStatusTV, mCallDateTV;
	private Call mCall;
	private String mCallType;
	private View itemView;
	private Date dateD;

	public static final int BTN_CAMERA = 1;
	public static final int BTN_PHONE = 2;

    public RecentCallsViewHolder(View itemView) {
        super(itemView);
        //onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
		mCocheIV = (ImageView)itemView.findViewById(R.id.dialogCocheIV);
		mMsgTypeIV = (ImageView)itemView.findViewById(R.id.dialogMsgTypeIV);
		mAvatarcfiv = (CircleFlagImageView) itemView.findViewById(R.id.dialogAvatar);
		mCameraIB = (ImageButton)itemView.findViewById(R.id.camera_ib);
		mPhoneIB = (ImageButton)itemView.findViewById(R.id.phone_ib);
		mCallStatusTV = (TextView)itemView.findViewById(R.id.call_tatus_tv);
		mCallDateTV = (TextView)itemView.findViewById(R.id.call_date_tv);

		mCameraIB.setOnClickListener(this);
		mPhoneIB.setOnClickListener(this);

		this.itemView = itemView;
	}

	@SuppressLint("SetTextI18n")
	@Override
    public void onBind(Call call) {
        super.onBind(call);

		mCameraIB.setTag(call);
		mPhoneIB.setTag(call);

		mAvatarcfiv.setInfo(call.getParticipantProfilePicUrl(),
			call.getParticipantLanguage(),
			Contact.getInitialsFromName(call.getParticipantName()));

		if(!call.isVideoCall()) {
                mMsgTypeIV.setImageResource(R.drawable.phone_grey);
                mCallType = itemView.getContext().getString(R.string.audio);
		}else{
		    	mCallType = itemView.getContext().getString(R.string.video);
		}

		//Call Status (Missed OutGoing Incoming)
		if(call.getCallState().equals(CallActivity.CALL_INCOMING)) {
            mCallStatusTV.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.font_gray_1));
            mCallStatusTV.setText(itemView.getContext().getString(R.string.incoming) + " "
                    + mCallType + " " + itemView.getContext().getString(R.string.call));
        }else if (call.getCallState().equals(CallActivity.CALL_OUTGOING)) {
            mCallStatusTV.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.font_gray_1));
            mCallStatusTV.setText(itemView.getContext().getString(R.string.outgoing) + " "
                    + mCallType + " " + itemView.getContext().getString(R.string.call));
        }else if(call.getCallState().equals(CallActivity.CALL_MISSED)) {
			mCallStatusTV.setTextColor(Color.RED);
			mCallStatusTV.setText(itemView.getContext().getString(R.string.missed) + " "
					+ mCallType  + " " +  itemView.getContext().getString(R.string.call));
		}

		dateD = new Date(call.getCallStartTimeStamp());
		mCallDateTV.setText(DateFormatter.format(dateD, DateFormatter.Template.TIME));
    }

	@Override
	public void onClick(View v) {

		 mCall = (Call) v.getTag();

		switch(v.getId()){
			case R.id.camera_ib:
					onButtonClickListener.onButtonClick(mCall, BTN_CAMERA);
				break;
			case R.id.phone_ib:
					onButtonClickListener.onButtonClick(mCall, BTN_PHONE);
				break;
		}
	}
}
