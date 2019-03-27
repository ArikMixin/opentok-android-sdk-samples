package io.wochat.app.ui.ContactInfo;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.wochat.app.R;
import io.wochat.app.db.entity.Message;
import io.wochat.app.ui.Consts;
import io.wochat.app.utils.ImagePickerUtil;
import io.wochat.app.viewmodel.ConversationViewModel;

public class ContactInfoMediaActivity extends AppCompatActivity implements MediaAdapter.AdapterListener {

	private MediaAdapter mMediaAdapter;
	private ConversationViewModel mConversationViewModel;
	private ImageView mPhotoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_info_media);


		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		mPhotoView = findViewById(R.id.media_pv);
		mPhotoView.setOnClickListener(v -> v.setVisibility(View.INVISIBLE));


		RecyclerView recyclerView = findViewById(R.id.media_grid_rv);
		GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
		recyclerView.setLayoutManager(gridLayoutManager);

		mMediaAdapter = new MediaAdapter(getDisplayMetrics(), this);
		recyclerView.setAdapter(mMediaAdapter);

		//view model
		mConversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel.class);
		String conversationId = getIntent().getStringExtra(Consts.INTENT_CONVERSATION_ID);
		String participantName = getIntent().getStringExtra(Consts.INTENT_PARTICIPANT_NAME);
		getSupportActionBar().setTitle("Media for " + participantName);

		mConversationViewModel.getMediaMessagesConversation(conversationId).observe(this,
			messages -> mMediaAdapter.setMessages(messages));

	}



	public DisplayMetrics getDisplayMetrics() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}

	@Override
	public void onImageClicked(String url) {
		mPhotoView.setVisibility(View.VISIBLE);
		Picasso.get().load(url).into(mPhotoView);
	}

	@Override
	public void onVideoClicked(Message message) {
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());

		//try locally
		Log.e("MediaFragment", "play file check local: " + message.getMediaLocalUri());
		if ((message.getMediaLocalUri() != null) && (!message.getMediaLocalUri().equals(""))) {
			Uri uri = message.getMediaLocalParseUri();
			Log.e("MediaFragment", "play file local uri: " + uri);
			String s = ImagePickerUtil.getFilePathFromContentUriNoStreaming(this, uri);
			Log.e("MediaFragment", "play file local file: " + s);
			if (s != null) {
				Log.e("MediaFragment", "play file local file exist: " + s);

				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setDataAndType(uri, "video/mp4");
				startActivity(intent);
				return;
			}
		}

		// play remotely
		Uri uri = Uri.parse(message.getMediaUrl());
		Log.e("MediaFragment", "play file remotely: " + uri);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setDataAndType(uri, "video/mp4");
		startActivity(intent);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_info, menu);
		return true;
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				finish();
				break;
		}

		return super.onOptionsItemSelected(item);
	}


}
