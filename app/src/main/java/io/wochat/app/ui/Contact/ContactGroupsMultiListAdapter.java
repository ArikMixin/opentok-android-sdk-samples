package io.wochat.app.ui.Contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.stfalcon.chatkit.commons.models.IContact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.wochat.app.R;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.model.ContactOrGroup;
import io.wochat.app.utils.Utils;

public class ContactGroupsMultiListAdapter<CONTACT extends IContact> extends RecyclerView.Adapter  implements Filterable {

	private static final int TYPE_HEADER = 1;
	private static final int TYPE_MEMBER = 2;

	public class Wrapper<CONTACT> {
		public CONTACT contact;
		public boolean isSelected;
		public boolean isCanceled;

		Wrapper(CONTACT contact) {
			this.contact = contact;
		}
	}

	private HashMap<String, Wrapper> mWrapperMap;
	private HashMap<String, Wrapper> mWrapperMapFiltered;
	private List<Wrapper> mWrapperListFiltered;

	private LayoutInflater mInflater;
	private ContactSelectListener mContactSelectListener;


	public void setContactSelectListener(ContactSelectListener contactSelectListener) {
		mContactSelectListener = contactSelectListener;
	}


	public interface ContactSelectListener {
		void onContactSelected(ContactOrGroup contactOrGroup);
		void onContactUnSelected(ContactOrGroup contactOrGroup);
	}


	public ContactGroupsMultiListAdapter(Context context){
		this.mInflater = LayoutInflater.from(context);
		mWrapperMap = new HashMap<>();
		//mWrapperMapFiltered = new HashMap<>();
		mWrapperListFiltered = new ArrayList<>();
	}


	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = null;
		if (viewType == TYPE_MEMBER) {
			view = mInflater.inflate(R.layout.contact_multi_list_item, parent, false);
			return new ContactMultiListViewHolder(view, new ContactMultiListViewHolder.ItemClickListener() {
				@Override
				public void onItemClick(View view, int position) {
					Wrapper wrapper = mWrapperListFiltered.get(position);
					wrapper.isSelected = !wrapper.isSelected;
					if (mContactSelectListener != null) {
						if (wrapper.isSelected)
							mContactSelectListener.onContactSelected((ContactOrGroup) wrapper.contact);
						else
							mContactSelectListener.onContactUnSelected((ContactOrGroup) wrapper.contact);
					}
					notifyDataSetChanged();
				}
			});
		}
//		else if (viewType == TYPE_HEADER) {
//			view = mInflater.inflate(R.layout.contact_multi_list_header, parent, false);
//
//			return new ContactMultiListHeaderViewHolder(view, new ContactMultiListHeaderViewHolder.ItemClickListener() {
//				@Override
//				public void onItemClick(View view, int position) {
//					if (mContactSelectListener != null) {
//						mContactSelectListener.onNewContactSelected();
//					}
//
//				}
//			});
//		}

		else
			return null;

	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (holder.getItemViewType() == TYPE_MEMBER) {
			ContactMultiListViewHolder theHolder = (ContactMultiListViewHolder) holder;

			Wrapper item = mWrapperListFiltered.get(position);

			ContactOrGroup contactOrGroup = (ContactOrGroup) item.contact;
			theHolder.mContactNameTV.setText(contactOrGroup.getDisplayName());
			String status = contactOrGroup.getStatus();
			if (Utils.isNullOrEmpty(status)) {
				theHolder.mContactStatusTV.setVisibility(View.GONE);
			}
			else {
				theHolder.mContactStatusTV.setVisibility(View.VISIBLE);
				theHolder.mContactStatusTV.setText(status);
			}
			//Uri uri = ContactsUtil.getThumbPhoto(mInflater.getContext(), Long.valueOf(contact.getContactLocal().getOSId()));
			String picUrl = contactOrGroup.getAvatar();
			theHolder.mContactPicFlagCFIV.setContactOrGroup(contactOrGroup, item.isSelected, item.isCanceled);



		}


	}

	@Override
	public int getItemCount() {
		return mWrapperListFiltered.size();
	}

	public void unselectContact(CONTACT contact) {
		Wrapper wrapper = mWrapperMap.get(contact.getId());
		wrapper.isSelected = false;
		wrapper = mWrapperMapFiltered.get(contact.getId());
		wrapper.isSelected = false;
		mWrapperListFiltered = new ArrayList<Wrapper>(mWrapperMapFiltered.values());
		notifyDataSetChanged();
	}

	public void selectContact(CONTACT contact) {
		Wrapper wrapper = mWrapperMap.get(contact.getId());
		wrapper.isSelected = true;
		wrapper = mWrapperMapFiltered.get(contact.getId());
		wrapper.isSelected = true;
		mWrapperListFiltered = new ArrayList<Wrapper>(mWrapperMapFiltered.values());
		notifyDataSetChanged();
	}


	public void setGroups(List<CONTACT> groups) {

		if (mWrapperMap.size() == 0){
			for (CONTACT group :  groups){
				mWrapperMap.put(group.getId(), new Wrapper(group));
			}

		}
		else {
			for (CONTACT group :  groups){
				Wrapper wrapper = mWrapperMap.get(group.getId());
				if (wrapper == null)
					mWrapperMap.put(group.getId(), new Wrapper(group));
				else
					wrapper.contact = group;
			}

		}
		mWrapperMapFiltered = (HashMap<String, Wrapper>) mWrapperMap.clone();
		mWrapperListFiltered = new ArrayList<Wrapper>(mWrapperMapFiltered.values());
		notifyDataSetChanged();
	}


	public void setContacts(List<CONTACT> contacts) {

		if (mWrapperMap.size() == 0){
			for (CONTACT contact :  contacts){
				mWrapperMap.put(contact.getId(), new Wrapper(contact));
			}

		}
		else {
			for (CONTACT contact :  contacts){
				Wrapper wrapper = mWrapperMap.get(contact.getId());
				if (wrapper == null)
					mWrapperMap.put(contact.getId(), new Wrapper(contact));
				else
					wrapper.contact = contact;
			}

		}
		mWrapperMapFiltered = (HashMap<String, Wrapper>) mWrapperMap.clone();
		mWrapperListFiltered = new ArrayList<Wrapper>(mWrapperMapFiltered.values());
		notifyDataSetChanged();
	}





	@Override
	public int getItemViewType(int position) {
		return  TYPE_MEMBER;
	}


	@Override
	public Filter getFilter() {
		return new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				String charString = constraint.toString();
				if (charString.isEmpty()) {
					mWrapperMapFiltered = (HashMap<String, Wrapper>) mWrapperMap.clone();
					mWrapperListFiltered = new ArrayList<Wrapper>(mWrapperMapFiltered.values());
				} else {
					List<Wrapper> filteredList = new ArrayList<>();
					mWrapperMapFiltered.clear();
					for (Wrapper wrapper : mWrapperMap.values()) {
						// name match condition. this might differ depending on your requirement
						// here we are looking for name or phone number match
						String name = ((IContact)wrapper.contact).getDisplayName().toLowerCase();
						if (name.contains(charString.toLowerCase()) || name.contains(constraint)) {
							mWrapperMapFiltered.put(((IContact) wrapper.contact).getId(), wrapper);
							filteredList.add(wrapper);
						}
					}

					mWrapperListFiltered = filteredList;
				}

				FilterResults filterResults = new FilterResults();
				filterResults.values = mWrapperListFiltered;
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults filterResults) {
				mWrapperListFiltered = (ArrayList<Wrapper>) filterResults.values;
				notifyDataSetChanged();
			}
		};
	}





}
