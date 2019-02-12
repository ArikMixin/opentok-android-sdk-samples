package io.wochat.app;

import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.AbstractPresenceEventListener;
import org.jivesoftware.smack.roster.AbstractRosterListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.List;

class XMPPProvider {


	private static final String TAG = "XMPPProvider";

	private static final String XMPP_DOMAIN = "ejabberd-dev.wochat.io";
	//private static final String XMPP_DOMAIN = "ejabberd.wochat.io";
	private static final String XMPP_RESOURCE = "android";
	private static final int XMPP_PORT = 5222;

//	private static final String XMPP_DOMAIN = "ejabberd.wochat.io";
//	private static final String XMPP_RESOURCE = "ejabberd.wochat.io";
//	private static final int XMPP_PORT = 5222;


//	private static final String XMPP_DOMAIN = "dev-ejabberd.hitshot.io";
//	private static final String XMPP_RESOURCE = "dev-ejabberd.hitshot.io";
//	private static final int XMPP_PORT = 5280;



	private static XMPPProvider mInstance;
	private final AppExecutors mExecutors;
	private XMPPTCPConnection mConnection;
	private String mUsername;
	private String mPassword;
	private String mUserJID;
	//private String mUserJIDAndroid;
	private ChatManager mChatManager;
	private OnChatMessageListener mOnChatMessageListener;
	private LastActivityManager mLastActivityManager;

	public interface OnChatMessageListener {
		void onNewIncomingMessage(String msg, String conversationId);
		void onNewOutgoingMessage(String msg, String conversationId);
		void onConnectionChange(boolean connected, boolean authenticated);
		void onPresenceChanged(boolean isAvailable, String contactId);
	}

	public void setOnChatMessageListener(OnChatMessageListener listener) {
		mOnChatMessageListener = listener;
	}

	public static XMPPProvider getInstance(WCApplication application, final AppExecutors executors) {
		if (mInstance == null) {
			synchronized (XMPPProvider.class) {
				if (mInstance == null) {
					mInstance = new XMPPProvider(application, executors);
				}
			}
		}
		return mInstance;
	}


	public XMPPProvider(WCApplication application, final AppExecutors executors) {
		mExecutors = executors;
	}

	private void init() {
		Log.e(TAG, "init called");

		SmackConfiguration.DEBUG = true;


		XMPPTCPConnectionConfiguration.Builder builder = null;
		try {
			builder = XMPPTCPConnectionConfiguration.builder().
				setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).
				setXmppDomain(XMPP_DOMAIN).
				setHostAddress(InetAddress.getByName(XMPP_DOMAIN)).
				setPort(XMPP_PORT);

		} catch (Exception e) {
			e.printStackTrace();
		}
		XMPPTCPConnectionConfiguration connectionConfiguration = builder.build();

		mConnection = new XMPPTCPConnection(connectionConfiguration);
		mConnection.addConnectionListener(mConnectionListener);
		mConnection.setUseStreamManagement(true);

		mChatManager = ChatManager.getInstanceFor(mConnection);
		mChatManager.addIncomingListener(mIncomingChatMessageListener);
		mChatManager.addOutgoingListener(mOutgoingChatMessageListener);

		mLastActivityManager = LastActivityManager.getInstanceFor(mConnection);
		mLastActivityManager.enable();



