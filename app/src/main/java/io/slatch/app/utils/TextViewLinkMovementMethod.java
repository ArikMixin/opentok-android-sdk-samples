package io.slatch.app.utils;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public class TextViewLinkMovementMethod extends LinkMovementMethod {
	private static TextViewLinkMovementMethod sInstance;

	public interface OnTouchListener {
		boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event);
	}

	public void setOnTouchListener(OnTouchListener onTouchListener) {
		mOnTouchListener = onTouchListener;
	}

	private OnTouchListener mOnTouchListener;

	public static TextViewLinkMovementMethod getInstance() {
		if (sInstance == null)
			sInstance = new TextViewLinkMovementMethod();
		return sInstance;
	}

	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
		boolean res = super.onTouchEvent(widget, buffer, event);
		if (mOnTouchListener != null)
			mOnTouchListener.onTouchEvent(widget, buffer, event);
		return res;
	}


}
