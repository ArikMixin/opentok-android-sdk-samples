package io.slatch.app.components;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.slatch.app.R;
import io.slatch.app.db.entity.Message;
import io.slatch.app.utils.Utils;

public class MessageReplyLayout extends LinearLayout {


	private ImageButton mCloseIB;
	private TextView mContactNameTV;
	private TextView mMessageTxtTV;
	private ImageView mMessageImageIV;
	private OnClickListener mOnCloseListener;
	private Message mMessage;
	private FrameLayout mMessageImageFL;
	private ImageView mMessageImageVideoIconIV;

	public MessageReplyLayout(Context context) {
		super(context);
		init(context);
	}

	public MessageReplyLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MessageReplyLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public MessageReplyLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}


	private void init(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.reply_layout, this);
		mContactNameTV = findViewById(R.id.rply_contact_name_tv);
		mMessageTxtTV = findViewById(R.id.rply_message_text_tv);
		mMessageImageIV = findViewById(R.id.rply_message_image_iv);
		mMessageImageVideoIconIV = findViewById(R.id.rply_message_image_video_icon_iv);
		mMessageImageFL = (FrameLayout)findViewById(R.id.rply_message_image_fl);
		mCloseIB = findViewById(R.id.rply_close_ib);
		mCloseIB.setOnClickListener(v -> {
			setVisibility(GONE);
			mOnCloseListener.onClick(v);
		});
	}



	public void setOnCloseListener(View.OnClickListener listener){
		mOnCloseListener = listener;
	}

	public void showCloseBtn(boolean isShow){
		mCloseIB.setVisibility(Utils.booleanToVisibilityGone(isShow));
	}

	public void showReplyMessage(Message message, String contactName){

		if (message == null) {
			setVisibility(GONE);
			return;
		}

		setVisibility(VISIBLE);


		mMessage = message;

		if (message.isOutgoing())
			mContactNameTV.setText(R.string.reply_contact_self_name);
		else
			mContactNameTV.setText(contactName);

		switch (message.getMessageType()){
			case Message.MSG_TYPE_TEXT:
				mMessageTxtTV.setText(message.getText());
				mMessageTxtTV.setCompoundDrawablesWithIntrinsicBounds(null , null, null, null);
				mMessageImageFL.setVisibility(GONE);
				setMinimumWidth(Utils.dp2px(getContext(), 50));
				break;
			case Message.MSG_TYPE_IMAGE:
				mMessageTxtTV.setText(R.string.reply_image_msg_text);
				mMessageTxtTV.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_camera) , null, null, null);
				mMessageImageFL.setVisibility(VISIBLE);
				mMessageImageVideoIconIV.setVisibility(INVISIBLE);
				try {
					Picasso.get().load(message.getImageThumbURL()).into(mMessageImageIV);
				} catch (Exception e) {
					e.printStackTrace();
				}
				setMinimumWidth(Utils.dp2px(getContext(), 250));
				break;
			case Message.MSG_TYPE_VIDEO:
				mMessageTxtTV.setText(String.format(getContext().getString(R.string.reply_video_msg_text), Utils.convertSecondsToHMmSs(message.getDurationMili())));
				mMessageTxtTV.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_video) , null, null, null);
				mMessageImageFL.setVisibility(VISIBLE);
				mMessageImageVideoIconIV.setVisibility(VISIBLE);
				try {
					Picasso.get().load(message.getImageThumbURL()).into(mMessageImageIV);
				} catch (Exception e) {
					e.printStackTrace();
				}
				setMinimumWidth(Utils.dp2px(getContext(), 250));
				break;
			case Message.MSG_TYPE_AUDIO:
			case Message.MSG_TYPE_SPEECHABLE:
				mMessageTxtTV.setText(String.format(getContext().getString(R.string.reply_audio_msg_text), Utils.convertSecondsToHMmSs(message.getDurationMili())));
				mMessageTxtTV.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.msg_in_mic_light) , null, null, null);
				mMessageImageFL.setVisibility(GONE);
				break;

			case Message.MSG_TYPE_LOCATION:
				mMessageTxtTV.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.msg_in_location) , null, null, null);
				mMessageTxtTV.setText(R.string.reply_location_msg_text);
				mMessageImageFL.setVisibility(GONE);
				break;

		}
	}


	public Message getMessage() {
		return mMessage;
	}
}
