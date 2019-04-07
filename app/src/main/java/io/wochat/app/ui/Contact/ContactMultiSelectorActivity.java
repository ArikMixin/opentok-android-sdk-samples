package io.wochat.app.ui.Contact;

import android.app.Activity;
import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.wochat.app.R;
import io.wochat.app.components.CircleFlagImageView;
import io.wochat.app.db.entity.Contact;
import io.wochat.app.ui.Consts;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.ContactViewModel;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

//import io.wochat.app.db.entity.ContactInvitation;

public class ContactMultiSelectorActivity extends AppCompatActivity implements ContactMultiListAdapter.ContactSelectListener {

	public static final String SELECTED_CONTACTS_RESULT = "SELECTED_CONTACTS_RESULT";

	private static final int PICK_CONTACT_REQUEST = 1001;
	private static final int DISPLAY_CONTACTS_REQUEST = 1002;


	private RecyclerView mContactRecyclerView;
	private LinearLayoutManager mLayoutManager;
	private ContactMultiListAdapter mAdapter;
	private SearchView searchView;

	private ContactViewModel mCntactViewModel;
	private Map<String, Boolean> mContactInvitationMap;
	private ProgressBar mProgressBar;
	private boolean mIsForCall;
	private boolean mIsForChat;
	private RecyclerView mHeaderRecyclerView;
	private SelectedAdapter mHeaderAdapter;
	private View mHeaderLL;
	private FloatingActionButton mSendFab;
	private String mSelectedMessageIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_multi_selector);

		mIsForCall = getIntent().getBooleanExtra("CALL", false);
		mIsForChat = getIntent().getBooleanExtra("CHAT", false);


		mSelectedMessageIntent = getIntent().getStringExtra(Consts.INTENT_MESSAGE_OBJ);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mProgressBar =  findViewById(R.id.toolbar_progress_bar);
		mProgressBar.setVisibility(View.GONE);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.search_frwrd_to);

		mHeaderLL = findViewById(R.id.header_ll);
		mHeaderLL.setVisibility(View.INVISIBLE);

		mHeaderRecyclerView = findViewById(R.id.header_rv);
		LinearLayoutManager headerLayoutManager= new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		mHeaderRecyclerView.setLayoutManager(headerLayoutManager);
		mHeaderAdapter = new SelectedAdapter();
		mHeaderRecyclerView.setAdapter(mHeaderAdapter);


		mContactRecyclerView = findViewById(R.id.contact_list_rv);

		mLayoutManager = new LinearLayoutManager(this);
		mContactRecyclerView.setLayoutManager(mLayoutManager);

		// specify an adapter (see also next example)
		mAdapter = new ContactMultiListAdapter(this);
		mAdapter.setContactSelectListener(this);
		mContactRecyclerView.setAdapter(mAdapter);
		mContactRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

		VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller);
		fastScroller.setRecyclerView(mContactRecyclerView);

		mContactRecyclerView.addOnScrollListener(fastScroller.getOnScrollListener());

		mCntactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);

		mCntactViewModel.getServerContactsWithoutSelf().observe(this, contacts -> {
			mAdapter.setContacts(contacts);
		});

		mCntactViewModel.getIsDuringRefreshContacts().observe(this, isDuringRefresh -> {
			mProgressBar.setVisibility(Utils.booleanToVisibilityGone(isDuringRefresh));
		});


		mSendFab = findViewById(R.id.send_fab);
		mSendFab.setOnClickListener(v -> {
			returnSelectedContacts();
		});
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();


		if (id == R.id.action_search) {
			return true;
		}
		else if (id == R.id.action_contacts) {
			Intent intent = new Intent(Intent.ACTION_DEFAULT, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, DISPLAY_CONTACTS_REQUEST);
			return true;
		}
		else if (id == R.id.action_invite_friend) {
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_invite_friend_body));
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_invite_friend_title));
			sendIntent.setType("text/plain");
			startActivity(sendIntent);
			return true;
		}
		else if (id == R.id.action_refresh) {
			refresh();
			return true;
		}
		else if (id == android.R.id.home){
			returnContactResult(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void returnContactResult(Contact contact) {
		if (contact != null) {
			Intent resultIntent = new Intent();
			resultIntent.putExtra(Consts.INTENT_PARTICIPANT_ID, contact.getId());
			resultIntent.putExtra(Consts.INTENT_PARTICIPANT_NAME, contact.getDisplayName());
			resultIntent.putExtra(Consts.INTENT_PARTICIPANT_LANG, contact.getLanguage());
			resultIntent.putExtra(Consts.INTENT_PARTICIPANT_PIC, contact.getAvatar());
			resultIntent.putExtra(Consts.INTENT_PARTICIPANT_CONTACT_OBJ, contact.toJson());
			resultIntent.putExtra(Consts.INTENT_MESSAGE_OBJ, mSelectedMessageIntent);
			setResult(RESULT_OK, resultIntent);
		}
		else
			setResult(RESULT_CANCELED);
		finish();
		overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
	}

	@Override
	public void onBackPressed() {
		// close search view on back button pressed
		if (!searchView.isIconified()) {
			searchView.setIconified(true);
			return;
		}
		super.onBackPressed();
		overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_contact_selector, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setMaxWidth(Integer.MAX_VALUE);

		// listening to search query text change
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				// filter recycler view when query submitted
				mAdapter.getFilter().filter(query);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String query) {
				// filter recycler view when text is changed
				mAdapter.getFilter().filter(query);
				return false;
			}
		});

		return true;
	}

	private void whiteNotificationBar(View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int flags = view.getSystemUiVisibility();
			flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			view.setSystemUiVisibility(flags);
			getWindow().setStatusBarColor(Color.WHITE);
		}
	}




