package io.slatch.app.ui.Contact;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ContactListHeaderNewContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private final ItemClickListener mItemClickListener;

	public interface ItemClickListener {
		void onItemClick();
	}
	public ContactListHeaderNewContactViewHolder(View itemView, ItemClickListener itemClickListener) {
		super(itemView);
//		mContactNameTV = itemView.findViewById(R.id.contact_name_tv);
		mItemClickListener = itemClickListener;
		itemView.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (mItemClickListener != null) mItemClickListener.onItemClick();
	}


}
