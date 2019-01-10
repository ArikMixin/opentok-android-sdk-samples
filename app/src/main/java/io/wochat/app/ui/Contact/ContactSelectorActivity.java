package io.wochat.app.ui.Contact;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Map;

import io.wochat.app.R;
import io.wochat.app.db.entity.Contact;
//import io.wochat.app.db.entity.ContactInvitation;
import io.wochat.app.utils.Utils;
import io.wochat.app.viewmodel.ContactViewModel;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class ContactSelectorActivity extends AppCompatActivity implements ContactListAdapter.ContactSelectListener {

	private static final int PICK_CONTACT_REQUEST = 1001;
	private static final int DISPLAY_CONTACTS_REQUEST = 1002;
	private RecyclerView mContactRecyclerView;
	private LinearLayoutManager mLayoutManager;
	private ContactListAdapter mAdapter;
	private SearchView searchView;

	public static final String INTENT_CONTACT_ID = "CONTACT_ID";
	public static final String INTENT_CONTACT_NAME = "CONTACT_NAME";
	private ContactViewModel mCntactViewModel;
	private Map<String, Boolean> mContactInvitationMap;
	private ProgressBar mProgressBar;
	private boolean mIsForCall;
	private boolean mIsForChat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_selector);

		mIsForCall = getIntent().getBooleanExtra("CALL", false);
		mIsForChat = getIntent().getBooleanExtra("CHAT", false);


		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mProgressBar =  (ProgressBar) findViewById(R.id.toolbar_progress_bar);
		mProgressBar.setVisibility(View.GONE);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("Select Contact");

		mContactRecyclerView = (RecyclerView)findViewById(R.id.contact_list_rv);
		//whiteNotificationBar(mCountryRecyclerView);

		mLayoutManager = new LinearLayoutManager(this);
		mContactRecyclerView.setLayoutManager(mLayoutManager);

		// specify an adapter (see also next example)
		mAdapter = new ContactListAdapter(this, mIsForChat);
		mAdapter.setContactSelectListener(this);
		mContactRecyclerView.setAdapter(mAdapter);
		mContactRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

		VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller);
		fastScroller.setRecyclerView(mContactRecyclerView);

		mContactRecyclerView.addOnScrollListener(fastScroller.getOnScrollListener());

		mCntactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
//		mCntactViewModel.getAllContacts().observe(this, contacts -> {
//			mAdapter.setContacts(contacts);
//		});
		mCntactViewModel.getServerContacts().observe(this, contacts -> {
			mAdapter.setContacts(contacts);
		});

		mCntactViewModel.getIsDuringRefreshContacts().observe(this, isDuringRefresh -> {
			mProgressBar.setVisibility(Utils.booleanToVisibilityGone(isDuringRefresh));
		});


//		mCntactViewModel.getContactInvitations().observe(this, contactInvitations -> {
//			mContactInvitationMap = ContactInvitation.getMap(contactInvitations);
//			mAdapter.setContactsInvitation(mContactInvitationMap);
//		});

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
			resultIntent.putExtra(INTENT_CONTACT_ID, contact.getId());
			resultIntent.putExtra(INTENT_CONTACT_NAME, contact.getContactLocal().getDisplayName());
			setResult(RESULT_OK, resultIntent);
		}
		else
			setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onBackPressed() {
		// close search view on back button pressed
		if (!searchView.isIconified()) {
			searchView.setIconified(true);
			return;
		}
		super.onBackPressed();
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

	@Override
	public void onContactSelected(Contact contact) {
		returnContactResult(contact);
	}

	@Override
	public void onNewContactSelected() {
		Intent i = new Intent(Intent.ACTION_INSERT);
		i.setType(ContactsContract.Contacts.CONTENT_TYPE);
		startActivityForResult(i, PICK_CONTACT_REQUEST);
	}

	@Override
	public void onNewGroupSelected() {

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
}
