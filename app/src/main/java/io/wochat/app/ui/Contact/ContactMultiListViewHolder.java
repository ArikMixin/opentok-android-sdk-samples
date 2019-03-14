package io.wochat.app.ui.Contact;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;

public class ContactMultiListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private final ItemClickListener mItemClickListener;
	final TextView mContactNameTV;
	final TextView mContactStatusTV;
	final CircleFlagImageView mContactPicFlagCFIV;
	private boolean mIsChecked;
	private boolean mIsCanceled;

	public interface ItemClickListener {
		void onItemClick(View view, int position);
	}
	public ContactMultiListViewHolder(View itemView, ItemClickListener itemClickListener) {
		super(itemView);
		mContactNameTV = itemView.findViewById(R.id.contact_name_tv);
		mContactStatusTV = itemView.findViewById(R.id.contact_status_tv);

		mContactPicFlagCFIV = (CircleFlagImageView)itemView.findViewById(R.id.contact_cfiv);
		mItemClickListener = itemClickListener;
		itemView.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (mItemClickListener != null)
			mItemClickListener.onItemClick(view, getAdapterPosition());
	}


	public void setChecked(boolean isChecked){
		mIsCanceled = false;
		mIsChecked = isChecked;
		mContactPicFlagCFIV.displayChecked(isChecked);
	}

	public void setCanceled(boolean isCanceled){
		mIsChecked = false;
		mIsCanceled = isCanceled;
		mContactPicFlagCFIV.displayCanceled(isCanceled);
	}


}
