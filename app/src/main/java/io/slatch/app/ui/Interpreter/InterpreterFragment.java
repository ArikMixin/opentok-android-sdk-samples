package io.slatch.app.ui.Interpreter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;

import io.slatch.app.R;
import io.slatch.app.db.WCSharedPreferences;
import io.slatch.app.ui.settings.SettingsActivity;


public class InterpreterFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

	private View view;
	private RelativeLayout mTopInsideRL;
	private RelativeLayout mBottomRL;
    private ImageView mRotationIV;
    private String selfLang;
    private InputStream mInputStream;
	private ImageView mTopIV;
	private ImageView mBottomIV;
	private TextView mTopTV;
	private TextView mBottomTV;
	private ImageView mMicTopIV;
	private ImageView mMicBottomIV;



	public static InterpreterFragment newInstance() { return new InterpreterFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.interpreter_fragment, container, false);
		setHasOptionsMenu(true);

		initView();
		return view;
	}

	private void initView() {
		mTopInsideRL = (RelativeLayout) view.findViewById(R.id.top_inside_rl);
		mBottomRL = (RelativeLayout) view.findViewById(R.id.bottom_rl);
		mTopIV = (ImageView) view.findViewById(R.id.top_iv);
		mBottomIV = (ImageView) view.findViewById(R.id.bottom_iv);
        mRotationIV = (ImageView) view.findViewById(R.id.rotation_iv);
		mTopTV = (TextView) view.findViewById(R.id.top_tv);
		mBottomTV = (TextView) view.findViewById(R.id.bottom_tv);
		mMicTopIV = (ImageView) view.findViewById(R.id.mic_top_iv);
		mMicBottomIV = (ImageView) view.findViewById(R.id.mic_bottom_iv);

		setUserLanguage();

		mRotationIV.setOnClickListener(this);
		mMicBottomIV.setOnTouchListener(this);
		mMicTopIV.setOnTouchListener(this);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main_activity3, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(getContext(), SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.rotation_iv:
 			         mTopInsideRL.setRotation(mTopInsideRL.getRotation()-180);
                break;
        }
    }


	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch(view.getId()){
			case R.id.mic_top_iv:
					onTouchActions(view, motionEvent);
				break;
			case R.id.mic_bottom_iv:
					onTouchActions(view, motionEvent); // Push to talk btn
				break;
		}
		return true;
	}

	private void  onTouchActions(View view, MotionEvent event){

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: //--Hold--
				view.setBackgroundResource(R.drawable.interpreter_active);
				break;

			case MotionEvent.ACTION_MOVE:
				break;

			case MotionEvent.ACTION_UP: //--Release--
				view.setBackgroundResource(R.drawable.interpreter_talk);
				break;
			default:
		}
	}

	private void setUserLanguage() {
		//Get users (Self) language
		 selfLang = WCSharedPreferences.getInstance(view.getContext()).getUserLang().toLowerCase();

		//Top language should be "French"
		//if users lang is French - change to top language to english
		 if(selfLang.equals("fr")) {
			 Picasso.get()
					 .load("file:///android_asset/interpreter_en.png")
					 .error(R.drawable.interpeter_general)
					 .into(mTopIV);
		 }else{
			Picasso.get()
					.load("file:///android_asset/interpreter_fr.png")
					.error(R.drawable.interpeter_general)
					.into(mTopIV);
		}

		//Bottom language should be Users languge
		Picasso.get()
					.load("file:///android_asset/interpreter_" + selfLang + ".png")
					.error(R.drawable.interpeter_general)
					.into(mBottomIV);


			return;


	}

}
