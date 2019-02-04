package io.wochat.app;

import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.net.InetAddress;

class XMPPProvider {


	private static final String TAG = "XMPPProvider";

	private static final String XMPP_DOMAIN = "ejabberd-dev.wochat.io";
	private static final String XMPP_RESOURCE = "ejabberd-dev.wochat.io";
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
	private String mUserJIDAndroid;
	private ChatManager mChatManager;
	private OnChatMessageListener mOnChatMessageListener;

	public interface OnChatMessageListener {
		void onNewIncomingMessage(String msg);
		void onNewOutgoingMessage(String msg);
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
				//setUsernameAndPassword(mUserJIDAndroid, mPassword).
				//setResource(XMPP_RESOURCE).
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

	private boolean connectAndLogin(){
		Log.e(TAG, "connectAndLogin called");
		try {
			if (mConnection.isConnected()) {
				if (mConnection.isAuthenticated()){
					return true; // all is ok
				}
				else { // not log in
					Log.e(TAG, "login called Username: " + mUsername + " , Password: " + mPassword );
					mConnection.login(mUsername, mPassword, Resourcepart.from(XMPP_RESOURCE));

					if (mConnection.isAuthenticated()) {
	//					Roster roster = Roster.getInstanceFor(mConnection);
	//					roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
	//					roster.createEntry(userJID, loginUser, null);
						Presence presence = new Presence(Presence.Type.available);
						presence.setStatus("Online!");
						presence.setPriority(1);
						Log.e(TAG, "sendStanza called");
						mConnection.sendStanza(presence);
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
						Presence presence = new Presence(Presence.Type.available);
						presence.setStatus("Online!");
						presence.setPriority(1);
						Log.e(TAG, "sendStanza called");
						mConnection.sendStanza(presence);
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



	public void initAsync(String username, String password){
		Log.e(TAG, "initAsync called");
		mUsername = username;
		mPassword = password;
		mUserJID = username + "@" + XMPP_DOMAIN;
		mUserJIDAndroid = mUserJID + "/android";

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
			Log.e(TAG, "newIncomingMessage: from: " + from + " , message body: " + message.getBody().toString());
			if (message != null)
				mOnChatMessageListener.onNewIncomingMessage(message.getBody());
		}
	};
	private OutgoingChatMessageListener mOutgoingChatMessageListener =  new OutgoingChatMessageListener() {
		@Override
		public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
			Log.e(TAG, "newOutgoingMessage: to: " + to + " , message: " + message.getBody());
			if (message != null)
				mOnChatMessageListener.onNewOutgoingMessage(message.getBody());
		}
	};


	private ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void connected(XMPPConnection connection) {
			Log.e(TAG, "ConnectionListener connected: " + connection.toString());
		}

		@Override
		public void authenticated(XMPPConnection connection, boolean resumed) {
			Log.e(TAG, "ConnectionListener authenticated: resumed: " + resumed + " , connection: " + connection.toString());
		}

		@Override
		public void connectionClosed() {
			Log.e(TAG, "ConnectionListener connectionClosed");
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			Log.e(TAG, "ConnectionListener connectionClosedOnError: " + e.getMessage());
		}
	};



	public void sendStringMessage(String theMessage, String participantId){
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
							chat.send(theMessage);
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
