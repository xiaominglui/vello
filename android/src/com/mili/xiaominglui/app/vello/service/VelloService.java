package com.mili.xiaominglui.app.vello.service;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.http.HttpStatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.PushService;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.Board;
import com.mili.xiaominglui.app.vello.data.model.DirtyCard;
import com.mili.xiaominglui.app.vello.data.model.List;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.model.WordList;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;
import com.mili.xiaominglui.app.vello.dialogs.ConnectionErrorDialogFragment.ConnectionErrorDialogListener;
import com.mili.xiaominglui.app.vello.ui.MainActivity;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class VelloService extends Service implements RequestListener,
		ConnectionErrorDialogListener {
	private static final String TAG = VelloService.class.getSimpleName();

	HashMap<String, DirtyCard> mDirtyCards = new HashMap<String, DirtyCard>();
	private static boolean isRunning = false;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track
																// of all
																// current
																// registered
																// clients.

	int mValue = 0; // Holds last value set by a client.
	public static final int MSG_UNREGISTER_CLIENT = -1;
	public static final int MSG_REGISTER_CLIENT = 0;

	public static final int MSG_SPINNER_ON = 1;
	public static final int MSG_SPINNER_OFF = 2;
	public static final int MSG_DIALOG_BAD_DATA_ERROR_SHOW = 3;
	public static final int MSG_DIALOG_CONNECTION_ERROR_SHOW = 4;
	public static final int MSG_TOAST_INIT_VOCABULARY_START = 5;
	public static final int MSG_TOAST_INIT_VOCABULARY_END = 6;
	public static final int MSG_TOAST_NO_WORD_NOW = 8;
	public static final int MSG_TOAST_NOT_AVAILABLE_WORD = 9;
	public static final int MSG_TOAST_WORD_REVIEWED_COUNT_PLUS = 10;
	public static final int MSG_SHOW_RESULT_WORDCARD = 11;
	public static final int MSG_AUTH_TOKEN_REVOKED = 12;
	public static final int MSG_VALID_TRELLO_CONNECTION = 13;
	public static final int MSG_INVALID_TRELLO_CONNECTION = 14;
	
	public static final int MSG_STATUS_WEBHOOK_ACTIVED = 15;
	public static final int MSG_STATUS_WEBHOOK_DEACTIVED = 16;
	
	public static final int MSG_STATUS_INIT_ACCOUNT_BEGIN = 50;
	public static final int MSG_STATUS_INIT_ACCOUNT_END = 51;
	public static final int MSG_STATUS_SYNC_BEGIN = 52;
	public static final int MSG_STATUS_SYNC_END = 53;
	public static final int MSG_STATUS_REVOKE_BEGIN = 54;
	public static final int MSG_STATUS_REVOKE_END = 55;

	public static final int MSG_CHECK_VOCABULARY_BOARD = 100;
	public static final int MSG_GET_DUE_REVIEW_CARD_LIST = 101;
	public static final int MSG_CLOSE_WORDCARD = 104;
	public static final int MSG_SYNC_LOCAL_CACHE = 106;
	public static final int MSG_TRIGGER_QUERY_WORD = 107;
	public static final int MSG_REVOKE_AUTH_TOKEN = 108;
	public static final int MSG_SET_WEBHOOK_ACTIVE_STATUS = 109;
	public static final int MSG_CHECK_TRELLO_CONNECTION = 110;
	public static final int MSG_READ_TRELLO_ACCOUNT_INFO = 111;
	public static final int MSG_RETURN_TRELLO_USERNAME = 112;

	final Messenger mMessenger = new Messenger(new IncomingHandler(this));

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
		private final WeakReference<VelloService> mService;

		IncomingHandler(VelloService service) {
			mService = new WeakReference<VelloService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			VelloService service = mService.get();
			if (service != null) {
				switch (msg.what) {
				case MSG_REGISTER_CLIENT:
					service.mClients.add(msg.replyTo);
					break;
				case MSG_UNREGISTER_CLIENT:
					service.mClients.remove(msg.replyTo);
					break;
				case MSG_CHECK_VOCABULARY_BOARD:
					service.checkVocabularyBoard();
					break;
				case MSG_GET_DUE_REVIEW_CARD_LIST:
					int startId = msg.arg1;
					service.getDueReviewCardList(startId);
					break;
				case MSG_TRIGGER_QUERY_WORD:
				    String query = (String) msg.obj;
				    service.queryInRemoteStorage(query);
				    break;
				case MSG_REVOKE_AUTH_TOKEN:
					service.sendMessageToUI(MSG_STATUS_REVOKE_BEGIN, null);
					service.revokeAuthToken();
					break;
				case MSG_SET_WEBHOOK_ACTIVE_STATUS:
					boolean isActive = (Boolean) msg.obj;
					service.setWebHookActive(isActive);
					break;
				case MSG_CHECK_TRELLO_CONNECTION:
					service.checkTrelloConnection();
					break;
				case MSG_READ_TRELLO_ACCOUNT_INFO:
					String token = (String) msg.obj;
					service.readTrelloAccountInfo(token);
				default:
					super.handleMessage(msg);
				}
			}

		}
	}

	private void sendMessageToUI(int type, Object obj) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
			    Message msg = Message.obtain(null, type);
			    if (obj != null) {
			        msg.obj = obj;
			    }
				mClients.get(i).send(msg);
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	if (VelloConfig.DEBUG_SWITCH) {
    		Log.d(TAG, "command started---#" + startId);
    	}
    	Message m = Message.obtain(null, VelloService.MSG_GET_DUE_REVIEW_CARD_LIST);
    	m.arg1 = startId;
    	m.arg2 = flags;
    	m.obj = intent.getExtras();
    	try {
			mMessenger.send(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return START_NOT_STICKY;
	}

	public static boolean isRunning() {
		return isRunning;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "VelloService Started.");
		mRequestManager = VelloRequestManager.from(this);
		mRequestList = new ArrayList<Request>();

		// Display a notification about us starting. We put an icon in the
		// status bar.
		// showNotification();
		
//		ClipboardManager clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
//		clipBoard.addPrimaryClipChangedListener( new ClipboardListener() );
		// https://code.google.com/p/android/issues/detail?id=58043
		
		isRunning = true;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		Log.i(TAG, "VelloService Stopped.");
		isRunning = false;
	}

	protected VelloRequestManager mRequestManager;
	protected ArrayList<Request> mRequestList;
	
	private void archiveWordCard(String idCard) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "archiveWordCard start...");
		}
		Request archiveWordCard = VelloRequestFactory
				.archiveWordCardRequest(idCard);
		mRequestManager.execute(archiveWordCard, this);
		mRequestList.add(archiveWordCard);
	}

	private void reviewedWordCard(String idCard, int position) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reviewedWordCard start...");
		}
		Request reviewedWordCard = VelloRequestFactory.reviewedWordCardRequest(
				idCard, position);
		mRequestManager.execute(reviewedWordCard, this);
		mRequestList.add(reviewedWordCard);
	}

	private void reviewedPlusWordCard(String idCard) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reviewedPlusWordCard start...");
		}
		Request reviewedPlusWordCard = VelloRequestFactory
				.reviewedPlusWordCardRequest(idCard);
		mRequestManager.execute(reviewedPlusWordCard, this);
		mRequestList.add(reviewedPlusWordCard);
	}

	private void checkVocabularyBoard() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "checkVocabularyBoard start...");
		}
		sendMessageToUI(VelloService.MSG_STATUS_INIT_ACCOUNT_BEGIN, null);

		Request checkVocabularyBoardRequest = VelloRequestFactory.checkVocabularyBoardRequest();
		mRequestManager.execute(checkVocabularyBoardRequest, this);
		mRequestList.add(checkVocabularyBoardRequest);
	}

	private void createVocabularyBoard() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "createVocabularyBoard start...");
		}
		Request createVocabularyBoardRequest = VelloRequestFactory
				.createVocabularyBoardRequest();
		mRequestManager.execute(createVocabularyBoardRequest, this);
		mRequestList.add(createVocabularyBoardRequest);
	}

	private void createVocabularyList(int position) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "createVocabularyList start...");
		}
		Request createVocabularyListRequest = VelloRequestFactory
				.createVocabularyListRequest(position);
		mRequestManager.execute(createVocabularyListRequest, this);
		mRequestList.add(createVocabularyListRequest);
	}

	private void reOpenVocabulayList(int position, String id) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reOpenVocabulayList start...");
		}
		Request reOpenVocabularyListRequest = VelloRequestFactory
				.reOpenVocabularyListRequest(position, id);
		mRequestManager.execute(reOpenVocabularyListRequest, this);
		mRequestList.add(reOpenVocabularyListRequest);
	}

	private void checkVocabularyLists() {
		for (int i = 0; i < AccountUtils.VOCABULARY_LISTS_TITLE_ID.length; i++) {
			if (VelloConfig.DEBUG_SWITCH) {
				Log.d(TAG, "checkVocabularyLists start..." + i);
			}
			Request checkVocabularyListReqest = VelloRequestFactory.checkVocabularyListRequest(i);

			mRequestManager.execute(checkVocabularyListReqest, this);
			mRequestList.add(checkVocabularyListReqest);
		}

	}

	private void configureVocabularyBoard(String id) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "configureVocabularyBoard start...");
		}
		Request configureVocabularyBoardRequest = VelloRequestFactory
				.configureVocabularyBoardRequest(id);
		mRequestManager.execute(configureVocabularyBoardRequest, this);
		mRequestList.add(configureVocabularyBoardRequest);
	}

	private void getOpenTrelloCardList(int startId, boolean force) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "getOpenTrelloCardList with startId = " + startId + ", force = " + force + " start...");
		}

		Request getOpenTrelloCardList = VelloRequestFactory.getOpenTrelloCardListRequest(startId, force);
		mRequestManager.execute(getOpenTrelloCardList, this);
		mRequestList.add(getOpenTrelloCardList);

	}
	
	private void getDueReviewCardList(int startId) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "getDueReviewCardList with startId = " + startId + " start...");
		}
		sendMessageToUI(VelloService.MSG_STATUS_SYNC_BEGIN, null);
		// step 1: get open trello cards
		getOpenTrelloCardList(startId, false);
	}

	private void queryInRemoteStorage(String query) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "checkWordCardStatusRequest start...");
		}
		Request queryInRemoteStorage = VelloRequestFactory
				.queryInRemoteStorageRequest(query);
		mRequestManager.execute(queryInRemoteStorage, this);
		mRequestList.add(queryInRemoteStorage);
	}
	
	private void lookUpInDictionary(String keyword) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "look up in dictionary...");
		}
		Request lookUpInDictionary = VelloRequestFactory.lookUpInDictionaryRequest(keyword);
		mRequestManager.execute(lookUpInDictionary, this);
		mRequestList.add(lookUpInDictionary);
	}

	private void addWordCard(String keyword, String data) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "addWordCard start...");
		}
		Request addWordCard = VelloRequestFactory.addWordCardRequest(keyword,
				data);
		mRequestManager.execute(addWordCard, this);
		mRequestList.add(addWordCard);
	}

	private void initializeWordCard(String idCard) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "initializeWordCard start...");
		}
		Request initializeWordCard = VelloRequestFactory
				.initializeWordCardRequest(idCard);
		mRequestManager.execute(initializeWordCard, this);
		mRequestList.add(initializeWordCard);
	}

	private void reStartWordCard(String idCard) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reStartWordCard start...");
		}
		Request reStartWordCard = VelloRequestFactory
				.reStartWordCardRequest(idCard);
		mRequestManager.execute(reStartWordCard, this);
		mRequestList.add(reStartWordCard);
	}
	
	private void createWebHook() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "createWebHook start...");
		}
		Request createWebHook = VelloRequestFactory.createWebHook();
		mRequestManager.execute(createWebHook, this);
		mRequestList.add(createWebHook);
	}
	
	private void setWebHookActive(boolean isActive) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "setWebHookActive start...");
		}
		Request setWebHookActive = VelloRequestFactory.setWebHookActive(isActive);
		mRequestManager.execute(setWebHookActive, this);
		mRequestList.add(setWebHookActive);
	}
	
	private void revokeAuthToken() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "revokeAuthToken start...");
		}
		Request revokeAuthToken = VelloRequestFactory.revokeAuthToken();
		mRequestManager.execute(revokeAuthToken, this);
		mRequestList.add(revokeAuthToken);
	}
	
	private void checkTrelloConnection() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "checkTrelloConnection start...");
		}
		Request checkTrelloConnection = VelloRequestFactory.checkTrelloConnection();
		mRequestManager.execute(checkTrelloConnection, this);
		mRequestList.add(checkTrelloConnection);
	}
	
	private void readTrelloAccountInfo(String token) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "readTrelloAccountInfo start...");
		}
		Request readTrelloAccountInfo = VelloRequestFactory.readTrelloAccountInfo(token);
		mRequestManager.execute(readTrelloAccountInfo, this);
		mRequestList.add(readTrelloAccountInfo);
	}
	
	private void deleteRemoteTrelloCard(TrelloCard card, int startId) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "deleteRemoteTrelloCard start...");
		}
		Request deleteRemoteTrelloCard = VelloRequestFactory.deleteRemoteTrelloCard(card, startId);
		mRequestManager.execute(deleteRemoteTrelloCard, this);
		mRequestList.add(deleteRemoteTrelloCard);
	}
	
	private void updateRemoteTrelloCard(TrelloCard card, int startId) {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "updateRemoteTrelloCard start...");
		}
		Request updateRemoteTrelloCard = VelloRequestFactory.updateRemoteTrelloCard(card, startId);
		mRequestManager.execute(updateRemoteTrelloCard, this);
		mRequestList.add(updateRemoteTrelloCard);
	}
	
	@Override
	public void onRequestFinished(Request request, Bundle resultData) {
		if (mRequestList.contains(request)) {
			mRequestList.remove(request);

			switch (request.getRequestType()) {
			case VelloRequestFactory.REQUEST_TYPE_REVIEWED_WORDCARD:
				TrelloCard reviewedWordCard = resultData
						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (reviewedWordCard != null) {
					// reviewed

				} else {
					// reviewed failed
					// do nothing at present
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "reviewedWordCard end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_REVIEWED_PLUS_WORDCARD:
				WordList wordList = resultData
						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDLIST);
				String idReviewedPlusCard = request
						.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_CARD_ID);
				if (wordList != null) {
					String idList = wordList.id;
					int position = AccountUtils.getVocabularyListPosition(getApplicationContext(), idList);
					reviewedWordCard(idReviewedPlusCard, position);
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_BOARD:
				ArrayList<Board> boardList = resultData
						.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_BOARD_LIST);
				for (Board board : boardList) {
					if (board.name.equals(AccountUtils
							.getVocabularyBoardName(getApplicationContext()))
							&& board.desc.equals(AccountUtils
									.getVocabularyBoardVerification())) {
						// s - 0 find out vocabulary board, check the closed
						// flag.

						if (!board.closed.equals("true")) {
							// s - 0.0 vocabulary board is NOT well
							// configured.
							configureVocabularyBoard(board.id);
							return;
						} else {
							// s - 0.1 well configured vocabulary board,
							// save id
							AccountUtils.setVocabularyBoardId(getApplicationContext(), board.id);
							checkVocabularyLists();
							return;
						}
					}
				}

				// s - 1 NO vocabulary board found, need create one
				createVocabularyBoard();
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "check vocabulary board end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD:
				String boardId = request.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_BOARD_ID);
				if (resultData != null) {
					// configure board successfully & save it
					String id = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);
					AccountUtils.setVocabularyBoardId(getApplicationContext(), id);
					Log.d(TAG, "configure board successfully! vocabulary board id = " + id);

					// continue to check vocabulary list if not well formed
					checkVocabularyLists();
				} else {
					// configure board failed, try again
					configureVocabularyBoard(boardId);
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_LIST:
				ArrayList<List> listList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_LIST);
				int position = request.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				for (List list : listList) {
					if (list.name.equals(AccountUtils.VOCABULARY_LISTS_TITLE_ID[position])) {
						// get the vocabulary list[position]
						if (!list.closed.equals("false")) {
							// list[position] be closed unexpectedly
							reOpenVocabulayList(position, list.id);
							return;
						} else {
							// list[position]'s status ok, save listId
							AccountUtils.setVocabularyListId(getApplicationContext(), list.id, position);
							if (AccountUtils.isVocabularyBoardWellFormed(getApplicationContext())) {
								sendMessageToUI(VelloService.MSG_STATUS_INIT_ACCOUNT_END, null);
							}
							return;
						}
					}
				}
				
				// no vocabulary list found
				createVocabularyList(position);
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "check vocabulary list end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_REOPEN_VOCABULARY_LIST:
				int positonList = request
						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				String idList = request
						.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
				if (resultData != null) {
					// reopen list successfully
					String id = resultData
							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
					int pos = resultData
							.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
					AccountUtils.setVocabularyListId(getApplicationContext(), id, pos);
				} else {
					// reopen failed, try again
					reOpenVocabulayList(positonList, idList);
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_LIST:
				int positionList = request
						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				if (resultData != null) {
					// create list successfully
					String id = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
					int pos = request.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
					AccountUtils.setVocabularyListId(getApplicationContext(), id, pos);
				} else {
					// create list failed, try again
					createVocabularyList(positionList);
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "create vocabulary list end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_BOARD:
				if (resultData != null) {
					// create vocabulary board successfully
					String id = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);

					configureVocabularyBoard(id);
				} else {
					// create vocabulary board failed, try again
					createVocabularyBoard();
				}
				return;
			case VelloRequestFactory.REQUEST_TYPE_LOOK_UP_IN_DICTIONARY:
			    String keywordInQuery = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
				String wsResponse = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_DICTIONARY_WS_RESPONSE);
				if (!wsResponse.equals("null\n")) {
				    addWordCard(keywordInQuery, wsResponse);
				} else {
				    // no result in dictionary server. TODO
				    Toast.makeText(getApplicationContext(), "no result", Toast.LENGTH_SHORT).show();
				}
				
				return;

			case VelloRequestFactory.REQUEST_TYPE_QUERY_IN_REMOTE_STORAGE:
				ArrayList<TrelloCard> existedWordCardList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CARD_LIST);
				String keyword = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
				if (existedWordCardList.isEmpty()) {
					// not exist in remote storage
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "not exist in remote storage");
					}
					// query in dictionary service
					lookUpInDictionary(keyword);
				} else {
					// check more to decide
					// filter to find the right one
					for (TrelloCard w : existedWordCardList) {
						if (w.name.equals(keyword)) {
							// got the right word card
							// show with user first
							sendMessageToUI(MSG_SHOW_RESULT_WORDCARD, w);

							if (w.closed.equals("true")) {
								// the existed word card has be closed,
								// re-open it.
								if (VelloConfig.DEBUG_SWITCH) {
									Log.d(TAG, "re-start existed word card.");
								}
								reStartWordCard(w.id);
							} else {
								if (w.due.equals("null")) {
									// the existed word card has not be
									// initialized, initialize it. this is the
									// double check
									if (VelloConfig.DEBUG_SWITCH) {
										Log.d(TAG,
												"initialize existed word card.");
									}
									initializeWordCard(w.id);
								} else {
									// the existed word is in review
									// process, do
									// nothing at present.
									if (VelloConfig.DEBUG_SWITCH) {
										Log.d(TAG,
												"the existed word is in review.");
									}
								}
							}
							return;
						}
					}
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_ADD_WORDCARD:
			    String keywordInAddWordCard = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
			    String wsResult = request.getString(VelloRequestFactory.PARAM_EXTRA_DICTIONARY_WS_RESULT);
				TrelloCard addedWordCard = resultData.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (addedWordCard != null) {
					// show added wordcard to user
					// TODO
					// new WordCardToWordTask().execute(addedWordCard);
					// at the same time initialize it.
					initializeWordCard(addedWordCard.id);
				} else {
					// add failed, add again
				    addWordCard(keywordInAddWordCard, wsResult);
				}

				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "addWordCard end.");
				}

				return;

			case VelloRequestFactory.REQUEST_TYPE_RESTART_WORDCARD:
				TrelloCard restartedWordCard = resultData.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (restartedWordCard != null) {
					// restarted
					// do nothing at present.
				} else {
					// restart failed
					// do nothing at present.
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "reStartzeWordCard end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_INITIALIZE_WORDCARD:
				TrelloCard initializedWordCard = resultData.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (initializedWordCard != null) {
					// initialized
					// should show to user
					// TODO
				} else {
					// initialized failed
					// do nothing at present
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "initializeWordCard end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_GET_OPEN_TRELLO_CARD_LIST:
				int startId = request.getInt(VelloRequestFactory.PARAM_EXTRA_SERVICE_START_ID);
				boolean force = request.getBoolean(VelloRequestFactory.PARAM_EXTRA_FORCE_GET_OPEN_TRELLO_CARD);
				ArrayList<TrelloCard> OpenTrelloCardList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CARD_LIST);
				if (OpenTrelloCardList.size() > 0) {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "has open trello card, with startId = " + startId + ", force = " + force);
					}
					final ContentResolver resolver = getContentResolver();
					if (!force) {
						// query and backup all local items that syncInNext=true
						// or merge
						// locally later
						ProviderCriteria criteria = new ProviderCriteria();
						criteria.addNe(DbWordCard.Columns.DATE_LAST_OPERATION, "");
						Cursor c = resolver.query(DbWordCard.CONTENT_URI,
								DbWordCard.PROJECTION,
								criteria.getWhereClause(),
								criteria.getWhereParams(),
								criteria.getOrderClause());
						if (c != null) {
							while (c.moveToNext()) {
								DirtyCard dc = new DirtyCard(c);
								mDirtyCards.put(dc.id, dc);
							}
						}
					}
					
					if (force || !(mDirtyCards.size() > 0)) {
						// no dirty, commit to local DB directly
						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "no dirty card, commit to local DB");
						}
						try {
							resolver.delete(DbWordCard.CONTENT_URI, null, null);

							ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
							for (TrelloCard trelloCard : OpenTrelloCardList) {
								operationList.add(ContentProviderOperation
										.newInsert(DbWordCard.CONTENT_URI)
										.withValues(trelloCard.toContentValues())
										.build());
							}
							resolver.applyBatch(VelloProvider.AUTHORITY, operationList);
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (OperationApplicationException e) {
							e.printStackTrace();
						}

						// show notification
						Intent intent = new Intent(getApplicationContext(), MainActivity.class);
						PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

						ProviderCriteria cri = new ProviderCriteria();
						cri.addSortOrder(DbWordCard.Columns.DUE, true);

						Calendar rightNow = Calendar.getInstance();
						SimpleDateFormat fo = new SimpleDateFormat(
								"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						String now = fo.format(rightNow.getTime());
						cri.addLt(DbWordCard.Columns.DUE, now, true);
						cri.addNe(DbWordCard.Columns.CLOSED, "true");
						Cursor cur = getContentResolver().query(
								DbWordCard.CONTENT_URI, DbWordCard.PROJECTION,
								cri.getWhereClause(), cri.getWhereParams(),
								cri.getOrderClause());
						if (cur != null) {
							int num = cur.getCount();
							if (num > 0) {
								Resources res = getApplicationContext().getResources();
								String stringContentTitle = res.getQuantityString(R.plurals.notif_content_title, num, num);
								String stringContentText = res.getString(R.string.notif_content_text);
								Notification noti = new NotificationCompat.Builder(getApplicationContext())
										.setContentTitle(stringContentTitle)
										.setContentText(stringContentText)
										.setSmallIcon(R.drawable.ic_launcher)
										.setContentIntent(pIntent)
										.build();

								NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

								// Hide the notification after its selected
								noti.flags |= Notification.FLAG_AUTO_CANCEL;

								notificationManager.notify(0, noti);
							}
						}

						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "...stop command---#" + startId);
						}
						sendMessageToUI(VelloService.MSG_STATUS_SYNC_END, null);

						stopSelf(startId);
					}
					
					if (mDirtyCards.size() > 0 && !force) {
						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "dirty card found: " + mDirtyCards.size());
						}
						// maybe need merging
						for (TrelloCard tCard : OpenTrelloCardList) {
							if (mDirtyCards.containsKey(tCard.id)) {
								// need merging
								if (VelloConfig.DEBUG_SWITCH) {
									Log.d(TAG, "merge card --- " + tCard.id);
								}
								DirtyCard dCard = mDirtyCards.get(tCard.id);
								if (dCard.markDeleted.equals("true")) {
									// delete remote Trello card
									deleteRemoteTrelloCard(tCard, startId);
								} else if (dCard.dateLastActivity.equals(tCard.dateLastActivity)) {
									// remote has no commit
									// commit local due, closed, listId to
									// Trello
									updateRemoteTrelloCard(upgradeTrelloCard(tCard, dCard, false), startId);
								} else {
									// remote has commit
									// update remote data based on local data
									updateRemoteTrelloCard(upgradeTrelloCard(tCard, dCard, true), startId);
								}
							}
						}
					}
				} else {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "has no open trello card");
					}
				}
				/*
				if (finished) {
					// TODO finished expectly
					// Build notification
		            // TODO need rework
		        	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

		            
		            ProviderCriteria cri = new ProviderCriteria();
		            cri.addSortOrder(DbWordCard.Columns.DUE, true);

		            Calendar rightNow = Calendar.getInstance();
		            SimpleDateFormat fo = new SimpleDateFormat(
		                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		            String now = fo.format(rightNow.getTime());
		            cri.addLt(DbWordCard.Columns.DUE, now, true);
		            cri.addNe(DbWordCard.Columns.CLOSED, "true");
		            Cursor cur = getContentResolver().query(DbWordCard.CONTENT_URI, DbWordCard.PROJECTION,
		                    cri.getWhereClause(), cri.getWhereParams(), cri.getOrderClause());
		            if (cur != null) {
		                int num = cur.getCount();
		                if (num > 0) {
//		                    Notification noti = new Notification.Builder(getApplicationContext())
//		                            .setContentTitle(
//		                                    "You have " + num + " words need reviewing!")
//		                            .setContentText("Click me to begin reviewing!")
//		                            .setSmallIcon(R.drawable.ic_launcher)
//		                            .setContentIntent(pIntent).build();
//
//		                    NotificationManager notificationManager = (NotificationManager) getApplicationContext()
//		                            .getSystemService(Context.NOTIFICATION_SERVICE);
//
//		                    // Hide the notification after its selected
//		                    noti.flags |= Notification.FLAG_AUTO_CANCEL;
//
//		                    notificationManager.notify(0, noti);
		                }
		            }
				} else {
					// TODO something error
				}
				*/
				return;

			case VelloRequestFactory.REQUEST_TYPE_ARCHIVE_WORDCARD:
				TrelloCard closedWordCard = resultData
						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				String idCard = request
						.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_CARD_ID);
				if (closedWordCard != null
						&& closedWordCard.closed.equals("true")) {
					// success, do nothing
				} else {
					// retry
					archiveWordCard(idCard);
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_CREATE_WEBHOOK:
				String hookId = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_WEBHOOK_ID);
				if (hookId != null) {
					// hook created, save it
					AccountUtils.setVocabularyBoardWebHookId(getApplicationContext(), hookId);
					
					// save Installation for push
					PushService.setDefaultPushCallback(this, MainActivity.class);
					AVInstallation.getCurrentInstallation().saveInBackground();

					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "webhook created.");
					}
					
				} else {
					// to create again
					createWebHook();
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_SET_WEBHOOK_ACTIVE:
				boolean requestActive = request.getBoolean(VelloRequestFactory.PARAM_EXTRA_WEBHOOK_ACTIVE);
				if (resultData != null) {
					boolean currentActive = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_WEBHOOK_ACTIVE);
					
					if (currentActive == requestActive) {
						// request success
						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "setWebHookActive end --- success");
						}
						if (currentActive) {
							// PUSH
							sendMessageToUI(MSG_STATUS_WEBHOOK_ACTIVED, null);
						} else {
							// schedule sync
							sendMessageToUI(MSG_STATUS_WEBHOOK_DEACTIVED, null);
						}
					}
				} else {
					// request fail, try again
					setWebHookActive(requestActive);
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_REVOKE_AUTH_TOKEN:
				boolean hasRevoked = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_HAS_AUTH_TOKEN_REVOKED);
				if (hasRevoked) {
					// revoked
					sendMessageToUI(VelloService.MSG_AUTH_TOKEN_REVOKED, null);
				} else {
					// TODO
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_CHECK_TRELLO_CONNECTION:
				boolean hasTrelloConnection = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CONNECTION);
				if (hasTrelloConnection) {
					// valid connection
					sendMessageToUI(VelloService.MSG_VALID_TRELLO_CONNECTION, null);
				} else {
					// invalid connection
					sendMessageToUI(VelloService.MSG_INVALID_TRELLO_CONNECTION, null);
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "CheckTrelloConnection end...");
				}
				return;
			case VelloRequestFactory.REQUEST_TYPE_READ_TRELLO_ACCOUNT_INFO:
				String username = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_ACCOUNT_USERNAME);
				String token = request.getString(VelloRequestFactory.PARAM_EXTRA_TRELLO_ACCESS_TOKEN);
				if (username != null && !username.isEmpty()) {
					sendMessageToUI(VelloService.MSG_RETURN_TRELLO_USERNAME, username);
				} else {
					// failed, retry
					readTrelloAccountInfo(token);
				}
				return;
				
			case VelloRequestFactory.REQUEST_TYPE_DELETE_REMOTE_TRELLO_CARD:
				boolean deleted = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_TRELLO_CARD_DELETED);
				TrelloCard deletingCard = (TrelloCard) request.getParcelable(VelloRequestFactory.PARAM_EXTRA_TRELLO_CARD);
				int startIdTriggerDelete = request.getInt(VelloRequestFactory.PARAM_EXTRA_SERVICE_START_ID);
				if (deleted) {
					mDirtyCards.remove(deletingCard.id);
				} else {
					// retry
					deleteRemoteTrelloCard(deletingCard, startIdTriggerDelete);
				}
				
				if (mDirtyCards.isEmpty()) {
					// merge ok, go clean full sync
					getOpenTrelloCardList(startIdTriggerDelete, true);
				}
				
			case VelloRequestFactory.REQUEST_TYPE_UPDATE_REMOTE_TRELLO_CARD:
				boolean updated = resultData.getBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_TRELLO_CARD_UPDATED);
				TrelloCard updatingCard = (TrelloCard) request.getParcelable(VelloRequestFactory.PARAM_EXTRA_TRELLO_CARD);
				int startIdTriggerUpdate = request.getInt(VelloRequestFactory.PARAM_EXTRA_SERVICE_START_ID);
				if (updated) {
					mDirtyCards.remove(updatingCard.id);
				} else {
					updateRemoteTrelloCard(updatingCard, startIdTriggerUpdate);
				}
				
				if (mDirtyCards.isEmpty()) {
					// merge ok, go clean full sync
					getOpenTrelloCardList(startIdTriggerUpdate, true);
				}
				
			default:
				return;
			}
		}
	}

	@Override
	public void onRequestConnectionError(Request request, int statusCode) {
		if (mRequestList.contains(request)) {
			if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
				if (request.getRequestType() == VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD) {
					String boardId = request.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_BOARD_ID);
					AccountUtils.setVocabularyBoardId(getApplicationContext(), boardId);
					Log.d(TAG, "get a board without ownership, skip configuration! vocabulary board id = " + boardId);

					// continue to check vocabulary list if not well formed
					checkVocabularyLists();
				} else if (request.getRequestType() == VelloRequestFactory.REQUEST_TYPE_GET_OPEN_TRELLO_CARD_LIST) {
					// TODO force logout and re-login
					Log.i(TAG, "invalid token");
				}
				
			} else if (statusCode == HttpStatus.SC_NOT_FOUND && request.getRequestType() == VelloRequestFactory.REQUEST_TYPE_REVOKE_AUTH_TOKEN) {
				// token has been revoked via trello web app
				Log.d(TAG, "token has been revoked via trello web app.");
				sendMessageToUI(VelloService.MSG_AUTH_TOKEN_REVOKED, null);
			} else if (statusCode == -1 && request.getRequestType() == VelloRequestFactory.REQUEST_TYPE_CHECK_TRELLO_CONNECTION) {
				sendMessageToUI(VelloService.MSG_INVALID_TRELLO_CONNECTION, null);
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "CheckTrelloConnection end...");
				}
			}
			Log.d(TAG, "type=" + request.getRequestType() + "; status code=" + statusCode);
			mRequestList.remove(request);
		}
	}

	@Override
	public void onRequestDataError(Request request) {
		// TODO
		if (mRequestList.contains(request)) {
			mRequestList.remove(request);

		}

	}

	@Override
	public void onRequestCustomError(Request request, Bundle resultData) {
		// Never called.

	}

	@Override
	public void connectionErrorDialogCancel(Request request) {
	}

	@Override
	public void connectionErrorDialogRetry(Request request) {
		mRequestManager.execute(request, this);
		mRequestList.add(request);
	}
	
	/**
	 * merge wordcard when remote wordcard is NOT the same state
	 * @param wordCard remote wordcard
	 * @param dirtyWordCard local changed wordcard
	 * @return new wordcard merged
	 */
	private TrelloCard upgradeTrelloCard(TrelloCard trelloCard, DirtyCard dirtyCard, boolean forcePush) {
		if (forcePush) {
			int localPositionInLists = AccountUtils.getVocabularyListPosition(getApplicationContext(), dirtyCard.idList);
			int remotePostionInLists = AccountUtils.getVocabularyListPosition(getApplicationContext(), trelloCard.idList);
			int delta = remotePostionInLists - localPositionInLists;
			if (delta > 0) {
				int compensation = delta + 1;
				if (remotePostionInLists + compensation > VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
					trelloCard.closed = "true";
				} else {
					int newPostionInLists = remotePostionInLists + compensation;
					trelloCard.idList = AccountUtils.getVocabularyListId(getApplicationContext(), newPostionInLists);
					Calendar rightNow = Calendar.getInstance();
					long rightNowUnixTime = rightNow.getTimeInMillis();
					long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
					long deltaTime = VelloConfig.VOCABULARY_LIST_DUE_DELTA[newPostionInLists];
					long newDueUnixTime = rightNowUnixTimeGMT + deltaTime;
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					Date newDueDate = new Date(newDueUnixTime);
					String stringNewDueDate = format.format(newDueDate);
					trelloCard.due = stringNewDueDate;
				}
			}
		} else {
			trelloCard.idList = dirtyCard.idList;
			trelloCard.due = dirtyCard.due;
			trelloCard.closed = dirtyCard.closed;
		}
		
		return trelloCard;
	}
	
	class ClipboardListener implements
			ClipboardManager.OnPrimaryClipChangedListener {
		public void onPrimaryClipChanged() {
			// do something useful here with the clipboard
			// use getText() method
			Toast.makeText(getApplicationContext(), "clipboard changed", Toast.LENGTH_SHORT).show();
		}
	}
}
