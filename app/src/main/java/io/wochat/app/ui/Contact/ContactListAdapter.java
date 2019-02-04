package io.wochat.app.ui.Contact;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.wochat.app.R;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.utils.ContactsUtil;

public class ContactListAdapter extends RecyclerView.Adapter  implements Filterable {

	private static final int TYPE_HEADER_NEW_CONTACT = 1;
	private static final int TYPE_HEADER_NEW_GROUP = 2;
	private static final int TYPE_MEMBER = 3;
	private final boolean mHasNewGroupHeader;
	private final int mNumHeaders;

	private List<Contact> mContactList;
	private List<Contact> mContactListFiltered;
	private LayoutInflater mInflater;
	private ContactSelectListener mContactSelectListener;
	//private Map<String, Boolean> mContactInvitationMap;

	public void setContactSelectListener(ContactSelectListener contactSelectListener) {
		mContactSelectListener = contactSelectListener;
	}


	public interface ContactSelectListener {
		void onContactSelected(Contact contact);
		void onNewContactSelected();
		void onNewGroupSelected();
		//void onInvitePressed(String contactId);
	}


	public ContactListAdapter(Context context, boolean hasNewGroupHeader){
		this.mInflater = LayoutInflater.from(context);
		this.mHasNewGroupHeader = hasNewGroupHeader;
		mNumHeaders = mHasNewGroupHeader? 2 : 1;
	}


	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = null;
		if (viewType == TYPE_MEMBER) {
			view = mInflater.inflate(R.layout.contact_list_item, parent, false);
			return new ContactListViewHolder(view, new ContactListViewHolder.ItemClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					if (mContactSelectListener != null) {
						mContactSelectListener.onContactSelected(mContactListFiltered.get(position-mNumHeaders));
					}
				}
			});
		}
		else if (viewType == TYPE_HEADER_NEW_CONTACT) {
			view = mInflater.inflate(R.layout.contact_list_new_contact_header, parent, false);
			return new ContactListHeaderNewContactViewHolder(view, new ContactListHeaderNewContactViewHolder.ItemClickListener() {
				@Override
				public void onItemClick() {
					if (mContactSelectListener != null) {
						mContactSelectListener.onNewContactSelected();
					}
				}
			});
		}
		else if (viewType == TYPE_HEADER_NEW_GROUP) {
			view = mInflater.inflate(R.layout.contact_list_new_group_header, parent, false);
			return new ContactListHeaderNewGroupViewHolder(view, new ContactListHeaderNewGroupViewHolder.ItemClickListener() {
				@Override
				public void onItemClick() {
					if (mContactSelectListener != null) {
						mContactSelectListener.onNewGroupSelected();
					}
				}
			});
		}
		else
			return null;

	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (holder.getItemViewType() == TYPE_MEMBER) {
			ContactListViewHolder theHolder = (ContactListViewHolder) holder;
			Contact contact = mContactListFiltered.get(position-mNumHeaders);
			theHolder.mContactNameTV.setText(contact.getContactLocal().getDisplayName());
			String status = contact.getContactServer().getStatus();
			if ((status == null) || (status.trim().equals(""))) {
				theHolder.mContactStatusTV.setVisibility(View.GONE);
			}
			else {
				theHolder.mContactStatusTV.setVisibility(View.VISIBLE);
				theHolder.mContactStatusTV.setText(contact.getContactServer().getStatus().trim());
			}
			//Uri uri = ContactsUtil.getThumbPhoto(mInflater.getContext(), Long.valueOf(contact.getContactLocal().getOSId()));
			if (contact.hasServerData()){
				String picUrl = contact.getContactServer().getProfilePicUrl();
				theHolder.mContactPicFlagCFIV.setContact(contact);
			}


		}
//		else if (holder.getItemViewType() == TYPE_HEADER_NEW_CONTACT) {
//			ContactListHeaderNewContactViewHolder theHolder = (ContactListHeaderNewContactViewHolder) holder;
//			Contact contact = mContactListWithHeadersFiltered.get(position);
//			theHolder.mContactNameTV.setText(String.valueOf(contact.getHeader()));
//		}
//		else if (holder.getItemViewType() == TYPE_HEADER_NEW_GROUP) {
//			ContactListHeaderNewGroupViewHolder theHolder = (ContactListHeaderNewGroupViewHolder) holder;
//			Contact contact = mContactListWithHeadersFiltered.get(position);
//			theHolder.mContactNameTV.setText(String.valueOf(contact.getHeader()));
//		}

	}

	@Override
	public int getItemCount() {

		//return (mContactListWithHeaders == null)? 0 : mContactListWithHeaders.size();

		return (mContactListFiltered== null)? mNumHeaders : mContactListFiltered.size()+mNumHeaders;
	}









//	Contact getItem(int id) {
//		return mContactListFiltered.get(id);
//	}


	public void setContacts(List<Contact> contacts) {
		mContactList = contacts;
		//mContactList = addAlphabetsHeaders(contacts);
		mContactListFiltered = mContactList;
		notifyDataSetChanged();
	}


//	public void setContactsInvitation(Map<String,Boolean> contactInvitationMap) {
//		mContactInvitationMap = contactInvitationMap;
//		notifyDataSetChanged();
//	}
//
//	private boolean isInvitationSent(String contactId){
//		return ((mContactInvitationMap != null) && (mContactInvitationMap.containsKey(contactId)));
//	}



	@Override
	public int getItemViewType(int position) {
		int viewType;
		if (position == 0)
			viewType = TYPE_HEADER_NEW_CONTACT;
		else if ((position == 1) && mHasNewGroupHeader)
			viewType = TYPE_HEADER_NEW_GROUP;
		else
			viewType = TYPE_MEMBER;

		return viewType;
	}

//	List<Contact> addAlphabetsHeaders(List<Contact> list) {
//		int i = 0;
//		ArrayList<Contact> customList = new ArrayList<Contact>();
//		Contact firstContact = new Contact();
//		firstContact.setHeader(list.get(0).getName().charAt(0));
//		customList.add(firstContact);
//		for (i = 0; i < list.size() - 1; i++) {
//			char name1 = list.get(i).getName().charAt(0);
//			char name2 = list.get(i + 1).getName().charAt(0);
//			if (name1 == name2) {
//				customList.add(list.get(i));
//			}
//			else {
//				Contact contact = new Contact();
//				customList.add(list.get(i));
//				Contact.setHeader(name2);
//				customList.add(Contact);
//			}
//		}
//		customList.add(list.get(i));
//		return customList;
//	}


	// https://www.androidhive.info/2017/11/android-recyclerview-with-search-filter-functionality/


	@Override
	public Filter getFilter() {
		return new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				String charString = constraint.toString();
				if (charString.isEmpty()) {
					mContactListFiltered = mContactList;
				} else {
					List<Contact> filteredList = new ArrayList<>();
					for (Contact contact : mContactList) {

						// name match condition. this might differ depending on your requirement
						// here we are looking for name or phone number match
						if (contact.getDisplayName().toLowerCase().contains(charString.toLowerCase()) || contact.getDisplayName().toLowerCase().contains(constraint)) {
							filteredList.add(contact);
						}
					}

					mContactListFiltered = filteredList;
				}

				FilterResults filterResults = new FilterResults();
				filterResults.values = mContactListFiltered;
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults filterResults) {
				mContactListFiltered = (ArrayList<Contact>) filterResults.values;
				notifyDataSetChanged();
			}
		};
	}
}
