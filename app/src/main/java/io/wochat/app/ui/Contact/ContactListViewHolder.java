package io.wochat.app.ui.Contact;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import io.wochat.app.R;
import io.wochat.app.components.CircleImageView;

public class ContactListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private final ItemClickListener mItemClickListener;
	final Button mInviteBtn;
	final Button mSentBtn;
	final TextView mContactNameTV;
	final CircleImageView mContactPicIV;

	public interface ItemClickListener {
		void onItemClick(View view, int position);
	}
	public ContactListViewHolder(View itemView, ItemClickListener itemClickListener) {
		super(itemView);
		mContactNameTV = itemView.findViewById(R.id.contact_name_tv);
		mContactPicIV = (CircleImageView)itemView.findViewById(R.id.contact_iv);
		mInviteBtn = (Button)itemView.findViewById(R.id.invite_btn);
		mSentBtn = (Button)itemView.findViewById(R.id.sent_btn);
		mItemClickListener = itemClickListener;
		itemView.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (mItemClickListener != null) mItemClickListener.onItemClick(view, getAdapterPosition());
	}


}