//	@Override
//	public void onInvitePressed(String contactId) {
//		//mCntactViewModel.updateInvited(contactId);
//		Utils.sendSMS(this, contactId, "Hey join me at WoChat http://wochat.io/");
//	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_CONTACT_REQUEST){
			if (resultCode == Activity.RESULT_OK){ // not working

			}
			else if (resultCode == Activity.RESULT_CANCELED){  // not working

			}
		}
		else if (requestCode == DISPLAY_CONTACTS_REQUEST){
			refresh();
		}
	}



	private void refresh(){
		ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
		contactViewModel.sycContacts();
	}



	private class SelectedAdapter extends RecyclerView.Adapter<SelectedAdapter.SelectedHolder> {

		private final HashMap<String, Contact> mSelectedMap;

		public class SelectedHolder extends RecyclerView.ViewHolder {
			private TextView mContactNameTV;
			public CircleFlagImageView mCircleFlagImageView;

			public SelectedHolder(View v) {
				super(v);
				mCircleFlagImageView = v.findViewById(R.id.contact_cfiv);
				mContactNameTV = v.findViewById(R.id.contact_name_tv);
			}
		}



		public SelectedAdapter(){
			super();
			mSelectedMap = new HashMap<String, Contact>();
		}

		@NonNull
		@Override
		public SelectedAdapter.SelectedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			// create a new view
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_multi_list_selected_item,
				parent, false);

			SelectedHolder vh = new SelectedHolder(v);
			return vh;
		}

		@Override
		public void onBindViewHolder(@NonNull SelectedAdapter.SelectedHolder holder, int position) {
			List<Contact> selectedContactList = new ArrayList<Contact>(mSelectedMap.values());
			Contact c = selectedContactList.get(position);
			holder.mCircleFlagImageView.setContact(c, false, true);
			holder.mCircleFlagImageView.setTag(c);
			holder.mContactNameTV.setText(c.getDisplayName());
			holder.mCircleFlagImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Contact contact = (Contact) v.getTag();
					removeContact(contact);
					mAdapter.unselectContact(contact);
				}
			});
		}

		@Override
		public int getItemCount() {
			return mSelectedMap.size();
		}


		public void addContact(Contact contact){
			mSelectedMap.put(contact.getContactId(), contact);
			notifyDataSetChanged();
		}

		public void removeContact(Contact contact){
			mSelectedMap.remove(contact.getContactId());
			notifyDataSetChanged();
		}

		public List<Contact> getSelectedContact(){
			return new ArrayList<Contact>(mSelectedMap.values());
		}

	}


	@Override
	public void onContactSelected(Contact contact) {
		mHeaderAdapter.addContact(contact);
		if (mHeaderAdapter.getItemCount() == 1){
			mHeaderLL.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onContactUnSelected(Contact contact) {
		mHeaderAdapter.removeContact(contact);
		if (mHeaderAdapter.getItemCount() == 0){
			mHeaderLL.setVisibility(View.INVISIBLE);
		}
	}


	private void returnSelectedContacts() {
		List<Contact> selectedContacts = mHeaderAdapter.getSelectedContact();
		if (selectedContacts.isEmpty()) {
			setResult(RESULT_CANCELED);
		}
		else {
			List<String> stringList = new ArrayList<>();
			for (Contact contact : selectedContacts){
				stringList.add(contact.getId());
			}
			String[] stringArray = stringList.toArray(new String[stringList.size()]);
			Intent intent = new Intent();
			intent.putExtra(SELECTED_CONTACTS_RESULT, stringArray);
			intent.putExtra(Consts.INTENT_MESSAGE_OBJ, mSelectedMessageIntent);
			setResult(RESULT_OK, intent);
		}
		finish();
		overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
	}

}