		boolean res = connectAndLogin();
		Log.e(TAG, "connectAndLogin result: " + res);


	}

	public void connectAndLoginAsync(){
		mExecutors.networkIO().execute(new Runnable() {
			@Override
			public void run() {
				boolean res = connectAndLogin();
				Log.e(TAG, "connectAndLogin result: " + res);
			}
		});
	}

	public void disconnectAsync(){
		mExecutors.networkIO().execute(new Runnable() {
			@Override
			public void run() {
				if (mConnection != null)
					mConnection.disconnect();
				Log.e(TAG, "mConnection.disconnect");
			}
		});
	}

	private void sendSelfPresence(){
		Roster roster = Roster.getInstanceFor(mConnection);
		roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
		roster.addRosterListener(mRosterListener);

		BareJid jid = null;
		try {
			jid = JidCreate.bareFrom(mUserJID);
			RosterEntry entry = roster.getEntry(jid);
			if (entry == null) {
				roster.createEntry(jid, mUsername, null);
				Log.e(TAG, "sendSelfPresence self entry was created");
			}

			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus("Online!");
			presence.setPriority(1);

			mConnection.sendStanza(presence);
			Log.e(TAG, "sendSelfPresence");

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private boolean connectAndLogin(){
		Log.e(TAG, "connectAndLogin called");
		try {
			if (mConnection.isConnected()) {
				if (mConnection.isAuthenticated()){
					sendSelfPresence();
					return true; // all is ok
				}
				else { // not log in
					Log.e(TAG, "login called Username: " + mUsername + " , Password: " + mPassword );
					mConnection.login(mUsername, mPassword, Resourcepart.from(XMPP_RESOURCE));

					if (mConnection.isAuthenticated()) {
						sendSelfPresence();
//						Roster roster = Roster.getInstanceFor(mConnection);
//						roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
//						//EntityBareJid jid = JidCreate.entityBareFrom(mUserJID);
//						BareJid jid = JidCreate.bareFrom(mUserJID);
//						roster.createEntry(jid, mUsername, null);

//						roster.addPresenceEventListener(new AbstractPresenceEventListener() {
//							@Override
//							public void presenceAvailable(FullJid address, Presence presence) {
//								Log.e(TAG, "presenceAvailable address: " + address + " , presence: " + presence);
//							}
//
//							@Override
//							public void presenceUnavailable(FullJid address, Presence presence) {
//								Log.e(TAG, "presenceUnavailable address: " + address + " , presence: " + presence);
//							}
//
//							@Override
//							public void presenceError(Jid address, Presence presence) {
//								Log.e(TAG, "presenceError address: " + address + " , presence: " + presence);
//							}
//
//							@Override
//							public void presenceSubscribed(BareJid address, Presence presence) {
//								Log.e(TAG, "presenceSubscribed address: " + address + " , presence: " + presence);
//							}
//
//							@Override
//							public void presenceUnsubscribed(BareJid address, Presence presence) {
//								Log.e(TAG, "presenceUnsubscribed address: " + address + " , presence: " + presence);
//							}
//						});
//
//						roster.addSubscribeListener(new SubscribeListener() {
//							@Override
//							public SubscribeAnswer processSubscribe(Jid from, Presence presence) {
//								Log.e(TAG, "processSubscribe from: " + from + " , presence: " + presence);
//								return SubscribeAnswer.Approve;
//							}
//						});

//						roster.addRosterListener(new AbstractRosterListener() {
//							@Override
//							public void entriesAdded(Collection<Jid> addresses) {
//								Log.e(TAG, "entriesAdded addresses: " + addresses );
//							}
//
//							@Override
//							public void entriesUpdated(Collection<Jid> addresses) {
//								Log.e(TAG, "entriesUpdated addresses: " + addresses );
//							}
//
//							@Override
//							public void entriesDeleted(Collection<Jid> addresses) {
//								Log.e(TAG, "entriesDeleted addresses: " + addresses );
//							}
//
//							@Override
//							public void presenceChanged(Presence presence) {
//								Log.e(TAG, "presenceChanged presence: " + presence );
//							}
//						});

//						Presence presence = new Presence(Presence.Type.available);
//						presence.setStatus("Online!");
//						presence.setPriority(1);
//						Log.e(TAG, "sendStanza called");
//						mConnection.sendStanza(presence);
						return true;
					}
					else
						return false;
				}
			}
			else {
				Log.e(TAG, "connect called");
				mConnection.connect();
				if (mConnection.isConnected()){
					Log.e(TAG, "login called");
					mConnection.login(mUsername, mPassword, Resourcepart.from(XMPP_RESOURCE));
					if (mConnection.isAuthenticated()) {
						sendSelfPresence();
//						Presence presence = new Presence(Presence.Type.available);
//						presence.setStatus("Online!");
//						presence.setPriority(1);
//						Log.e(TAG, "sendStanza called");
//						mConnection.sendStanza(presence);
						return true;
					}
					else
						return false;
				}
				else
					return false;
			}
		} catch (Exception e) {
			Log.e(TAG, "init error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public void unSubscribeContact(String contactId){
		Log.e(TAG, "unSubscribeContact: " + contactId);
		Roster roster = Roster.getInstanceFor(mConnection);
		try {
			BareJid bare = JidCreate.bareFrom(getUserJid(contactId));
			RosterEntry entry = roster.getEntry(bare);
			if (entry == null) {
				Log.e(TAG, "unSubscribeContact Entry not exists " + contactId);
			}
			else {
				roster.removeEntry(entry);
				Log.e(TAG, "unSubscribeContact removeEntry: " + contactId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void subscribeContact(String contactId, String name){
		Log.e(TAG, "subscribeContact: " + contactId);
		Roster roster = Roster.getInstanceFor(mConnection);

		try {
			BareJid bare = JidCreate.bareFrom(getUserJid(contactId));
			RosterEntry entry = roster.getEntry(bare);
			if (entry == null) {
				roster.createEntry(bare, name, null);
				Log.e(TAG, "subscribeContact Entry created: " + contactId);
			}
			else {
				Log.e(TAG, "subscribeContact Entry exists: " + entry.toString());
			}

			LastActivity lastAct = mLastActivityManager.getLastActivity(bare);
			long time = System.currentTimeMillis() - lastAct.getIdleTime()*1000;
			Log.e(TAG, "subscribeContact LastActivity: " + (new Date(time).toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public void getAllPresence(){
		Log.e(TAG, "getAllPresence");
		Roster roster = Roster.getInstanceFor(mConnection);
		Collection<RosterEntry> entries = roster.getEntries();
		for (RosterEntry rosterEntry : entries) {
			Presence presence = roster.getPresence(rosterEntry.getJid());
			Log.e(TAG, "getAllPresence from:" + presence.getFrom() + ",  presence:" + presence);
		}
	}

	public boolean getPresence(String contactId){
		Roster roster = Roster.getInstanceFor(mConnection);
		try {
			Presence presence = roster.getPresence(JidCreate.bareFrom(getUserJid(contactId)));
			Log.e(TAG, "getPresence for: " + contactId + " , res: " + presence);
			return presence.isAvailable();
		} catch (XmppStringprepException e) {
			e.printStackTrace();
			Log.e(TAG, "getPresence error res for: " + contactId);
			return false;
		}

//		mExecutors.networkIO().execute(new Runnable() {
//			@Override
//			public void run() {
//
//			}
//		});


	}


	private String getUserJid(String userId){
		return userId + "@" + XMPP_DOMAIN;
	}

//	private String getUserJidAndroid(String userId){
//		return userId + "@" + XMPP_DOMAIN + "/" + XMPP_RESOURCE;
//	}

	public void initAsync(String username, String password){
		Log.e(TAG, "initAsync called");
		mUsername = username;
		mPassword = password;
		mUserJID = getUserJid(username);
		//mUserJIDAndroid = getUserJidAndroid(username);

		mExecutors.networkIO().execute(new Runnable() {
			@Override
			public void run() {
				init();
			}
		});

//		InitAsycTask task = new InitAsycTask();
//		task.executeOnExecutor(get)
//		task.execute();
	}

//	private class InitAsycTask extends AsyncTask<String, Void, Void>{
//
//		@Override
//		protected Void doInBackground(String... params) {
//			Log.e(TAG, "InitAsycTask doInBackground called");
//			init();
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void aVoid) {
//			super.onPostExecute(aVoid);
//		}
//	}



//	private void connect(){
//		try {
//			mConnection.connect();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void login(){
//		try {
//			mConnection.login();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}


	private IncomingChatMessageListener mIncomingChatMessageListener = new IncomingChatMessageListener() {
		@Override
		public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
			Log.e(TAG, "newIncomingMessage: from: " + from + " , message body: " + message.getBody()+ " , convId: " + message.getThread());
			if (message != null)
				mOnChatMessageListener.onNewIncomingMessage(message.getBody(), message.getThread());
		}
	};
	private OutgoingChatMessageListener mOutgoingChatMessageListener =  new OutgoingChatMessageListener() {
		@Override
		public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
			Log.e(TAG, "newOutgoingMessage: to: " + to + " , message: " + message.getBody() + " , convId: " + message.getThread());
			if (message != null)
				mOnChatMessageListener.onNewOutgoingMessage(message.getBody(), message.getThread());
		}
	};


	private ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void connected(XMPPConnection connection) {
			Log.e(TAG, "ConnectionListener connected: " + connection.toString());
			mOnChatMessageListener.onConnectionChange(true, false);
		}

		@Override
		public void authenticated(XMPPConnection connection, boolean resumed) {
			Log.e(TAG, "ConnectionListener authenticated: resumed: " + resumed + " , connection: " + connection.toString());
			mOnChatMessageListener.onConnectionChange(true, true);
		}

		@Override
		public void connectionClosed() {
			Log.e(TAG, "ConnectionListener connectionClosed");
			mOnChatMessageListener.onConnectionChange(false, false);
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			Log.e(TAG, "ConnectionListener connectionClosedOnError: " + e.getMessage());
			mOnChatMessageListener.onConnectionChange(false, false);
		}
	};

	public long getLastOnline(String contactId){
		try {
			BareJid bare = JidCreate.bareFrom(getUserJid(contactId));
			LastActivity lastAct = mLastActivityManager.getLastActivity(bare);
			long idlTime = lastAct.getIdleTime() * 1000;
			long now = System.currentTimeMillis();
			long lastOnline = now - idlTime;
			Log.e(TAG, "getLastOnline for contactId " + contactId + " : " + new Date(lastOnline));
			return lastOnline;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}


	private AbstractRosterListener mRosterListener = new AbstractRosterListener() {
		@Override
		public void presenceChanged(Presence presence) {
			Log.e(TAG, "presenceChanged from: " + presence.getFrom().getLocalpartOrNull() + " , type: " + presence.getType());
			mOnChatMessageListener.onPresenceChanged(presence.getType() == Presence.Type.available, presence.getFrom().getLocalpartOrNull().toString());
		}
	};


	public void sendStringMessage(String theMessage, String participantId, String conversationId){
		mExecutors.networkIO().execute(new Runnable() {
			@Override
			public void run() {
				EntityBareJid jid = null;
				try {
					if(connectAndLogin()) {
						Log.e(TAG, "connectAndLogin result: " + true);
						if (mConnection.isAuthenticated()) {
							jid = JidCreate.entityBareFrom(participantId + "@" + XMPP_DOMAIN);
							Chat chat = mChatManager.chatWith(jid);
							Message message = new Message(jid);
							message.setBody(theMessage);
							message.setType(Message.Type.chat);
							message.setThread(conversationId);
							chat.send(message);
							//chat.send(theMessage);
						}
					}
					else {
						Log.e(TAG, "connectAndLogin result: " + false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}



}
