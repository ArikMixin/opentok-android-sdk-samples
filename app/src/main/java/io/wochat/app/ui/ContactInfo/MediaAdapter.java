package io.wochat.app.ui.ContactInfo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.db.entity.Message;
import io.wochat.app.utils.Utils;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ImageViewHolder> {


	private final DisplayMetrics mDisplayMetrics;
	private List<Message> mMessages = new ArrayList<>();
	private AdapterListener mAdapterListener;

	public MediaAdapter(DisplayMetrics displayMetrics, AdapterListener adapterListener) {
		mAdapterListener = adapterListener;
		mDisplayMetrics = displayMetrics;
	}

	@NonNull
	@Override
	public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_grid, parent, false);
		return new ImageViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
		int deviceWidth = mDisplayMetrics.widthPixels / 3;//"3" is number of spans in grid row
		holder.mediaIV.getLayoutParams().width = deviceWidth;
		holder.mediaIV.getLayoutParams().height = deviceWidth;
		Picasso.get().load(mMessages.get(position).getMediaThumbnailUrl()).into(holder.mediaIV);

		if (mMessages.get(position).getMessageType().equals(Message.MSG_TYPE_VIDEO)) {
			holder.playIV.setVisibility(View.VISIBLE);
			holder.durationTV.setVisibility(View.VISIBLE);
			holder.durationTV.setText(Utils.convertSecondsToHMmSs(mMessages.get(position).getDuration()));
		}
	}

	@Override
	public int getItemCount() {
		return mMessages.size();
	}

	class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		ImageView mediaIV, playIV;
		TextView durationTV;

		public ImageViewHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			mediaIV = itemView.findViewById(R.id.media_iv);
			playIV = itemView.findViewById(R.id.media_play_iv);
			durationTV = itemView.findViewById(R.id.media_video_duration_tv);
		}

		@Override
		public void onClick(View v) {
			if (mMessages.get(getAdapterPosition()).getMessageType().equals(Message.MSG_TYPE_VIDEO)) {
				mAdapterListener.onVideoClicked(mMessages.get(getAdapterPosition()));
			}
			else if (mMessages.get(getAdapterPosition()).getMessageType().equals(Message.MSG_TYPE_IMAGE)) {
				mAdapterListener.onImageClicked(mMessages.get(getAdapterPosition()).getMediaThumbnailUrl());
			}
		}
	}

	public void setMessages(List<Message> messages) {
		mMessages = messages;
		notifyDataSetChanged();
	}

	interface AdapterListener {
		void onImageClicked(String url);
		void onVideoClicked(Message message);
	}

}
