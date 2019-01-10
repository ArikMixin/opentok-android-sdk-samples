package io.wochat.app.ui.Contact;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.components.CircleImageView;

public class ContactListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private final ItemClickListener mItemClickListener;
	final TextView mContactNameTV;
	final TextView mContactStatusTV;
	//final CircleImageView mContactPicIV;
	final CircleFlagImageView mContactPicFlagCFIV;

	public interface ItemClickListener {
		void onItemClick(View view, int position);
	}
	public ContactListViewHolder(View itemView, ItemClickListener itemClickListener) {
		super(itemView);
		mContactNameTV = itemView.findViewById(R.id.contact_name_tv);
		mContactStatusTV = itemView.findViewById(R.id.contact_status_tv);

		mContactPicFlagCFIV = (CircleFlagImageView)itemView.findViewById(R.id.contact_cfiv);
		mItemClickListener = itemClickListener;
		itemView.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (mItemClickListener != null) mItemClickListener.onItemClick(view, getAdapterPosition());
	}


}
